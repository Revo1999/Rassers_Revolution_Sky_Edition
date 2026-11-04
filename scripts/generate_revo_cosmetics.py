"""Default cosmetic catalogue and a small working FTB quest chapter, not a progression rewrite."""
from pathlib import Path
import json

ROOT=Path(__file__).resolve().parents[1]
RES=ROOT/'revoachievements/src/main/resources'
def write(path,data):
    path.parent.mkdir(parents=True,exist_ok=True);path.write_text(json.dumps(data,indent=2,ensure_ascii=False)+'\n',encoding='utf-8')
titles=[
 ('unpaid_factory_intern','Unpaid Factory Intern','Your first official Revo job. Salary remains theoretical.','Complete Clock in at Revo.','#B3BDAB'),
 ('certified_crank_operator','Certified Crank Operator','Certified to turn things in circles.','Complete First Wooden Drill.','#D6AD5A'),
 ('senior_dirt_inspector','Senior Dirt Inspector','All soil must pass your rigorous stare.','Complete A Greener Workplace.','#83B86D'),
 ('chief_egg_hatcher','Chief Egg Hatcher','Responsible for suspicious noises from the incubator.','Assigned by Revo quest rewards.','#DCC98E'),
 ('licensed_shaft_technician','Licensed Shaft Technician','Shaft alignment is serious business.','Assigned by Revo quest rewards.','#77B8B8'),
 ('executive_cobblestone_manager','Executive Cobblestone Manager','A highly senior position in a rapidly growing pile.','Assigned by Revo quest rewards.','#ABB1B8'),
 ('director_questionable_science','Director of Questionable Science','The experiment is probably contained.','Complete Contained Curiosity.','#AD92CC'),
 ('master_of_industry','Master of Industry','The factory works because you understand it.','Assigned by Revo quest rewards.','#DFBD5B'),
 ('deforestation_supervisor','Deforestation Supervisor','Renewable wood, questionable paperwork.','Complete Branch Office.','#B4A170'),
]
hats=[
 ('capbackwards','Off-duty Cap','A relaxed beginning to a serious industrial career.','Complete Clock in at Revo.'),
 ('minerhat','Miner Helmet','Proper headwear for void extraction.','Complete First Wooden Drill.'),
 ('flowercrown','Flower Crown','Management-approved workplace greenery.','Complete A Greener Workplace.'),
 ('madscientist','Scientist Hat','Absolutely standard laboratory attire.','Complete Contained Curiosity.'),
 ('propelhat','Propeller Hat','An exceptionally aerodynamic promotion.','Complete Branch Office.'),
 ('tophat','Top Hat','Dress for the factory you want.','Assigned by Revo quest rewards.'),
 ('goggles','Workshop Goggles','For excellent-looking inspections.','Assigned by Revo quest rewards.'),
 ('crown','Industrial Crown','A purely cosmetic management position.','Assigned by Revo quest rewards.'),
]
for i,(key,name,desc,hint,color) in enumerate(titles):
    write(RES/'data/revoachievements/revo_cosmetics/title'/f'{key}.json',dict(kind='title',name=name,description=desc,unlock_hint=hint,item='minecraft:name_tag',color=color,order=i*10))
for i,(key,name,desc,hint) in enumerate(hats):
    item={'capbackwards':'revoachievements:off_duty_cap','minerhat':'revoachievements:miner_helmet'}.get(key,'simplehats:'+key)
    write(RES/'data/revoachievements/revo_cosmetics/hat'/f'{key}.json',dict(kind='hat',name=name,description=desc,unlock_hint=hint,item=item,color='#D0B785',order=100+i*10,requires_mod='simplehats'))
for item,model in [('off_duty_cap','capbackwards'),('miner_helmet','minerhat')]:
    write(RES/'assets/revoachievements/models/item'/f'{item}.json',{'parent':'simplehats:item/'+model})
write(RES/'assets/revoachievements/lang/en_us.json',{
    'key.revoachievements.wardrobe':'Open Revo Wardrobe','key.categories.revoachievements':'Revo Cosmetics',
    'ftbquests.reward.revoachievements.title':'Revo Title Unlock','ftbquests.reward.revoachievements.hat':'Revo Hat Unlock',
    'revoachievements.reward.cosmetic_id':'Cosmetic ID (see /revoachievements list)',
    'item.revoachievements.off_duty_cap':'Revo Off-duty Cap','item.revoachievements.miner_helmet':'Revo Miner Helmet',
})

def rid(n):return f'5E7000000000{n:04X}'
quests=[]
examples=[
 (1,'Clock in at Revo',None,'unpaid_factory_intern','capbackwards',0.,0.,['Welcome to the Revo workforce. Complete this checkmark to collect your first title and hat.','Press K, use the R button in your inventory, or run /revoachievements wardrobe to choose your cosmetics.','Unlocks are personal, permanent and cosmetic only. They do not equip automatically.']),
 (2,'First Wooden Drill','revoextraction:wooden_drill_controller','certified_crank_operator','minerhat',-3.,2.,['Obtain a Wooden Drill Controller. Build guidance is available in EMI.','This task detects the controller and does not consume it.']),
 (3,'A Greener Workplace','revocultivation:greenhouse','senior_dirt_inspector','flowercrown',-1.,2.,['Obtain a Wooden Greenhouse Controller.','Your crops approve of this workplace dress code.']),
 (4,'Contained Curiosity','revogenetics:mob_harvester','director_questionable_science','madscientist',1.,2.,['Obtain a Wooden Mob Harvester Controller.','The mob is contained. Your scientific ambitions are not.']),
 (5,'Branch Office','revocultivation:tree_grower','deforestation_supervisor','propelhat',3.,2.,['Obtain a Wooden Tree Grower Controller.','Your new hat demonstrates excellent upward mobility.']),
]
for n,name,item,title,hat,x,y,description in examples:
    task={'id':rid(0x100+n),'type':'checkmark' if item is None else 'item'}
    if item:task.update(item={'id':item,'count':1},count=1,consume_items=False)
    rewards=[{'id':rid(0x200+n*2+j),'type':'revoachievements:'+kind,'cosmetic':f'revoachievements:{kind}/{key}','team_reward':False,'auto':'enabled'} for j,(kind,key) in enumerate([('title',title),('hat',hat)])]
    quest=dict(id=rid(n),title=name,x=x,y=y,tasks=[task],rewards=rewards,description=description)
    if n>1:quest['dependencies']=[rid(1)]
    quests.append(quest)
write(ROOT/'pack/overrides/config/ftbquests/quests/chapters/5e70000000000000.snbt',dict(id=rid(0),filename='5e70000000000000',title='Revo Employee Handbook',icon={'id':'minecraft:name_tag','count':1},order_index=100,quests=quests))

# Integrate via the existing formatter's supported placeholder API; preserve nicknames and LuckPerms fields.
config=ROOT/'pack/overrides/config/playernamestyler-common.toml';config.parent.mkdir(parents=True,exist_ok=True)
config.write_text('''# RevoAchievements supplies %revo_title%; empty when no title is equipped.
nameplateRenderer = "text_display"
chatFormat = "&f<%prefix%%revo_title%&r%displayname%%suffix%&f>%colon%&r%chatmessage%"
tabListFormat = "%prefix%%revo_title%&r%displayname%%suffix%"
nameplateFormat = "%prefix%%revo_title%&r%displayname%%suffix%"
showBelowName = true
''',encoding='utf-8')
print(f'Generated {len(titles)} titles, {len(hats)} Simple Hats cosmetics and {len(quests)} working FTB example quests.')
