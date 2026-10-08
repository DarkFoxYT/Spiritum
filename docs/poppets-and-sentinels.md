# Poppets and sentinels

Socket a player-bound Spirit Gem into a voodoo poppet, then hold use to charge and release to throw the doll.
A full charge takes one second; throw speed ranges from 0.25 to 0.9 blocks per tick.
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

The Sentinel uses the supplied Blockbench model, texture and guard, aggro, walk,
slash and thrust animations. Its source is in `tools/models/sentinel.bbmodel`;
`tools/import_sentinel.py` converts it for the native renderer.

The poppet uses `tools/models/Poppet1.bbmodel` and its embedded texture.
`tools/import_poppet.py` extracts the texture and shared model dimensions. Its head,
arms and legs have separate physics bodies, joined to the torso at the model's neck,
shoulder and hip pivots. Client rendering interpolates each body's position and rotation.

Tests: `tools/dev.ps1 -Task runGameTest`.
Build: `tools/dev.ps1 -Task build`.
