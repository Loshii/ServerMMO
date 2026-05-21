const express = require('express');
const { WebSocketServer } = require('ws');
const multer = require('multer');
const cors = require('cors');
const crypto = require('crypto');
const fs = require('fs');
const path = require('path');

const PORT = process.env.PORT || 8080;
const UPLOAD_DIR = path.join(__dirname, 'uploads');

if (!fs.existsSync(UPLOAD_DIR)) {
  fs.mkdirSync(UPLOAD_DIR, { recursive: true });
}

const app = express();
app.use(cors());
app.use('/uploads', express.static(UPLOAD_DIR));
app.use(express.json());

const storage = multer.diskStorage({
  destination: (req, file, cb) => cb(null, UPLOAD_DIR),
  filename: (req, file, cb) => {
    const ext = path.extname(file.originalname) || '.jpg';
    cb(null, `${crypto.randomUUID()}${ext}`);
  }
});
const upload = multer({ storage, limits: { fileSize: 5 * 1024 * 1024 } });

app.post('/api/upload', upload.single('image'), (req, res) => {
  if (!req.file) return res.status(400).json({ error: 'No image provided' });
  const url = `/uploads/${req.file.filename}`;
  res.json({ url });
});

app.post('/api/progression', (req, res) => {
  const { playerId, xp, gold, level, attackPower, defense, maxHp, hp, displayName, characterClass, race } = req.body;
  if (!playerId) return res.status(400).json({ error: 'playerId required' });

  const player = players.get(playerId);
  if (player) {
    if (xp !== undefined) player.xp = xp;
    if (gold !== undefined) player.gold = gold;
    if (level !== undefined) player.level = level;
    if (attackPower !== undefined) player.attackPower = attackPower;
    if (defense !== undefined) player.defense = defense;
    if (maxHp !== undefined) player.maxHp = maxHp;
    if (hp !== undefined) player.hp = hp;
    if (displayName !== undefined) player.displayName = displayName;
    if (characterClass !== undefined) player.characterClass = characterClass;
    if (race !== undefined) player.race = race;
  }

  if (!playerProgression.has(playerId)) {
    playerProgression.set(playerId, {
      playerId, xp: 0, gold: 0, level: 1, attackPower: 10, defense: 5, maxHp: 100, hp: 100,
      displayName: 'Jugador', characterClass: 'Guerrero', race: 'Humano', avatarUrl: ''
    });
  }
  const prog = playerProgression.get(playerId);
  if (xp !== undefined) prog.xp = xp;
  if (gold !== undefined) prog.gold = gold;
  if (level !== undefined) prog.level = level;
  if (attackPower !== undefined) prog.attackPower = attackPower;
  if (defense !== undefined) prog.defense = defense;
  if (maxHp !== undefined) prog.maxHp = maxHp;
  if (hp !== undefined) prog.hp = hp;
  if (displayName !== undefined) prog.displayName = displayName;
  if (characterClass !== undefined) prog.characterClass = characterClass;
  if (race !== undefined) prog.race = race;

  res.json({ success: true, progression: prog });
});

app.get('/api/progression/:playerId', (req, res) => {
  const prog = playerProgression.get(req.params.playerId);
  if (!prog) return res.status(404).json({ error: 'Not found' });
  res.json(prog);
});

// Game state
const players = new Map();
const monsters = new Map();
const playerProgression = new Map();

