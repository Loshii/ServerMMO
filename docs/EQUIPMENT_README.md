# Equipment Paper Doll

## Resumen

Este componente Compose implementa un panel de equipamiento tipo "paper doll" con:

- Columna central prominente (Cabeza, Pecho, Piernas, Pies).
- Columnas laterales simétricas con accesorios, armas, guantes y anillos.
- Soporte básico para pick & place (tocar para recoger, tocar para soltar).
- Tooltips (pulsación larga) y visualización de rareza (color de borde).

## Archivos añadidos

- `app/src/main/java/com/loshii/dndzerinx/ui/components/EquipmentPaperDoll.kt` — Composable principal y helpers.
- `app/src/main/java/com/loshii/dndzerinx/ui/components/EquipmentModel.kt` — Modelos e interfaz `EquipmentHost` para integrar inventario.
- `app/src/main/res/drawable/slot_beige.xml`, `slot_border.xml`, `ring_circle.xml` — drawables placeholder para slots y anillos.

## Integración rápida

1. En una pantalla Compose, añade el Composable:

   EquipmentPaperDoll(onItemDropped = { fromSlot, toSlot -> /_ manejar equip/unequip _/ })

2. Para integrarlo con tu sistema de inventario, implementa `EquipmentHost` y llama a sus métodos desde el callback `onItemDropped`.

3. Texturas y bordes: sustituye los drawables en `res/drawable` por imágenes o 9-patch con la textura marrón que prefieras.

4. Drag & Drop: la implementación actual usa tap-to-pickup/tap-to-drop (móvil-friendly). Si quieres DnD con seguimiento del dedo, extiende los detectores de gestos en `SmallSlot`/`LargeSlot`.

## Próximos pasos sugeridos

- Reemplazar placeholders por `Painter`s con texturas y brillos.
- Agregar animaciones de brillo según rareza (glow, partículas).
- Añadir pruebas de integración y una Activity/Screen de demo en la navegación.
