"""Create the version-matched dedicated server setup archive."""
import json
import zipfile

from package_modpack import ROOT, release_jars

PACK = ROOT / 'pack'
SERVER = PACK / 'server'
OVERRIDES = PACK / 'overrides'
SERVER_OVERRIDES = (
    'config/ftbquests',
    'config/skyblockbuilder',
    'config/modernfix-mixins.properties',
    'config/playernamestyler-common.toml',
    'defaultconfigs',
    'kubejs/data',
    'kubejs/server_scripts',
    'kubejs/startup_scripts',
)


def server_mods(lock, pins):
    projects = {mod['project'] for mod in lock['mods']}
    reviewed = pins['omit'].keys() | pins['includeUnknown'].keys()
    if reviewed - projects:
        raise ValueError(f'Server decisions refer to missing mods: {sorted(reviewed - projects)}')
    for mod in lock['mods']:
        project, side = mod['project'], mod['server']
        if project in pins['omit'] or side == 'unsupported':
            continue
        if side not in {'required', 'optional'} and project not in pins['includeUnknown']:
            raise ValueError(f'Review dedicated server compatibility for {project} in pack/server/pins.json')
        yield {key: mod[key] for key in ('project', 'file', 'url', 'sha512')}


def main():
    version = (PACK / 'version.txt').read_text(encoding='utf-8').strip()
    lock = json.loads((PACK / 'mods.lock.json').read_text(encoding='utf-8-sig'))
    pins = json.loads((SERVER / 'pins.json').read_text(encoding='utf-8'))
    installer = pins['installer']
    if installer['file'] != f"neoforge-{lock['neoForge']}-installer.jar":
        raise ValueError('Update the server installer pin to match the pack loader')
    mods = list(server_mods(lock, pins))
    manifest = dict(version=version, minecraft=lock['minecraft'], neoForge=lock['neoForge'],
                    installer=installer, mods=mods)
    output = ROOT.parent / f'Rassers-Revolution-Sky-Edition-{version}-server.zip'
    jars = release_jars(version)

    with zipfile.ZipFile(output, 'w', zipfile.ZIP_DEFLATED) as archive:
        archive.writestr('install.json', json.dumps(manifest, indent=2) + '\n')
        for filename in ('README.md', 'install.py', 'server.properties.example'):
            archive.write(SERVER / filename, filename)
        for relative in SERVER_OVERRIDES:
            path = OVERRIDES / relative
            paths = [path] if path.is_file() else sorted(path.rglob('*'))
            for item in paths:
                if item.is_file() and item.name not in {'.gitkeep', 'README.md'}:
                    archive.write(item, item.relative_to(OVERRIDES).as_posix())
        for jar in jars:
            archive.write(jar, 'mods/' + jar.name)
    with zipfile.ZipFile(output) as archive:
        if archive.testzip() is not None:
            raise OSError(f'Bad server archive: {output}')
    print(f'Created {output} with {len(jars)} Revo jars and {len(mods)} pinned downloads.')


if __name__ == '__main__':
    main()
