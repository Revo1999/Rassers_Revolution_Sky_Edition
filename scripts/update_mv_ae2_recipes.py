"""Rebase the pack's AE2 gates on the pinned upstream recipes, preserving recipe IDs."""
from pathlib import Path
import json
import zipfile

ROOT = Path(__file__).resolve().parents[1]
TARGET = ROOT / 'revoextraction/src/main/resources'
CIRCUIT = 'modern_industrialization:electronic_circuit'  # MI's second (MV) circuit

def write(name, value):
    path = TARGET / name
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2) + '\n', encoding='utf-8')

def main():
    with zipfile.ZipFile(next((ROOT/'revolib/run/mods').glob('appliedenergistics2-*.jar'))) as jar:
        for processor in ['logic', 'engineering', 'calculation']:
            name = f'data/ae2/recipe/inscriber/{processor}_processor.json'
            recipe = json.loads(jar.read(name))
            recipe['ingredients']['middle'] = {'item': CIRCUIT}
            write(name, recipe)
        for name, key in [('inscribers', 'c'), ('energy_energy_acceptor', 'c')]:
            path = f'data/ae2/recipe/network/blocks/{name}.json'
            recipe = json.loads(jar.read(path))
            recipe['key'][key] = {'item': CIRCUIT}
            write(path, recipe)
        path = 'data/ae2/recipe/network/blocks/controller.json'
        recipe = json.loads(jar.read(path))
        recipe['key']['m'] = {'item': CIRCUIT}
        recipe['pattern'] = ['ama', 'bcb', 'ama']
        write(path, recipe)
    with zipfile.ZipFile(next((ROOT/'revolib/run/mods').glob('Modern-Industrialization-*.jar'))) as jar:
        for processor in ['logic', 'engineering', 'calculation']:
            name = f'data/modern_industrialization/recipe/compat/ae2/{processor}_processor.json'
            recipe = json.loads(jar.read(name))
            recipe['item_inputs'].append({'item': CIRCUIT, 'amount': 1})
            recipe['eu'] = 32  # The circuit is the progression gate; this recipe uses 32 EU/t.
            write(name, recipe)

if __name__ == '__main__':
    main()
