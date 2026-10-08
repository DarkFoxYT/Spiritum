# Higher rituals

| Rite | Offerings | Candles | Result |
| --- | --- | --- | --- |
| Leech Binding | 1 argent nugget, 2 calx of Hades, 2 hex ash | 3 ordinary + 1 player-bound | Owned Leech Demon; returns the bound gem |
| Imp Binding | 1 living flesh, 2 calx of Hades, 2 hex ash | 3 ordinary + 1 player-bound | Owned Imp Demon; returns the bound gem |
| Lemure Binding | 1 Spirit Fragment (the existing shard item), 3 calx of Hades, 2 hex ash | 3 ordinary + 1 player-bound | Owned Lemure Demon; returns the bound gem |
| Calling | 2 ender pearls, 2 hex ash | 2 ordinary + 1 player-bound | Notifies the online player when activation begins, then teleports them atop the pedestal |
| Dominion | 1 nether star, 6 calx of Hades, 3 argent ingots | 4 unbound gem-fueled + up to 4 player-bound | Persistent protection within 50 blocks |

Binding rites return the exact gem supplied to the owner candle, including its
player binding and other data components. The gem drops above the pedestal after
successful completion; an imp can fetch it using its normal collection behavior.
Cancelled or interrupted bindings do not return a gem or summon a demon. Candle
fuel is saved so a binding can complete correctly after reloading the world.

Calling allows the player to move during activation. Warding Ring protection and
online-player checks continue to apply.

Dominion uses a 50-block sphere around the pedestal and the existing ten-minute
persistent-rite lifetime. Unbound players mine breakable blocks using obsidian's
hardness and tool requirements. Unbreakable blocks remain unbreakable. Bound
players mine normally. Storage inventories (including chests, barrels, shulker
boxes, hoppers, furnaces, brewing stands and ender chests) cannot be used by unbound
players; an already-open storage menu is revoked when protection starts.
Crafting tables and hexed candles remain usable, so an unbound player can still
snuff a candle to interrupt the rite.

Mining attempts and blocked storage use display a rune at the targeted block face
for 20 ticks (one second). The rune lies flush against that face at the raycast hit.
Overlapping Dominion rites allow players bound to any covering rite to use its area.
No bound candles means the restrictions apply to everyone.

Withering remains available alongside Dominion. Vigilance is documented in
[Poppets and sentinels](poppets-and-sentinels.md).
