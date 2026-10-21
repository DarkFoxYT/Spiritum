# Retrosonus

Upgrade a Tuned Blade to Retrosonus at a smithing table with a Retrosonus Upgrade
Smithing Template and one Exalted Ingot. Craft one template with this pattern:

```text
Diamond  Philosopher's Ash  Diamond
Diamond      Hexstone      Diamond
Diamond      Diamond       Diamond
```

Smithing preserves the blade's name, enchantments, wear and socketed gem.

Craft one Exalted Ingot shapelessly with three
`waywardalchemist:philosophers_ash` and two argent ingots. The ingot and template recipes load
when Wayward Alchemist is installed. Both the ingot recipe and the Retrosonus
smithing recipe are hidden in JEI, while remaining usable at their workstations.

Retrosonus has 9 total attack damage, 1.4 attack speed and +0.5 entity
interaction range. It retains the Tuned Blade's durability, enchantability,
calx-of-Hades repair ingredient and sword sweeping attacks.

Retrosonus accepts empty or player-bound Spirit Gems. A direct player kill binds
an empty socketed gem to the slain player. Already-bound gems keep their binding.
Sneak-use removes the gem. Retrosonus cannot track players; the Tuned Blade keeps
its tracking ability.

Resonance is built into every Retrosonus and needs no enchantment.
Right-click a supporting block to place a Singing
Rod on its collision surface, including partial blocks such as slabs and fences.
Each placement costs one durability and has a five-second (100-tick) cooldown.
The old enchantment definition remains readable for existing saved items, but
it is no longer offered through enchanting, random loot or librarian trades.

Four rods placed around an open area form a barrier. Each new rod must be within
32 blocks of the other rods and within four blocks vertically. The four corners
must form a convex enclosure. New sets belong to the placing player; several
players can maintain separate barriers, and completed sets are independent.

The barrier allows entry but prevents living entities and occupied vehicles from
walking or flying out at any height. Pearls and other teleports can escape. Each
rod has 10 HP, 15 armor (full iron armor), no armor toughness and no collision
pushing. The supplied model is four blocks tall, with a matching hitbox.
Destroying any rod removes its whole group immediately. Unloaded group members
are removed when loaded, even after a server restart. Rod ownership and barrier groups
persist across world saves. A barrier needs all four rods loaded and alive.

Barrier particles use the supplied Retrosonus sprite with an emissive core and
the mod's additive bloom pass; no shader pack is required.