const MONSTER_TYPES = {
  ABERRATION:     { name: 'Aberración',        baseHp: 60,  baseAtk: 12, baseDef: 8,  xpReward: 30,  aggressionRange: 200 },
  ANKHEG:         { name: 'Ankheg',            baseHp: 80,  baseAtk: 18, baseDef: 12, xpReward: 50,  aggressionRange: 220 },
  BASILISK:       { name: 'Basilisco',         baseHp: 90,  baseAtk: 15, baseDef: 14, xpReward: 45,  aggressionRange: 180 },
  BEAR:           { name: 'Oso',               baseHp: 100, baseAtk: 15, baseDef: 10, xpReward: 50,  aggressionRange: 180 },
  BEHOLDER:       { name: 'Contemplador',      baseHp: 250, baseAtk: 30, baseDef: 20, xpReward: 150, aggressionRange: 350 },
  BUGBEAR:        { name: 'Bugbear',           baseHp: 45,  baseAtk: 10, baseDef: 6,  xpReward: 20,  aggressionRange: 170 },
  CENTAUR:        { name: 'Centauro',          baseHp: 70,  baseAtk: 14, baseDef: 8,  xpReward: 35,  aggressionRange: 250 },
  CHIMERA:        { name: 'Quimera',           baseHp: 130, baseAtk: 22, baseDef: 15, xpReward: 80,  aggressionRange: 280 },
  COCKATRICE:     { name: 'Cocatriz',          baseHp: 50,  baseAtk: 10, baseDef: 8,  xpReward: 25,  aggressionRange: 160 },
  COUATL:         { name: 'Couatl',            baseHp: 120, baseAtk: 18, baseDef: 16, xpReward: 70,  aggressionRange: 300 },
  DARKWEIR:       { name: 'Espectro Oscuro',   baseHp: 55,  baseAtk: 14, baseDef: 6,  xpReward: 30,  aggressionRange: 200 },
  DEATH_KNIGHT:   { name: 'Caballero Muerte',  baseHp: 200, baseAtk: 28, baseDef: 22, xpReward: 120, aggressionRange: 320 },
  DEMON_GLABREZU: { name: 'Glabrezu',          baseHp: 180, baseAtk: 25, baseDef: 18, xpReward: 100, aggressionRange: 300 },
  DEMON_HEZROU:   { name: 'Hezrou',            baseHp: 150, baseAtk: 22, baseDef: 16, xpReward: 85,  aggressionRange: 280 },
  DEVIL_BONE:     { name: 'Diablo Huesos',     baseHp: 100, baseAtk: 18, baseDef: 14, xpReward: 60,  aggressionRange: 250 },
  DEVIL_HORNED:   { name: 'Diablo Cornudo',    baseHp: 160, baseAtk: 24, baseDef: 18, xpReward: 90,  aggressionRange: 290 },
  DEVIL_IMP:      { name: 'Diablillo',         baseHp: 30,  baseAtk: 8,  baseDef: 4,  xpReward: 15,  aggressionRange: 140 },
  DISPLACER:      { name: 'Bestia Displacer',  baseHp: 85,  baseAtk: 16, baseDef: 12, xpReward: 45,  aggressionRange: 230 },
  DRAGON_BLACK:   { name: 'Dragón Negro',      baseHp: 220, baseAtk: 28, baseDef: 18, xpReward: 120, aggressionRange: 320 },
  DRAGON_BLUE:    { name: 'Dragón Azul',       baseHp: 240, baseAtk: 30, baseDef: 20, xpReward: 130, aggressionRange: 340 },
  DRAGON_BRASS:   { name: 'Dragón Latón',      baseHp: 200, baseAtk: 26, baseDef: 16, xpReward: 110, aggressionRange: 300 },
  DRAGON_BRONZE:  { name: 'Dragón Bronce',     baseHp: 210, baseAtk: 27, baseDef: 17, xpReward: 115, aggressionRange: 310 },
  DRAGON_COPPER:  { name: 'Dragón Cobre',      baseHp: 190, baseAtk: 25, baseDef: 15, xpReward: 105, aggressionRange: 290 },
  DRAGON_GOLD:    { name: 'Dragón Dorado',     baseHp: 260, baseAtk: 32, baseDef: 22, xpReward: 140, aggressionRange: 360 },
  DRAGON_GREEN:   { name: 'Dragón Verde',      baseHp: 230, baseAtk: 29, baseDef: 19, xpReward: 125, aggressionRange: 330 },
  DRAGON_RED:     { name: 'Dragón Rojo',       baseHp: 280, baseAtk: 35, baseDef: 24, xpReward: 150, aggressionRange: 380 },
  DRAGON_SILVER:  { name: 'Dragón Plateado',   baseHp: 250, baseAtk: 31, baseDef: 21, xpReward: 135, aggressionRange: 350 },
  DRAGON_WHITE:   { name: 'Dragón Blanco',     baseHp: 200, baseAtk: 26, baseDef: 16, xpReward: 110, aggressionRange: 300 },
  DRAGON:         { name: 'Dragón',            baseHp: 200, baseAtk: 25, baseDef: 15, xpReward: 100, aggressionRange: 300 },
  DRYAD:          { name: 'Dríada',            baseHp: 60,  baseAtk: 10, baseDef: 8,  xpReward: 30,  aggressionRange: 180 },
  DUODAR:         { name: 'Duodár',            baseHp: 40,  baseAtk: 8,  baseDef: 5,  xpReward: 18,  aggressionRange: 150 },
  ETTIN:          { name: 'Ettin',             baseHp: 120, baseAtk: 20, baseDef: 14, xpReward: 70,  aggressionRange: 260 },
  ETTERCAP:       { name: 'Ettercap',          baseHp: 55,  baseAtk: 12, baseDef: 8,  xpReward: 28,  aggressionRange: 190 },
  GARGOYLE:       { name: 'Gárgola',           baseHp: 75,  baseAtk: 14, baseDef: 12, xpReward: 40,  aggressionRange: 210 },
  GELATINOUS_CUBE:{ name: 'Cubo Gelatinoso',   baseHp: 90,  baseAtk: 12, baseDef: 10, xpReward: 45,  aggressionRange: 150 },
  GHAST:          { name: 'Ghast',             baseHp: 60,  baseAtk: 14, baseDef: 8,  xpReward: 32,  aggressionRange: 200 },
  GHOST:          { name: 'Fantasma',          baseHp: 70,  baseAtk: 16, baseDef: 10, xpReward: 40,  aggressionRange: 240 },
  GIANT_FIRE:     { name: 'Gigante Fuego',     baseHp: 180, baseAtk: 26, baseDef: 18, xpReward: 100, aggressionRange: 300 },
  GIANT_FROST:    { name: 'Gigante Escarcha',  baseHp: 170, baseAtk: 24, baseDef: 16, xpReward: 95,  aggressionRange: 290 },
  GIANT_HILL:     { name: 'Gigante Colina',    baseHp: 140, baseAtk: 20, baseDef: 14, xpReward: 80,  aggressionRange: 270 },
  GIANT_STONE:    { name: 'Gigante Piedra',    baseHp: 160, baseAtk: 22, baseDef: 16, xpReward: 90,  aggressionRange: 280 },
  GIANT_STORM:    { name: 'Gigante Tormenta',  baseHp: 200, baseAtk: 28, baseDef: 20, xpReward: 110, aggressionRange: 320 },
  GIBBERING_MOUTHER:{ name: 'Boca Chillante',  baseHp: 80,  baseAtk: 14, baseDef: 6,  xpReward: 40,  aggressionRange: 180 },
  GITHYANKI:      { name: 'Githyanki',         baseHp: 65,  baseAtk: 14, baseDef: 10, xpReward: 35,  aggressionRange: 220 },
  GITHZERAI:      { name: 'Githzerai',         baseHp: 60,  baseAtk: 12, baseDef: 10, xpReward: 32,  aggressionRange: 210 },
  GNOLL:          { name: 'Gnoll',             baseHp: 40,  baseAtk: 10, baseDef: 5,  xpReward: 20,  aggressionRange: 160 },
  GOLEM_CLAY:     { name: 'Golem Arcilla',     baseHp: 130, baseAtk: 20, baseDef: 16, xpReward: 75,  aggressionRange: 200 },
  GOLEM_FLESH:    { name: 'Golem Carne',       baseHp: 110, baseAtk: 18, baseDef: 14, xpReward: 65,  aggressionRange: 220 },
  GOLEM_IRON:     { name: 'Golem Hierro',      baseHp: 180, baseAtk: 24, baseDef: 20, xpReward: 100, aggressionRange: 250 },
  GOLEM_STONE:    { name: 'Golem Piedra',      baseHp: 150, baseAtk: 22, baseDef: 18, xpReward: 85,  aggressionRange: 230 },
  GORGON:         { name: 'Gorgona',           baseHp: 100, baseAtk: 18, baseDef: 14, xpReward: 55,  aggressionRange: 240 },
  GREEN_HAG:      { name: 'Bruja Verde',       baseHp: 80,  baseAtk: 16, baseDef: 10, xpReward: 45,  aggressionRange: 220 },
  GRICK:          { name: 'Grick',             baseHp: 45,  baseAtk: 10, baseDef: 8,  xpReward: 22,  aggressionRange: 170 },
  GRIFFON:        { name: 'Grifo',             baseHp: 90,  baseAtk: 18, baseDef: 12, xpReward: 50,  aggressionRange: 260 },
  GUARDIAN_NATURE:{ name: 'Guardián Natural',  baseHp: 70,  baseAtk: 12, baseDef: 10, xpReward: 35,  aggressionRange: 190 },
  GYRENAUT:       { name: 'Gyrenauta',         baseHp: 55,  baseAtk: 12, baseDef: 8,  xpReward: 28,  aggressionRange: 180 },
  HARPY:          { name: 'Arpía',             baseHp: 50,  baseAtk: 12, baseDef: 6,  xpReward: 25,  aggressionRange: 200 },
  HELLDON:        { name: 'Helldon',           baseHp: 100, baseAtk: 18, baseDef: 14, xpReward: 55,  aggressionRange: 240 },
  HIPPOGRIFF:     { name: 'Hipogrifo',         baseHp: 65,  baseAtk: 14, baseDef: 10, xpReward: 35,  aggressionRange: 220 },
  HOBOGOBLIN:     { name: 'Hobgoblin',         baseHp: 45,  baseAtk: 10, baseDef: 6,  xpReward: 22,  aggressionRange: 170 },
  HYDRA:          { name: 'Hidra',             baseHp: 160, baseAtk: 22, baseDef: 16, xpReward: 90,  aggressionRange: 280 },
  INVISIBLE_STALKER:{ name: 'Acechador Invis.',baseHp: 100, baseAtk: 16, baseDef: 12, xpReward: 60,  aggressionRange: 250 },
  KOBOLD:         { name: 'Kobold',            baseHp: 25,  baseAtk: 6,  baseDef: 3,  xpReward: 12,  aggressionRange: 130 },
  LICH:           { name: 'Liche',             baseHp: 200, baseAtk: 28, baseDef: 20, xpReward: 120, aggressionRange: 340 },
  LIZARDMAN:      { name: 'Hombre Lagarto',    baseHp: 50,  baseAtk: 12, baseDef: 8,  xpReward: 25,  aggressionRange: 180 },
  MANTICORE:      { name: 'Mantícora',         baseHp: 100, baseAtk: 18, baseDef: 14, xpReward: 55,  aggressionRange: 260 },
  MEDUSA:         { name: 'Medusa',            baseHp: 110, baseAtk: 20, baseDef: 14, xpReward: 65,  aggressionRange: 280 },
  MIMIC:          { name: 'Mímico',            baseHp: 70,  baseAtk: 14, baseDef: 12, xpReward: 40,  aggressionRange: 150 },
  MINOTAUR:       { name: 'Minotauro',         baseHp: 120, baseAtk: 20, baseDef: 14, xpReward: 70,  aggressionRange: 260 },
  MUMMY:          { name: 'Momia',             baseHp: 90,  baseAtk: 16, baseDef: 12, xpReward: 50,  aggressionRange: 220 },
  NIGHTMARE:      { name: 'Pesadilla',         baseHp: 80,  baseAtk: 16, baseDef: 10, xpReward: 45,  aggressionRange: 240 },
  OCHRE_JELLY:    { name: 'Gelatina Ocre',     baseHp: 75,  baseAtk: 12, baseDef: 10, xpReward: 38,  aggressionRange: 160 },
  OGRE:           { name: 'Ogro',              baseHp: 80,  baseAtk: 16, baseDef: 10, xpReward: 45,  aggressionRange: 200 },
  ONI:            { name: 'Oni',               baseHp: 110, baseAtk: 20, baseDef: 14, xpReward: 65,  aggressionRange: 260 },
  Ooze:           { name: 'Cieno',             baseHp: 60,  baseAtk: 10, baseDef: 8,  xpReward: 30,  aggressionRange: 140 },
  OWLBEAR:        { name: 'Oso Búho',          baseHp: 90,  baseAtk: 18, baseDef: 12, xpReward: 50,  aggressionRange: 240 },
  PEGASUS:        { name: 'Pegaso',            baseHp: 70,  baseAtk: 14, baseDef: 10, xpReward: 38,  aggressionRange: 260 },
  PHASE_SPIDER:   { name: 'Araña de Fase',     baseHp: 55,  baseAtk: 14, baseDef: 8,  xpReward: 30,  aggressionRange: 200 },
  PURPLE_WORM:    { name: 'Gusano Púrpura',    baseHp: 200, baseAtk: 26, baseDef: 20, xpReward: 110, aggressionRange: 280 },
  RAKSHASA:       { name: 'Rakshasa',          baseHp: 130, baseAtk: 22, baseDef: 16, xpReward: 80,  aggressionRange: 300 },
  REMORHAZ:       { name: 'Remorhaz',          baseHp: 150, baseAtk: 24, baseDef: 18, xpReward: 85,  aggressionRange: 270 },
  ROCS:           { name: 'Roc',               baseHp: 180, baseAtk: 24, baseDef: 18, xpReward: 100, aggressionRange: 300 },
  ROPER:          { name: 'Roper',             baseHp: 100, baseAtk: 18, baseDef: 14, xpReward: 55,  aggressionRange: 200 },
  RUST_MONSTER:   { name: 'Monstruo Oxidado',  baseHp: 60,  baseAtk: 12, baseDef: 10, xpReward: 32,  aggressionRange: 180 },
  SAHUAGIN:       { name: 'Sahuagin',          baseHp: 45,  baseAtk: 10, baseDef: 6,  xpReward: 22,  aggressionRange: 170 },
  SALAMANDER:     { name: 'Salamandra',        baseHp: 120, baseAtk: 20, baseDef: 14, xpReward: 70,  aggressionRange: 260 },
  SATYR:          { name: 'Sátiro',            baseHp: 50,  baseAtk: 10, baseDef: 6,  xpReward: 25,  aggressionRange: 180 },
  SCORPION:       { name: 'Escorpión Gigante', baseHp: 40,  baseAtk: 10, baseDef: 8,  xpReward: 20,  aggressionRange: 160 },
  SHADOW:         { name: 'Sombra',            baseHp: 45,  baseAtk: 12, baseDef: 6,  xpReward: 22,  aggressionRange: 190 },
  SHIELD_GUARDIAN:{ name: 'Guardián Escudo',   baseHp: 140, baseAtk: 20, baseDef: 18, xpReward: 80,  aggressionRange: 250 },
  SKELETON:       { name: 'Esqueleto',         baseHp: 70,  baseAtk: 12, baseDef: 8,  xpReward: 35,  aggressionRange: 250 },
  SLIME:          { name: 'Slime',             baseHp: 30,  baseAtk: 5,  baseDef: 2,  xpReward: 10,  aggressionRange: 150 },
  SPECTATOR:      { name: 'Espectador',        baseHp: 80,  baseAtk: 14, baseDef: 10, xpReward: 42,  aggressionRange: 220 },
  SPECTER:        { name: 'Espectro',          baseHp: 50,  baseAtk: 12, baseDef: 6,  xpReward: 25,  aggressionRange: 210 },
  SPIDER:         { name: 'Araña Gigante',     baseHp: 35,  baseAtk: 8,  baseDef: 4,  xpReward: 18,  aggressionRange: 150 },
  SPRITE:         { name: 'Duendecillo',       baseHp: 20,  baseAtk: 6,  baseDef: 3,  xpReward: 10,  aggressionRange: 120 },
  STIRGE:         { name: 'Stirge',            baseHp: 30,  baseAtk: 8,  baseDef: 4,  xpReward: 15,  aggressionRange: 140 },
  TREANT:         { name: 'Treant',            baseHp: 160, baseAtk: 22, baseDef: 18, xpReward: 90,  aggressionRange: 220 },
  TRIPLE:         { name: 'Triple',            baseHp: 90,  baseAtk: 16, baseDef: 12, xpReward: 48,  aggressionRange: 240 },
  TROLL:          { name: 'Troll',             baseHp: 110, baseAtk: 18, baseDef: 12, xpReward: 65,  aggressionRange: 250 },
  UNICORN:        { name: 'Unicornio',         baseHp: 100, baseAtk: 16, baseDef: 14, xpReward: 60,  aggressionRange: 280 },
  VAMPIRE:        { name: 'Vampiro',           baseHp: 180, baseAtk: 26, baseDef: 18, xpReward: 100, aggressionRange: 320 },
  WEREWOLF:       { name: 'Hombre Lobo',       baseHp: 90,  baseAtk: 18, baseDef: 12, xpReward: 55,  aggressionRange: 260 },
  WIGHT:          { name: 'Espectro Hielo',    baseHp: 70,  baseAtk: 14, baseDef: 10, xpReward: 40,  aggressionRange: 230 },
  WILL_O_WISP:    { name: 'Will-o-Wisp',       baseHp: 35,  baseAtk: 10, baseDef: 4,  xpReward: 18,  aggressionRange: 180 },
  WOLF:           { name: 'Lobo',              baseHp: 50,  baseAtk: 10, baseDef: 5,  xpReward: 25,  aggressionRange: 200 },
  WRAITH:         { name: 'Ánima',             baseHp: 100, baseAtk: 18, baseDef: 14, xpReward: 60,  aggressionRange: 270 },
  WYVERN:         { name: 'Wyvern',            baseHp: 130, baseAtk: 22, baseDef: 16, xpReward: 75,  aggressionRange: 290 },
  XORN:           { name: 'Xorn',              baseHp: 80,  baseAtk: 16, baseDef: 12, xpReward: 45,  aggressionRange: 200 },
  YETI:           { name: 'Yeti',              baseHp: 90,  baseAtk: 16, baseDef: 12, xpReward: 50,  aggressionRange: 240 },
  YUAN_TI:        { name: 'Yuan-ti',           baseHp: 75,  baseAtk: 14, baseDef: 10, xpReward: 40,  aggressionRange: 220 },
  ZOMBIE:         { name: 'Zombi',             baseHp: 55,  baseAtk: 8,  baseDef: 6,  xpReward: 25,  aggressionRange: 140 }
};

