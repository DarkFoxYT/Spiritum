"""Import the poppet's six rigid parts, attachment pivots and embedded texture."""
import base64
import json
import math
from pathlib import Path
import sys

root = Path(__file__).resolve().parent.parent
source = json.loads(Path(sys.argv[1]).read_text(encoding="utf-8"))
groups = {g["uuid"]: g for g in source["groups"]}
elements = {e["uuid"]: e for e in source["elements"]}
parts = {}


def visit(node):
    group = groups[node["uuid"]]
    cubes = [elements[c] for c in node["children"] if isinstance(c, str)]
    assert len(cubes) == 1, f"Expected one rigid cube for {group['name']}"
    assert not any(group.get("rotation", [0, 0, 0]))
    parts[group["name"]] = (group, cubes[0])
    for child in node["children"]:
        if isinstance(child, dict):
            visit(child)


for node in source["outliner"]:
    visit(node)
names = ["body", "head", "leftarm", "rightarm", "leftleg", "rightleg"]
assert set(parts) == set(names)
body = parts["body"][1]
body_center = [(a+b)/2 for a, b in zip(body["from"], body["to"])]


def physical(v):
    # The supplied doll lies along Z. Stand it up with Z becoming world Y.
    return [v[0]/16, v[2]/16, -v[1]/16]


def subtract(a, b):
    return [x-y for x, y in zip(a, b)]


def vec(v):
    return "new Vec3d(" + ", ".join(f"{x:.9f}" for x in v) + ")"


lines = []
for name in names:
    group, cube = parts[name]
    assert cube["type"] == "cube" and cube.get("box_uv", False)
    rotation = cube.get("rotation", [0, 0, 0])
    assert rotation[0] == 0 and rotation[2] == 0
    angle = math.radians(rotation[1])
    center = [(a+b)/2 for a, b in zip(cube["from"], cube["to"])]
    pivot = cube.get("origin", [0, 0, 0])
    relative = subtract(center, pivot)
    center = [pivot[0] + math.cos(angle)*relative[0] + math.sin(angle)*relative[2],
              center[1], pivot[2] - math.sin(angle)*relative[0] + math.cos(angle)*relative[2]]
    offset = physical(subtract(center, body_center))
    joint = physical(subtract(group["origin"], body_center))
    size = [b-a for a, b in zip(cube["from"], cube["to"])]
    half = [size[0]/32, size[2]/32, size[1]/32]
    uv = cube.get("uv_offset")
    if uv is None:
        uv = [min(cube["faces"]["east"]["uv"][::2]), min(cube["faces"]["up"]["uv"][1::2])]
    mirror = str(cube.get("mirror_uv", False)).lower()
    lines.append(f'new Part("{name}", {vec(offset)}, {vec(half)}, {vec(joint)}, {-angle:.9f}f, {int(uv[0])}, {int(uv[1])}, {mirror})')

java = '''package net.dark.spiritum.entity.poppet;

import java.util.List;
import net.minecraft.util.math.Vec3d;

/** Geometry and joint pivots imported from tools/models/Poppet1.bbmodel. */
public final class PoppetModelDefinition {
    public record Part(String name, Vec3d offset, Vec3d half, Vec3d joint,
                       float restRoll, int u, int v, boolean mirrored) {}

    public static final int TEXTURE_WIDTH = %d;
    public static final int TEXTURE_HEIGHT = %d;
    public static final List<Part> PARTS = List.of(
        %s);

    private PoppetModelDefinition() {}
}
''' % (source["resolution"]["width"], source["resolution"]["height"], ",\n        ".join(lines))
destination = root / "src/main/java/net/dark/spiritum/entity/poppet/PoppetModelDefinition.java"
destination.write_text(java, encoding="utf-8")
texture = source["textures"][0]["source"]
assert texture.startswith("data:image/png;base64,")
(root / "src/main/resources/assets/spiritum/textures/entity/poppet.png").write_bytes(base64.b64decode(texture.split(",", 1)[1]))
print("Imported six model parts, shoulder/hip/neck pivots, and the embedded texture")
