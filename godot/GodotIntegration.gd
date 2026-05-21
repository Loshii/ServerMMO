extends Node

@export var server_url: String = "wss://servermmo.onrender.com"
@export var player_id: String = "player_123"
@export var player_name: String = "Hero"
@export var player_level: int = 1
@export var player_max_hp: int = 100

var ws := WebSocketClient.new()

func _ready():
    ws.connect("connection_established", Callable(self, "_on_connected"))
    ws.connect("connection_error", Callable(self, "_on_error"))
    ws.connect("connection_closed", Callable(self, "_on_closed"))
    ws.connect_to_url(server_url)

func _process(delta):
    if ws.get_connection_status() == WebSocketClient.CONNECTION_CONNECTED:
        ws.poll()

func _on_connected(protocol = ""):
    print("Godot WebSocket conectado")
    var join = {
        "type": "Join",
        "playerId": "player_123",
        "playerName": "Hero",
        "level": 1,
        "maxHp": 100
    }
    ws.get_peer(1).put_packet(JSON.print(join).to_utf8())

func _on_error():
    print("Error WebSocket en Godot")

func _on_closed(code=0, reason=""):
    print("WebSocket cerrado: %s".format(reason))

func _on_data_received():
    var peer = ws.get_peer(1)
    while peer.get_available_packet_count() > 0:
        var packet = peer.get_packet().get_string_from_utf8()
        var parsed = JSON.parse(packet)
        if parsed.error != OK:
            continue
        var message = parsed.result
        # Manejar WorldState, PlayerMoved, MonsterDied, etc.
        print(message)
