"""Regenerate JSON resources using supplied artwork; never create or edit images."""
import json
from copy import deepcopy
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / 'src/main/resources'
SOURCE = Path(__file__).resolve().parents[1] / 'art/source'
def write(path, value):
    file = ROOT / path
    file.parent.mkdir(parents=True, exist_ok=True)
    file.write_text(json.dumps(value, indent=2) + '\n', encoding='utf-8')
def asset(path, value): write('assets/spiritum/' + path + '.json', value)
def data(path, value): write('data/spiritum/' + path + '.json', value)

blocks = {name:name for name in ('hexstone','polished_hexstone','hexstone_bricks')}
for name, texture in blocks.items():
    asset('models/block/'+name, {'parent':'minecraft:block/cube_all', 'textures':{'all':'spiritum:block/'+texture}})
    asset('blockstates/'+name, {'variants':{'':{'model':'spiritum:block/'+name}}})

def supplied_block(name, texture):
    model = json.loads((SOURCE/'block'/f'{name}.json').read_text(encoding='utf-8-sig'))
    if name=='hexed_candle':
        source=json.loads((SOURCE/'block'/'hexed_candle.bbmodel').read_text(encoding='utf-8-sig'))
        width,height=source['resolution']['width'],source['resolution']['height']
        model['elements']=[]
        for cube in source['elements']:
            if not cube.get('export',True): continue
            part={'from':cube['from'],'to':cube['to'],'faces':{}}
            for direction,face in cube['faces'].items():
                if face.get('texture') is None: continue
                part['faces'][direction]={'uv':[coord*16/(width if i%2==0 else height) for i,coord in enumerate(face['uv'])],'texture':'#0'}
                if face.get('rotation'): part['faces'][direction]['rotation']=face['rotation']
            model['elements'].append(part)
    model['parent']='minecraft:block/block'
    model['textures']={'0':'spiritum:block/'+texture,'particle':'spiritum:block/'+texture}
    if name == 'alchemy_vat':
        for part in model.get('elements',[]):
            for face in part.get('faces',{}).values(): face.pop('tintindex',None)
    model.pop('format_version',None)
    return model

for flame in range(6):
    model=supplied_block('hexed_candle','hexed_candle')
    if flame==0: model['elements']=model['elements'][:1]
    else:
        for part in model['elements'][1:]:
            part['light_emission']=15
            for face in part['faces'].values(): face['tintindex']=0
    asset('models/block/hexed_candle_'+str(flame), model)
asset('blockstates/hexed_candle', {'variants':{'flame='+str(f):{'model':'spiritum:block/hexed_candle_'+str(f)} for f in range(6)}})

def element(start, end, texture):
    return {'from':start, 'to':end, 'faces':{d:{'texture':'#'+texture} for d in ('up','down','north','south','east','west')}}
asset('models/block/ritual_pedestal', supplied_block('ritual_pedestal','rkitual_pedestal'))
asset('blockstates/ritual_pedestal', {'variants':{'':{'model':'spiritum:block/ritual_pedestal'}}})
for filled in (False, True):
    model=supplied_block('alchemy_vat','alchemy_vat')
    if filled:
        model['textures']['water']='minecraft:block/water_still'
        water=element([2,10.5,2],[14,10.5,14],'water')
        for face in water['faces'].values(): face['tintindex']=0
        model['elements'].append(water)
    asset('models/block/alchemy_vat_'+str(filled).lower(), model)
asset('blockstates/alchemy_vat', {'variants':{'filled='+str(f).lower():{'model':'spiritum:block/alchemy_vat_'+str(f).lower()} for f in (False,True)}})

