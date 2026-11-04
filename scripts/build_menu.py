"""Build a native FancyMenu 3 layout from its editor-generated schema."""
from pathlib import Path
import re

ROOT=Path(__file__).resolve().parents[1]
CONFIG=ROOT/'pack/overrides/config/fancymenu'
TEMPLATES=ROOT/'scripts/templates'

def fields(body): return dict(re.findall(r'^  ([^=\n]+?) = (.*)$',body,re.M))
def update(body, values):
    for key,value in values.items():
        value=str(value).lower() if isinstance(value,bool) else str(value)
        pattern=r'(?m)^  '+re.escape(key)+r' = .*$'
        if re.search(pattern,body):body=re.sub(pattern,lambda _:f'  {key} = {value}',body)
        else:body+='\n  '+key+' = '+value
    return body

def main():
    source=(TEMPLATES/'fancymenu_layout.txt').read_text(encoding='utf-8')
    blocks=re.findall(r'(?m)^(\w+) \{\n(.*?)^\}',source,re.S)
    out=[]
    for kind,body in blocks:
        f=fields(body)
        if kind=='element':continue
        if kind=='vanilla_button':
            if f.get('instance_identifier') not in ['minecraft_logo_widget','minecraft_splash_widget']:continue
            body=update(body,{'is_hidden':True})
        if kind=='layout-meta':body=update(body,{'identifier':'title_screen','is_enabled':True})
        if kind=='menu_background':
            mode=f.get('background_type')
            if mode=='video':body=update(body,{'show_background':True,'loop':True,'volume':0.0,
                      'source':'[source:local]/config/fancymenu/assets/skyblock_loop.mp4','play_in_editor':False})
            elif mode=='image':body=update(body,{'show_background':True,
                      'source':'[source:local]/config/fancymenu/assets/skyblock_poster.png'})
        if kind=='customization':body=update(body,{'keepaspectratio':True})
        out.append((kind,body))
    out.append(('element','''  element_type = image
  instance_identifier = revo_pack_logo
  source = [source:local]/config/fancymenu/assets/revo_logo.png
  anchor_point = top-centered
  x = -95
  y = 8
  width = 190
  height = 76
  stay_on_screen = true
  appearance_delay = no_delay
  disappearance_delay = no_delay
  fade_in_v2 = no_fading
  fade_out = no_fading
  base_opacity = 1.0
  repeat_texture = false
  nine_slice_texture = false
  image_tint = #FFFFFFFF'''))
    path=CONFIG/'customization/title_screen_layout.txt';path.parent.mkdir(parents=True,exist_ok=True)
    path.write_text('type = fancymenu_layout\n\n'+'\n\n'.join(k+' {\n'+b+'\n}' for k,b in out)+'\n',encoding='utf-8')
    (CONFIG/'customizablemenus.txt').write_text('type = customizablemenus\n\nnet.minecraft.client.gui.screens.TitleScreen {\n}\n',encoding='utf-8')
    options=(TEMPLATES/'fancymenu_options.txt').read_text(encoding='utf-8')
    # Authoring configuration requested by the pack owner: leave the editor accessible.
    window={
        'show_custom_window_icon':('B','true'),
        'custom_window_icon_16':('S','/config/fancymenu/assets/icons/revo_16.png'),
        'custom_window_icon_32':('S','/config/fancymenu/assets/icons/revo_32.png'),
        'custom_window_icon_macos':('S','/config/fancymenu/assets/icons/revo.icns'),
        'custom_window_title':('S','Rassers Revolution: Sky Edition'),
    }
    for key,(kind,value) in window.items():
        options,n=re.subn(r"(?m)^"+kind+':'+key+r" = '[^']*';$",lambda _:kind+':'+key+" = '"+value+"';",options)
        if n!=1:raise ValueError('Missing FancyMenu setting: '+key)
    (CONFIG/'options.txt').write_text(options,encoding='utf-8')
    print('Built the supplied logo image; hid vanilla logo and splash, preserving other widgets.')

if __name__=='__main__':main()
