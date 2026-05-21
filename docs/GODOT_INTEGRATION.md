# Integración Godot para DnD RoleGate

Este documento describe cómo convertir la parte de juego actual en un sistema compatible con Godot y cómo mantener la app Android como lobby/auth.

## 1. Arquitectura recomendada

- La app Android sigue manejando:
  - login / registro con Firebase
  - navegación de usuario
  - configuración / perfil
  - inventario y datos locales
- Godot se encarga de:
  - render del mundo de juego
  - físicas, animaciones y efectos
  - entrada de jugador (touch, teclado, gamepad)
  - lógica de juego interactiva en escena
- El backend permanece igual:
  - WebSocket de `server/` para estado multijugador
  - Firestore para datos de usuario y chat

## 2. Motor de juego independiente

El proyecto ya cuenta con un núcleo de juego más independiente en `app/src/main/java/com/loshii/dndzerinx/model/game/GameEngine.kt`.
Ese módulo está diseñado para que cualquier motor pueda consumir:

- `Vector2`
- `WorldBounds`
- `Monster`, `MonsterType`
- `DamageNumber`
- `GameEngine` (lógica de update/ataque/movimiento)

## 3. Base del proyecto Godot

En `godot/` se incluye una guía de inicio para crear el proyecto Godot.

### Pasos iniciales

1. Instalar Godot 4.x.
2. Crear un nuevo proyecto llamado `DnD RoleGate`.
3. En `project.godot` definir `run/main_scene` como `res://scenes/Main.tscn`.
4. Crear `res://scripts/GameSync.gd` para manejar WebSocket y estado del mundo.

### Estructura recomendada

- `res://scenes/Main.tscn`
- `res://scripts/GameSync.gd`
- `res://scripts/PlayerController.gd`
- `res://scripts/MonsterController.gd`

## 4. Integración con Android

### Opción A: app Android lanza Godot como APK separado

1. Exportar el proyecto Godot como APK para Android.
2. Utilizar un `Intent` desde Android para lanzar la APK de Godot.
3. Pasar datos como `playerId`, `avatarUrl`, y `accessKey` como extras.

> La app Android usa `ServerConfig.GAME_WEBSOCKET_URL` para conectarse al servidor remoto `wss://servermmo.onrender.com`.

### Opción B: Godot como módulo Android integrado

1. Exportar Godot como biblioteca Android (`.aar`).
2. Incluir ese `.aar` en el módulo `app` de Android.
3. Configurar un `GodotActivity` personalizado que se llame desde `MainActivity`.
4. Desde Android usa `GodotLauncher.launchGodot(...)` para transferir el estado inicial.

## 5. Contrato Android - Godot

En el módulo Android hay un contrato de integración en `app/src/main/java/com/loshii/dndzerinx/engine/GodotIntegration.kt`.
Este objeto define los extras que se pueden pasar desde Android a Godot para inyectar:

- `PLAYER_ID`
- `PLAYER_NAME`
- `PLAYER_LEVEL`
- `PLAYER_MAX_HP`
- `ACCESS_KEY`
- `SERVER_URL`

Godot puede leer esos parámetros en la escena inicial y conectarse al servidor con la misma sesión del jugador.

## 6. Mensajes WebSocket compartidos

Godot puede usar el mismo protocolo JSON que `app/src/main/java/com/loshii/dndzerinx/network/GameClient.kt`.

### Ejemplo de mensajes en Godot

- `Welcome` / `PlayerJoined` / `PlayerMoved`
- `PlayerAttacked` / `MonsterDied`
- `PlayerDamaged` / `PlayerRespawned` / `WorldState`
- `ChatMessage`

## 6. Ventajas de este enfoque

- separa UI Android de render de juego
- facilita agregar efectos y físicas de Godot
- permite mantener el backend actual
- deja tu app Android como launcher y administración

## 7. Próximos pasos

- Crear el proyecto Godot con escena principal y `WebSocketClient`
- Implementar `GameSync.gd` para recibir `WorldState` y actualizar entidades
- Usar `GameEngine.kt` como referencia de reglas y balance
- Probar la exportación Android en dispositivo

## 8. Base de proyecto Godot incluida

El directorio `godot/` ahora ofrece un proyecto base con los siguientes archivos:

- `godot/project.godot`
- `godot/scenes/Main.tscn`
- `godot/scripts/GameSync.gd`
- `godot/scripts/PlayerController.gd`

Este proyecto es un punto de partida para que Godot maneje el render y la lógica de juego, mientras Android conserva el login, el perfil y la navegación.
