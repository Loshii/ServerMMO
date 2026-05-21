const { WebSocketServer } = require('ws');
const crypto = require('crypto');

const PORT = process.env.PORT || 8080;

// Game state
const players = new Map();
const monsters = new Map();

const MONSTER_TYPES = {
  SLIME:    { name: 'Slime',    baseHp: 30,  baseAtk: 5,  baseDef: 2,  xpReward: 10,  aggressionRange: 150 },
  WOLF:     { name: 'Lobo',     baseHp: 50,  baseAtk: 10, baseDef: 5,  xpReward: 25,  aggressionRange: 200 },
  BEAR:     { name: 'Oso',      baseHp: 100, baseAtk: 15, baseDef: 10, xpReward: 50,  aggressionRange: 180 },
  SKELETON: { name: 'Esqueleto',baseHp: 70,  baseAtk: 12, baseDef: 8,  xpReward: 35,  aggressionRange: 250 },
  DRAGON:   { name: 'Dragón',   baseHp: 200, baseAtk: 25, baseDef: 15, xpReward: 100, aggressionRange: 300 }
};

const WORLD_WIDTH = 800;
const WORLD_HEIGHT = 1600;
const MAX_MONSTERS = 15;

function distance(x1, y1, x2, y2) {
  return Math.sqrt((x2 - x1) ** 2 + (y2 - y1) ** 2);
}

function spawnMonster() {
  if (monsters.size >= MAX_MONSTERS) return;
  const types = Object.keys(MONSTER_TYPES);
  const typeKey = types[Math.floor(Math.random() * types.length)];
  const type = MONSTER_TYPES[typeKey];
  const level = Math.floor(Math.random() * 3) + 1;
  const id = 'm_' + crypto.randomUUID().slice(0, 8);
  const monster = {
    id, type: typeKey, typeName: type.name,
    x: Math.random() * (WORLD_WIDTH - 100) + 50,
    y: Math.random() * (WORLD_HEIGHT - 100) + 50,
    hp: type.baseHp + (level - 1) * 10,
    maxHp: type.baseHp + (level - 1) * 10,
    level,
    isDead: false,
    state: 'PATROL',
    patrolTarget: { x: Math.random() * WORLD_WIDTH, y: Math.random() * WORLD_HEIGHT },
    lastAttackTime: 0,
    aggressionRange: type.aggressionRange,
    attackRange: 40,
    attackCooldown: 1500,
    speed: 60
  };
  monsters.set(id, monster);
  broadcast({ type: 'MonsterSpawned', monsterId: id, type: type.name, x: monster.x, y: monster.y, level, hp: monster.hp, maxHp: monster.maxHp });
}

function spawnInitialMonsters() {
  for (let i = 0; i < 8; i++) spawnMonster();
}

function broadcast(message, excludeId = null) {
  const data = JSON.stringify(message);
  players.forEach((player, id) => {
    if (id !== excludeId && player.ws.readyState === 1) {
      player.ws.send(data);
    }
  });
}

function sendTo(ws, message) {
  if (ws.readyState === 1) ws.send(JSON.stringify(message));
}

function calculateDamage(atk, def) {
  return Math.max(1, Math.floor(atk * 1.5 - def * 0.5));
}

