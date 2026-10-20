# BackSlot compatibility

Spiritum supports [BackSlot Mod](https://modrinth.com/mod/backslot-mod) without
requiring it. Compatibility assets are in `assets/spiritum/back_transforms/`.

The Hexblade, Tuned Blade, Retrosonus and Argent Needle have dedicated back
transforms. Fixed-context models use the full weapon artwork, while inventory
icons remain unchanged. The Hexblade keeps its socketed/empty appearance, and
coated needles retain their potion tint. The back models avoid inheriting the
large first-person tracking transforms.

BackSlot keeps its own equipment component, independent of the ring slot. Selecting
a socketed blade from BackSlot supports tracking through the normal held-item path.
The inventory ring slot uses `textures/item/ring_icon.png` when empty, including
the creative inventory tab. Singing rods use an emissive render layer and remain
bright at night.
