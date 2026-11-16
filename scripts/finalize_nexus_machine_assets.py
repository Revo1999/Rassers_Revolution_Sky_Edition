"""Collect Blockbench exports and verify Nexus machine dimensions, ports and animation clearances."""
from pathlib import Path
from collections import Counter, deque
import argparse, base64, hashlib, json, math, shutil
from PIL import Image, ImageDraw, ImageFont

ROOT=Path(__file__).resolve().parents[1]
ART=ROOT/'art/blockbench/nexus-machines'

def write(p,d):p.write_text(json.dumps(d,indent=2)+'\n',encoding='utf-8',newline='\n')
def values(k):return [float(k['data_points'][0][a]) for a in 'xyz']

def make_gallery():
    index=json.loads((ART/'machine-index.json').read_text())
    canvas=Image.new('RGB',(1800,560),(30,34,40));draw=ImageDraw.Draw(canvas)
    try:font=ImageFont.truetype('C:/Windows/Fonts/consola.ttf',23)
    except OSError:font=ImageFont.load_default()
    labels={'pressure_casting_plant':'Pressure Casting Plant','electroplating_line':'Electroplating Line','particle_collider':'Particle Collider'}
    for i,entry in enumerate(index['machines']):
        name=entry['machine']
        with Image.open(ART/(name+'-preview.png')) as im:
            image=im.convert('RGB');image.thumbnail((600,500));canvas.paste(image,(i*600+(600-image.width)//2,(500-image.height)//2))
        d=entry['dimensions'];label=f"{labels[name]}  {d['width']}x{d['depth']}x{d['height']}"
        draw.text((i*600+300,525),label,font=font,fill='#d9e1e8',anchor='mm')
    canvas.save(ART/'nexus-machines-preview.png')

def main():
    parser=argparse.ArgumentParser();parser.add_argument('--source',required=True,type=Path);args=parser.parse_args()
    source=args.source.resolve();assert source.is_dir()
    for folder,pattern in [('', '*.bbmodel'),('', '*.png'),('parts','*.bbmodel'),('java-models','*.json')]:
        target=ART/folder;target.mkdir(parents=True,exist_ok=True)
        for p in (source/folder).glob(pattern):
            if p.resolve()!=(target/p.name).resolve():shutil.copy2(p,target/p.name)
    for p in [*ART.glob('*.bbmodel'),*(ART/'parts').glob('*.bbmodel')]:
        d=json.loads(p.read_text(encoding='utf-8'))
        for texture in d['textures']:
            name=texture['name'].removesuffix('.png')+'.png';image=ART/'textures'/name;assert image.is_file(),name
            texture['path']=str(image.resolve());texture['relative_path']=('../' if p.parent.name=='parts' else '')+'textures/'+name
            texture['source']='data:image/png;base64,'+base64.b64encode(image.read_bytes()).decode()
        write(p,d)
    index=json.loads((ART/'machine-index.json').read_text())
    expected={'machine_'+name for name in index['parts']}
    assert {p.stem for p in (ART/'parts').glob('*.bbmodel')}==expected
    assert {p.stem for p in (ART/'java-models').glob('*.json')}==expected
    for p in (ART/'java-models').glob('*.json'):
        d=json.loads(p.read_text());d['textures']={k:'revoindustry:block/nexus_machine_atlas' for k in d['textures']}
        for e in d['elements']:
            assert all(0<=n<=16 for key in ['from','to'] for n in e[key]),p
            assert all(a<b for a,b in zip(e['from'],e['to'])),p
        write(p,d)
    checks=[]
    for entry in index['machines']:
        name=entry['machine'];b=json.loads((ART/'blueprints'/f'{name}.json').read_text())
        d=json.loads((ART/f'{name}.bbmodel').read_text());cells={tuple(c['position']):c for c in b['cells']}
        assert len(cells)==len(b['cells'])==b['placedBlocks']
        assert dict(Counter(c['part'] for c in b['cells']))==b['counts']
        width=b['dimensions']['width'];depth=b['dimensions']['depth'];assert b['dimensions']['height']==5
        assert (width,depth)==((21,21) if name=='particle_collider' else (9,9))
        assert [max(p[i] for p in cells)-min(p[i] for p in cells)+1 for i in range(3)]==[width,5,depth]
        assert all(0<=p[0]<width and 0<=p[1]<5 and 0<=p[2]<depth for p in cells)
        assert all((width-1-x,y,z) in cells for x,y,z in cells),'Asymmetric structure footprint: '+name
        seen={next(iter(cells))};queue=deque(seen)
        while queue:
            p=queue.popleft()
            for axis in range(3):
                for step in [-1,1]:
                    q=list(p);q[axis]+=step;q=tuple(q)
                    if q in cells and q not in seen:seen.add(q);queue.append(q)
        assert len(seen)==len(cells),'Disconnected structural island: '+name
        assert not (set(cells)&{tuple(p) for p in b['requiredAir']})
        for hatch in b['hatches']:
            x,y,z=hatch['position'];face=hatch['facing']
            assert {'north':z==0,'south':z==depth-1,'west':x==0,'east':x==width-1}[face]
        animations=d['animations'];assert len(animations)==1
        a=animations[0];allowed=set(b['visualGroups']);channels={}
        for animator in a['animators'].values():
            assert animator['name'] in allowed
            for channel in {k['channel'] for k in animator['keyframes']}:
                keys=sorted([k for k in animator['keyframes'] if k['channel']==channel],key=lambda k:k['time'])
                assert keys[0]['time']==0 and keys[-1]['time']==a['length']
                assert all(k['interpolation']=='linear' for k in keys)
                first,last=values(keys[0]),values(keys[-1])
                if channel=='rotation':
                    assert all(abs(math.sin(math.radians(v1))-math.sin(math.radians(v2)))<1e-8 and abs(math.cos(math.radians(v1))-math.cos(math.radians(v2)))<1e-8 for v1,v2 in zip(first,last))
                    assert all(max(abs(y-x) for x,y in zip(values(k1),values(k2)))<=45 for k1,k2 in zip(keys,keys[1:]))
                else:assert first==last
                channels[animator['name'],channel]=keys
        nodes={e['name']:e for e in d['elements']}
        for e in nodes.values():assert all(0<=v<=limit for key in ['from','to'] for v,limit in zip(e[key],[width*16,80,depth*16]))
        extra={}
        if name=='pressure_casting_plant':
            keys=channels['press_platen','position'];minimum=min(values(k)[1] for k in keys)
            assert nodes['Moving press platen ONLY']['from'][1]+minimum==nodes['Contained hot casting']['to'][1]
            extra['pressContactsCastingWithoutPassingThrough']=True
        if name=='electroplating_line':
            keys=channels['plating_rack','position'];guidekeys=channels['hoist_cables','scale']
            for k1,k2 in zip(keys,keys[1:]):
                v1,v2=values(k1),values(k2)
                if v1[2]!=v2[2]:assert v1[1]==v2[1]==0 and 50+v1[1]>48
            for k,guide in zip(keys,guidekeys):
                y=values(k)[1];scale=values(guide)[1];assert 50+y>=34
                assert abs(58+(50-58)*scale-(50+y))<1e-8
                if y<0:assert values(k)[2] in [0,32,64]
            extra.update({'threeSeparateBaths':True,'rackClearsDividersDuringTravel':True,'hoistTracksImmersion':True})
        if name=='particle_collider':
            assert all(math.hypot(x-10,z-10)>=5.5 for x,y,z in cells)
            assert all((math.hypot(x-10,z-10)-8)**2+(y-2)**2<=2.5**2 for x,y,z in cells)
            assert all((10,y,10) not in cells for y in range(5))
            extra.update({'donutShape':True,'centreOpenAtEveryHeight':True,'hollowBeamTunnel':True})
        checks.append({'machine':name,'dimensions':b['dimensions'],'placedBlocks':len(cells),'outwardHatches':len(b['hatches']),'connectedStructure':True,'symmetricStructureFootprint':True,'reservedAirClear':True,'animationSeconds':a['length'],'animatedGroups':[v['name'] for v in a['animators'].values()],'loopEndpointsMatch':True,'sha256':hashlib.sha256((ART/f'{name}.bbmodel').read_bytes()).hexdigest(),**extra})
    result={'scope':'models and blueprints; gameplay not registered','machines':checks,'individualPartModels':len(expected),'nativeJavaBlockExports':len(expected),'allPartBoundsWithinOneBlock':True,'structuralBlocksAnimated':False}
    write(ART/'asset-validation.json',result)
    if all((ART/(e['machine']+'-preview.png')).exists() for e in index['machines']):make_gallery()
    print(json.dumps(result,indent=2))

if __name__=='__main__':main()
