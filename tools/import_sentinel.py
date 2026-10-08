"""Convert the supplied Blockbench Sentinel into the native renderer's model data."""
import json
from pathlib import Path
import sys

source = json.loads(Path(sys.argv[1]).read_text(encoding="utf-8"))
groups = {g["uuid"]: g for g in source["groups"]}
elements = {e["uuid"]: e for e in source["elements"]}
paths = {}


def vector(value, rotation=False):
    signs = (-1, -1, 1)
    return [float(v) * s for v, s in zip(value, signs)]


def bone(node, parent_origin, parent_path):
    group = groups[node["uuid"]]
    origin = group["origin"]
    path = parent_path + [group["name"]]
    paths[group["uuid"]] = path
    result = {"name": group["name"], "origin": vector([a-b for a, b in zip(origin, parent_origin)]),
              "rotation": vector(group.get("rotation", [0, 0, 0])), "cubes": [], "children": []}
    if not parent_path:
        result["origin"][1] += 24
    for child in node["children"]:
        if isinstance(child, dict):
            result["children"].append(bone(child, origin, path))
            continue
        cube = elements[child]
        assert cube["type"] == "cube" and cube.get("box_uv", False)
        pivot = cube.get("origin", origin) if any(cube.get("rotation", [])) else origin
        uv = cube.get("uv_offset")
        if uv is None:
            uv = [min(cube["faces"]["east"]["uv"][::2]), min(cube["faces"]["up"]["uv"][1::2])]
        result["cubes"].append({
            "origin": vector([a-b for a, b in zip(pivot, origin)]),
            "rotation": vector(cube.get("rotation", [0, 0, 0])),
            "from": [pivot[0]-cube["to"][0], pivot[1]-cube["to"][1], cube["from"][2]-pivot[2]],
            "size": [b-a for a, b in zip(cube["from"], cube["to"])],
            "uv": uv, "mirror": cube.get("mirror_uv", False), "inflate": cube.get("inflate", 0)})
    return result


model = {"width": source["resolution"]["width"], "height": source["resolution"]["height"],
         "bones": [bone(node, [0, 0, 0], []) for node in source["outliner"]], "animations": {}}
for animation in source["animations"]:
    tracks = []
    for uuid, animator in animation["animators"].items():
        if uuid not in paths:
            continue
        for channel in ("rotation", "position"):
            frames = sorted((f for f in animator.get("keyframes", []) if f["channel"] == channel), key=lambda f: f["time"])
            if frames:
                tracks.append({"path": paths[uuid], "channel": channel, "frames": [
                    {"time": f["time"], "value": vector([f["data_points"][0][axis] for axis in ("x", "y", "z")]),
                     "smooth": f["interpolation"] == "catmullrom"} for f in frames]})
    model["animations"][animation["name"]] = {"length": animation["length"], "tracks": tracks}

destination = Path(__file__).resolve().parent.parent / "src/main/resources/assets/spiritum/models/entity/sentinel.json"
destination.parent.mkdir(parents=True, exist_ok=True)
destination.write_text(json.dumps(model, indent=2) + "\n", encoding="utf-8")
print(f"Imported {len(elements)} cubes, {len(groups)} bones, {len(model['animations'])} animations")