building_variants=[]
for base, prefix in [('hexstone','hexstone'),('polished_hexstone','polished_hexstone'),('hexstone_bricks','hexstone_brick')]:
    for kind in ('stairs','slab','wall'):
        name=prefix+'_'+kind
        building_variants.append(name)
        state=json.loads((Path(__file__).parent/'templates'/f'{kind}.json').read_text())
        def replace_models(value):
            if isinstance(value,dict):
                for key,entry in value.items():
                    if key=='model': value[key]='spiritum:block/'+(base if entry=='minecraft:block/cobblestone' else entry.removeprefix('minecraft:block/').replace('cobblestone',prefix))
                    else: replace_models(entry)
            elif isinstance(value,list):
                for entry in value: replace_models(entry)
        replace_models(state)
        asset('blockstates/'+name,state)
        texture='spiritum:block/'+base
        if kind=='stairs':
            for suffix,parent in [('', 'stairs'),('_inner','inner_stairs'),('_outer','outer_stairs')]:
                asset('models/block/'+name+suffix,{'parent':'minecraft:block/'+parent,'textures':{'bottom':texture,'top':texture,'side':texture}})
        elif kind=='slab':
            for suffix,parent in [('', 'slab'),('_top','slab_top')]:
                asset('models/block/'+name+suffix,{'parent':'minecraft:block/'+parent,'textures':{'bottom':texture,'top':texture,'side':texture}})
        else:
            for suffix,parent in [('_post','template_wall_post'),('_side','template_wall_side'),('_side_tall','template_wall_side_tall'),('_inventory','wall_inventory')]:
                asset('models/block/'+name+suffix,{'parent':'minecraft:block/'+parent,'textures':{'wall':texture}})

all_blocks = list(blocks)+building_variants+['hexed_candle','ritual_pedestal','alchemy_vat']
for name in all_blocks:
    parent = 'hexed_candle_0' if name == 'hexed_candle' else 'alchemy_vat_false' if name == 'alchemy_vat' else name
    if name.endswith('_wall'): parent=name+'_inventory'
    asset('models/item/'+name, {'parent':'spiritum:block/'+parent})
    data('loot_table/blocks/'+name, {'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'spiritum:'+name}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
    if name.endswith('_slab'):
        data('loot_table/blocks/'+name,{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'spiritum:'+name,'functions':[{'function':'minecraft:set_count','count':2,'conditions':[{'condition':'minecraft:block_state_property','block':'spiritum:'+name,'properties':{'type':'double'}}]},{'function':'minecraft:explosion_decay'}]}]}]})

items = {name:name for name in ('spirit_fragment','argent_nugget','spirit_gem','living_flesh','hexblade','hex_ash','calx_of_hades','argent_needle')}
items.update({'argent_ingot':'argent','voodoo_poppet':'poppet'})
items['bottle_of_hades']='bottle_of_hades'
items['sentinel']='sentinel'
items.update({name:name for name in ('summoners_ring','soulbind_ring','warding_ring')})
for name, texture in items.items():
    asset('models/item/'+name, {'parent':'minecraft:item/handheld' if name == 'hexblade' else 'minecraft:item/generated','textures':{'layer0':'spiritum:item/'+texture}})
for name in all_blocks + list(items):
    asset('items/'+name, {'model':{'type':'minecraft:model','model':'spiritum:item/'+name}})
asset('models/item/argent_needle_coated',{'parent':'minecraft:item/generated','textures':{'layer0':'spiritum:item/argent_needle','layer1':'spiritum:item/argent_needle_overlay'}})
asset('items/argent_needle',{'model':{'type':'minecraft:condition','property':'minecraft:has_component','component':'minecraft:potion_contents','on_true':{'type':'minecraft:model','model':'spiritum:item/argent_needle_coated','tints':[{'type':'minecraft:constant','value':16777215},{'type':'minecraft:potion','default':16777215}]},'on_false':{'type':'minecraft:model','model':'spiritum:item/argent_needle'}}})
asset('items/soulbind_ring',{'model':{'type':'minecraft:condition','property':'minecraft:has_component','component':'minecraft:container','on_true':{'type':'minecraft:model','model':'spiritum:item/soulbind_ring'},'on_false':{'type':'minecraft:model','model':'spiritum:item/empty_soulbind_ring'}}})
for image in sorted((SOURCE/'particle').glob('*.png')):
    asset('particles/'+image.stem,{'textures':['spiritum:'+image.stem]})
    write('assets/spiritum/textures/particle/'+image.name+'.mcmeta',{'texture':{'blur':True,'clamp':True}})
for name in ('dominion_rune','dominion_rite','vigilance_rite'):
    asset('particles/'+name,{'textures':['spiritum:'+name]})
# Keep models ready for supplied assets whose gameplay has not been specified yet.
for name in ('bottle_of_hades','empty_soulbind_ring','soulbind_ring','summoners_ring','warding_ring'):
    asset('models/item/'+name,{'parent':'minecraft:item/generated','textures':{'layer0':'spiritum:item/'+name}})
asset('models/block/flesh_block',{'parent':'minecraft:block/cube_all','textures':{'all':'spiritum:block/flesh_block'}})

