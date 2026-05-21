package com.loshii.dndzerinx.model.game

class GameWorld(
    bounds: WorldBounds,
    playerLevel: Int = 1,
    playerMaxHp: Int = 100,
    playerAtk: Int = 10,
    playerDef: Int = 5
) : GameEngine(bounds, playerLevel, playerMaxHp, playerAtk, playerDef)
