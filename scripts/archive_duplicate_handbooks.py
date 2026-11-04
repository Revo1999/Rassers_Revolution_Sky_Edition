"""Archive exact old handbook duplicates produced by previous dev editor saves."""
from pathlib import Path
import json,datetime,re
import nbtlib
root=Path(__file__).resolve().parents[1]
run=(root/'revolib/run/config/ftbquests/quests').resolve()
backup=(root/'.local/quest-backups'/datetime.datetime.now().strftime('%Y%m%d-%H%M%S')).resolve()
assert run.is_relative_to(root.resolve()) and backup.is_relative_to(root.resolve())
source=json.loads((root/'pack/overrides/config/ftbquests/quests/chapters/5e70000000000000.snbt').read_text(encoding='utf-8-sig'))
lang_path=run/'lang/en_us.snbt'
def parse_ftb(path):
 text=path.read_text(encoding='utf-8-sig')
 text=re.sub(r'(?m)^\s*//[^\n]*','',text)
 text=re.sub(r'(?m)([^\s,{\[])\s*\n\s*(?=[\w"\[{])',r'\1,\n',text)
 return nbtlib.parse_nbt(text)
lang=parse_ftb(lang_path) if lang_path.exists() else {}
def fingerprint(chapter,translations):
 values=[]
 for q in chapter.get('quests',[]):
  qid=str(q['id']);title=str(q.get('title',translations.get(f'quest.{qid}.title','')))
  desc=tuple(str(x) for x in q.get('description',translations.get(f'quest.{qid}.quest_desc',[])))
  tasks=tuple(sorted((str(t.get('type','')),str(t.get('item',{}).get('id','')),int(t.get('count',1))) for t in q.get('tasks',[])))
  rewards=tuple(sorted((str(r.get('type','')),str(r.get('cosmetic',''))) for r in q.get('rewards',[])))
  values.append((title,desc,tasks,rewards,float(q.get('x',0)),float(q.get('y',0))))
 return sorted(values)
expected=fingerprint(source,{})
moved=[]
for path in (run/'chapters').glob('*.snbt'):
 if path.name=='5e70000000000000.snbt':continue
 try:chapter=parse_ftb(path)
 except Exception:continue
 title=str(chapter.get('title',lang.get(f"chapter.{chapter.get('id','')}.title",'')))
 if title=='Revo Employee Handbook' and fingerprint(chapter,lang)==expected:
  backup.mkdir(parents=True,exist_ok=True);destination=backup/path.name
  assert path.resolve().is_relative_to(run) and destination.resolve().is_relative_to(backup)
  path.replace(destination);moved.append(path.name)
(root/'.local/quest-duplicate-cleanup.json').write_text(json.dumps({'archive':str(backup),'exactDuplicatesArchived':moved},indent=2)+'\n',encoding='utf-8')
print(f'Archived {len(moved)} exact handbook duplicates; other quest files untouched.')

