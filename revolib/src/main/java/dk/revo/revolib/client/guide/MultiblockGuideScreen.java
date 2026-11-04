package dk.revo.revolib.client.guide;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Full-size construction guide shared by Revo machines; caller supplies the EMI recipe action. */
public final class MultiblockGuideScreen extends Screen {
    private final Screen previous;
    public final MultiblockPreview preview;
    private final Runnable recipes;
    private int x,y,w,h,sceneW,sceneH;
    public MultiblockGuideScreen(Screen previous,MultiblockPreview preview,Runnable recipes){super(preview.guide.title());this.previous=previous;this.preview=preview;this.recipes=recipes;}
    private void button(String text,int bx,int by,int width,Runnable action){addRenderableWidget(Button.builder(Component.literal(text),b->{action.run();rebuildWidgets();}).bounds(bx,by,width,18).build());}
    @Override protected void init(){
        w=Math.min(width-16,560);h=Math.min(height-16,340);x=(width-w)/2;y=(height-h)/2;sceneW=(int)(w*.55)-15;sceneH=h-124;
        int controls=y+h-69;
        button("<",x+10,controls,20,()->preview.layer(-1));button(">",x+34,controls,20,()->preview.layer(1));
        button(preview.single()?"Single layer":"Build up",x+58,controls,82,preview::toggleSingle);
        button("-",x+144,controls,20,()->preview.zoom(-1));button("+",x+168,controls,20,()->preview.zoom(1));
        button("Reset",x+192,controls,46,preview::reset);
        int right=x+sceneW+20;
        button(preview.highlightLabel(),right,y+132,w-sceneW-30,preview::cycleHighlight);
        button("EMI recipes",x+10,y+h-25,85,recipes);
        button("Back",x+99,y+h-25,65,this::onClose);
    }
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        super.renderBackground(g,mx,my,partial);
        g.fill(x-1,y-1,x+w+1,y+h+1,0xFF363A39);g.fill(x,y,x+w,y+h,0xFFC6C5BB);
        g.drawString(font,title,x+10,y+10,0xFF30372F,false);
        preview.render(g,x+10,y+28,sceneW,sceneH,mx,my);
        var guide=preview.guide;int right=x+sceneW+20,rw=w-sceneW-30;
        g.drawString(font,guide.sizeX()+" x "+guide.layers()+" x "+guide.sizeZ()+" | "+guide.blocks()+" blocks",right,y+29,0xFF404238,false);
        g.drawWordWrap(font,guide.power(),right,y+43,rw,0xFF555C51);
        g.drawString(font,"Materials (entire structure)",right,y+69,0xFF404238,false);
        var materials=guide.materials();int cols=Math.max(1,rw/20);
        for(int i=0;i<materials.size();i++){
            int ix=right+i%cols*20,iy=y+83+i/cols*20;
            g.renderItem(materials.get(i),ix,iy);g.renderItemDecorations(font,materials.get(i),ix,iy);
            if(mx>=ix&&mx<ix+18&&my>=iy&&my<iy+18)g.renderTooltip(font,materials.get(i),mx,my);
        }
        g.drawString(font,"How to build: layer "+(preview.layer()+1)+" / "+guide.layers(),right,y+158,0xFF404238,false);
        g.drawWordWrap(font,guide.layerSteps().get(preview.layer()),right,y+172,rw,0xFF555C51);
        g.drawString(font,"Layer "+(preview.layer()+1)+" / "+guide.layers()+" | drag: rotate, scroll: zoom",x+10,y+h-88,0xFF555C51,false);
        g.drawString(font,"R",x+w-16,y+h-16,0xFF000000,false);
        super.render(g,mx,my,partial);
        if(inScene(mx,my))g.renderComponentTooltip(font,preview.tooltip(),mx,my);
    }
    // Screen.render invokes renderBackground again before widgets; keep the single blur above.
    @Override public void renderBackground(GuiGraphics g,int mx,int my,float partial){}
    private boolean inScene(double mx,double my){return mx>=x+10&&mx<x+10+sceneW&&my>=y+28&&my<y+28+sceneH;}
    @Override public boolean mouseClicked(double mx,double my,int button){if(inScene(mx,my)){preview.click(mx,my,button);return true;}return super.mouseClicked(mx,my,button);}
    @Override public boolean mouseScrolled(double mx,double my,double dx,double dy){if(inScene(mx,my)){preview.zoom(dy);return true;}return super.mouseScrolled(mx,my,dx,dy);}
    @Override public boolean keyPressed(int key,int scan,int modifiers){
        if(key==82){preview.reset();return true;}if(key==72){preview.cycleHighlight();rebuildWidgets();return true;}
        if(key==262){preview.layer(1);return true;}if(key==263){preview.layer(-1);return true;}
        return super.keyPressed(key,scan,modifiers);
    }
    @Override public void onClose(){minecraft.setScreen(previous);}
    @Override public boolean isPauseScreen(){return false;}
}
