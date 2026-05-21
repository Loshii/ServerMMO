# Sistema de experiencia y roles

## Experiencia RPG

La experiencia se calcula desde actividades de monstruos, no desde bonos planos.

- `HUNT`: cacerias normales y farmeo guiado.
- `MONSTER_EVENT`: eventos temporales de monstruos.
- `RAID`: contenido avanzado de equipo.
- `DUNGEON`: instancias con dificultad y recompensas mayores.
- `MISSION`: misiones y encargos narrativos.

El calculo aplica:

- Experiencia base por tipo de actividad.
- Multiplicador por dificultad: facil, normal, dificil, elite, boss y pesadilla.
- Ajuste por diferencia entre nivel del jugador y nivel del monstruo.
- Bonus por primer kill.
- Bonus por participacion en equipo.
- Escalado por contribucion individual.
- Multiplicador de evento.

La curva de nivel esta centralizada en `LevelProgression.experienceForNextLevel(level)`.
Cada nivel entrega 3 puntos de habilidad y cada 5 niveles agrega 2 puntos extra.

## Roles

- `Inicial`: rol base para usuarios nuevos. No pertenece a gremio y tiene acceso limitado.
- `Aventurero`: usuario unido a un gremio. Puede participar en hunts avanzados, eventos de gremio, raids y dungeons.
- `Lider : [Nombre del Gremio]`: rol unico para lideres de gremio. Lo otorga solo un GM.
- `GM`: unico rol que puede asignar lideres de gremio.
- `ADMIN`: rol administrativo.

## Permisos

- Solo `GM` puede asignar `Lider : [Nombre del Gremio]`.
- Un lider de gremio puede aceptar usuarios como `Aventurero` dentro de su propio gremio.
- Un usuario `Inicial` no puede recibir experiencia de `RAID`, `DUNGEON` ni `MONSTER_EVENT` hasta unirse a un gremio.
