from pathlib import Path
import json,math
root=Path('.');src=root/'art/blockbench/revoextraction';assets=root/'revoextraction/src/main/resources/assets/revoextraction'
report={'sources':[],'runtime_models':[],'animation':{},'issues':[]}
for path in sorted(src.glob('*.bbmodel')):
    data=json.loads(path.read_text());elements=data['elements'];uuids=[e['uuid'] for e in elements]
    assert len(uuids)==len(set(uuids)),path
    assert data['textures'] and 'base64,' in data['textures'][0]['source'],path
    density=3 if path.stem.endswith(('assembled','unassembled')) else 1
    # Both full-machine sources use a uniform one-third geometric scale.
    if path.stem=='wooden_drill_casing':density=1
    samples=[]
    for e in elements:
        dims=[b-a for a,b in zip(e['from'],e['to'])];assert min(dims)>0,(path,e['name'])
        for face,f in e['faces'].items():
            if f.get('texture') is None:continue
            uv=f['uv'];du,dv=abs(uv[2]-uv[0]),abs(uv[3]-uv[1]);wh=[dims[0],dims[2]] if face in ['up','down'] else [dims[2],dims[1]] if face in ['east','west'] else [dims[0],dims[1]]
            densities=[du/wh[0],dv/wh[1]]
            if any(abs(v-density)>1e-4 for v in densities):report['issues'].append({'source':path.name,'cube':e['name'],'face':face,'density':densities,'expected':density})
            assert min(uv)>=0 and max(uv)<=512,(path,e['name'])
            samples.append(densities)
    report['sources'].append({'name':path.name,'cubes':len(elements),'quads':len(elements)*6,'texture_size':[512,512],'target_pixels_per_source_unit':density})
    if path.stem=='wooden_drill_assembled':
        anim=data['animations'][0];report['animation']={'name':anim['name'],'length':anim['length'],'loop':anim['loop'],'bones':len(anim['animators'])}
for path in sorted((assets/'models/block').glob('wooden_drill_*.json')):
    data=json.loads(path.read_text());es=data.get('elements',[])
    for e in es:
        assert all(-16<=v<=32 for v in e['from']+e['to']),(path,e)
        if 'rotation' in e:assert e['rotation'].get('angle',0) in [-45,-22.5,0,22.5,45],(path,e['rotation'])
        for f in e['faces'].values():assert f['texture']=='#0'
    report['runtime_models'].append({'name':path.name,'cubes':len(es)})
(src/'validation.json').write_text(json.dumps(report,indent=2)+'\n')
print(json.dumps(report,indent=2))

assert not report["issues"], "Drill asset UV validation failed; see validation.json"
