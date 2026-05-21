# D&D MMO Server - Ktor + WebSockets

Servidor multiplayer para D&D RoleGate usando Ktor y WebSockets.

## Ejecutar localmente

```bash
# Desde la raiz del proyecto
./gradlew :server:run
```

El servidor arranca en `ws://localhost:8080/ws`

## Deploy en VPS (Oracle Cloud Free Tier)

### 1. Crear VPS
- Oracle Cloud → Always Free → VM.Standard.A1.Flex (4 cores, 24GB RAM gratis)
- Ubuntu 22.04

### 2. Instalar Java
```bash
sudo apt update
sudo apt install openjdk-21-jre-headless -y
```

### 3. Instalar Docker (opcional)
```bash
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER
```

### 4. Deploy con Docker
```bash
# Subir el proyecto al VPS
scp -r D&D/ ubuntu@TU_IP:~/

# Construir y ejecutar
cd D&D
sudo docker compose up -d --build
```

### 5. Deploy manual (sin Docker)
```bash
# Subir y compilar
scp -r D&D/ ubuntu@TU_IP:~/
cd D&D
./gradlew :server:installDist

# Ejecutar
./server/build/install/server/bin/server
```

### 6. Configurar firewall
```bash
sudo ufw allow 8080/tcp
```

## Endpoints

| Endpoint | Descripcion |
|----------|-------------|
| `GET /` | Info del servidor |
| `GET /health` | Health check |
| `GET /players` | Lista de jugadores conectados |
| `WS /ws` | WebSocket para el juego |

## Protocolo WebSocket

### Cliente envia:
```json
{"type": "Join", "playerId": "abc", "playerName": "Hero", "level": 1, "maxHp": 100}
{"type": "Move", "x": 100, "y": 200}
{"type": "Attack", "monsterId": "m_123"}
{"type": "Respawn"}
{"type": "Chat", "message": "Hola!"}
{"type": "Ping"}
```

### Servidor envia:
```json
{"type": "Welcome", "playerId": "abc", "worldTime": 1234567890}
{"type": "WorldState", "players": [...], "monsters": [...]}
{"type": "PlayerMoved", "playerId": "abc", "x": 100, "y": 200}
{"type": "PlayerJoined", "playerId": "abc", "name": "Hero", "x": 100, "y": 200, "level": 1}
{"type": "PlayerLeft", "playerId": "abc"}
{"type": "PlayerAttacked", "playerId": "abc", "monsterId": "m_123", "damage": 15, "isCrit": false}
{"type": "MonsterDied", "monsterId": "m_123", "killedBy": "abc", "xpReward": 10, "goldReward": 5}
{"type": "MonsterSpawned", "monsterId": "m_456", "type": "Slime", "x": 300, "y": 400, "level": 1, "hp": 30, "maxHp": 30}
{"type": "PlayerDamaged", "playerId": "abc", "damage": 5, "currentHp": 95, "maxHp": 100, "attackerMonsterId": "m_123"}
{"type": "ChatMessage", "playerId": "abc", "playerName": "Hero", "message": "Hola!"}
```

## Costo: $0/mes con Oracle Cloud Free Tier
- 4 ARM cores
- 24GB RAM
- 200GB storage
- 10TB bandwidth
