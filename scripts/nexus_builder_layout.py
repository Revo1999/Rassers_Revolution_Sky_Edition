"""Runtime block-role mapping only; reuses existing MCP-authored placed-block assets."""
def integrate_builder(manifest):
 portal=manifest['structures']['nexus_portal'];mapping={(0,0,3):('builder','revoindustry:nexus_builder','north'),(-1,0,3):('builder_input','modern_industrialization:advanced_item_input_hatch','west'),(1,0,3):('builder_output','modern_industrialization:advanced_item_output_hatch','east'),(0,0,4):('builder_energy','modern_industrialization:mv_energy_input_hatch','south')}
 for cell in portal['cells']:
  if tuple(cell['position']) in mapping:cell['part'],cell['block'],cell['facing']=mapping[tuple(cell['position'])]
 portal['builderOrigin']=[0,0,3]
 return manifest
if __name__=='__main__':
 import json
 from pathlib import Path
 p=Path(__file__).resolve().parents[1]/'revoindustry/src/main/resources/nexus/structures.json';p.write_text(json.dumps(integrate_builder(json.loads(p.read_text())),indent=2)+'\n')
