package dk.revo.revolib.client.guide.emi;

import java.util.*;
import java.util.function.*;
import dk.revo.revolib.client.guide.*;
import dev.emi.emi.api.*;
import dev.emi.emi.api.recipe.*;
import dev.emi.emi.api.stack.*;
import dev.emi.emi.api.widget.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/** Shared EMI entry: real blocks, layers, port filters, ingredient slots and a large guide. */
public class MultiblockEmiRecipe implements EmiRecipe {
    private final EmiRecipeCategory category;
    private final ResourceLocation id;
    public final MultiblockGuide guide;
    private final EmiStack controller;
    public MultiblockEmiRecipe(EmiRecipeCategory category,ResourceLocation id,MultiblockGuide guide,EmiStack controller){this.category=category;this.id=id;this.guide=guide;this.controller=controller;}
    @Override public EmiRecipeCategory getCategory(){return category;}
    @Override public ResourceLocation getId(){return id;}
    @Override public List<EmiIngredient> getInputs(){return guide.materials().stream().map(s->(EmiIngredient)EmiStack.of(s)).toList();}
    @Override public List<EmiStack> getOutputs(){return List.of(controller);}
    @Override public int getDisplayWidth(){return 300;}
    @Override public int getDisplayHeight(){return 160;}
    @Override public boolean supportsRecipeTree(){return false;}
    public MultiblockGuideScreen openGuide(MultiblockPreview preview){
        var mc=Minecraft.getInstance();var screen=new MultiblockGuideScreen(mc.screen,preview,()->EmiApi.displayRecipes(controller));mc.setScreen(screen);return screen;
    }
    @Override public void addWidgets(WidgetHolder w){
        var preview=new MultiblockPreview(guide);
        w.add(new Widget(){
            private boolean hovered;
            public Bounds getBounds(){return new Bounds(4,4,178,90);}
            public void render(GuiGraphics g,int mx,int my,float partial){hovered=getBounds().contains(mx,my);preview.render(g,4,4,178,90,mx,my);}
            public List<ClientTooltipComponent> getTooltip(int mx,int my){return preview.tooltip().stream().map(c->ClientTooltipComponent.create(c.getVisualOrderText())).toList();}
            public boolean mouseClicked(int mx,int my,int button){if(getBounds().contains(mx,my)){preview.click(mx,my,button);return true;}return false;}
            public boolean keyPressed(int key,int scan,int modifiers){
                if(!hovered)return false;
                if(key==82){preview.reset();return true;}if(key==72){preview.cycleHighlight();return true;}
                if(key==61||key==334){preview.zoom(1);return true;}if(key==45||key==333){preview.zoom(-1);return true;}
                return false;
            }
        });
        w.addText(Component.literal("Materials"),190,4,0x404040,false);
        var materials=guide.materials();
        for(int i=0;i<materials.size();i++)w.addSlot(EmiStack.of(materials.get(i)),190+i%5*18,18+i/5*18);
        w.addText(Component.literal(guide.sizeX()+" x "+guide.layers()+" x "+guide.sizeZ()+" | "+guide.blocks()+" blocks"),190,62,0x404040,false);
        action(w,190,76,106,()->"Open 3D guide",()->openGuide(preview));
        action(w,190,98,106,preview::highlightLabel,preview::cycleHighlight);
        w.addDrawable(4,99,180,11,(g,mx,my,partial)->g.drawString(Minecraft.getInstance().font,"Layer "+(preview.layer()+1)+" / "+guide.layers()+" | "+(preview.single()?"single":"build up"),0,0,0xFF404040,false));
        action(w,4,113,16,()->"<",()->preview.layer(-1));action(w,22,113,16,()->">",()->preview.layer(1));
        action(w,40,113,68,()->preview.single()?"Single layer":"Build up",preview::toggleSingle);
        action(w,110,113,16,()->"-",()->preview.zoom(-1));action(w,128,113,16,()->"+",()->preview.zoom(1));action(w,146,113,36,()->"Reset",preview::reset);
        w.addDrawable(4,136,280,21,(g,mx,my,partial)->g.drawWordWrap(Minecraft.getInstance().font,guide.layerSteps().get(preview.layer()),0,0,280,0xFF404040));
        w.addText(Component.literal("R"),288,148,0x000000,false);
    }
    private static void action(WidgetHolder w,int x,int y,int width,Supplier<String> label,Runnable run){
        w.add(new Widget(){
            public Bounds getBounds(){return new Bounds(x,y,width,18);}
            public void render(GuiGraphics g,int mx,int my,float partial){
                boolean hover=getBounds().contains(mx,my);g.fill(x,y,x+width,y+18,0xFF50554F);g.fill(x+1,y+1,x+width-1,y+17,hover?0xFFDCDDD2:0xFFC1C3B7);
                var font=Minecraft.getInstance().font;g.drawString(font,label.get(),x+(width-font.width(label.get()))/2,y+5,0xFF252C25,false);
            }
            public boolean mouseClicked(int mx,int my,int button){if(button==0&&getBounds().contains(mx,my)){run.run();return true;}return false;}
        });
    }
}
