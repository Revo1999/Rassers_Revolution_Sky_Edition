"""Original, restrained machine foley; no external audio samples.
Dependencies: numpy, soundfile. Four-second periodic loops use band-limited cyclic
noise and integer-cycle oscillators, with matched endpoints and conservative RMS.
Client envelopes, distance attenuation and equal-power crowd mixing handle playback.
"""
from pathlib import Path
import json,re
import numpy as np
import soundfile as sf

REPO=Path(__file__).resolve().parents[1]
ROOT=REPO/'revolib/src/main/resources/assets/revolib'
RATE=22050
LOOPS={
    # fundamental, noise cutoff, noise fraction, mechanical pulse rate
    'wood_drill':(60,600,.65,8),'metal_drill':(90,1100,.55,12),
    'steel_drill':(65,850,.55,10),'electric_drill':(120,950,.3,16),
    'irrigation':(0,1200,1,2),'greenhouse':(90,1000,.65,2),
    'tree_steel':(65,800,.65,4),'underwater':(0,450,1,3),
    'crystal_growth':(70,600,.7,1),'harvester':(65,650,.65,4),
    'harvester_steel':(80,800,.45,6),'sluice':(60,1500,.85,8),
    'vacuum':(120,1100,.5,1),'vacuum_2':(105,900,.45,1),'vacuum_3':(90,800,.4,1),
    'crucible':(0,700,1,5),'rain':(0,1600,1,7),
    'converter':(100,300,.1,1),'converter_2':(80,300,.1,1),'converter_3':(60,300,.1,1),
    'storage':(70,600,.45,6),'furnace':(60,750,.4,2),
    'furnace_turbine':(80,850,.3,2),'furnace_heavy':(45,600,.35,2),
    'furnace_crystal':(90,450,.15,1),'furnace_rainbow':(65,900,.3,2),
}
CUES=['compost','silk','trade','interact','transfer','fuel_feed','valve','gravel_feed',
      'harvest','chop','saw','crystal_pick','wind','craft','light_switch','pour',
      'ignite','motor_start','motor_stop']
LABELS={
 'wood_drill':'Wooden drill turns','metal_drill':'Iron drill grinds','steel_drill':'Steel drill grinds',
 'electric_drill':'Electric drill hums','irrigation':'Irrigation trickles','greenhouse':'Greenhouse circulates water',
 'tree_steel':'Tree grower pumps','underwater':'Water bubbles','crystal_growth':'Crystal bath circulates',
 'harvester':'Wooden harvester cycles','harvester_steel':'Steel harvester cycles','compost':'Compost settles',
 'silk':'Silkworms rustle','sluice':'Sluice washes','vacuum':'Vacuum draws items','vacuum_2':'Vacuum draws items',
 'vacuum_3':'Vacuum draws items','crucible':'Crucible bubbles','rain':'Rain fills barrel',
 'converter':'Converter hums','converter_2':'Converter hums','converter_3':'Converter hums',
 'storage':'Storage crank turns','furnace':'Furnace hums','furnace_turbine':'Furnace turbine hums',
 'furnace_heavy':'Heavy furnace hums','furnace_crystal':'Crystal furnace hums','furnace_rainbow':'Furnace turbine hums',
 'trade':'Colony trades','interact':'Machine latch clicks','transfer':'Hatch transfers items',
 'fuel_feed':'Fuel feeds','valve':'Valve clicks','gravel_feed':'Gravel tumbles','harvest':'Harvest collected',
 'chop':'Branches cut','saw':'Tree harvested','crystal_pick':'Crystals collected','wind':'Spring winds',
 'craft':'Terminal crafts','light_switch':'Grow lights switch','pour':'Liquid pours','ignite':'Furnace ignites',
 'motor_start':'Motor starts','motor_stop':'Motor slows'}

def noise(rng,n,cutoff):
    frequencies=np.fft.rfftfreq(n,1/RATE)
    spectrum=np.fft.rfft(rng.normal(size=n))/(1+(frequencies/cutoff)**6)
    spectrum[0]=0
    a=np.fft.irfft(spectrum,n=n)
    return a/max(1e-9,np.sqrt(np.mean(a*a)))

