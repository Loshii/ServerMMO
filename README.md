# DnD RoleGate

Proyecto Android en Kotlin para seguir campañas de rol con Firebase y una interfaz moderna tipo Role Gate.

## Características

- **Autenticación Firebase**: Login/Registro con email y contraseña
- **Perfil de Usuario**: Nivel, etiquetas, monedero (oro/plata/cobre/gemas)
- **Jetpack Compose** con Material 3
- **Firebase Firestore**: Sincronización de chat y datos
- **Estructura de UI** para reglas, personajes, seguimiento y comunidad
- **Ficha de Personaje** con stats 5E (Fuerza, Destreza, Constitución, Inteligencia, Sabiduría, Carisma)
- **Chat de Campaña** con soporte para dados (/roll)
- **Reglas 5e SRD** optimizadas en español

## Pantallas

- **Auth**: Login y registro de usuarios
- **Home**: Menú principal con acceso rápido a todas las funciones
- **Profile**: Perfil de usuario con nivel, etiquetas y monedero
- **Chat**: Sala de campaña con mensajería y dados
- **Group Chat**: Sala grupal con lanzamiento de dados
- **Character Sheet**: Ficha de personaje 5E
- **Rules 5e**: Resumen de reglas SRD
- **Class Library**: Biblioteca de clases 5E
- **Integración Godot**: Base de integración descrita en `docs/GODOT_INTEGRATION.md`

## Modelos

### User

- id, displayName, email
- avatarUrl, bannerUrl
- level, role, tags
- wallet (gold, silver, copper, gems)

### CharacterSheet

- 6 stats con modificadores
- HP, CA, Iniciativa
- Wallet integrado

## Requisitos

- Android Studio Hedgehog o posterior
- SDK Android 34
- JDK 21
- `google-services.json` en `app/` desde Firebase

## Uso

1. Abre el proyecto en Android Studio
2. Agrega tu `google-services.json` en `app/`
3. Sincroniza Gradle
4. Ejecuta en emulador o dispositivo

## Versión de dependencias

- Kotlin 1.9.22
- Compose 1.6.7
- Material3 1.2.1
- Firebase BOM 33.1.0
- Coroutines 1.8.1
