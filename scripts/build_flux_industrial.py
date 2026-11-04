"""Stage native Aseprite exports and the industrial Flux Networks language overlay."""
from pathlib import Path
import argparse, json, shutil
ROOT=Path(__file__).resolve().parents[1]
ART=ROOT/'art/flux-industrial'
PACK=ROOT/'pack/overrides/resourcepacks/revo-industrial-grid'

NAMES={
 'itemGroup.fluxnetworks':'Industrial Power Grid',
 'block.fluxnetworks.flux_block':'Conductive Alloy Block',
 'block.fluxnetworks.flux_controller':'Grid Controller',
 'block.fluxnetworks.flux_point':'Power Receiver',
 'block.fluxnetworks.flux_plug':'Power Transmitter',
 'block.fluxnetworks.basic_flux_storage':'Capacitor Bank Mk I',
 'block.fluxnetworks.herculean_flux_storage':'Capacitor Bank Mk II',
 'block.fluxnetworks.gargantuan_flux_storage':'Capacitor Bank Mk III',
 'item.fluxnetworks.flux_dust':'Conductive Compound',
 'item.fluxnetworks.flux_core':'Induction Module',
 'item.fluxnetworks.flux_configurator':'Grid Multimeter',
 'item.fluxnetworks.admin_configurator':'Service Multimeter',
 'tooltip.fluxnetworks.flux_dust':'Produced by compressing Redstone Dust between Bedrock and Obsidian',
 'tooltip.fluxnetworks.flux_controller':'Manages wireless equipment charging on your power grid',
 'tooltip.fluxnetworks.flux_plug':'Takes power from adjacent generators and transmits it to the grid',
 'tooltip.fluxnetworks.flux_point':'Receives power from the grid and supplies adjacent machines',
 'tooltip.fluxnetworks.flux_storage':'Stores electrical energy for your power grid',
 'tooltip.fluxnetworks.flux_storage_2':'Energy Capacity: %s',
 'gui.fluxnetworks.tab.home':'Device Status',
 'gui.fluxnetworks.tab.selection':'Grid Selection',
 'gui.fluxnetworks.tab.wireless':'Equipment Charging',
 'gui.fluxnetworks.tab.connection':'Grid Connections',
 'gui.fluxnetworks.tab.statistics':'Grid Telemetry',
 'gui.fluxnetworks.tab.member':'Grid Operators',
 'gui.fluxnetworks.tab.setting':'Grid Settings',
 'gui.fluxnetworks.tab.create':'Commission New Grid',
 'gui.fluxnetworks.network.fullname':'Grid Name',
 'gui.fluxnetworks.response.hascontroller':'This grid already has a Grid Controller',
 'gui.fluxnetworks.error.noselected':'No power grid selected',
 'gui.fluxnetworks.error.nonetwork':'No power grid available',
 'gui.fluxnetworks.flux.wireless':'Enable Equipment Charging',
 'gui.fluxnetworks.flux.wirelessnetwork':'Equipment charging is active',
 'gui.fluxnetworks.flux.nonwirelessnetwork':'Equipment charging is inactive',
 'gui.fluxnetworks.stat.plug':'Transmitters',
 'gui.fluxnetworks.stat.point':'Receivers',
 'gui.fluxnetworks.stat.controller':'Grid Controllers',
 'gui.fluxnetworks.stat.storage':'Capacitor Banks',
 'gui.fluxnetworks.jei.creatingfluxrecipe':'Compressing Conductive Compound',
}

def write(path,data):
 path.parent.mkdir(parents=True,exist_ok=True)
 path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')

def enable(options):
 options.parent.mkdir(parents=True,exist_ok=True)
 text=options.read_text(encoding='utf-8') if options.exists() else ''
 lines=text.splitlines();prefix='resourcePacks:'
 for i,line in enumerate(lines):
  if line.startswith(prefix):
   packs=json.loads(line[len(prefix):]);break
 else:i=len(lines);packs=['vanilla','mod_resources']
 # Keep the owner's other selections and make our overrides win.
 entry='file/revo-industrial-grid'
 # NeoForge inserts its required pack after user packs if omitted from this list.
 # Keep it explicit so third-party assets cannot override this overlay.
 packs=['vanilla','mod_resources']+[p for p in packs if p not in [entry,'vanilla','mod_resources']]+[entry]
 result=prefix+json.dumps(packs,separators=(',',':'))
 if i==len(lines):lines.append(result)
 else:lines[i]=result
 options.write_text('\n'.join(lines)+'\n',encoding='utf-8')

def main():
 parser=argparse.ArgumentParser();parser.add_argument('--install-dev',action='store_true');args=parser.parse_args()
 exports=ART/'exports';assert len(list(exports.rglob('*.png')))==41
 target=PACK/'assets/fluxnetworks/textures';shutil.copytree(exports,target,dirs_exist_ok=True)
 write(PACK/'pack.mcmeta',{'pack':{'pack_format':34,'description':'Revo Industrial Power Grid | coordinated steel, copper, teal indicators and electrical equipment names'}})
 for locale in ['en_us','en_gb']:write(PACK/f'assets/fluxnetworks/lang/{locale}.json',NAMES)
 danish=dict(NAMES);danish.update({
  'itemGroup.fluxnetworks':'Industrielt Elnet','block.fluxnetworks.flux_block':'Ledende Legeringsblok',
  'block.fluxnetworks.flux_controller':'Elnetstyring','block.fluxnetworks.flux_point':'Strømmodtager',
  'block.fluxnetworks.flux_plug':'Strømsender','block.fluxnetworks.basic_flux_storage':'Kondensatorbank Mk I',
  'block.fluxnetworks.herculean_flux_storage':'Kondensatorbank Mk II','block.fluxnetworks.gargantuan_flux_storage':'Kondensatorbank Mk III',
  'item.fluxnetworks.flux_dust':'Ledende Forbindelse','item.fluxnetworks.flux_core':'Induktionsmodul',
  'item.fluxnetworks.flux_configurator':'Elnetmultimeter','item.fluxnetworks.admin_configurator':'Servicemultimeter'})
 write(PACK/'assets/fluxnetworks/lang/da_dk.json',danish)
 shutil.copy2(exports/'block/flux_block.png',PACK/'pack.png')
 shutil.copy2(ART/'FLUX-NETWORKS-LICENSE.txt',PACK/'FLUX-NETWORKS-LICENSE.txt')
 (PACK/'CREDITS.txt').write_text('Original Flux Networks mod: Sonar Sonic and BloCamLimb.\nRevo Industrial Power Grid is a presentation overlay; registry IDs and gameplay belong to Flux Networks.\nOriginal material atlas created with the built-in imagegen tool and fitted/painted in Aseprite.\nNative editable art and prompts are in art/flux-industrial in the Revo source project.\n',encoding='utf-8')
 if args.install_dev:
  shutil.copytree(PACK,ROOT/'revolib/run/resourcepacks/revo-industrial-grid',dirs_exist_ok=True)
  enable(ROOT/'revolib/run/options.txt')
 print('Staged 41 texture exports, 3 language overlays and the enabled development resource pack.' if args.install_dev else 'Staged Industrial Power Grid resource pack.')

if __name__=='__main__':main()
