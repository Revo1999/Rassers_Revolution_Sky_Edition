"""Craftable vanilla eggs -> typed vanilla spawners -> retained controller programs."""
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'revocultivation/src/main/resources'
EGGS={
 'zombie':['minecraft:dirt','revocultivation:compost','minecraft:wheat_seeds'],
 'skeleton':['minecraft:cobblestone','revocultivation:compost','minecraft:wheat_seeds'],
 'spider':['minecraft:string','revocultivation:compost','minecraft:wheat_seeds'],
 'cow':['minecraft:wheat','revocultivation:compost','minecraft:wheat_seeds'],
 'slime':['minecraft:cactus','revocultivation:compost','minecraft:wheat_seeds'],
 'blaze':['modern_industrialization:steel_ingot','modern_industrialization:coke','minecraft:redstone'],
 'enderman':['modern_industrialization:steel_ingot','minecraft:quartz','minecraft:coal'],
 'magma_cube':['modern_industrialization:steel_ingot','minecraft:lava_bucket','minecraft:coal'],
 'ghast':['modern_industrialization:steel_ingot','minecraft:string','minecraft:glowstone_dust'],
 'shulker':['minecraft:netherite_ingot','minecraft:chorus_fruit','minecraft:diamond'],
 'wither_skeleton':['minecraft:netherite_ingot','minecraft:soul_sand','minecraft:bone']
}
def write(path,data):path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(data,indent=2)+'\n',encoding='utf-8')
def main():
 for mob,ingredients in EGGS.items():
  write(RES/f'data/revocultivation/recipe/harvester/egg_{mob}.json',{'type':'minecraft:crafting_shapeless','category':'misc','ingredients':[{'item':i} for i in ingredients],'result':{'id':f'minecraft:{mob}_spawn_egg','count':1}})
  write(RES/f'data/revocultivation/recipe/harvester/spawner_{mob}.json',{'type':'minecraft:crafting_shaped','category':'misc','pattern':['BBB','BEB','BBB'],'key':{'B':{'item':'minecraft:iron_bars'},'E':{'item':f'minecraft:{mob}_spawn_egg'}},'result':{'id':'minecraft:spawner','count':1,'components':{'minecraft:block_entity_data':{'id':'minecraft:mob_spawner','SpawnData':{'entity':{'id':f'minecraft:{mob}'}}}}}})
 langpath=RES/'assets/revocultivation/lang/en_us.json';lang=json.loads(langpath.read_text())
 lang.update({'recipe.revocultivation.filter':'Insert a crafted mob-specific spawner into the controller.','guide.revocultivation.bars':'Add barred walls and the front filter. Insert a crafted mob spawner into the controller to select its species.','gui.revocultivation.mob_spawner':'Mob spawner','message.revocultivation.spawner_in_controller':'Insert a mob-specific spawner into the controller to choose a program.','tooltip.revocultivation.egg_ingredient':'Crafting ingredient for a mob spawner. Cannot spawn mobs.','tooltip.revocultivation.spawner_program':'Harvester program: %s (cannot spawn mobs)'})
 write(langpath,lang)
 # Patch existing lessons by stable ID. Do not regenerate old quest chapters.
 chapters=ROOT/'pack/overrides/config/ftbquests/quests/chapters'
 p=chapters/'5e50000000000000.snbt';d=json.loads(p.read_text());q=next(q for q in d['quests'] if q['id']=='5E50000000000002')
 q['title']='Spawner Programs';q['description']=['Craft a vanilla mob egg with its recipe, then surround that egg with eight Iron Bars to craft a mob-specific vanilla Spawner. Insert the Spawner into the controller slot; it is retained, not consumed. The decorative filter no longer cycles species.', 'Wooden harvesters accept Zombie, Skeleton, Spider, Cow and Slime programs. Steel adds Blaze, Enderman, Magma Cube, Ghast, Shulker and Wither Skeleton. Shulker and Wither Skeleton still need a retained Netherite Ingot in the hatch. Eggs are recipe ingredients and cannot directly spawn mobs or change a spawner. Placed spawners are disabled and never spawn mobs.']
 write(p,d)
 print('Generated eleven vanilla egg recipes and eleven typed vanilla spawner recipes.')
if __name__=='__main__':main()
