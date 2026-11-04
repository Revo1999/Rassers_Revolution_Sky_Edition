"""Encode the real shader-rendered Minecraft framebuffer capture for FancyMenu."""
from pathlib import Path
import json
import subprocess
import shutil
import imageio_ffmpeg

ROOT=Path(__file__).resolve().parents[1]
FRAMES=ROOT/'.local/menu-frames'
ASSETS=ROOT/'pack/overrides/config/fancymenu/assets'

def main():
    if not (FRAMES/'complete.txt').exists():raise RuntimeError('The in-game capture has not completed.')
    raw=FRAMES/'minecraft-realtime.mp4'
    if not raw.exists():raise RuntimeError('Real-time recording is missing.')
    ASSETS.mkdir(parents=True,exist_ok=True)
    ffmpeg=imageio_ffmpeg.get_ffmpeg_exe()
    # Keep real-time timestamps and the centered image; no sidebar tint or retiming.
    subprocess.run([ffmpeg,'-y','-i',str(raw),'-map','0:v:0','-c:v','copy',
        '-an','-movflags','+faststart',str(ASSETS/'skyblock_loop.mp4')],check=True)
    subprocess.run([ffmpeg,'-y','-i',str(ASSETS/'skyblock_loop.mp4'),'-frames:v','1','-update','1',
                    str(ASSETS/'skyblock_poster.png')],check=True)
    (ASSETS/'CREDITS.txt').write_text(
        'Rassers Revolution: Sky Edition — starting island menu film\n'
        'Recorded in Minecraft Java 1.21.1, NeoForge 21.1.265.\n'
        'Shader: Complementary Reimagined r5.9.3 by EminGTR / Complementary Development.\n'
        'https://www.complementary.dev/shaders/\n'
        'Shader loader: Iris 1.8.12 with Sodium 0.6.13.\n'
        'Island template: config/skyblockbuilder/templates/islands/revo_start.nbt\n'
        '20-second centered 1280x720 H.264 loop, 30 fps, real-time animation, no audio.\n'
        'Character and shadow hidden using spectator mode in the QA world.\n'
        'The shader source is downloaded separately, not bundled into these assets.\n'
        'Font: Departure Mono by Helena Zhang; see the resource pack for its SIL OFL license.\n',encoding='utf-8')
    (ASSETS/'capture.json').write_text(json.dumps({
        'minecraft':'1.21.1','shader':'Complementary Reimagined r5.9.3',
        'source':'Minecraft framebuffer, real-time clock','frames':600,'fps':30,'durationSeconds':20,
        'centered':True,'characterVisible':False,'viewBobbing':False,
        'captureTiming':(FRAMES/'complete.txt').read_text().strip(),
        'resolution':[1280,720],'audio':False,'island':'revo_start.nbt'},indent=2)+'\n')
    print('Encoded the authentic starting-island shader film and fallback poster.')

if __name__=='__main__':main()
