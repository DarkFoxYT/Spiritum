# Poppets and sentinels

Socket a player-bound Spirit Gem into a voodoo poppet, then use it to throw the doll.
The bound player gets pushed in the same direction. The doll's physics are adapted
from Asterion's ragdolls and run on the server.

Wall impacts deal four damage points per block/tick of incoming wall-normal speed,
capped at ten points (five hearts), with a ten-tick impact cooldown. Floor impacts
do not cause voodoo damage. Warding Rings prevent both voodoo damage and the throw
impulse. The bound player must be alive and online to be affected.

Sneak-right-click the doll to retrieve its original gem and remaining durability.
Right-click it with an argent needle to apply the existing damage and potion effects.
The held-poppet needle interaction remains available. World dolls persist across saves.

Craft a sentinel with six argent ingots surrounding a Spirit Gem:

```text
A A
AGA
A A
```

Use the sentinel item on a block to place dormant armor. Sentinels have 40 health,
20 armor and 12 armor toughness. They have no autonomous retaliation or wandering.

The persistent Rite of Vigilance consumes six hex ash, two echo shards and two calx
of Hades, using four ordinary fueled candles plus zero to four player-bound candles.
It lasts ten minutes. Sentinels within 50 blocks defend the bound players when
they're attacked. Without bound candles, no player is protected.

Awakened sentinels pursue the attacker, slash for 12 damage with knockback at close
range, and thrust forward for 10 damage at medium range. Stopping the rite, leaving
its range, or losing the target returns them to dormancy. Overlapping rites protect
all their bound players against the sentinel. Server PvP settings still apply.

Both entities use temporary models and textures. To regenerate the textures, run
`tools/generate_temp_assets.ps1`. Tests: `tools/dev.ps1 -Task runGameTest`.
Build: `tools/dev.ps1 -Task build`.
