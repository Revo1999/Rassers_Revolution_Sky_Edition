"""Install pinned dependencies and NeoForge into this extracted server directory."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
from tempfile import NamedTemporaryFile
from urllib.request import Request, urlopen

ROOT = Path(__file__).resolve().parent


def checksum(path, algorithm):
    with path.open('rb') as file:
        return hashlib.file_digest(file, algorithm).hexdigest()


def install_file(pin, directory, algorithm, cache):
    filename = pin['file']
    if filename != Path(filename).name or any(char in filename for char in '/\\:'):
        raise ValueError(f'Invalid dependency filename: {filename}')
    target = directory / filename
    expected = pin[algorithm]
    if target.exists():
        if checksum(target, algorithm) != expected:
            raise ValueError(f'{target} differs from the release pin. Move it aside before retrying.')
        return target

    directory.mkdir(parents=True, exist_ok=True)
    cached = cache / filename if cache else None
    print(f'Preparing {filename}', flush=True)
    temporary = None
    try:
        with NamedTemporaryFile(dir=directory, delete=False) as file:
            temporary = Path(file.name)
            if cached and cached.is_file():
                with cached.open('rb') as source:
                    shutil.copyfileobj(source, file)
            else:
                request = Request(pin['url'], headers={'User-Agent': 'Rassers-Revolution-Server/1.0'})
                with urlopen(request, timeout=60) as source:
                    shutil.copyfileobj(source, file)
        if checksum(temporary, algorithm) != expected:
            raise ValueError(f'Checksum mismatch for {filename}')
        os.replace(temporary, target)
        return target
    finally:
        if temporary:
            temporary.unlink(missing_ok=True)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--java', default='java', help='Java 21 executable')
    parser.add_argument('--cache', type=Path, help='Reuse matching artifacts from a local directory')
    args = parser.parse_args()
    manifest = json.loads((ROOT / 'install.json').read_text(encoding='utf-8'))

    properties = ROOT / 'server.properties'
    try:
        with properties.open('x', encoding='utf-8') as file:
            file.write((ROOT / 'server.properties.example').read_text(encoding='utf-8'))
    except FileExistsError:
        pass
    for mod in manifest['mods']:
        install_file(mod, ROOT / 'mods', 'sha512', args.cache)
    installer = install_file(manifest['installer'], ROOT / '.installer', 'sha256', args.cache)
    subprocess.run([args.java, '-jar', str(installer), '--installServer'], cwd=ROOT, check=True)
    print('Setup complete. See README.md for the first launch and whitelist.')


if __name__ == '__main__':
    try:
        main()
    except (OSError, ValueError, subprocess.CalledProcessError) as error:
        raise SystemExit(f'Server setup failed: {error}') from error
