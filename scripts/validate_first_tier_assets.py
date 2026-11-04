"""Verify exported first-tier Blockbench models and runtime registration data."""
from pathlib import Path
import base64, json, math
from PIL import Image

ROOT=Path(__file__).resolve().parents[1]
ART=ROOT/'art/blockbench/first-tier-machines'
ASSETS=ROOT/'revocultivation/src/main/resources/assets/revocultivation'
def read(path): return json.loads(path.read_text(encoding='utf-8-sig'))

def validate_cage_joints():
    elements=read(ASSETS/'models/block/mob_harvester_body.json')['elements']
    def overlaps(a,b):
        return all(min(a['to'][i],b['to'][i])-max(a['from'][i],b['from'][i])>1e-5 for i in range(3))
    posts=[e for e in elements if e['name'].startswith('oak_pillar_')]
    collars=[e for e in elements if e['name'].startswith('square_stone_bracing_')]
    checks=0
    for face in ['front','back','left','right']:
        panel={e['name'].removeprefix('framed_cage_'+face+'_'):e for e in elements if e['name'].startswith('framed_cage_'+face+'_')}
        assert len(panel)==9,(face,'Incomplete framed panel')
        for name,e in panel.items():
            if name.startswith('inset_bar_'):
                assert overlaps(e,panel['lower_rail']) and overlaps(e,panel['upper_rail']),(face,name,'Floating bar end')
                checks+=2
            elif name.startswith('pillar_stile_'):
                assert any(overlaps(e,p) for p in posts),(face,name,'Detached post connection')
                checks+=1
        assert sum(overlaps(panel['lower_rail'],p) for p in collars)>=2,(face,'Rail not seated into stone collars')
        assert sum(overlaps(panel['upper_rail'],p) for p in posts)>=2,(face,'Header detached from timber')
        checks+=4
    return checks

def main():
    atlas=ASSETS/'textures/block/first_tier_atlas.png'
    atlases={name:ASSETS/'textures/block'/name for name in ['first_tier_atlas.png','harvester_atlas.png']}
    for name,path in atlases.items():
        assert Image.open(path).size==(512,512)
        assert path.read_bytes()==(ART/'textures'/name).read_bytes()
    assert Image.open(atlas).convert('RGBA').crop((0,0,512,192)).tobytes()==Image.open(atlases['harvester_atlas.png']).convert('RGBA').crop((0,0,512,192)).tobytes(),'Original vanilla materials changed'
    models=[]
    for path in sorted((ASSETS/'models/block').glob('*.json')):
        model=read(path)
        if model.get('textures',{}).get('0')=='revocultivation:block/small_cultivation_atlas':continue # Validated separately by validate_small_cultivation.py.
        for element in model.get('elements',[]):
            a,b=element['from'],element['to']
            assert all(-16<=v<=32 and math.isfinite(v) for v in a+b),path
            assert all(b[i]>a[i] for i in range(3)),(path,element.get('name'))
            for face in element['faces'].values():
                assert all(0<=v<=16 and math.isfinite(v) for v in face['uv']),path
                assert face['texture']=='#0',path
        if model.get('elements'):
            assert model['render_type']=='minecraft:cutout',path
            assert model['textures']['0'] in ['revocultivation:block/first_tier_atlas','revocultivation:block/harvester_atlas'],path
            assert len(model['elements'])<=96,path
        models.append({'file':path.name,'cubes':len(model.get('elements',[]))})
    sources=[]
    for path in sorted(ART.glob('*.bbmodel')):
        source=read(path)
        assert source['meta']['model_format']=='java_block',path
        texture=source['textures'][0]
        assert base64.b64decode(texture['source'].split(',',1)[1])==atlases[texture['name']].read_bytes(),path
        elements=source['elements']
        assert len(elements)<=96,path
        scale=3 if path.stem.endswith('_assembled') else 1
        # Full-crop preview planes deliberately use sprite UVs; all material faces
        # must preserve one pixel per physical world-art unit on both axes.
        faces=0
        for element in elements:
            if element['name'].startswith('wheat_preview_'):continue
            dims=[(b-a)*scale for a,b in zip(element['from'],element['to'])]
            for face,data in element['faces'].items():
                if data.get('texture') is None:continue
                wh=(dims[0],dims[2]) if face in ('up','down') else (dims[2],dims[1]) if face in ('east','west') else (dims[0],dims[1])
                u,v,U,V=data['uv']
                assert abs(abs(U-u)-wh[0])<.001 and abs(abs(V-v)-wh[1])<.001,(path,element['name'],face)
                faces+=1
        sources.append({'file':path.name,'cubes':len(elements),'density_checked_faces':faces})
    for mod in ['revocultivation','revogenetics']:
        resources=ROOT/mod/'src/main/resources'
        for path in resources.rglob('*.json'):read(path)
        for path in (resources/'assets'/mod/'blockstates').glob('*.json'):
            for variant in read(path)['variants'].values():
                namespace,name=variant['model'].split(':')
                assert (ROOT/namespace/'src/main/resources/assets'/namespace/'models'/f'{name}.json').is_file(),path
        for path in (resources/'assets'/mod/'models/item').glob('*.json'):
            namespace,name=read(path)['parent'].split(':')
            if namespace=='minecraft':continue # Vanilla generated item parents; custom single-block items have their own validator.
            assert (ROOT/namespace/'src/main/resources/assets'/namespace/'models'/f'{name}.json').is_file(),path
    joints=validate_cage_joints()
    texture_audit=read(ART/'cage-texture-audit.json');pixels=Image.open(atlases['harvester_atlas.png']).convert('RGBA')
    texture_sources={name:read(ART/f'{name}.bbmodel') for name in {f['model'] for f in texture_audit['faces']}}
    for face in texture_audit['faces']:
        u,v,U,V=face['uv'];region=pixels.crop((math.floor(u),math.floor(v),math.ceil(U),math.ceil(V)))
        assert region.getchannel('A').getextrema()==(255,255),(face['cube'],'Transparent iron or edge bleed')
        source=texture_sources[face['model']];element=next(e for e in source['elements'] if e['name']==face['cube'])
        assert element['faces'][face['face']]['uv']==face['uv'],(face['cube'],'Stale texture UV audit')
        assert source['textures'][0]['name']=='harvester_atlas.png',(face['model'],'Wrong embedded atlas')
    for name in ['mob_harvester_body','barred_panel','mob_filter','mob_harvester_controller']:
        old=read(ART/'history/pre-cage-textures'/f'{name}.json')['elements'];new=read(ASSETS/'models/block'/f'{name}.json')['elements']
        geometry=lambda values:[(e['name'],e['from'],e['to'],e.get('rotation')) for e in values]
        assert geometry(old)==geometry(new),(name,'Texture edit changed geometry')
    report={'atlases':{name:[512,512] for name in atlases},'runtime_models':models,'editable_sources':sources,'cage_attachment_checks':joints,'opaque_iron_face_checks':len(texture_audit['faces']),'geometry_preserved':True,'result':'passed'}
    (ART/'asset-validation.json').write_text(json.dumps(report,indent=2)+'\n',encoding='utf-8')
    print(f'PASS: {len(sources)} editable sources, {len(models)} runtime models; embedded atlases, bounds, UV density, {joints} cage joints, {len(texture_audit["faces"])} opaque iron faces and unchanged geometry.')

if __name__=='__main__': main()
