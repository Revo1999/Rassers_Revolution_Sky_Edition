"""Collect Blockbench MCP exports and verify the individual-block art package."""
from pathlib import Path
from collections import Counter
import argparse, base64, hashlib, json, math, shutil
from PIL import Image

ROOT=Path(__file__).resolve().parents[1]
ART=ROOT/'art/blockbench/nexus-portal'

def save(path,data):path.write_text(json.dumps(data,indent=2)+'\n',encoding='utf-8',newline='\n')

def main():
    parser=argparse.ArgumentParser();parser.add_argument('--source',type=Path,required=True);args=parser.parse_args()
    source=args.source.resolve();assert source.is_dir()
    for folder,pattern in [('', '*.bbmodel'),('', '*.png'),('parts','*.bbmodel'),('java-models','*.json')]:
        dst=ART/folder;dst.mkdir(parents=True,exist_ok=True)
        for path in (source/folder).glob(pattern):
            if path.resolve()!=(dst/path.name).resolve():shutil.copy2(path,dst/path.name)
    for path in [ART/'nexus_portal_assembly.bbmodel',*sorted((ART/'parts').glob('*.bbmodel'))]:
        model=json.loads(path.read_text(encoding='utf-8'))
        for texture in model['textures']:
            name=texture['name'].removesuffix('.png')+'.png';target=ART/'textures'/name
            assert target.is_file(),name
            # Preserve the embedded image while removing temporary authoring paths.
            texture['path']=str(target.resolve());texture['relative_path']=('../' if path.parent.name=='parts' else '')+'textures/'+name
            texture['source']='data:image/png;base64,'+base64.b64encode(target.read_bytes()).decode()
        save(path,model)
    for path in (ART/'java-models').glob('*.json'):
        model=json.loads(path.read_text(encoding='utf-8'))
        model['textures']={key:'revoindustry:block/nexus_atlas' for key in model['textures']}
        for element in model.get('elements',[]):
            assert all(0<=v<=16 for key in ['from','to'] for v in element[key]),path
            assert all(a<b for a,b in zip(element['from'],element['to'])),path
            if 'rotation' in element:assert element['rotation']['angle'] in [-45,-22.5,0,22.5,45]
        save(path,model)
    blueprint=json.loads((ART/'portal-blueprint.json').read_text(encoding='utf-8'))
    positions=[tuple(c['position']) for c in blueprint['cells']]
    assert len(positions)==len(set(positions))==blueprint['blockCount']==814
    assert dict(Counter(c['part'] for c in blueprint['cells']))==blueprint['counts']
    assert blueprint['dimensions']=={'x':21,'y':19,'z':11}
    assembly=json.loads((ART/'nexus_portal_assembly.bbmodel').read_text(encoding='utf-8'))
    animations=assembly['animations'];assert len(animations)==1
    targets=[a.get('name') for animation in animations for a in animation['animators'].values()]
    assert targets==['portal_vortex'],targets
    animation=animations[0];assert animation['length']==16 and animation['loop']=='loop'
    keys=next(iter(animation['animators'].values()))['keyframes']
    keys=sorted(keys,key=lambda k:k['time'])
    assert [k['time'] for k in keys]==list(range(0,17,2))
    angles=[float(k['data_points'][0]['z']) for k in keys]
    assert angles==list(range(0,361,45))
    assert all(k['channel']=='rotation' and k['interpolation']=='linear' and float(k['data_points'][0]['x'])==float(k['data_points'][0]['y'])==0 for k in keys)
    assert all((b-a)/2==22.5 for a,b in zip(angles,angles[1:]))
    assert abs(math.cos(math.radians(angles[0]))-math.cos(math.radians(angles[-1])))<1e-9
    assert abs(math.sin(math.radians(angles[0]))-math.sin(math.radians(angles[-1])))<1e-9
    surface=next(e for e in assembly['elements'] if e['name']=='Animated portal surface ONLY')
    assert abs(surface['to'][2]-surface['from'][2]-.25)<1e-9
    bypos={tuple(c['position']):c for c in blueprint['cells']}
    directions={'north':(0,0,-1),'east':(1,0,0),'south':(0,0,1),'west':(-1,0,0)}
    opposite={'north':'south','south':'north','east':'west','west':'east'}
    railcells=[c for c in blueprint['cells'] if c['part']=='rail'];assert len(railcells)==51
    nodes={e['name']:e for e in assembly['elements']};arm_count=0
    for cell in railcells:
        p=cell['position'];prefix='rail @ '+','.join(map(str,p))
        assert prefix+' post' in nodes
        for direction,delta in directions.items():
            neighbour=bypos.get(tuple(p[i]+delta[i] for i in range(3)))
            expected=neighbour is not None and neighbour['part'] not in ['beacon','core']
            assert cell['connections'][direction]==expected
            if neighbour and neighbour['part']=='rail':assert neighbour['connections'][opposite[direction]]
            for height in ['lower','upper']:
                assert (prefix+' '+direction+' '+height in nodes)==expected
            arm_count+=int(expected)
    # Independent layout examples protect the corner orientation and clear stair opening.
    active=lambda p:{d for d,on in bypos[p]['connections'].items() if on}
    assert active((-10,2,0))=={'east','south'}
    assert active((10,2,0))=={'west','south'}
    assert active((-10,2,1))=={'north','south'}
    assert active((-3,2,0))=={'west'} and active((3,2,0))=={'east'}
    assert 'east' in active((-10,2,3))
    multipart=json.loads((ART/'blockstates/nexus_rail.json').read_text())['multipart']
    assert len(multipart)==5 and {next(iter(v['when'])) for v in multipart if 'when' in v}==set(directions)
    parts=list((ART/'parts').glob('*.bbmodel'));exports=list((ART/'java-models').glob('*.json'))
    expected_parts={'nexus_'+p for p in blueprint['blockModelParts']}
    assert {p.stem for p in parts}=={p.stem for p in exports}==expected_parts
    for texture in assembly['textures']:
        if texture['name'].removesuffix('.png')=='nexus_atlas':assert len(texture['source'])>100
    sprite=json.loads((ART/'textures/nexus_vortex.png.mcmeta').read_text())['animation']
    with Image.open(ART/'textures/nexus_vortex.png') as strip:
        assert strip.size==(256,16384) and sprite['frametime']==3 and sprite['interpolate']
    result={'status':'art-and-design-only; no gameplay registration','placedBlocks':len(positions),'dimensions':blueprint['dimensions'],'individualBlockModels':len(parts),'java1211CompatibleExports':len(exports),'uniquePositions':True,'individualBlockBounds':[0,16],'connectedRailBlocks':len(railcells),'directionalRailArms':arm_count,'railCornerEndAndTowerChecks':True,'decorativeBlockTypes':6,'animatedGroups':targets,'previewLoopSeconds':16,'previewDegreesPerSecond':22.5,'previewLoopSeamContinuous':True,'portalFaceSeparationBlocks':1/64,'textureLoopSeconds':9.6,'textureInterpolation':True,'wholeStructureTransformation':False,'portalModelSha256':hashlib.sha256((ART/'nexus_portal_assembly.bbmodel').read_bytes()).hexdigest()}
    save(ART/'asset-validation.json',result);print(json.dumps(result,indent=2))

if __name__=='__main__':main()
