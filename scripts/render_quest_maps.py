"""Offline layout previews from the actual quest coordinates, icons and installed model assets.
This does not start Minecraft. The game uses its own item renderer.
"""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont, ImageEnhance, ImageOps
import io, json, math, textwrap, zipfile

ROOT=Path(__file__).resolve().parents[1]
OUT=ROOT/'art/quests/refreshed';OUT.mkdir(parents=True,exist_ok=True)
resources={};jars=[]
asset_hint=ROOT/'.local/quest-minecraft-assets.txt'
paths=([Path(asset_hint.read_text().strip())] if asset_hint.exists() else [])+list((ROOT/'.local/integration-mods').glob('*.jar'))
for path in paths:
    jar=zipfile.ZipFile(path);jars.append(jar)
    for name in jar.namelist():
        if name.startswith('assets/') and ('/models/' in name or '/textures/' in name):resources[name]=(jar,name)

def data(path):
    for mod in ['revolib','revoindustry','revoextraction','revocultivation','revogenetics','revostorage','revoachievements']:
        p=ROOT/mod/'src/main/resources'/path
        if p.exists():return p.read_bytes()
    jar,name=resources[path];return jar.read(name)

def location(value):return value.split(':',1) if ':' in value else ('minecraft',value)

def model(value,depth=0):
    if depth>12:return {}
    ns,name=location(value)
    try:d=json.loads(data(f'assets/{ns}/models/{name}.json'))
    except KeyError:return {}
    inherited=model(d['parent'],depth+1) if 'parent' in d else {}
    return {**inherited,**d,'textures':{**inherited.get('textures',{}),**d.get('textures',{})}}

def texture(value,textures):
    for _ in range(12):
        if not value.startswith('#'):break
        value=textures[value[1:]]
    ns,name=location(value)
    return Image.open(io.BytesIO(data(f'assets/{ns}/textures/{name}.png'))).convert('RGBA')

cache={}
def obj_icon(m):
    ns,path=location(m['model']);raw=data('assets/'+ns+'/'+path).decode();folder=path.rsplit('/',1)[0]
    mats={};current='';vertices=[];uvs=[];normals=[];faces=[]
    for line in raw.splitlines():
        parts=line.split()
        if not parts:continue
        if parts[0]=='mtllib':
            for row in data('assets/'+ns+'/'+folder+'/'+parts[1]).decode().splitlines():
                p=row.split()
                if p and p[0]=='newmtl':current=p[1]
                elif p and p[0]=='map_Kd':mats[current]=p[1]
        elif parts[0]=='v':vertices.append(tuple(map(float,parts[1:4])))
        elif parts[0]=='vt':uvs.append(tuple(map(float,parts[1:3])))
        elif parts[0]=='vn':normals.append(tuple(map(float,parts[1:4])))
        elif parts[0]=='usemtl':current=parts[1]
        elif parts[0]=='f':
            indices=[list(map(int,p.split('/'))) for p in parts[1:]]
            normal=normals[indices[0][2]-1]
            if normal[0]+normal[1]-normal[2]<=0:continue
            pts=[vertices[p[0]-1] for p in indices];UV=[uvs[p[1]-1] for p in indices]
            try:
                im=texture(mats[current],m.get('textures',{}));u=sum(p[0] for p in UV)/len(UV);v=sum(p[1] for p in UV)/len(UV)
                color=im.getpixel((int(u*im.width)%im.width,int((1-v)*im.height)%im.height));shade=.72+.28*max(normal[1],0);color=tuple(round(x*shade) for x in color[:3])+(255,)
            except KeyError:color=(125,104,72,255)
            faces.append((sum(p[0]+p[1]-p[2] for p in pts)/len(pts),pts,color))
    points=[((p[0]+p[2])*.866,(p[0]-p[2])*.5-p[1]) for _,pts,_ in faces for p in pts]
    lx=min(p[0] for p in points);hx=max(p[0] for p in points);ly=min(p[1] for p in points);hy=max(p[1] for p in points);scale=64/max(hx-lx,hy-ly)
    out=Image.new('RGBA',(72,72));draw=ImageDraw.Draw(out)
    for _,pts,color in sorted(faces,key=lambda x:x[0]):
        q=[(((p[0]+p[2])*.866-lx)*scale+(72-(hx-lx)*scale)/2,((p[0]-p[2])*.5-p[1]-ly)*scale+(72-(hy-ly)*scale)/2) for p in pts];draw.polygon(q,fill=color)
    return out