def shaped(name, pattern, key, output=None, count=1):
    data('recipe/'+name, {'type':'minecraft:crafting_shaped','category':'misc','pattern':pattern,'key':key,'result':{'id':output or 'spiritum:'+name,'count':count}})
def shapeless(name, ingredients, output, count=1):
    data('recipe/'+name, {'type':'minecraft:crafting_shapeless','category':'misc','ingredients':ingredients,'result':{'id':output,'count':count}})
shaped('hexstone',['sss','sas','sss'],{'s':'minecraft:stone','a':'spiritum:argent_nugget'},count=8)
shaped('polished_hexstone',['HH','HH'],{'H':'spiritum:hexstone'},count=4)
shaped('hexstone_bricks',['HH','HH'],{'H':'spiritum:polished_hexstone'},count=4)
for base,prefix in [('hexstone','hexstone'),('polished_hexstone','polished_hexstone'),('hexstone_bricks','hexstone_brick')]:
    for kind,pattern,count in [('stairs',['H  ','HH ','HHH'],4),('slab',['HHH'],6),('wall',['HHH','HHH'],6)]:
        name=prefix+'_'+kind
        shaped(name,pattern,{'H':'spiritum:'+base},count=count)
        data('recipe/'+name+'_stonecutting',{'type':'minecraft:stonecutting','ingredient':'spiritum:'+base,'result':{'id':'spiritum:'+name,'count':2 if kind=='slab' else 1}})
shaped('hexed_candle',['S','H','H'],{'S':'minecraft:string','H':'spiritum:hexstone'})
shaped('ritual_pedestal',['AA','HH','HH'],{'A':'spiritum:argent_ingot','H':'spiritum:hexstone'})
shaped('alchemy_vat',['A A','A A','AAA'],{'A':'spiritum:argent_ingot'})
shaped('voodoo_poppet',[' F ','FFF',' F '],{'F':'spiritum:living_flesh'})
shaped('sentinel',['A A','AGA','A A'],{'A':'spiritum:argent_ingot','G':'spiritum:spirit_gem'})
shaped('argent_needle',['N','N','N'],{'N':'spiritum:argent_nugget'})
shaped('hexblade',[' A ',' A ','HSA'],{'A':'spiritum:argent_ingot','S':'minecraft:stick','H':'spiritum:hex_ash'})
shaped('bottle_of_hades',[' C ','C C','CCC'],{'C':'spiritum:calx_of_hades'})
for name, material, center in [('summoners_ring','calx_of_hades','spirit_gem'),('soulbind_ring','hex_ash','argent_nugget'),('warding_ring','argent_nugget','argent_ingot')]:
    shaped(name,[' M ','M M',' C '],{'M':'spiritum:'+material,'C':'spiritum:'+center})
data('recipe/needle_coating',{'type':'spiritum:needle_coating','category':'misc'})
shaped('argent_ingot',['NNN','NNN','NNN'],{'N':'spiritum:argent_nugget'})
shapeless('argent_nuggets',['spiritum:argent_ingot'],'spiritum:argent_nugget',9)
for block in ('polished_hexstone','hexstone_bricks'):
    data('recipe/'+block+'_stonecutting',{'type':'minecraft:stonecutting','ingredient':'spiritum:hexstone','result':{'id':'spiritum:'+block,'count':1}})

write('data/minecraft/tags/block/mineable/pickaxe.json',{'replace':False,'values':list('spiritum:'+b for b in list(blocks)+building_variants)+['spiritum:ritual_pedestal','spiritum:alchemy_vat']})
write('data/minecraft/tags/block/walls.json',{'replace':False,'values':['spiritum:'+name for name in building_variants if name.endswith('_wall')]})
write('data/minecraft/tags/item/swords.json',{'replace':False,'values':['spiritum:hexblade']})
data('worldgen/configured_feature/hexstone_deposit', {'type':'minecraft:ore','config':{'size':64,'discard_chance_on_air_exposure':0.0,'targets':[{'target':{'predicate_type':'minecraft:tag_match','tag':'minecraft:base_stone_overworld'},'state':{'Name':'spiritum:hexstone'}}]}})
data('worldgen/placed_feature/hexstone_deposit', {'feature':'spiritum:hexstone_deposit','placement':[{'type':'minecraft:count','count':2},{'type':'minecraft:in_square'},{'type':'minecraft:height_range','height':{'type':'minecraft:uniform','min_inclusive':{'absolute':-48},'max_inclusive':{'absolute':-48}}},{'type':'minecraft:biome'}]})