const WORLD_WIDTH = 800;
const WORLD_HEIGHT = 1600;
const MAX_MONSTERS = 15;

function distance(x1, y1, x2, y2) {
  return Math.sqrt((x2 - x1) ** 2 + (y2 - y1) ** 2);
}

function spawnInitialMonsters() {
  const commonTypes = ['SLIME', 'WOLF', 'SKELETON', 'KOBOLD', 'GNOLL', 'SPIDER', 'ZOMBIE', 'HOBOGOBLIN', 'STIRGE', 'SPRITE'];
  const uncommonTypes = ['BUGBEAR', 'OGRE', 'HARPY', 'GARGOYLE', 'MIMIC', 'GRICK', 'ETTERCAP', 'OWLBEAR', 'GITHYANKI', 'GITHZERAI'];
  const rareTypes = ['TROLL', 'MINOTAUR', 'HYDRA', 'CHIMERA', 'GRIFFON', 'MANTICORE', 'MEDUSA', 'VAMPIRE', 'WEREWOLF', 'WYVERN'];
  const epicTypes = ['DRAGON_RED', 'DRAGON_BLUE', 'DRAGON_GREEN', 'DRAGON_BLACK', 'DRAGON_WHITE', 'DRAGON_GOLD', 'BEHOLDER', 'LICH', 'DEATH_KNIGHT', 'PURPLE_WORM'];

  for (let i = 0; i < 4; i++) spawnMonsterOfType(commonTypes[Math.floor(Math.random() * commonTypes.length)]);
  for (let i = 0; i < 3; i++) spawnMonsterOfType(uncommonTypes[Math.floor(Math.random() * uncommonTypes.length)]);
  for (let i = 0; i < 2; i++) spawnMonsterOfType(rareTypes[Math.floor(Math.random() * rareTypes.length)]);
  if (Math.random() < 0.3) spawnMonsterOfType(epicTypes[Math.floor(Math.random() * epicTypes.length)]);
}

