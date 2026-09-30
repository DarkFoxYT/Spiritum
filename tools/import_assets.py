"""Import supplied artwork unchanged and convert Blockbench entity data to Java.

Usage: python tools/import_assets.py "C:/path/to/asset/folders"
The originals remain in art/source. This tool never generates or edits images.
"""
import json
import math
import shutil
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / 'art/source'
TEXTURES = ROOT / 'src/main/resources/assets/spiritum/textures'

def f(value):
    return str(round(float(value), 7)) + 'f'

def args(values):
    return ', '.join(map(f, values))

def generate_models():
    lines = ['package net.dark.spiritum.client.model;',
             'import net.minecraft.client.model.*;',
             'import net.minecraft.client.render.entity.animation.*;',
             'import java.util.*;',
             '/** Converted from supplied Blockbench files by tools/import_assets.py. */',
             'public final class SuppliedDemonModels {']
    for kind in ('Leech', 'Imp', 'Lemure'):
        model = json.loads((SOURCE / 'entity' / (kind+'DemonModel.bbmodel')).read_text(encoding='utf-8-sig'))
        groups = {g['uuid']: g for g in model['groups']}
        cubes = {e['uuid']: e for e in model['elements']}
        lines += [f'public static TexturedModelData {kind.lower()}() {{',
                  'ModelData data = new ModelData();', 'ModelPartData root = data.getRoot();']
        counter = 0

        def walk(nodes, parent, parent_origin):
            nonlocal counter
            for node in nodes:
                if isinstance(node, str):
                    cube = cubes[node]
                    if not cube.get('export', True):
                        continue
                    size = [b-a for a,b in zip(cube['from'],cube['to'])]
                    offset = [cube['from'][0]-parent_origin[0], parent_origin[1]-cube['to'][1],cube['from'][2]-parent_origin[2]]
                    uv = cube.get('uv_offset')
                    if uv is None:
                        uv = [min(face['uv'][index] for face in cube['faces'].values() for index in (0,2)), min(face['uv'][index] for face in cube['faces'].values() for index in (1,3))]
                    inflate = cube.get('inflate',0)
                    builder = f'ModelPartBuilder.create().uv({int(uv[0])}, {int(uv[1])}).mirrored({str(cube.get("mirror_uv",False)).lower()})'
                    builder += f'.cuboid({args(offset+size)}, new Dilation({f(inflate)}))'
                    counter += 1
                    lines.append(f'{parent}.addChild("cube_{counter}", {builder}, ModelTransform.NONE);')
                else:
                    group = groups[node['uuid']]
                    origin = group['origin']
                    offset = [origin[0]-parent_origin[0], parent_origin[1]-origin[1],origin[2]-parent_origin[2]]
                    rotation = group.get('rotation',[0,0,0])
                    radians = [-math.radians(rotation[0]),-math.radians(rotation[1]),math.radians(rotation[2])]
                    counter += 1
                    name = 'part'+str(counter)
                    lines.append(f'ModelPartData {name} = {parent}.addChild("{group["name"]}", ModelPartBuilder.create(), ModelTransform.of({args(offset+radians)}));')
                    walk(node['children'],name,origin)

        walk(model['outliner'],'root',[0,24,0])
        lines += [f'return TexturedModelData.of(data, {model["resolution"]["width"]}, {model["resolution"]["height"]});','}']
        lines.append(f'public static Map<String, AnimationDefinition> {kind.lower()}Animations() {{')
        lines.append('Map<String, AnimationDefinition> result = new LinkedHashMap<>();')
        for anim in model.get('animations',[]):
            lines.append(f'AnimationDefinition.Builder {anim["name"]} = AnimationDefinition.Builder.create({f(anim["length"])}).looping();')
            for animator in anim['animators'].values():
                for channel in ('rotation','position','scale'):
                    frames = sorted((k for k in animator.get('keyframes',[]) if k['channel']==channel),key=lambda k:k['time'])
                    if not frames:
                        continue
                    target = {'rotation':'ROTATE','position':'MOVE_ORIGIN','scale':'SCALE'}[channel]
                    helper = {'rotation':'createRotationalVector','position':'createTranslationalVector','scale':'createScalingVector'}[channel]
                    keys = []
                    for key in frames:
                        point = key['data_points'][0]
                        values = [float(point[c]) for c in ('x','y','z')]
                        if channel == 'rotation': values = [-values[0],-values[1],values[2]]
                        interpolation = 'CUBIC' if key['interpolation']=='catmullrom' else 'LINEAR'
                        keys.append(f'new Keyframe({f(key["time"])}, AnimationHelper.{helper}({args(values)}), Transformation.Interpolations.{interpolation})')
                    lines.append(f'{anim["name"]}.addBoneAnimation("{animator["name"]}", new Transformation(Transformation.Targets.{target}, '+', '.join(keys)+'));')
            lines.append(f'result.put("{anim["name"]}", {anim["name"]}.build());')
        lines += ['return result;', '}']
    lines += ['private SuppliedDemonModels() {}','}']
    output = ROOT / 'src/main/java/net/dark/spiritum/client/model/SuppliedDemonModels.java'
    output.parent.mkdir(parents=True,exist_ok=True)
    output.write_text('\n'.join(lines)+'\n',encoding='utf-8')

if __name__ == '__main__':
    if len(sys.argv)>1:
        supplied = Path(sys.argv[1]).resolve()
        for folder in ('block','entity','item','particle'):
            for original in sorted((supplied/folder).iterdir()):
                if original.suffix.lower() not in ('.png','.json','.bbmodel'):
                    continue
                destination=SOURCE/folder/original.name
                destination.parent.mkdir(parents=True,exist_ok=True)
                if original.resolve()!=destination.resolve(): shutil.copy2(original,destination)
    for original in SOURCE.rglob('*.png'):
        destination=TEXTURES/original.relative_to(SOURCE)
        destination.parent.mkdir(parents=True,exist_ok=True)
        shutil.copy2(original,destination)
    generate_models()
    print('Imported all supplied images unchanged; converted demon models and animation keyframes.')
