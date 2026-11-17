"""Collect native Blockbench MCP exports; no model geometry is authored here."""
from pathlib import Path
import argparse,hashlib,json,shutil

ROOT=Path(__file__).resolve().parents[1]
ART=ROOT/'art/blockbench/running-shoes'
RES=ROOT/'revocultivation/src/main/resources'
def main():
    parser=argparse.ArgumentParser();parser.add_argument('--source',required=True,type=Path);args=parser.parse_args();source=args.source.resolve()
    colors=['white','black','brown','red','orange','yellow','green','cyan','blue','purple','magenta','pink']
    checks=[]
    for name in [*(c+'_running_shoes' for c in colors),'running_shoes_worn']:
        src=source/(name+'.bbmodel');d=json.loads(src.read_text(encoding='utf-8'))
        color=name.removesuffix('_running_shoes') if name!='running_shoes_worn' else 'blue'
        for texture in d['textures']:
            texture['path']=str((ART/'textures'/f'{color}.png').resolve());texture['relative_path']=f'textures/{color}.png'
        (ART/src.name).write_text(json.dumps(d,indent=2)+'\n',encoding='utf-8')
    for color in colors:
        src=source/f'exports/assets/revocultivation/models/item/{color}_running_shoes.json'
        data=json.loads(src.read_text());assert data['format_version']=='1.9.0'
        assert len(data['elements'])==4
        assert set(data['textures'].values())=={f'revocultivation:models/armor/running_shoes/{color}_layer_1'}
        target=RES/f'assets/revocultivation/models/item/{color}_running_shoes.json';shutil.copy2(src,target)
        checks.append({'color':color,'cubes':len(data['elements']),'runtimeSha256':hashlib.sha256(target.read_bytes()).hexdigest()})
    native=(source/'RunningShoesNative.java').read_text(encoding='utf-8');shutil.copy2(source/'RunningShoesNative.java',ART/'RunningShoesNative.java')
    start=native.index('public static LayerDefinition createBodyLayer()');end=native.index('\n\t}',start)+4
    method=native[start:end];assert method.count('.addBox(')==4
    assert 'PartPose.offset(-1.9F, 12.0F, 0.0F)' in method and 'PartPose.offset(1.9F, 12.0F, 0.0F)' in method
    java='''package dk.revo.revocultivation.client;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** Geometry exported by Blockbench MCP; edit running_shoes_worn.bbmodel and re-export. */
public final class RunningShoesGeometry {
    private RunningShoesGeometry(){}
    '''+method+'\n}\n'
    (ROOT/'revocultivation/src/main/java/dk/revo/revocultivation/client/RunningShoesGeometry.java').write_text(java,encoding='utf-8')
    for src in source.glob('*-preview.png'):shutil.copy2(src,ART/src.name)
    (ART/'export-validation.json').write_text(json.dumps({'authoring':'Blockbench MCP','wearableCubes':4,'items':checks},indent=2)+'\n',encoding='utf-8')
    print('Collected 13 MCP sources, 12 native item exports and native wearable layer geometry.')
if __name__=='__main__':main()
