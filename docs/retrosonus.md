# Retrosonus

Upgrade a Tuned Blade to Retrosonus at a smithing table with a Retrosonus Upgrade
Smithing Template and one Exalted Ingot. The template is uncraftable and has no
duplication recipe. It is available through creative mode or commands for server
distribution. Smithing preserves the blade's name, enchantments, wear and socketed gem.

Craft one Exalted Ingot shapelessly with three
`waywardalchemist:philophers_ash` and two argent ingots. This ingot recipe loads
when Wayward Alchemist is installed. Both the ingot recipe and the Retrosonus
smithing recipe are hidden in JEI, while remaining usable at their workstations.

Retrosonus has 9 total attack damage, 1.4 attack speed and +0.5 entity
interaction range. It retains the Tuned Blade's durability, enchantability,
calx-of-Hades repair ingredient and sword sweeping attacks.

Retrosonus accepts empty or player-bound Spirit Gems. A direct player kill binds
an empty socketed gem to the slain player. Already-bound gems keep their binding.
Without Resonance, hold use to track an online bound player just as with a Tuned
Blade. Sneak-use removes the gem.

Resonance is a single-level enchantment exclusive to Retrosonus, available
through enchanting, random enchanted loot and librarian trades. It replaces
tracking with rod placement. Right-click a supporting block to place a Singing
Rod on its collision surface, including partial blocks such as slabs and fences.
Each placement costs one durability and has a half-second cooldown.

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
