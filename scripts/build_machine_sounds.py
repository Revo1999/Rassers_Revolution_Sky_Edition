"""Original deterministic, short machine foley. No third party audio samples.
Python dependencies: numpy, soundfile (libsndfile Vorbis encoder).
Phrases have short fades and headroom; gameplay handles cadence, attenuation and BLOCKS volume.
"""
from pathlib import Path
import json
import numpy as np
import soundfile as sf

ROOT=Path(__file__).resolve().parents[1]/'revolib/src/main/resources/assets/revolib'
NAMES=['wood_drill','metal_drill','electric_drill','irrigation','harvester','compost','silk',
       'sluice','vacuum','crucible','rain','converter','storage','furnace','trade','interact']
RATE=22050

def build():
    (ROOT/'sounds/machine').mkdir(parents=True,exist_ok=True)
    definitions={}
    subtitles={}
    for i,name in enumerate(NAMES):
        rng=np.random.default_rng(4820+i)
        t=np.arange(int(RATE*(.2 if name=='interact' else .92)))/RATE
        noise=rng.normal(0,1,len(t))
        soft=np.convolve(noise,np.ones(15)/15,mode='same')
        low=np.convolve(noise,np.ones(101)/101,mode='same')
        hum=lambda f: np.sin(2*np.pi*f*t)+.25*np.sin(2*np.pi*2*f*t)
        pulse=lambda f: (np.maximum(0,np.sin(2*np.pi*f*t))**18)
        if name=='wood_drill': a=.35*low+.1*hum(65)+.42*soft*pulse(9)
        elif name=='metal_drill': a=.15*hum(85)+.23*soft+.15*hum(650)*pulse(12)
        elif name=='electric_drill': a=.15*hum(120)+.13*soft+.09*hum(850)*pulse(16)
        elif name in ['irrigation','sluice','rain']: a=.55*soft*(.6+.4*np.sin(2*np.pi*3*t)**2)+.45*low
        elif name=='harvester': a=.13*hum(95)+.38*soft*pulse(6)
        elif name=='compost': a=.55*low+.38*soft*pulse(4)
        elif name=='silk': a=.22*soft*(.4+.6*pulse(5))+.012*hum(180)
        elif name=='vacuum': a=.24*hum(155)+.32*soft
        elif name in ['crucible','furnace']: a=.5*low+.25*soft*pulse(17)
        elif name=='converter': a=.16*hum(100)+.02*hum(300)+.035*soft
        elif name=='storage': a=.12*hum(70)+.2*soft*pulse(8)
        elif name=='trade': a=.28*soft*pulse(3)+.22*low
        else: a=.4*hum(180)*np.exp(-t*30)+.3*soft*np.exp(-t*40)
        fade=np.minimum(1,t/.025)*np.minimum(1,(t[-1]-t)/.07)
        a=np.clip(a*fade,-.65,.65)
        sf.write(ROOT/f'sounds/machine/{name}.ogg',a,RATE,format='OGG',subtype='VORBIS')
        key='machine.'+name
        definitions[key]={'subtitle':'subtitles.revolib.'+key,'sounds':[{'name':f'revolib:machine/{name}','attenuation_distance':12}]}
        subtitles['subtitles.revolib.'+key]={'wood_drill':'Wooden drill rattles','metal_drill':'Drill grinds',
            'electric_drill':'Electric drill whirs','irrigation':'Irrigation trickles','harvester':'Harvester cycles',
            'compost':'Compost settles','silk':'Silkworms rustle','sluice':'Sluice washes','vacuum':'Vacuum draws items',
            'crucible':'Crucible bubbles','rain':'Rain fills barrel','converter':'Converter hums','storage':'Storage crank turns',
            'furnace':'Furnace roars','trade':'Colony trades','interact':'Machine latch clicks'}[name]
    (ROOT/'sounds.json').write_text(json.dumps(definitions,indent=2)+'\n')
    lang=ROOT/'lang/en_us.json'
    data=json.loads(lang.read_text()) if lang.exists() else {}
    data.update(subtitles);lang.parent.mkdir(exist_ok=True);lang.write_text(json.dumps(data,indent=2)+'\n')

if __name__=='__main__': build()