def build():
    (ROOT/'sounds/machine').mkdir(parents=True,exist_ok=True)
    definitions={};subtitles={};report=[]
    for index,name in enumerate(list(LOOPS)+CUES):
        rng=np.random.default_rng(4820+index)
        loop=name in LOOPS
        duration=4 if loop else .65 if name in ['pour','saw','ignite','gravel_feed'] else .35
        t=np.arange(int(RATE*duration))/RATE
        if loop:
            f,cutoff,mix,rate=LOOPS[name]
            mechanical=.78+.22*np.sin(2*np.pi*rate*t)**4
            a=noise(rng,len(t),cutoff)*mix*mechanical
            if f:a+=(1-mix)*(np.sin(2*np.pi*f*t)+.13*np.sin(2*np.pi*2*f*t))
            if name=='sluice':a+=.1*noise(rng,len(t),500)*np.maximum(0,np.sin(2*np.pi*8*t))**10
            if name=='furnace_crystal':a+=.025*np.sin(2*np.pi*270*t)*(.7+.3*np.sin(2*np.pi*.5*t))
        else:
            cutoff=450 if name in ['compost','silk','trade'] else 1000
            a=noise(rng,len(t),cutoff)*np.exp(-t*(5 if name in ['pour','saw','gravel_feed'] else 14))
            if name in ['interact','transfer','fuel_feed','valve','wind','craft','light_switch']:
                a=.45*a+.45*np.sin(2*np.pi*150*t)*np.exp(-t*25)
            if name=='crystal_pick':a=.16*a+.5*np.sin(2*np.pi*360*t)*np.exp(-t*10)
            if name in ['motor_start','motor_stop','ignite']:
                f=60+(30 if name=='motor_start' else -25)*t
                a=.15*a+np.sin(2*np.pi*f*t)*np.sin(np.pi*t/duration)**2
            a*=np.minimum(1,t/.012)*np.minimum(1,(t[-1]-t)/.045)
        target=.10 if name in ['silk','compost','converter','converter_2','converter_3','crystal_growth'] else .12
        a*=target/max(1e-9,np.sqrt(np.mean(a*a)))
        a=np.tanh(a/.6)*.6
        if loop:
            # Tiny endpoint correction, not a volume dip at the four-second loop seam.
            a[-128:]-=(a[-1]-a[0])*np.linspace(0,1,128)
        path=ROOT/f'sounds/machine/{name}.ogg'
        sf.write(path,a,RATE,format='OGG',subtype='VORBIS')
        decoded,actual_rate=sf.read(path)
        peak=float(np.max(np.abs(decoded)));rms=float(np.sqrt(np.mean(decoded**2)))
        assert peak<.65 and rms<.14,(name,peak,rms)
        definitions['machine.'+name]={'subtitle':'subtitles.revolib.machine.'+name,
            'sounds':[{'name':'revolib:machine/'+name,'attenuation_distance':12}]}
        subtitles['subtitles.revolib.machine.'+name]=LABELS[name]
        report.append({'voice':name,'loop':loop,'seconds':len(decoded)/actual_rate,
                       'peak_dbfs':round(20*np.log10(peak),2),'rms_dbfs':round(20*np.log10(rms),2),
                       'decoded_seam_step':round(float(abs(decoded[-1]-decoded[0])),6) if loop else None})
    # Every registered voice must have audio, and vice versa.
    source=(REPO/'revolib/src/main/java/dk/revo/revolib/MachineFeedback.java').read_text()
    enum=source.split('public enum Voice {',1)[1].split(';',1)[0]
    assert set(re.findall(r'\b[A-Z][A-Z_0-9]+\b',enum))=={n.upper() for n in list(LOOPS)+CUES}
    (ROOT/'sounds.json').write_text(json.dumps(definitions,indent=2)+'\n')
    lang=ROOT/'lang/en_us.json';data=json.loads(lang.read_text()) if lang.exists() else {}
    data.update(subtitles);lang.parent.mkdir(exist_ok=True);lang.write_text(json.dumps(data,indent=2)+'\n')
    (REPO/'docs/machine-audio-validation.json').write_text(json.dumps({'original_procedural_foley':True,
        'source':'scripts/build_machine_sounds.py','sound_source':'BLOCKS','default_machine_volume':.65,
        'maximum_nearby_loops':8,'crowd_mix':'1/sqrt(active loops)','voices':report},indent=2)+'\n')
    print(f'Built {len(LOOPS)} loops and {len(CUES)} quiet cues; decoded peaks/RMS validated.')

if __name__=='__main__':build()