function spawnMonsterOfType(typeKey) {
  if (monsters.size >= MAX_MONSTERS) return;
  const type = MONSTER_TYPES[typeKey];
  if (!type) return;
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

function spawnMonster() {
  if (monsters.size >= MAX_MONSTERS) return;
  const roll = Math.random();
  let pool;
  if (roll < 0.4) pool = ['SLIME', 'WOLF', 'SKELETON', 'KOBOLD', 'GNOLL', 'SPIDER', 'ZOMBIE', 'HOBOGOBLIN', 'STIRGE', 'SPRITE'];
  else if (roll < 0.7) pool = ['BUGBEAR', 'OGRE', 'HARPY', 'GARGOYLE', 'MIMIC', 'GRICK', 'ETTERCAP', 'OWLBEAR', 'GITHYANKI', 'GITHZERAI'];
  else if (roll < 0.9) pool = ['TROLL', 'MINOTAUR', 'HYDRA', 'CHIMERA', 'GRIFFON', 'MANTICORE', 'MEDUSA', 'VAMPIRE', 'WEREWOLF', 'WYVERN'];
  else pool = ['DRAGON_RED', 'DRAGON_BLUE', 'DRAGON_GREEN', 'BEHOLDER', 'LICH', 'DEATH_KNIGHT', 'PURPLE_WORM', 'RAKSHASA', 'DEMON_GLABREZU', 'DEVIL_HORNED'];
  spawnMonsterOfType(pool[Math.floor(Math.random() * pool.length)]);
}

function broadcast(message, excludeId = null) {
  const data = JSON.stringify(message);
  players.forEach((player, id) => {
    if (id !== excludeId && player.ws.readyState === 1) player.ws.send(data);
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
          const damage = calculateDamage(type.baseAtk + monster.level * 2, nearest.player.defense || 5);
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
      if (distToPlayer > monster.attackRange * 1.5) monster.state = 'CHASE';
    }
  });
}

