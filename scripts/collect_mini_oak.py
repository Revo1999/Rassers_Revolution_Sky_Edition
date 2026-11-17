"""Collect the MCP tree source/export and adapt its grid to vanilla block rendering."""
from pathlib import Path
import json, shutil

ROOT = Path(__file__).resolve().parents[1]
SOURCE = Path('C:/Users/victo/Desktop/AssetForge/models')
ART = ROOT / 'art/blockbench/mini-oak'
ART.mkdir(parents=True, exist_ok=True)
for name in ['mini_oak.bbmodel', 'mini_oak.json', 'mini_oak-preview.png']:
    shutil.copy2(SOURCE / name, ART / name)
model = json.loads((ART / 'mini_oak.bbmodel').read_text())
rows = []
for cube in model['elements']:
    kind, x, y, z = cube['name'].split('_')
    assert cube['from'] == [7 + int(x)*2, int(y)*2, 7 + int(z)*2]
    assert cube['to'] == [9 + int(x)*2, int(y)*2+2, 9 + int(z)*2]
    rows.append('        {' + ','.join([x,y,z,'0' if kind == 'log' else '1']) + '}')
java = '''package dk.revo.revocultivation.client;

/** Block grid collected from mini_oak.bbmodel, authored through Blockbench MCP. */
final class MiniOakGeometry {
    private MiniOakGeometry(){}
    static final int[][] BLOCKS = {
'''+',\n'.join(rows)+'''\n    };
}
'''
(ROOT/'revocultivation/src/main/java/dk/revo/revocultivation/client/MiniOakGeometry.java').write_text(java)
print(f'Collected {len(rows)} miniature vanilla blocks from Blockbench MCP.')
