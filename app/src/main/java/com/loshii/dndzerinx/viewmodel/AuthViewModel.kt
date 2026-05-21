package com.loshii.dndzerinx.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import com.loshii.dndzerinx.model.EncounterDifficulty
import com.loshii.dndzerinx.model.Equipment
import com.loshii.dndzerinx.model.EquipmentCategory
import com.loshii.dndzerinx.model.EquipmentItem
import com.loshii.dndzerinx.model.ExperienceActivityType
import com.loshii.dndzerinx.model.ExperienceReward
import com.loshii.dndzerinx.model.GuildRole
import com.loshii.dndzerinx.model.LevelProgression
import com.loshii.dndzerinx.model.User
import com.loshii.dndzerinx.model.UserRole
import com.loshii.dndzerinx.model.InventoryItem
import com.loshii.dndzerinx.model.withAddedStats
import com.loshii.dndzerinx.model.Wallet
import com.loshii.dndzerinx.util.ImageUploader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val isRegistered: Boolean = false
)

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            loadUserData(firebaseUser.uid)
        } else {
            _currentUser.value = null
            _state.value = AuthState()
        }
    }
    private val db = FirebaseFirestore.getInstance()

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _userSearchResults = MutableStateFlow<List<User>>(emptyList())
    val userSearchResults: StateFlow<List<User>> = _userSearchResults

    private val _globalAccessKey = MutableStateFlow<String?>(null)
    val globalAccessKey: StateFlow<String?> = _globalAccessKey

    init {
        auth.addAuthStateListener(authStateListener)
        loadGlobalAccessKey()
        auth.currentUser?.let { firebaseUser ->
            _state.value = AuthState(isLoading = true)
            loadUserData(firebaseUser.uid)
        }
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
    }

    fun refreshUserData() {
        val uid = auth.currentUser?.uid ?: return
        loadUserData(uid)
    }

    fun addItemsToInventory(items: List<InventoryItem>) {
        val currentUser = auth.currentUser ?: return
        if (items.isEmpty()) return

        viewModelScope.launch {
            try {
                val user = _currentUser.value ?: return@launch
                val mergedItems = user.inventory.items.toMutableList()

                for (item in items) {
                    val existingIndex = mergedItems.indexOfFirst { it.id == item.id }
                    if (existingIndex >= 0) {
                        val existing = mergedItems[existingIndex]
                        mergedItems[existingIndex] = existing.copy(quantity = existing.quantity + item.quantity)
                    } else {
                        mergedItems.add(item)
                    }
                }

                val updatedInventory = user.inventory.copy(items = mergedItems)
                _currentUser.value = user.copy(inventory = updatedInventory)

                val inventoryUpdate = mapOf(
                    "inventory" to updatedInventory.toMap()
                )

                db.collection("users").document(currentUser.uid)
                    .set(inventoryUpdate, com.google.firebase.firestore.SetOptions.merge())
                    .await()
                loadUserData(currentUser.uid)
                _state.value = AuthState(isSuccess = true)
            } catch (e: Exception) {
                _state.value = AuthState(errorMessage = e.message ?: "Error al actualizar inventario")
            }
        }
    }

    fun useInventoryItem(itemId: String) {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val user = _currentUser.value ?: return@launch
                val item = user.inventory.items.firstOrNull { it.id == itemId }
                if (item == null) {
                    _state.value = AuthState(errorMessage = "Item no encontrado")
                    return@launch
                }
                if (!item.category.contains("consumable", ignoreCase = true) && !item.category.contains("potion", ignoreCase = true)) {
                    _state.value = AuthState(errorMessage = "Este item no es consumible")
                    return@launch
                }

                val remainingItems = user.inventory.items.toMutableList()
                val updatedItem = if (item.quantity > 1) item.copy(quantity = item.quantity - 1) else null
                val updatedInventory = if (updatedItem != null) {
                    remainingItems[remainingItems.indexOfFirst { it.id == itemId }] = updatedItem
                    user.inventory.copy(items = remainingItems)
                } else {
                    remainingItems.removeAll { it.id == itemId }
                    user.inventory.copy(items = remainingItems)
                }

                val updatedStatus = user.characterStatus.withAddedStats(item.stats)
                val inventoryUpdate = mapOf(
                    "inventory" to updatedInventory.toMap(),
                    "characterStatus" to updatedStatus.toMap()
                )

                db.collection("users").document(currentUser.uid)
                    .set(inventoryUpdate, com.google.firebase.firestore.SetOptions.merge())
                    .await()

                _currentUser.value = user.copy(inventory = updatedInventory, characterStatus = updatedStatus)
                loadUserData(currentUser.uid)
                _state.value = AuthState(isSuccess = true)
            } catch (e: Exception) {
                _state.value = AuthState(errorMessage = e.message ?: "Error al usar el item")
            }
        }
    }

    fun equipInventoryItem(itemId: String) {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val user = _currentUser.value ?: return@launch
                val item = user.inventory.items.firstOrNull { it.id == itemId }
                if (item == null) {
                    _state.value = AuthState(errorMessage = "Item no encontrado")
                    return@launch
                }
                val targetCategory = item.equipmentCategory()
                if (targetCategory == null) {
                    _state.value = AuthState(errorMessage = "No se puede equipar este item")
                    return@launch
                }

                val equipmentItem = EquipmentItem(
                    id = item.id,
                    name = item.name,
                    icon = item.icon,
                    rarity = item.rarity,
                    category = targetCategory,
                    stats = item.stats,
                    isEquipped = true
                )

                val newEquipment = when (targetCategory) {
                    EquipmentCategory.WEAPON_LEFT, EquipmentCategory.WEAPON_RIGHT -> {
                        when {
                            user.equipment.weaponLeft == null -> user.equipment.copy(weaponLeft = equipmentItem)
                            user.equipment.weaponRight == null -> user.equipment.copy(weaponRight = equipmentItem)
                            else -> null
                        }
                    }
                    EquipmentCategory.HEAD -> user.equipment.copy(head = equipmentItem)
                    EquipmentCategory.CHEST -> {
                        when {
                            user.equipment.chest == null -> user.equipment.copy(chest = equipmentItem)
                            else -> null
                        }
                    }
                    EquipmentCategory.LEGS -> user.equipment.copy(legs = equipmentItem)
                    EquipmentCategory.FEET -> user.equipment.copy(feet = equipmentItem)
                    EquipmentCategory.COLLAR -> {
                        when {
                            user.equipment.collar == null -> user.equipment.copy(collar = equipmentItem)
                            user.equipment.earringLeft == null -> user.equipment.copy(earringLeft = equipmentItem)
                            user.equipment.earringRight == null -> user.equipment.copy(earringRight = equipmentItem)
                            user.equipment.cape == null -> user.equipment.copy(cape = equipmentItem)
                            else -> null
                        }
                    }
                    EquipmentCategory.RING_SLOT -> user.equipment.copy(ringSlot = user.equipment.ringSlot + equipmentItem)
                    EquipmentCategory.GLOVE_LEFT, EquipmentCategory.GLOVE_RIGHT -> {
                        when {
                            user.equipment.gloveLeft == null -> user.equipment.copy(gloveLeft = equipmentItem)
                            user.equipment.gloveRight == null -> user.equipment.copy(gloveRight = equipmentItem)
                            else -> null
                        }
                    }
                    EquipmentCategory.EARRING_LEFT, EquipmentCategory.EARRING_RIGHT -> {
                        when {
                            user.equipment.earringLeft == null -> user.equipment.copy(earringLeft = equipmentItem)
                            user.equipment.earringRight == null -> user.equipment.copy(earringRight = equipmentItem)
                            else -> null
                        }
                    }
                    EquipmentCategory.CAPE -> user.equipment.copy(cape = equipmentItem)
                }

                if (newEquipment == null) {
                    _state.value = AuthState(errorMessage = "No hay espacio para equipar ese item")
                    return@launch
                }

                val updatedInventoryItems = user.inventory.items.toMutableList()
                val updatedItem = if (item.quantity > 1) item.copy(quantity = item.quantity - 1) else null
                if (updatedItem != null) {
                    updatedInventoryItems[updatedInventoryItems.indexOfFirst { it.id == itemId }] = updatedItem
                } else {
                    updatedInventoryItems.removeAll { it.id == itemId }
                }
                val updatedInventory = user.inventory.copy(items = updatedInventoryItems)

                val inventoryUpdate = mapOf(
                    "inventory" to updatedInventory.toMap(),
                    "equipment" to newEquipment.toMap()
                )

                db.collection("users").document(currentUser.uid)
                    .set(inventoryUpdate, com.google.firebase.firestore.SetOptions.merge())
                    .await()

                // Actualizar localmente para UI inmediata
                _currentUser.value = user.copy(inventory = updatedInventory, equipment = newEquipment)
                
                // Recargar desde Firestore para verificar consistencia
                try {
                    val doc = db.collection("users").document(currentUser.uid).get().await()
                    if (doc.exists()) {
                        val loadedUser = User.fromDocument(doc)
                        if (loadedUser != null) {
                            _currentUser.value = loadedUser
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AuthViewModel", "Error reloading user after equip", e)
                }
                
                _state.value = AuthState(isSuccess = true)
            } catch (e: Exception) {
                _state.value = AuthState(errorMessage = e.message ?: "Error al equipar item")
            }
        }
    }

    fun unequipEquipment(equipmentId: String) {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val user = _currentUser.value ?: return@launch
                var found = false
                var newEquipment = user.equipment
                // remove from singular slots
                if (newEquipment.head?.id == equipmentId) { newEquipment = newEquipment.copy(head = null); found = true }
                if (newEquipment.chest?.id == equipmentId) { newEquipment = newEquipment.copy(chest = null); found = true }
                if (newEquipment.legs?.id == equipmentId) { newEquipment = newEquipment.copy(legs = null); found = true }
                if (newEquipment.feet?.id == equipmentId) { newEquipment = newEquipment.copy(feet = null); found = true }
                if (newEquipment.collar?.id == equipmentId) { newEquipment = newEquipment.copy(collar = null); found = true }
                if (newEquipment.earringLeft?.id == equipmentId) { newEquipment = newEquipment.copy(earringLeft = null); found = true }
                if (newEquipment.earringRight?.id == equipmentId) { newEquipment = newEquipment.copy(earringRight = null); found = true }
                if (newEquipment.cape?.id == equipmentId) { newEquipment = newEquipment.copy(cape = null); found = true }
                if (newEquipment.weaponLeft?.id == equipmentId) { newEquipment = newEquipment.copy(weaponLeft = null); found = true }
                if (newEquipment.weaponRight?.id == equipmentId) { newEquipment = newEquipment.copy(weaponRight = null); found = true }
                if (newEquipment.gloveLeft?.id == equipmentId) { newEquipment = newEquipment.copy(gloveLeft = null); found = true }
                if (newEquipment.gloveRight?.id == equipmentId) { newEquipment = newEquipment.copy(gloveRight = null); found = true }
                // remove from rings
                if (!found && newEquipment.ringSlot.any { it.id == equipmentId }) {
                    newEquipment = newEquipment.copy(ringSlot = newEquipment.ringSlot.filter { it.id != equipmentId })
                    found = true
                }

                if (!found) {
                    _state.value = AuthState(errorMessage = "Equipo no encontrado")
                    return@launch
                }

                // add back to inventory (quantity 1)
                val inventoryItems = user.inventory.items.toMutableList()
                val existingIndex = inventoryItems.indexOfFirst { it.id == equipmentId }
                if (existingIndex >= 0) {
                    val existing = inventoryItems[existingIndex]
                    inventoryItems[existingIndex] = existing.copy(quantity = existing.quantity + 1)
                } else {
                    // try to find equipment item details from previous equipment
                    val removedItem = listOfNotNull(
                        user.equipment.head, user.equipment.chest, user.equipment.legs, user.equipment.feet,
                        user.equipment.collar, user.equipment.earringLeft, user.equipment.earringRight, user.equipment.cape,
                        user.equipment.weaponLeft, user.equipment.weaponRight, user.equipment.gloveLeft, user.equipment.gloveRight
                    ).plus(user.equipment.ringSlot).firstOrNull { it.id == equipmentId }

                    val invItem = removedItem?.let {
                        InventoryItem(
                            id = it.id,
                            name = it.name,
                            description = "",
                            icon = it.icon,
                            rarity = it.rarity,
                            quantity = 1,
                            category = it.category.name,
                            stackable = false,
                            sellPrice = 0,
                            stats = it.stats
                        )
                    } ?: InventoryItem(id = equipmentId, name = "Objeto", quantity = 1)

                    inventoryItems.add(invItem)
                }

                val updatedInventory = user.inventory.copy(items = inventoryItems)

                val update = mapOf(
                    "inventory" to updatedInventory.toMap(),
                    "equipment" to newEquipment.toMap()
                )

                db.collection("users").document(currentUser.uid)
                    .set(update, com.google.firebase.firestore.SetOptions.merge())
                    .await()

                _currentUser.value = user.copy(inventory = updatedInventory, equipment = newEquipment)
                loadUserData(currentUser.uid)
                _state.value = AuthState(isSuccess = true)
            } catch (e: Exception) {
                _state.value = AuthState(errorMessage = e.message ?: "Error al desequipar el item")
            }
        }
    }

    private fun loadUserData(uid: String) {
        viewModelScope.launch {
            try {
                val doc = db.collection("users").document(uid).get().await()
                if (doc.exists()) {
                    val loadedUser = User.fromDocument(doc)
                    Log.d("AuthViewModel", "Loaded user data for $uid: role=${loadedUser?.role}")
                    _currentUser.value = loadedUser
                } else {
                    val defaultUser = User(
                        id = uid,
                        displayName = auth.currentUser?.displayName ?: "Usuario",
                        email = auth.currentUser?.email ?: "",
                        level = 1,
                        role = UserRole.INITIAL
                    )
                    _currentUser.value = defaultUser
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error loading user data", e)
                _currentUser.value = User(
                    id = uid,
                    displayName = auth.currentUser?.displayName ?: "Usuario",
                    email = auth.currentUser?.email ?: "",
                    level = 1,
                    role = UserRole.INITIAL
                )
            }
        }
    }

    fun loadGlobalAccessKey() {
        viewModelScope.launch {
            try {
                val docRef = db.collection("appSettings").document("accessKey")
                val snapshot = docRef.get().await()
                val keyValue = snapshot.getString("value") ?: "VHKM0"
                if (!snapshot.exists()) {
                    docRef.set(mapOf("value" to keyValue)).await()
                }
                _globalAccessKey.value = keyValue
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error loading global access key", e)
                _globalAccessKey.value = "VHKM0"
            }
        }
    }

    fun verifyAccessKey(enteredKey: String) {
        val currentUser = auth.currentUser
        val expectedKey = _globalAccessKey.value ?: "VHKM0"
        if (currentUser == null) {
            _state.value = AuthState(errorMessage = "No hay usuario autenticado")
            return
        }
        if (enteredKey.isBlank()) {
            _state.value = AuthState(errorMessage = "Ingresa la clave de acceso")
            return
        }
        viewModelScope.launch {
            if (enteredKey != expectedKey) {
                _state.value = AuthState(errorMessage = "Clave incorrecta")
                return@launch
            }
            try {
                db.collection("users").document(currentUser.uid)
                    .update("accessKey", enteredKey).await()
                loadUserData(currentUser.uid)
                _state.value = AuthState(isSuccess = true)
            } catch (e: Exception) {
                _state.value = AuthState(errorMessage = e.message ?: "Error al validar la clave")
            }
        }
    }

    fun deleteAccount(onDeleted: () -> Unit = {}) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _state.value = AuthState(errorMessage = "No hay usuario autenticado")
            return
        }
        viewModelScope.launch {
            try {
                db.collection("users").document(currentUser.uid).delete().await()
                currentUser.delete().await()
                signOut()
                onDeleted()
                _state.value = AuthState(isSuccess = true)
            } catch (e: Exception) {
                _state.value = AuthState(errorMessage = e.message ?: "Error al eliminar la cuenta")
            }
        }
    }

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = _state.value.copy(errorMessage = "Completa todos los campos")
            return
        }

        viewModelScope.launch {
            _state.value = AuthState(isLoading = true)
            try {
                auth.signInWithEmailAndPassword(email.trim(), password).await()
                auth.currentUser?.uid?.let { loadUserData(it) }
                _state.value = AuthState(isSuccess = true)
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _state.value = AuthState(errorMessage = "Email o contraseña incorrectos")
            } catch (e: FirebaseAuthInvalidUserException) {
                _state.value = AuthState(errorMessage = "Este usuario no existe")
            } catch (e: Exception) {
                _state.value = AuthState(errorMessage = e.message ?: "Error al iniciar sesión")
            }
        }
    }

    fun signUp(displayName: String, email: String, password: String, confirmPassword: String, accessKey: String = "VHKM0") {
        if (displayName.isBlank() || email.isBlank() || password.isBlank()) {
            _state.value = AuthState(errorMessage = "Completa todos los campos")
            return
        }
        if (displayName.length < 2) {
            _state.value = AuthState(errorMessage = "El nombre debe tener al menos 2 caracteres")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            _state.value = AuthState(errorMessage = "Email no válido")
            return
        }
        if (password.length < 6) {
            _state.value = AuthState(errorMessage = "La contraseña debe tener al menos 6 caracteres")
            return
        }
        if (password != confirmPassword) {
            _state.value = AuthState(errorMessage = "Las contraseñas no coinciden")
            return
        }
        val expectedKey = _globalAccessKey.value ?: "VHKM0"
        if (accessKey != expectedKey) {
            _state.value = AuthState(errorMessage = "Clave de acceso incorrecta")
            return
        }

        viewModelScope.launch {
            _state.value = AuthState(isLoading = true)
            try {
                val result = auth.createUserWithEmailAndPassword(email.trim(), password).await()
                val firebaseUser = result.user ?: throw Exception("Error al crear usuario")
                val uid = firebaseUser.uid
                firebaseUser.updateProfile(com.google.firebase.auth.UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName.trim())
                    .build()).await()
                val user = User(
                    id = uid,
                    displayName = displayName.trim(),
                    email = email.trim(),
                    role = UserRole.INITIAL,
                    accessKey = accessKey,
                    wallet = Wallet()
                )
                db.collection("users").document(uid).set(user).await()
                loadUserData(uid)
                _state.value = AuthState(isSuccess = true, isRegistered = true)
            } catch (e: FirebaseAuthWeakPasswordException) {
                _state.value = AuthState(errorMessage = "Contraseña demasiado débil")
            } catch (e: FirebaseAuthUserCollisionException) {
                _state.value = AuthState(errorMessage = "Este email ya está registrado")
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _state.value = AuthState(errorMessage = "Formato de email no válido")
            } catch (e: Exception) {
                _state.value = AuthState(errorMessage = e.message ?: "Error al registrarse")
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(errorMessage = null, isSuccess = false)
    }

    fun signOut() {
        auth.signOut()
        _state.value = AuthState()
        _currentUser.value = null
    }

    fun updateProfile(bio: String = "", location: String = "", website: String = "") {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val updates = mapOf(
                    "bio" to bio,
                    "location" to location,
                    "website" to website
                )
                db.collection("users").document(currentUser.uid).update(updates).await()
                loadUserData(currentUser.uid)
            } catch (e: Exception) {
                _state.value = AuthState(errorMessage = e.message ?: "Error al actualizar perfil")
            }
        }
    }

    fun applySpecies(species: com.loshii.dndzerinx.model.SpeciesDefinition) {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val user = _currentUser.value ?: return@launch
                // add species stats to base characterStatus
                val updatedStatus = user.characterStatus.withAddedStats(species.stats)
                val updates = mapOf(
                    "race" to species.name,
                    "characterStatus" to updatedStatus.toMap()
                )
                db.collection("users").document(currentUser.uid).update(updates).await()
                _currentUser.value = user.copy(race = species.name, characterStatus = updatedStatus)
                loadUserData(currentUser.uid)
                _state.value = AuthState(isSuccess = true)
            } catch (e: Exception) {
                _state.value = AuthState(errorMessage = e.message ?: "Error al aplicar especie")
            }
        }
    }

    fun searchUsersByDisplayName(query: String) {
        if (query.isBlank()) {
            _userSearchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            try {
                val snapshot = db.collection("users")
                    .orderBy("displayName")
                    .startAt(query)
                    .endAt("$query\uf8ff")
                    .limit(20)
                    .get()
                    .await()

                _userSearchResults.value = snapshot.documents.mapNotNull { User.fromDocument(it) }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Error searching users: ${e.message}", e)
                _userSearchResults.value = emptyList()
            }
        }
    }

    fun updateUserRole(targetUserId: String, role: UserRole) {
        val currentUser = auth.currentUser
        if (currentUser == null || _currentUser.value?.role != UserRole.GM) {
            _state.value = AuthState(errorMessage = "No tienes permisos para realizar esta acción")
            return
        }
        viewModelScope.launch {
            try {
                db.collection("users").document(targetUserId).update("role", role.name).await()
                _state.value = AuthState(isSuccess = true)
            } catch (e: Exception) {
                _state.value = AuthState(errorMessage = e.message ?: "Error al actualizar rol")
            }
        }
    }

    fun changePassword(newPassword: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _state.value = AuthState(errorMessage = "No hay usuario autenticado")
            return
        }
        if (newPassword.length < 6) {
            _state.value = AuthState(errorMessage = "La contraseña debe tener al menos 6 caracteres")
            return
        }
        viewModelScope.launch {
            try {
                currentUser.updatePassword(newPassword).await()
                _state.value = AuthState(isSuccess = true)
            } catch (e: Exception) {
                _state.value = AuthState(errorMessage = e.message ?: "Error al cambiar contraseña")
            }
        }
    }

    fun updateAccessKey(newKey: String) {
        val currentUser = auth.currentUser
        if (currentUser == null || _currentUser.value?.role != UserRole.GM) {
            _state.value = AuthState(errorMessage = "No tienes permisos para cambiar la clave de acceso")
            return
        }
        viewModelScope.launch {
            try {
                val updates = mapOf("accessKey" to newKey)
                db.collection("users").document(currentUser.uid).update(updates).await()
                db.collection("appSettings").document("accessKey")
                    .set(mapOf("value" to newKey)).await()
                _globalAccessKey.value = newKey
                loadUserData(currentUser.uid)
                _state.value = AuthState(isSuccess = true)
            } catch (e: Exception) {
                _state.value = AuthState(errorMessage = e.message ?: "Error al actualizar clave")
            }
        }
    }

    fun upgradeToGM() {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                db.collection("users").document(currentUser.uid)
                    .update("role", UserRole.GM.name).await()
                val doc = db.collection("users").document(currentUser.uid).get().await()
                if (doc.exists()) {
                    _currentUser.value = User.fromDocument(doc)
                }
                _state.value = AuthState(isSuccess = true)
            } catch (e: Exception) {
                _state.value = AuthState(errorMessage = e.message ?: "Error al actualizar rol")
            }
        }
    }

    fun assignGuildLeader(userId: String, guildId: String, guildName: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) return

        val user = _currentUser.value
        if (user?.role != UserRole.GM) {
            _state.value = AuthState(errorMessage = "Solo un Game Master puede asignar líderes de gremio")
            return
        }

        viewModelScope.launch {
            try {
                val leaderRoleName = "Líder : ${guildName.trim()}"
                val updates = mapOf(
                    "role" to UserRole.ADVENTURER.name,
                    "guildId" to guildId,
                    "guildName" to guildName.trim(),
                    "guildRole" to GuildRole.LEADER.name,
                    "guildLeaderRoleName" to leaderRoleName
                )
                db.collection("users").document(userId).update(updates).await()
                _state.value = AuthState(isSuccess = true)
            } catch (e: Exception) {
                _state.value = AuthState(errorMessage = e.message ?: "Error al asignar líder")
            }
        }
    }

    fun grantAdventurerRole(userId: String, guildId: String, guildName: String) {
        val currentUser = auth.currentUser
        val actor = _currentUser.value
        if (currentUser == null || actor == null) return

        val canInviteToGuild = actor.role == UserRole.GM ||
            (actor.guildRole == GuildRole.LEADER && actor.guildId == guildId)
        if (!canInviteToGuild) {
            _state.value = AuthState(errorMessage = "Solo el GM o el líder del gremio puede aceptar aventureros")
            return
        }

        viewModelScope.launch {
            try {
                val updates = mapOf(
                    "role" to UserRole.ADVENTURER.name,
                    "guildId" to guildId,
                    "guildName" to guildName.trim(),
                    "guildRole" to GuildRole.MEMBER.name,
                    "guildLeaderRoleName" to ""
                )
                db.collection("users").document(userId).update(updates).await()
                _state.value = AuthState(isSuccess = true)
            } catch (e: Exception) {
                _state.value = AuthState(errorMessage = e.message ?: "Error al unir aventurero al gremio")
            }
        }
    }

    fun setActiveStatus(isActive: Boolean) {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                db.collection("users").document(currentUser.uid).update("isActive", isActive).await()
                loadUserData(currentUser.uid)
            } catch (e: Exception) {
                _state.value = AuthState(errorMessage = e.message ?: "Error al actualizar estado")
            }
        }
    }

    fun updateDisplayName(newName: String) {
        val currentUser = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                db.collection("users").document(currentUser.uid).set(
                    mapOf("displayName" to newName),
                    com.google.firebase.firestore.SetOptions.merge()
                ).await()
                currentUser.updateProfile(
                    com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName(newName)
                        .build()
                ).await()
                loadUserData(currentUser.uid)
            } catch (e: Exception) {
                _state.value = AuthState(errorMessage = e.message ?: "Error al actualizar nombre")
            }
        }
    }

    fun updateCharacterStat(statName: String, increment: Int) {
        val currentUser = auth.currentUser ?: return
        val user = _currentUser.value ?: return
        
        if (user.characterStatus.skillPoints < increment) {
            _state.value = AuthState(errorMessage = "Puntos de habilidad insuficientes")
            return
        }

        val currentValue = when (statName) {
            "strength" -> user.characterStatus.strength
            "defense" -> user.characterStatus.defense
            "agility" -> user.characterStatus.agility
            "magic" -> user.characterStatus.magic
            "intelligence" -> user.characterStatus.intelligence
            "charisma" -> user.characterStatus.charisma
            "speed" -> user.characterStatus.speed
            "magicResist" -> user.characterStatus.magicResist
            "physicalResist" -> user.characterStatus.physicalResist
            "tenacity" -> user.characterStatus.tenacity
            "truth" -> user.characterStatus.truth
            "resonance" -> user.characterStatus.resonance
            "spirit" -> user.characterStatus.spirit
            "critChance" -> user.characterStatus.critChance
            "amplifier" -> user.characterStatus.amplifier
            "luck" -> user.characterStatus.luck
            else -> return
        }

        viewModelScope.launch {
            try {
                val updates = mapOf(
                    "characterStatus.$statName" to currentValue + increment,
                    "characterStatus.skillPoints" to user.characterStatus.skillPoints - increment
                )
                db.collection("users").document(currentUser.uid).update(updates).await()
                loadUserData(currentUser.uid)
                _state.value = AuthState(isSuccess = true)
            } catch (e: Exception) {
                _state.value = AuthState(errorMessage = e.message ?: "Error al actualizar estadística")
            }
        }
    }

    fun addSkillPoints(points: Int) {
        val currentUser = auth.currentUser ?: return
        val user = _currentUser.value ?: return
        
        viewModelScope.launch {
            try {
                val updates = mapOf(
                    "characterStatus.skillPoints" to user.characterStatus.skillPoints + points
                )
                db.collection("users").document(currentUser.uid).update(updates).await()
                loadUserData(currentUser.uid)
            } catch (e: Exception) {
                _state.value = AuthState(errorMessage = e.message ?: "Error al agregar puntos")
            }
        }
    }

    fun addExperience(amount: Int) {
        val currentUser = auth.currentUser ?: return
        val user = _currentUser.value ?: return

        viewModelScope.launch {
            try {
                val updates = progressionUpdates(user, amount)
                db.collection("users").document(currentUser.uid).update(updates).await()
                loadUserData(currentUser.uid)
                _state.value = AuthState(isSuccess = true)
            } catch (e: Exception) {
                _state.value = AuthState(errorMessage = e.message ?: "Error al agregar experiencia")
            }
        }
    }

    fun grantMonsterExperience(
        activityType: ExperienceActivityType,
        difficulty: EncounterDifficulty,
        monsterLevel: Int,
        contributionPercent: Int,
        isFirstKill: Boolean,
        teamSize: Int,
        eventMultiplier: Float = 1.0f
    ): ExperienceReward? {
        val currentUser = auth.currentUser ?: return null
        val user = _currentUser.value ?: return null

        if (user.role == UserRole.INITIAL && activityType in listOf(ExperienceActivityType.RAID, ExperienceActivityType.DUNGEON, ExperienceActivityType.MONSTER_EVENT)) {
            _state.value = AuthState(errorMessage = "Los usuarios Inicial deben unirse a un gremio para acceder a contenido avanzado")
            return null
        }

        val reward = LevelProgression.calculateReward(
            activityType = activityType,
            difficulty = difficulty,
            participantLevel = user.level,
            monsterLevel = monsterLevel,
            contributionPercent = contributionPercent,
            isFirstKill = isFirstKill,
            teamSize = teamSize,
            eventMultiplier = eventMultiplier
        )

        viewModelScope.launch {
            try {
                val updates = progressionUpdates(user, reward.total).toMutableMap()
                updates["progression.lastActivity"] = activityType.name
                updates["progression.lastDifficulty"] = difficulty.name
                updates["progression.lastReward"] = reward.total
                db.collection("users").document(currentUser.uid).update(updates).await()
                loadUserData(currentUser.uid)
                _state.value = AuthState(isSuccess = true)
            } catch (e: Exception) {
                _state.value = AuthState(errorMessage = e.message ?: "Error al otorgar experiencia de monstruos")
            }
        }

        return reward
    }

    fun grantDailyBonus() {
        addExperience(60)
    }

    fun grantQuestReward(questId: String, experience: Int, gold: Int = 0, gems: Int = 0) {
        val currentUser = auth.currentUser ?: return
        
        viewModelScope.launch {
            try {
                val user = _currentUser.value ?: return@launch

                val walletUpdates = progressionUpdates(user, experience).toMutableMap()
                walletUpdates["progression.lastQuestId"] = questId
                if (gold > 0) walletUpdates["wallet.gold"] = user.wallet.gold + gold
                if (gems > 0) walletUpdates["wallet.gems"] = user.wallet.gems + gems
                
                db.collection("users").document(currentUser.uid).update(walletUpdates).await()
                loadUserData(currentUser.uid)
                _state.value = AuthState(isSuccess = true)
            } catch (e: Exception) {
                _state.value = AuthState(errorMessage = e.message ?: "Error al reclamar recompensa")
            }
        }
    }

    private fun progressionUpdates(user: User, gainedExperience: Int): Map<String, Any> {
        var level = user.level.coerceAtLeast(1)
        var experience = user.experience + gainedExperience.coerceAtLeast(0)
        var experienceToNext = LevelProgression.experienceForNextLevel(level)
        var skillPointsGained = 0

        while (experience >= experienceToNext && level < LevelProgression.MAX_LEVEL) {
            experience -= experienceToNext
            level += 1
            skillPointsGained += LevelProgression.skillPointsForLevel(level)
            experienceToNext = LevelProgression.experienceForNextLevel(level)
        }

        if (level >= LevelProgression.MAX_LEVEL) {
            level = LevelProgression.MAX_LEVEL
            experience = experience.coerceAtMost(experienceToNext)
        }

        val updates = mutableMapOf<String, Any>(
            "experience" to experience,
            "level" to level,
            "experienceToNextLevel" to experienceToNext,
            "progression.lastExperienceGained" to gainedExperience.coerceAtLeast(0)
        )
        if (skillPointsGained > 0) {
            updates["characterStatus.skillPoints"] = user.characterStatus.skillPoints + skillPointsGained
            updates["progression.lastSkillPointsGained"] = skillPointsGained
        }
        return updates
    }

