# Godot Base Project para DnD RoleGate

Este directorio contiene la guía de inicio para un proyecto Godot que se conecta al backend del juego.

## Estructura sugerida

- `project.godot`
- `res://scenes/Main.tscn`
- `res://scripts/GameSync.gd`
- `res://scripts/PlayerController.gd`
- `res://assets/`

## Recomendaciones

- Usa Godot 4.x para compatibilidad con Android y WebSocket moderno.
- Mantén el juego de red separado del UI de Android.
- Exporta Godot como APK o como módulo `.aar` si requieres integración nativa.

## Flujo de integración

1. Inicia Godot y crea la escena principal.
2. Crea un nodo `WebSocketClient` en `GameSync.gd`.
3. Lee los mensajes del servidor y actualiza posiciones/monstruos.
4. Envía eventos de movimiento y ataque al servidor desde Godot.

## Archivos base incluidos

- `project.godot`
- `res://scenes/Main.tscn`
- `res://scripts/GameSync.gd`
- `res://scripts/PlayerController.gd`

## Uso

1. Abre este directorio como proyecto en Godot 4.x.
2. Verifica que `run/main_scene` apunte a `res://scenes/Main.tscn`.
3. Ajusta `GameSync.gd` para leer los datos iniciales de Android y conectar con `wss://servermmo.onrender.com`.

> El proyecto base ya está configurado para conectar al servidor en la nube `wss://servermmo.onrender.com`. 4. Exporta como APK o `.aar` según tu flujo de integración.