function updateMonsters() {
  const now = Date.now();
  const alivePlayers = Array.from(players.values()).filter(p => !p.isDead);

  monsters.forEach(monster => {
    if (monster.isDead) {
      if (now >= monster.respawnTime) {
        monster.hp = monster.maxHp;
        monster.isDead = false;
        monster.state = 'PATROL';
        monster.x = Math.random() * (WORLD_WIDTH - 100) + 50;
        monster.y = Math.random() * (WORLD_HEIGHT - 100) + 50;
        monster.patrolTarget = { x: monster.x + Math.random() * 200 - 100, y: monster.y + Math.random() * 200 - 100 };
        broadcast({ type: 'MonsterSpawned', monsterId: monster.id, type: monster.typeName, x: monster.x, y: monster.y, level: monster.level, hp: monster.hp, maxHp: monster.maxHp });
      }
      return;
    }

    const nearest = alivePlayers.reduce((closest, p) => {
      const d = distance(monster.x, monster.y, p.x, p.y);
      return d < closest.dist ? { player: p, dist: d } : closest;
    }, { player: null, dist: Infinity });

    if (!nearest.player) return;

    const distToPlayer = nearest.dist;

    if (monster.state === 'PATROL') {
      if (distToPlayer < monster.aggressionRange) {
        monster.state = 'CHASE';
      } else {
        const dx = monster.patrolTarget.x - monster.x;
        const dy = monster.patrolTarget.y - monster.y;
        const d = Math.sqrt(dx * dx + dy * dy);
        if (d > 1) {
          monster.x = Math.max(20, Math.min(WORLD_WIDTH - 20, monster.x + (dx / d) * monster.speed * 0.4 * 0.05));
          monster.y = Math.max(20, Math.min(WORLD_HEIGHT - 20, monster.y + (dy / d) * monster.speed * 0.4 * 0.05));
        }
        if (d < 20) {
          monster.patrolTarget = {
            x: Math.max(20, Math.min(WORLD_WIDTH - 20, monster.x + Math.random() * 200 - 100)),
            y: Math.max(20, Math.min(WORLD_HEIGHT - 20, monster.y + Math.random() * 200 - 100))
          };
        }
      }
    } else if (monster.state === 'CHASE') {
      if (distToPlayer > monster.aggressionRange * 2) {
        monster.state = 'PATROL';
      } else if (distToPlayer <= monster.attackRange) {
        monster.state = 'ATTACK';
        if (now - monster.lastAttackTime >= monster.attackCooldown) {
          monster.lastAttackTime = now;
          const type = MONSTER_TYPES[monster.type];
          const damage = calculateDamage(type.baseAtk + monster.level * 2, 5);
          nearest.player.hp = Math.max(0, nearest.player.hp - damage);
          if (nearest.player.hp <= 0) {
            nearest.player.isDead = true;
            broadcast({ type: 'PlayerDied', playerId: nearest.player.id });
          }
          broadcast({ type: 'PlayerDamaged', playerId: nearest.player.id, damage, currentHp: nearest.player.hp, maxHp: nearest.player.maxHp, attackerMonsterId: monster.id });
        }
      } else {
        const dx = nearest.player.x - monster.x;
        const dy = nearest.player.y - monster.y;
        const d = Math.sqrt(dx * dx + dy * dy);
        if (d > 1) {
          monster.x = Math.max(20, Math.min(WORLD_WIDTH - 20, monster.x + (dx / d) * monster.speed * 0.05));
          monster.y = Math.max(20, Math.min(WORLD_HEIGHT - 20, monster.y + (dy / d) * monster.speed * 0.05));
        }
      }
    } else if (monster.state === 'ATTACK') {
      if (distToPlayer > monster.attackRange * 1.5) {
        monster.state = 'CHASE';
      }
    }
  });
}

// WebSocket server
const wss = new WebSocketServer({ port: PORT });

wss.on('listening', () => {
  console.log(`D&D MMO Server running on port ${PORT}`);
  spawnInitialMonsters();
  setInterval(() => {
    updateMonsters();
    if (Math.random() < 0.3) spawnMonster();
  }, 50);
});