def icon(ident):
    if ident in cache:return cache[ident]
    ns,name=location(ident);m=model(ns+':item/'+name);textures=m.get('textures',{})
    if m.get('loader')=='neoforge:obj':
        cache[ident]=obj_icon(m);return cache[ident]
    canvas=Image.new('RGBA',(72,72))
    if 'layer0' in textures:
        try:im=texture(textures['layer0'],textures);ratio=62/max(im.size);im=im.resize((round(im.width*ratio),round(im.height*ratio)),Image.Resampling.NEAREST);canvas.alpha_composite(im,((72-im.width)//2,(72-im.height)//2));cache[ident]=canvas;return canvas
        except KeyError:pass
    faces=[]
    for e in m.get('elements',[]):
        lo=e['from'];hi=e['to'];x,y,z=lo;X,Y,Z=hi
        quads={'north':[(x,Y,z),(X,Y,z),(X,y,z),(x,y,z)],
            'south':[(X,Y,Z),(x,Y,Z),(x,y,Z),(X,y,Z)],
            'east':[(X,Y,z),(X,Y,Z),(X,y,Z),(X,y,z)],
            'west':[(x,Y,Z),(x,Y,z),(x,y,z),(x,y,Z)],
            'up':[(x,Y,z),(x,Y,Z),(X,Y,Z),(X,Y,z)],
            'down':[(x,y,Z),(x,y,z),(X,y,z),(X,y,Z)]}
        normals={'north':(0,0,-1),'south':(0,0,1),'east':(1,0,0),'west':(-1,0,0),'up':(0,1,0),'down':(0,-1,0)}
        rot=e.get('rotation',{});axis=rot.get('axis');angle=math.radians(rot.get('angle',0));origin=rot.get('origin',[8,8,8])
        def rotate(p,normal=False):
            p=list(p)
            if axis:
                a,b={'x':(1,2),'y':(2,0),'z':(0,1)}[axis];o=[0,0,0] if normal else origin
                u,v=p[a]-o[a],p[b]-o[b];p[a]=o[a]+u*math.cos(angle)-v*math.sin(angle);p[b]=o[b]+u*math.sin(angle)+v*math.cos(angle)
            return p
        for side,f in e.get('faces',{}).items():
            n=rotate(normals[side],True)
            if n[0]+n[1]-n[2]<=.001:continue
            try:im=texture(f['texture'],textures)
            except (KeyError,TypeError):continue
            uv=f.get('uv',[0,0,16,16]);u,v,U,V=uv;W,H=im.size
            tile=im.crop((round(min(u,U)*W/16),round(min(v,V)*H/16),max(round(max(u,U)*W/16),round(min(u,U)*W/16)+1),max(round(max(v,V)*H/16),round(min(v,V)*H/16)+1)))
            if u>U:tile=ImageOps.mirror(tile)
            if v>V:tile=ImageOps.flip(tile)
            if f.get('rotation'):tile=tile.rotate(-f['rotation'],expand=True)
            if f.get('tintindex') is not None and 'leaves' in name:
                rgb=Image.new('RGBA',tile.size,(116,170,70,255));rgb.putalpha(tile.getchannel('A'));tile=rgb
            shade=.78+.22*max(n[1],0);tile=ImageEnhance.Brightness(tile).enhance(shade)
            pts=[rotate(p) for p in quads[side]];faces.append((sum(p[0]+p[1]-p[2] for p in pts)/4,pts,tile))
    projected=[((p[0]+p[2])*.866,(p[0]-p[2])*.5-p[1]) for _,pts,_ in faces for p in pts]
    if projected:
        lx=min(p[0] for p in projected);hx=max(p[0] for p in projected);ly=min(p[1] for p in projected);hy=max(p[1] for p in projected);scale=64/max(hx-lx,hy-ly,.01)
        def project(p):return ((p[0]+p[2])*.866*scale-lx*scale+(72-(hx-lx)*scale)/2,((p[0]-p[2])*.5-p[1])*scale-ly*scale+(72-(hy-ly)*scale)/2)
        for _,pts,tile in sorted(faces,key=lambda a:a[0]):
            q=[project(p) for p in pts];a=q[1][0]-q[0][0];b=q[3][0]-q[0][0];c=q[1][1]-q[0][1];d=q[3][1]-q[0][1];det=a*d-b*c
            if abs(det)<.001:continue
            w,h=tile.size;x,y=q[0];coeff=(d/det*w,-b/det*w,(-d*x+b*y)/det*w,-c/det*h,a/det*h,(c*x-a*y)/det*h)
            warped=tile.transform((72,72),Image.Transform.AFFINE,coeff,Image.Resampling.NEAREST)
            mask=Image.new('L',(72,72));ImageDraw.Draw(mask).polygon(q,fill=255)
            from PIL import ImageChops
            warped.putalpha(ImageChops.multiply(warped.getchannel('A'),mask));canvas.alpha_composite(warped)
    else:
        draw=ImageDraw.Draw(canvas);draw.rounded_rectangle((9,9,63,63),radius=7,fill='#597382');draw.text((18,27),name[:3].upper(),fill='white')
    cache[ident]=canvas;return canvas

FONT='C:/Windows/Fonts/consola.ttf'
def render(chapter_file,filename):
    d=json.loads(chapter_file.read_text(encoding='utf-8'));qs=d['quests'];S=46;minx=min(q['x'] for q in qs)-1.8;maxx=max(q['x'] for q in qs)+1.8;miny=min([q['y'] for q in qs]+[i['y'] for i in d.get('images',[])])-1.2;maxy=max(q['y'] for q in qs)+2.5
    W=round((maxx-minx)*S);H=round((maxy-miny)*S)+80;im=Image.new('RGBA',(W,H),'#141d25');dr=ImageDraw.Draw(im)
    title=ImageFont.truetype(FONT,27);font=ImageFont.truetype(FONT,14);head=ImageFont.truetype(FONT,19)
    dr.text((25,20),d['title'],font=title,fill='#e3e9e7');dr.text((25,55),'Layout preview | hexagons: milestones | diamonds: optional',font=font,fill='#91a9b3')
    def pos(x,y):return ((x-minx)*S,(y-miny)*S+80)
    for x in range(math.floor(minx),math.ceil(maxx)+1):
        for y in range(math.floor(miny),math.ceil(maxy)+1):
            X,Y=pos(x,y);dr.ellipse((X-1,Y-1,X+1,Y+1),fill='#25333c')
    lookup={q['id']:q for q in qs}
    for q in qs:
        for dep in q.get('dependencies',[]):
            if dep not in lookup:continue
            a=lookup[dep];A=pos(a['x'],a['y']);B=pos(q['x'],q['y']);dr.line([A,B],fill='#536f7d',width=2)
    for b in d.get('images',[]):
        X,Y=pos(b['x'],b['y']);w=b['width']*S;h=b['height']*S;dr.rounded_rectangle((X-w/2,Y-h/2,X+w/2,Y+h/2),radius=4,fill='#263843');text=b['title'];box=dr.textbbox((0,0),text,font=head);dr.text((X-(box[2]-box[0])/2,Y-11),text,font=head,fill='#bad9df')
    for q in qs:
        X,Y=pos(q['x'],q['y']);r=22*q.get('size',1);color='#b99b68' if q['shape']=='hexagon' else '#688d9c';fill='#263640'
        if q['shape']=='circle':dr.ellipse((X-r,Y-r,X+r,Y+r),fill=fill,outline=color,width=2)
        else:
            n=6 if q['shape']=='hexagon' else 4;offset=0 if n==4 else math.pi/6;points=[(X+math.cos(offset+2*math.pi*i/n)*r,Y+math.sin(offset+2*math.pi*i/n)*r) for i in range(n)];dr.polygon(points,fill=fill);dr.line(points+[points[0]],fill=color,width=2)
        image=icon(q['icon']['id']).resize((36,36),Image.Resampling.NEAREST);im.alpha_composite(image,(round(X-18),round(Y-18)))
        lines=textwrap.wrap(q['title'],13)
        for i,line in enumerate(lines):
            box=dr.textbbox((0,0),line,font=font);dr.text((X-(box[2]-box[0])/2,Y+r+7+i*16),line,font=font,fill='#d5dfe0')
    im.convert('RGB').save(OUT/filename)
    return {'file':filename,'quests':len(qs),'width':W,'height':H}

if __name__=='__main__':
    chapters=ROOT/'pack/overrides/config/ftbquests/quests/chapters'
    result=[render(chapters/'5e10000000000000.snbt','chapter-one.png'),render(chapters/'5e20000000000000.snbt','create-and-iron.png')]
    print(json.dumps(result))