// HTTP server
const server = app.listen(PORT, () => {
  console.log(`D&D MMO Server running on port ${PORT}`);
  spawnInitialMonsters();
  setInterval(() => {
    updateMonsters();
    if (Math.random() < 0.3) spawnMonster();
  }, 50);
});

// WebSocket server
const wss = new WebSocketServer({ server });

wss.on('connection', (ws) => {
  let playerId = null;

  ws.on('message', (data) => {
    let msg;
    try { msg = JSON.parse(data.toString()); } catch { return; }

    switch (msg.type) {
      case 'Join': {
        playerId = msg.playerId;
        const prog = playerProgression.get(playerId) || {
          xp: 0, gold: 0, level: msg.level || 1, attackPower: 10, defense: 5, maxHp: msg.maxHp || 100,
          displayName: msg.playerName || 'Jugador', characterClass: 'Guerrero', race: 'Humano', avatarUrl: ''
        };

        players.set(playerId, {
          id: playerId, name: prog.displayName, level: prog.level, maxHp: prog.maxHp,
          x: WORLD_WIDTH / 2 + Math.random() * 200 - 100,
          y: WORLD_HEIGHT / 2 + Math.random() * 200 - 100,
          hp: msg.maxHp || prog.maxHp, isDead: false, lastAttackTime: 0, ws,
          xp: prog.xp, gold: prog.gold, attackPower: prog.attackPower, defense: prog.defense,
          displayName: prog.displayName, characterClass: prog.characterClass, race: prog.race, avatarUrl: prog.avatarUrl
        });

        sendTo(ws, { type: 'Welcome', playerId, worldTime: Date.now() });
        sendTo(ws, { type: 'WorldState', players: Array.from(players.values()).filter(p => p.id !== playerId).map(p => ({ id: p.id, name: p.name, x: p.x, y: p.y, hp: p.hp, maxHp: p.maxHp, level: p.level, isDead: p.isDead })), monsters: Array.from(monsters.values()).filter(m => !m.isDead).map(m => ({ id: m.id, type: m.typeName, x: m.x, y: m.y, hp: m.hp, maxHp: m.maxHp, level: m.level, isDead: m.isDead })) });
        const p = players.get(playerId);
        sendTo(ws, { type: 'PlayerProgression', playerId, level: p.level, xp: p.xp, gold: p.gold, attackPower: p.attackPower, defense: p.defense, maxHp: p.maxHp, displayName: p.displayName, characterClass: p.characterClass, race: p.race, avatarUrl: p.avatarUrl });
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
        const damage = calculateDamage(player.attackPower || (10 + player.level * 2), MONSTER_TYPES[monster.type]?.baseDef || 0);
        const isCrit = Math.random() < 0.15;
        const finalDamage = isCrit ? damage * 2 : damage;
        monster.hp -= finalDamage;

        broadcast({ type: 'PlayerAttacked', playerId, monsterId: msg.monsterId, damage: finalDamage, isCrit });

        if (monster.hp <= 0) {
          monster.isDead = true;
          monster.respawnTime = now + 10000;
          const xpReward = (MONSTER_TYPES[monster.type]?.xpReward || 10) * monster.level;
          const goldReward = Math.floor(Math.random() * 10) * monster.level;

          player.xp += xpReward;
          player.gold += goldReward;

          const xpToLevel = player.level * 100;
          let leveledUp = false;
          while (player.xp >= xpToLevel) {
            player.xp -= xpToLevel;
            player.level += 1;
            player.maxHp += 10;
            player.hp = player.maxHp;
            player.attackPower += 2;
            player.defense += 1;
            leveledUp = true;
          }

          broadcast({ type: 'MonsterDied', monsterId: msg.monsterId, killedBy: playerId, xpReward, goldReward });
          sendTo(ws, { type: 'MonsterDied', monsterId: msg.monsterId, killedBy: playerId, xpReward, goldReward });
          if (leveledUp) {
            sendTo(ws, { type: 'PlayerLevelUp', playerId, level: player.level, maxHp: player.maxHp, attackPower: player.attackPower, defense: player.defense });
          }
          sendTo(ws, { type: 'PlayerProgression', playerId, level: player.level, xp: player.xp, gold: player.gold, attackPower: player.attackPower, defense: player.defense, maxHp: player.maxHp });
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

      case 'UpdateAvatar': {
        const player = players.get(playerId);
        if (player && msg.avatarUrl) {
          player.avatarUrl = msg.avatarUrl;
          if (!playerProgression.has(playerId)) {
            playerProgression.set(playerId, { playerId, xp: 0, gold: 0, level: 1, attackPower: 10, defense: 5, maxHp: 100, hp: 100, displayName: player.name, characterClass: 'Guerrero', race: 'Humano', avatarUrl: msg.avatarUrl });
          } else {
            playerProgression.get(playerId).avatarUrl = msg.avatarUrl;
          }
          sendTo(ws, { type: 'AvatarUpdated', playerId, avatarUrl: msg.avatarUrl });
        }
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
