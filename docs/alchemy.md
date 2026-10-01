# Alchemy compatibility

Alchemy uses ordinary server datapack recipes with the type `spiritum:alchemy`.
Another mod can ship these JSON files in its resources, or a pack author can add
them in a datapack. No Java API or compile dependency on Spiritum is needed.
Spiritum must be installed at runtime to process the recipes.

Place a recipe at `data/<namespace>/recipe/<path>.json`, for example
`data/my_compat/recipe/alchemy/diamond.json`:

```json
{
  "type": "spiritum:alchemy",
  "ingredients": [
    { "item": "minecraft:coal", "count": 4 },
    { "item": "minecraft:quartz", "count": 2 }
  ],
  "result": { "id": "minecraft:diamond", "count": 1 },
  "byproducts": [
    { "id": "minecraft:glass_bottle", "count": 1 }
  ]
}
```

`ingredients` contains 1–64 entries. Each entry names an item registry ID and an
optional `count` from 1–64 (default 1). Ingredients match item types, regardless
of stack components. Counts accumulate across dropped stacks, and repeated item
entries add their counts. Tags are not supported. `result` is a standard item
stack, including an optional count and components. `byproducts` is an optional
list of additional item stacks; it defaults to empty. Container returns must be
listed explicitly, as the slimeball recipe does for its honey bottle.

The vat accepts dropped items used by any loaded alchemy recipe. It consumes the
required amounts, returns excess offerings, produces the result and byproducts,
and drains its water. The existing 30-second boiling window still applies.
If multiple recipes match, the first in recipe-ID order wins; use distinct
ingredient combinations to avoid ambiguity.

Use your own namespace for additions. To replace a built-in recipe, use its
existing ID in a higher-priority datapack, such as
`data/spiritum/recipe/alchemy/living_flesh.json`. The six defaults are in
`src/main/resources/data/spiritum/recipe/alchemy/`. `/reload` reloads these
recipes, and Fabric synchronizes them to connected clients for JEI/REI.

For optional compatibility with an item from another mod, guard the recipe with
Fabric resource conditions so it only loads when both mods are present:

```json
"fabric:load_conditions": [
  {
    "condition": "fabric:all_mods_loaded",
    "values": ["spiritum", "other_mod"]
  }
]
```

Add this field alongside `type`, and use the other mod's registered item IDs in
the recipe. Neither mod needs to be declared as a mandatory dependency of the
compatibility mod just to supply the JSON.
