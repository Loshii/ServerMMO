package com.loshii.dndzerinx.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.compose.NavHost
import com.google.firebase.auth.FirebaseAuth
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.loshii.dndzerinx.ui.components.CollapsibleBottomNavBar
import com.loshii.dndzerinx.ui.screens.auth.AuthScreen
import com.loshii.dndzerinx.ui.screens.chat.ChatScreen
import com.loshii.dndzerinx.ui.screens.library.ClassLibraryScreen
import com.loshii.dndzerinx.ui.screens.profile.LocalProfileScreen
import com.loshii.dndzerinx.ui.screens.profile.CharacterScreen
import com.loshii.dndzerinx.ui.screens.settings.SettingsScreen
import com.loshii.dndzerinx.ui.screens.game.GameWorldScreen
import com.loshii.dndzerinx.viewmodel.AuthViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

sealed class Screen(val route: String) {
    data object Auth : Screen("auth")
    data object Profile : Screen("profile")
    data object Character : Screen("character")
    data object Chat : Screen("chat")
    data object Library : Screen("library")
    data object Settings : Screen("settings")
    data object GameWorld : Screen("gameworld")
}

private val bottomNavRoutes = listOf("library", "chat", "profile", "settings")

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ""
    val auth = FirebaseAuth.getInstance()
    val authViewModel: AuthViewModel = viewModel()
    val startDestination = if (auth.currentUser != null) Screen.GameWorld.route else Screen.Auth.route

    val showBottomBar = currentRoute in bottomNavRoutes
    var isBottomNavVisible by remember { mutableStateOf(true) }

    val user by authViewModel.currentUser.collectAsState()
    val globalAccessKey by authViewModel.globalAccessKey.collectAsState()
    var accessKeyInput by remember { mutableStateOf("") }
    var accessKeyPromptError by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val needsAccessKeyPrompt = user?.let { u -> globalAccessKey != null && u.accessKey != globalAccessKey } == true

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Screen.Auth.route) {
                AuthScreen(
                    viewModel = authViewModel,
                    onAuthenticated = {
                        navController.navigate(Screen.GameWorld.route) {
                            popUpTo(Screen.Auth.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.GameWorld.route) {
                GameWorldScreen(
                    viewModel = authViewModel,
                    onNavigateToProfile = {
                        navController.navigate(Screen.Profile.route)
                    },
                    onNavigateToSettings = {
                        navController.navigate(Screen.Settings.route)
                    },
                    onSignOut = {
                        auth.signOut()
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Chat.route) {
                ChatScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.Library.route) {
                ClassLibraryScreen(onBack = { navController.popBackStack() })
            }

            composable(Screen.Character.route) {
                CharacterScreen(
                    onBack = { navController.popBackStack() },
                    viewModel = authViewModel
                )
            }

            composable(Screen.Profile.route) {
                LocalProfileScreen(
                    viewModel = authViewModel,
                    onBack = { navController.popBackStack() },
                    onSettings = { navController.navigate(Screen.Settings.route) },
                    onSignOut = {
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onOpenCharacter = {
                        navController.navigate(Screen.Character.route)
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = authViewModel,
                    onBack = { navController.popBackStack() },
                    onSignOut = {
                        navController.navigate(Screen.Auth.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        if (needsAccessKeyPrompt && currentRoute != Screen.Auth.route) {
            Dialog(
                onDismissRequest = {},
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.80f))
                ) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(
                                text = "Clave de acceso actualizada",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Ingresa la nueva clave para continuar.",
                                color = Color(0xFFBBBBBB),
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(18.dp))

                            OutlinedTextField(
                                value = accessKeyInput,
                                onValueChange = {
                                    accessKeyInput = it
                                    accessKeyPromptError = null
                                },
                                label = { Text("Clave de acceso", color = Color.White) },
                                placeholder = { Text("VHKM0") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                            )

                            if (accessKeyPromptError != null) {
                                Text(
                                    text = accessKeyPromptError ?: "",
                                    color = Color(0xFFFF6B6B),
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            Button(
                                onClick = {
                                    if (accessKeyInput.isBlank()) {
                                        accessKeyPromptError = "Debes ingresar la clave"
                                        return@Button
                                    }
                                    if (accessKeyInput != globalAccessKey) {
                                        accessKeyPromptError = "Clave incorrecta"
                                    } else {
                                        authViewModel.verifyAccessKey(accessKeyInput)
                                        accessKeyInput = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5))
                            ) {
                                Text("Confirmar", color = Color.White)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Opciones",
                                color = Color(0xFFBBBBBB),
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { navController.navigate(Screen.Settings.route) },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF424242))
                            ) {
                                Text("Ir a configuración", color = Color.White)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            TextButton(
                                onClick = { showDeleteConfirm = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Eliminar cuenta", color = Color(0xFFFF5252))
                            }
                        }
                    }
                }
            }
        }

        if (showDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirm = false },
                title = { Text("Eliminar cuenta") },
                text = { Text("¿Estás seguro de que deseas eliminar tu cuenta? Esta acción es irreversible.") },
                confirmButton = {
                    TextButton(onClick = {
                        showDeleteConfirm = false
                        authViewModel.deleteAccount {
                            navController.navigate(Screen.Auth.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }) {
                        Text("Eliminar", color = Color(0xFFFF5252))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirm = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        if (showBottomBar && currentRoute != Screen.Auth.route) {
            CollapsibleBottomNavBar(
                currentRoute = currentRoute,
                onTabSelected = { tab ->
                    when (tab) {
                        "library" -> navController.navigate(Screen.Library.route) { popUpTo(Screen.Library.route) { inclusive = true } }
                        "chat" -> navController.navigate(Screen.Chat.route)
                        "profile" -> navController.navigate(Screen.Profile.route) { popUpTo(Screen.Profile.route) { inclusive = true } }
                        "create" -> navController.navigate(Screen.Character.route)
                    }
                },
                isVisible = isBottomNavVisible,
                onToggleVisibility = { isBottomNavVisible = !isBottomNavVisible },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
            )
        }
    }
}
