extends Node2D

@export var server_url: String = "wss://servermmo.onrender.com"
@export var player_id: String = "unknown"
@export var player_name: String = "Jugador"
@export var player_level: int = 1
@export var player_max_hp: int = 100

@onready var status_label: Label = $StatusLabel
var ws := WebSocketClient.new()
var connected: bool = false

func _ready():
    status_label.text = "Iniciando conexión..."
    ws.connect("connection_established", Callable(self, "_on_connected"))
    ws.connect("connection_error", Callable(self, "_on_error"))
    ws.connect("connection_closed", Callable(self, "_on_closed"))
    ws.connect("data_received", Callable(self, "_on_data_received"))
    connect_to_server(server_url)

func _process(delta: float) -> void:
    if ws.get_connection_status() == WebSocketClient.CONNECTION_CONNECTED:
        ws.poll()

func connect_to_server(url: String, id: String = "player", name: String = "Jugador", level: int = 1, max_hp: int = 100) -> void:
    server_url = url
    player_id = id
    player_name = name
    player_level = level
    player_max_hp = max_hp

    if ws.get_connection_status() == WebSocketClient.CONNECTION_DISCONNECTED:
        ws.connect_to_url(server_url)
        status_label.text = "Conectando a %s...".format(server_url)

func _on_connected(protocol: String = "") -> void:
    connected = true
    status_label.text = "Conectado como %s".format(player_name)
    var join = {
        "type": "Join",
        "playerId": player_id,
        "playerName": player_name,
        "level": player_level,
        "maxHp": player_max_hp
    }
    var peer = ws.get_peer(1)
    if peer:
        peer.put_packet(JSON.print(join).to_utf8())

func _on_error() -> void:
    status_label.text = "Error de conexión"
    print("Error WebSocket en Godot")

func _on_closed(code: int = 0, reason: String = "") -> void:
    connected = false
    status_label.text = "Conexión cerrada: %s".format(reason)

func _on_data_received() -> void:
    var peer = ws.get_peer(1)
    while peer.get_available_packet_count() > 0:
        var packet = peer.get_packet().get_string_from_utf8()
        var parsed = JSON.parse(packet)
        if parsed.error != OK:
            continue
        var message = parsed.result
        match message.get("type", ""):
            "WorldState":
                _handle_world_state(message)
            "PlayerMoved":
                _handle_player_moved(message)
            "MonsterDied":
                _handle_monster_died(message)
            "PlayerDamaged":
                _handle_player_damaged(message)
            "PlayerRespawned":
                _handle_player_respawned(message)
            _:
                print(message)

func send_move(position: Vector2) -> void:
    if not connected:
        return
    var move = {"type": "Move", "x": position.x, "y": position.y}
    ws.get_peer(1).put_packet(JSON.print(move).to_utf8())

func send_attack(monster_id: String) -> void:
    if not connected:
        return
    var attack = {"type": "Attack", "monsterId": monster_id}
    ws.get_peer(1).put_packet(JSON.print(attack).to_utf8())

func send_respawn() -> void:
    if not connected:
        return
    var respawn = {"type": "Respawn"}
    ws.get_peer(1).put_packet(JSON.print(respawn).to_utf8())

func _handle_world_state(message: Dictionary) -> void:
    # Actualiza jugadores y monstruos en la escena.
    print("WorldState recibido: %s".format(message))

func _handle_player_moved(message: Dictionary) -> void:
    print("PlayerMoved: %s".format(message))

func _handle_monster_died(message: Dictionary) -> void:
    print("MonsterDied: %s".format(message))

func _handle_player_damaged(message: Dictionary) -> void:
    print("PlayerDamaged: %s".format(message))

func _handle_player_respawned(message: Dictionary) -> void:
    print("PlayerRespawned: %s".format(message))