fun uploadImage(type: ImageType, context: android.content.Context, uri: android.net.Uri, onComplete: (String?) -> Unit) {
        val currentUser = auth.currentUser ?: run {
            onComplete(null)
            return
        }

        // Get current URL for cleanup after successful upload
        val currentUrl = when (type) {
            ImageType.AVATAR -> _currentUser.value?.avatarUrl
            ImageType.BANNER -> _currentUser.value?.bannerUrl
        }

        viewModelScope.launch {
            _state.value = AuthState(isLoading = true)
            try {
                val (dbKey, logPrefix) = when (type) {
                    ImageType.AVATAR -> "avatarUrl" to "avatar"
                    ImageType.BANNER -> "bannerUrl" to "banner"
                }

                Log.d("AuthViewModel", "Starting $logPrefix upload for user: ${currentUser.uid}")
                
                val url = when (type) {
                    ImageType.AVATAR -> ImageUploader.uploadAvatar(context, uri, currentUser.uid)
                    ImageType.BANNER -> ImageUploader.uploadBanner(context, uri, currentUser.uid)
                }
                Log.d("AuthViewModel", "$logPrefix upload result: $url")
                
                if (url != null) {
                    db.collection("users").document(currentUser.uid).set(
                        mapOf(dbKey to url),
                        com.google.firebase.firestore.SetOptions.merge()
                    ).await()
                    Log.d("AuthViewModel", "$logPrefix saved to Firestore")
                    
                    // Update local currentUser state
                    _currentUser.value = _currentUser.value?.let { currentUserData ->
                        when (type) {
                            ImageType.AVATAR -> currentUserData.copy(avatarUrl = url)
                            ImageType.BANNER -> currentUserData.copy(bannerUrl = url)
                        }
                    } ?: _currentUser.value

                    Log.d("AuthViewModel", "Updated currentUser locally with new $dbKey")
                    
                    // Delete old image if it exists
                    if (currentUrl != null && currentUrl.isNotBlank()) {
                        viewModelScope.launch {
                            try {
                                val deleted = ImageUploader.deleteImage(currentUrl)
                                Log.d("AuthViewModel", "Old $logPrefix image deletion: $deleted (URL: $currentUrl)")
                            } catch (e: Exception) {
                                Log.e("AuthViewModel", "Error deleting old $logPrefix image: ${e.message}", e)
                            }
                        }
                    }

                    _state.value = AuthState(isSuccess = true)
                    onComplete(url)
                } else {
                    Log.e("AuthViewModel", "$logPrefix upload returned null")
                    _state.value = AuthState(errorMessage = "Error al subir la imagen")
                    onComplete(null)
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Image upload error: ${e.message}", e)
                _state.value = AuthState(errorMessage = "Error: ${e.message}")
                onComplete(null)
            }
        }
    }
}