lang = {'itemGroup.spiritum':'Spiritum', 'message.spiritum.ritual_busy':'The pedestal is channeling. Snuff a ritual candle to interrupt it.', 'message.spiritum.active_ritual':'Active rite: %s. Snuff a candle to end it.'}
for name in all_blocks: lang['block.spiritum.'+name] = name.replace('_',' ').title()
for name in items: lang['item.spiritum.'+name] = name.replace('_',' ').title()
for name in ('rain','clear_skies','thunder','daytime','nighttime','warding','libido','zombie_summoning','skeleton_summoning','abundance','argentic_transmutation'): lang['ritual.spiritum.'+name] = 'Rite of '+name.replace('_',' ').title()
for name in ('leech_binding','imp_binding','lemure_binding','calling','withering','vigilance','dominion'): lang['ritual.spiritum.'+name] = 'Rite of '+name.replace('_',' ').title()
for name in ('leech_demon','imp_demon','lemure_demon','spirit_energy','sentinel','poppet'): lang['entity.spiritum.'+name] = name.replace('_',' ').title()
lang['entity.spiritum.poppet']='Voodoo Poppet'
for name in ('leech','imp','soulbind_transfer'):
    data('damage_type/'+name,{'message_id':'spiritum.'+name,'scaling':'never','exhaustion':0.1})
    lang['death.attack.spiritum.'+name]='%1$s was consumed by a '+name+' demon'
    lang['death.attack.spiritum.'+name+'.player']='%1$s was consumed by %2$s\'s '+name+' demon'
