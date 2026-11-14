"""Separate overlapping detail faces in Blockbench without changing UVs or adding geometry.
Only audited exact coplanar pairs are touched. Smaller detail surfaces sit 0.004 model
units proud (1/4000 block); ties choose the later detail. Historical source copies stay intact.
Native sources are exported through Blockbench; runtime bounds are obtained from its Cube API.
Rerun the audit afterward, and visually review affected machines in-game.
"""
from pathlib import Path
import copy,json
from audit_model_planes import ROOT,audit,read_model,DIRECTIONS
from blockbench_mcp_client import Blockbench,literal,embedded

def plan(data):
    draft=copy.deepcopy(data);ops={}
    for iteration in range(12):
        hits=audit(draft)
        if not hits:break
        changed=set()
        for hit in hits:
            pick=hit['a'] if hit['a_area']<hit['b_area'] else hit['b']
            i,name,face=pick;e=draft['elements'][i]
            if e.get('type','cube')!='cube':continue
            axis,sign=DIRECTIONS[face];bound='to' if sign==1 else 'from'
            key=(i,bound,axis)
            if key in changed:continue
            changed.add(key);e[bound][axis]=round(e[bound][axis]+sign*.004,8)
            ops.setdefault(i,{})[bound]=e[bound].copy()
        if not changed:break
    return draft,[dict(index=i,**v) for i,v in ops.items()],audit(draft)

def main():
    bb=Blockbench();bb.call('get_capabilities',{'include_tools':True})
    report=[]
    paths=list(sorted(ROOT.glob('revo*/src/main/resources/assets/*/models/**/*.json')))
    paths+=list(sorted((ROOT/'art').rglob('*.bbmodel')))
    for path in paths:
        if any(v in path.as_posix() for v in ['/history/','/archive/','/variants/','/preserved-','/before-']):continue
        data=read_model(path);before=audit(data)
        if not before:continue
        draft,ops,left=plan(data)
        if left:raise RuntimeError(f'Unresolved geometry: {path}: {len(left)}')
        if not ops:continue
        native=path.suffix=='.bbmodel'
        payload={'data':data,'operations':ops,'native':native,'name':path.stem}
        code='''(()=>{const d=DATA;newProject(d.native?Formats[d.data.meta.model_format]:Formats.java_block);Project.name="plane_audit_"+d.name;
        if(d.native)Codecs.project.parse(d.data);else for(const e of d.data.elements)new Cube({...e,autouv:0,box_uv:false}).init();
        const targets=d.operations.map(op=>d.native?Outliner.elements.find(e=>e.uuid===d.data.elements[op.index].uuid):Cube.all[op.index]);
        Undo.initEdit({elements:targets});for(let i=0;i<d.operations.length;i++){const op=d.operations[i],c=targets[i];if(!c)throw new Error("Missing audited cube");if(op.from)c.from=op.from.slice();if(op.to)c.to=op.to.slice();}
        Canvas.updateAll();Undo.finishEdit("Separate audited coplanar detail faces");Project.saved=true;return targets.map(c=>({from:c.from,to:c.to}));})()'''.replace('DATA',literal(payload))
        result=bb.call('risky_eval',{'code':code})
        if native:
            output=embedded(bb.call('export_model',{'codec_id':'project','result_format':'embedded','max_content_length':2000000}))
            fixed=json.loads(output)
            if audit(fixed):raise RuntimeError('Native export still overlaps: '+str(path))
            path.write_text(output,encoding='utf-8')
        else:
            # Use actual native Cube bounds, preserving all original Java metadata/texture references.
            query=bb.call('risky_eval',{'code':'(()=>Cube.all.map(c=>({from:c.from,to:c.to})))()'})
            text=next(c['text'] for c in query['content'] if c['type']=='text');value=json.loads(text)
            if isinstance(value,dict):value=value.get('result',value.get('value',value))
            if not isinstance(value,list):raise RuntimeError('Unexpected native bound response: '+text[:400])
            for op in ops:
                for key in ['from','to']:data['elements'][op['index']][key]=value[op['index']][key]
            path.write_text(json.dumps(data,indent=2)+'\n',encoding='utf-8')
        report.append({'file':path.relative_to(ROOT).as_posix(),'pairs_before':len(before),'adjusted_cubes':len(ops),'remaining':0})
        bb.call('risky_eval',{'code':'(()=>{Project.saved=true;Project.close();return true;})()'})
        print(path.name,len(before),'pairs fixed',flush=True)
    (ROOT/'docs/model-plane-fixes.json').write_text(json.dumps(report,indent=2)+'\n')

if __name__=='__main__':main()