wss.on('connection', (ws) => {
  let playerId = null;

  ws.on('message', (data) => {
    let msg;
    try { msg = JSON.parse(data.toString()); } catch { return; }

    switch (msg.type) {
      case 'Join': {
        playerId = msg.playerId;
        players.set(playerId, {
          id: playerId, name: msg.playerName, level: msg.level, maxHp: msg.maxHp,
          x: WORLD_WIDTH / 2 + Math.random() * 200 - 100,
          y: WORLD_HEIGHT / 2 + Math.random() * 200 - 100,
          hp: msg.maxHp, isDead: false, lastAttackTime: 0, ws
        });

        sendTo(ws, { type: 'Welcome', playerId, worldTime: Date.now() });
        sendTo(ws, { type: 'WorldState', players: Array.from(players.values()).filter(p => p.id !== playerId).map(p => ({ id: p.id, name: p.name, x: p.x, y: p.y, hp: p.hp, maxHp: p.maxHp, level: p.level, isDead: p.isDead })), monsters: Array.from(monsters.values()).filter(m => !m.isDead).map(m => ({ id: m.id, type: m.typeName, x: m.x, y: m.y, hp: m.hp, maxHp: m.maxHp, level: m.level, isDead: m.isDead })) });
        broadcast({ type: 'PlayerJoined', playerId, name: msg.playerName, x: players.get(playerId).x, y: players.get(playerId).y, level: msg.level }, playerId);
        break;
      }

      case 'Move': {
        const player = players.get(playerId);
        if (player && !player.isDead) {
          player.x = Math.max(20, Math.min(WORLD_WIDTH - 20, msg.x));
          player.y = Math.max(20, Math.min(WORLD_HEIGHT - 20, msg.y));
          broadcast({ type: 'PlayerMoved', playerId, x: player.x, y: player.y }, playerId);
        }
        break;
      }

      case 'Attack': {
        const player = players.get(playerId);
        if (!player || player.isDead) break;
        const now = Date.now();
        if (now - player.lastAttackTime < 800) break;
        const monster = monsters.get(msg.monsterId);
        if (!monster || monster.isDead) break;
        const dist = distance(player.x, player.y, monster.x, monster.y);
        if (dist > 60) break;

        player.lastAttackTime = now;
        const damage = calculateDamage(10 + player.level * 2, MONSTER_TYPES[monster.type]?.baseDef || 0);
        const isCrit = Math.random() < 0.15;
        const finalDamage = isCrit ? damage * 2 : damage;
        monster.hp -= finalDamage;

        broadcast({ type: 'PlayerAttacked', playerId, monsterId: msg.monsterId, damage: finalDamage, isCrit });

        if (monster.hp <= 0) {
          monster.isDead = true;
          monster.respawnTime = now + 10000;
          broadcast({ type: 'MonsterDied', monsterId: msg.monsterId, killedBy: playerId, xpReward: MONSTER_TYPES[monster.type]?.xpReward * monster.level || 10, goldReward: Math.floor(Math.random() * 10) * monster.level });
          sendTo(ws, { type: 'MonsterDied', monsterId: msg.monsterId, killedBy: playerId, xpReward: MONSTER_TYPES[monster.type]?.xpReward * monster.level || 10, goldReward: Math.floor(Math.random() * 10) * monster.level });
        } else {
          broadcast({ type: 'MonsterDamaged', monsterId: msg.monsterId, damage: finalDamage, currentHp: monster.hp, maxHp: monster.maxHp });
        }
        break;
      }

      case 'Respawn': {
        const player = players.get(playerId);
        if (player) {
          player.x = WORLD_WIDTH / 2;
          player.y = WORLD_HEIGHT / 2;
          player.hp = player.maxHp;
          player.isDead = false;
          sendTo(ws, { type: 'PlayerRespawned', playerId, x: player.x, y: player.y, hp: player.hp, maxHp: player.maxHp });
        }
        break;
      }

      case 'Chat': {
        const player = players.get(playerId);
        if (player) broadcast({ type: 'ChatMessage', playerId, playerName: player.name, message: msg.message });
        break;
      }

      case 'Ping': {
        sendTo(ws, { type: 'Welcome', playerId: playerId || 'unknown', worldTime: Date.now() });
        break;
      }
    }
  });

  ws.on('close', () => {
    if (playerId) {
      players.delete(playerId);
      broadcast({ type: 'PlayerLeft', playerId });
    }
  });
});