write('data/minecraft/tags/damage_type/bypasses_armor.json',{'replace':False,'values':['spiritum:leech','spiritum:soulbind_transfer']})
write('data/minecraft/tags/damage_type/bypasses_cooldown.json',{'replace':False,'values':['spiritum:imp','spiritum:soulbind_transfer']})
write('data/minecraft/tags/damage_type/bypasses_effects.json',{'replace':False,'values':['spiritum:soulbind_transfer']})
write('data/minecraft/tags/damage_type/bypasses_enchantments.json',{'replace':False,'values':['spiritum:soulbind_transfer']})
write('data/minecraft/tags/damage_type/no_knockback.json',{'replace':False,'values':['spiritum:leech','spiritum:soulbind_transfer']})
lang.update({
    'viewer.spiritum.rituals':'Spirit Rituals',
    'viewer.spiritum.alchemy':'Spirit Alchemy',
    'viewer.spiritum.bound_gem':'Player-bound spirit gem',
    'viewer.spiritum.boiling_water':'Drop items into a filled, boiling vat.',
    'viewer.spiritum.thirty_seconds':'Finish within 30 seconds.',
    'viewer.spiritum.candles':'%s candles, fueled with fragments or gems',
    'viewer.spiritum.bound_candles':'%s ordinary + 1 player-bound candle',
    'viewer.spiritum.optional_candles':'2 ordinary + 0–4 player-bound candles',
    'viewer.spiritum.vigilance_candles':'4 ordinary candles, plus up to 4 bound candles',
    'viewer.spiritum.dominion_candles':'4 gem-fueled candles, plus up to 4 bound candles',
    'viewer.spiritum.binding_return':'Returns the original bound gem upon completion.',
    'viewer.spiritum.instant':'Instant; flames are consumed in sequence.',
    'viewer.spiritum.persistent':'10-minute aura; keep its flames burning.',
    'viewer.spiritum.bound_protection':'Bound players are protected.',
    'message.spiritum.bottle_captured':'Stored %s demons. Bottle: %s / %s.',
    'message.spiritum.bottle_released':'Released %s demons. %s remain in the bottle.',
    'tooltip.spiritum.bottle_of_hades':'Sneak-use in air to store your demons within 12 blocks; use on ground to release.',
    'tooltip.spiritum.bottle_contents':'Stored demons: %s / 10',
    'message.spiritum.calling_started':'A Rite of Calling is summoning you.',
    'message.spiritum.bound_gem_required':'This ring needs a player-bound Spirit Gem.',
    'container.spiritum.ring':'Ring',
    'message.spiritum.target_warded':'That player is protected by a Warding Ring.',
    'tooltip.spiritum.summoners_ring':'Equipped: +20% demon damage, +50% demon defense; -1 entity interaction range.',
    'tooltip.spiritum.soulbind_ring':'Equip with a bound gem to share half their damage; +15% attack within 16 blocks.',
    'tooltip.spiritum.warding_ring':'Equipped: immune to voodoo and Calling; demon damage reduced by 50%.',
    'death.attack.spiritum.soulbind_transfer':'%1$s gave their life for a bound soul',
    'death.attack.spiritum.soulbind_transfer.player':'%1$s gave their life for a bound soul',
    'message.spiritum.calling_complete':'The Rite of Calling has brought you to its pedestal.',
    'message.spiritum.demon_claimed':'This demon now serves you.',
    'message.spiritum.gem_bound':'Spirit gem bound to %s.',
    'message.spiritum.gem_inserted':'Spirit gem inserted. Sneak-use to remove it.',
    'message.spiritum.gem_removed':'Spirit gem removed.',
    'message.spiritum.target_unavailable':'The poppet has no living, online bound player.',
    'message.spiritum.voodoo_applied':'The needle reaches %s.',
    'tooltip.spiritum.bound':'Bound soul: %s',
    'tooltip.spiritum.socket_empty':'Empty gem socket. Hold a gem in the other hand and use.',
    'tooltip.spiritum.socket_full':'Gem socket occupied. Sneak-use to remove.',
    'tooltip.spiritum.coated':'Potion-coated: one use, quarter duration.',
    'tooltip.spiritum.hexstone':'Wax-like cave stone; the foundation of spirit magic.',
    'tooltip.spiritum.polished_hexstone':'Polished wax-like stone for occult architecture.',
    'tooltip.spiritum.hexstone_bricks':'Carved hexstone for ritual chambers.',
    'tooltip.spiritum.hexed_candle':'Ignite with blaze powder, soul soil, flint and steel, fragments or gems.',
    'tooltip.spiritum.ritual_pedestal':'Offer items, then light nearby candles with spirit fuel.',
    'tooltip.spiritum.alchemy_vat':'Fill with water. Throw ingredients in before 30 seconds pass.',
    'tooltip.spiritum.spirit_fragment':'An esoteric remnant of a departing spirit.',
    'tooltip.spiritum.argent_nugget':'Channels spirit; found in ancient cities, dungeons and strongholds.',
    'tooltip.spiritum.argent_ingot':'Refined argent stores spirit within itself.',
    'tooltip.spiritum.spirit_gem':'Crystallized spirit. Sneak-use to bind yourself; empowers candles.',
    'tooltip.spiritum.living_flesh':'Reanimated flesh, still moving in your hand.',
    'tooltip.spiritum.hex_ash':'Hexstone and spirit dust, tied to a realm beyond.',
    'tooltip.spiritum.calx_of_hades':'Otherworldly calx, an anchor for outer beings.',
    'tooltip.spiritum.hexblade':'Harvests fragments. Player kills bind its socketed gem.',
    'tooltip.spiritum.voodoo_poppet':'Use to throw; sneak-use the doll to pick up. Needles work held or thrown.',
    'tooltip.spiritum.sentinel':'Place dormant argent armor. A nearby Rite of Vigilance awakens it to defend bound players.',
    'tooltip.spiritum.argent_needle':'Use with a bound poppet. Combine with a potion to coat.'
})
asset('lang/en_us',lang)
effects={
    'rain':'Rain for 10 minutes.', 'clear_skies':'Clear skies for 10 minutes.', 'thunder':'Thunder for 10 minutes.',
    'daytime':'Changes time to midday.', 'nighttime':'Changes time to midnight.',
    'warding':'Hostile mobs flee within 50 blocks.', 'libido':'Breeds animals within 15 blocks each minute.',
    'zombie_summoning':'Summons a zombie.', 'skeleton_summoning':'Summons a skeleton.',
    'abundance':'Crops within 4 blocks grow 3× as fast.', 'argentic_transmutation':'Produces one argent nugget.',
    'leech_binding':'Summons a leech owned by the bound player.', 'imp_binding':'Summons an imp owned by the bound player.',
    'lemure_binding':'Summons a lemure owned by the bound player.', 'calling':'Teleports the online bound player here.',
    'withering':'Wither I within 50 blocks.',
    'vigilance':'Sentinels within 50 blocks defend attacked players bound by the candles.',
    'dominion':'Unbound players mine blocks like obsidian and cannot use storage within 50 blocks.'}
for name,effect in effects.items():lang['viewer.spiritum.effect.'+name]=effect
asset('lang/en_us',lang)
print('Generated JSON resources linked to supplied assets; no textures created.')