enum class ImageType {
    AVATAR,
    BANNER
}

enum class StatType(val displayName: String, val description: String, val costPerPoint: Int) {
    STRENGTH("Fuerza", "Aumenta daño físico y capacidad de carga", 1),
    DEFENSE("Defensa", "Reduce daño recibido", 1),
    AGILITY("Destreza", "Aumenta velocidad y evasión", 1),
    MAGIC("Magia", "Aumenta daño mágico", 1),
    INTELLIGENCE("Inteligencia", "Aumenta habilidades cognitivas", 1),
    CHARISMA("Carisma", "Aumenta persuasión y liderazgo", 1),
    SPEED("Velocidad", "Aumenta velocidad de ataque y movimiento", 1),
    MAGIC_RESIST("Resistencia Mágica", "Reduce daño mágico recibido", 1),
    PHYSICAL_RESIST("Resistencia Física", "Reduce daño físico recibido", 1),
    TRUTH("Verdad", "Aumenta precisión y detección", 1),
    RESONANCE("Resonancia", "Aumenta efectividad de buffs", 1),
    SPIRIT("Espíritu", "Aumenta mana y regeneración", 1),
    CRIT_CHANCE("Golpe Crítico", "Aumenta probabilidad de crítico", 2),
    AMPLIFIER("Amplificador", "Aumenta daño de habilidades", 2)
}
