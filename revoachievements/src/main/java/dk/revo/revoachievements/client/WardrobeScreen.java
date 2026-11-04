package dk.revo.revoachievements.client;

import dk.revo.revoachievements.Cosmetic;
import dk.revo.revolib.client.VanillaMachineGui;
import java.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/** Independent cosmetic selector; previews never alter real equipment or the server's selection. */
public final class WardrobeScreen extends Screen {
    private final Screen parent;
    private Cosmetic.Kind kind=Cosmetic.Kind.TITLE;
    private String selected="",query="";private boolean onlyOwned;private int page,x,y,w,h,rows;
    private EditBox search;private Button equip,clear,previous,next,filter,titles,hats;
    private RemotePlayer preview;
    public WardrobeScreen(Screen parent){super(Component.literal("Revo Wardrobe"));this.parent=parent;}
    @Override protected void init(){
        w=Math.min(420,width-12);h=Math.min(274,height-12);x=(width-w)/2;y=(height-h)/2;rows=Math.max(1,(h-120)/24);
        int left=w-178,right=x+w-166;
        titles=addRenderableWidget(Button.builder(Component.literal("Titles"),b->{kind=Cosmetic.Kind.TITLE;selected="";page=0;refresh();}).bounds(x+10,y+31,78,20).build());
        hats=addRenderableWidget(Button.builder(Component.literal("Hats"),b->{kind=Cosmetic.Kind.HAT;selected="";page=0;refresh();}).bounds(x+92,y+31,70,20).build());
        filter=addRenderableWidget(Button.builder(Component.literal("All"),b->{onlyOwned=!onlyOwned;page=0;refresh();}).bounds(x+166,y+31,left-166+10,20).build());
        search=addRenderableWidget(new EditBox(font,x+10,y+58,left,18,Component.literal("Search cosmetics")));search.setHint(Component.literal("Search titles and hats"));search.setMaxLength(64);search.setValue(query);search.setResponder(value->{query=value;page=0;refresh();});
        previous=addRenderableWidget(Button.builder(Component.literal("<"),b->{page--;refresh();}).bounds(x+10,y+h-27,20,18).build());
        next=addRenderableWidget(Button.builder(Component.literal(">"),b->{page++;refresh();}).bounds(x+34,y+h-27,20,18).build());
        equip=addRenderableWidget(Button.builder(Component.literal("Equip"),b->{if(!selected.isEmpty())WardrobeClient.request(1,selected);}).bounds(right,y+h-48,74,18).build());
        clear=addRenderableWidget(Button.builder(Component.literal("Clear"),b->WardrobeClient.request(kind==Cosmetic.Kind.TITLE?2:3,"")).bounds(right+78,y+h-48,76,18).build());
        addRenderableWidget(Button.builder(Component.literal("Done"),b->onClose()).bounds(x+w-98,y+h-26,70,18).build());
        if(minecraft.player!=null&&minecraft.level!=null){
            preview=new RemotePlayer(minecraft.level,minecraft.player.getGameProfile());
            for(var slot:EquipmentSlot.values()){
                var stack=minecraft.player.getItemBySlot(slot);
                preview.setItemSlot(slot,slot==EquipmentSlot.HEAD&&!stack.isEmpty()&&BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals("simplehats")?ItemStack.EMPTY:stack.copy());
            }
        }
        refresh();
    }
    public List<Cosmetic> filtered(){String q=query.toLowerCase(Locale.ROOT);return WardrobeClient.catalogue.stream().filter(e->e.kind()==kind&&(!onlyOwned||WardrobeClient.owned.contains(e.id().toString()))&&(e.name().toLowerCase(Locale.ROOT).contains(q)||e.id().toString().contains(q))).toList();}
    public void refresh(){
        if(equip==null)return;var entries=filtered();int pages=Math.max(1,(entries.size()+rows-1)/rows);page=Math.clamp(page,0,pages-1);
        if(selected.isEmpty()||entries.stream().noneMatch(e->e.id().toString().equals(selected))){
            String worn=kind==Cosmetic.Kind.TITLE?WardrobeClient.title:WardrobeClient.hat;
            selected=entries.stream().anyMatch(e->e.id().toString().equals(worn))?worn:entries.isEmpty()?"":entries.getFirst().id().toString();
        }
        boolean owned=WardrobeClient.owned.contains(selected),wearing=selected.equals(kind==Cosmetic.Kind.TITLE?WardrobeClient.title:WardrobeClient.hat);
        equip.active=owned&&!wearing;equip.setMessage(Component.literal(wearing&&!selected.isEmpty()?"Equipped":owned?"Equip":"Locked"));
        clear.active=!(kind==Cosmetic.Kind.TITLE?WardrobeClient.title:WardrobeClient.hat).isEmpty();previous.active=page>0;next.active=page+1<pages;
        filter.setMessage(Component.literal(onlyOwned?"Owned":"All"));titles.active=kind!=Cosmetic.Kind.TITLE;hats.active=kind!=Cosmetic.Kind.HAT;
    }
    @Override public boolean mouseClicked(double mx,double my,int button){
        if(button==0&&mx>=x+10&&mx<x+w-168&&my>=y+82&&my<y+82+rows*24){
            int index=page*rows+(int)(my-y-82)/24;var entries=filtered();if(index<entries.size()){selected=entries.get(index).id().toString();refresh();return true;}
        }return super.mouseClicked(mx,my,button);
    }
    @Override public boolean mouseScrolled(double mx,double my,double horizontal,double vertical){if(mx>=x&&mx<x+w-168){page+=vertical>0?-1:1;refresh();return true;}return super.mouseScrolled(mx,my,horizontal,vertical);}
    // Screen.render() invokes renderBackground again in 1.21. Draw it explicitly
    // before our panel, then keep the widget pass from blurring the finished UI.
    @Override public void renderBackground(GuiGraphics g,int mx,int my,float partial){}
    @Override public void render(GuiGraphics g,int mx,int my,float partial){
        super.renderBackground(g,mx,my,partial);VanillaMachineGui.panel(g,x,y,w,h);
        g.drawString(font,title,x+10,y+9,0x353B2F,false);g.drawString(font,"Your earned titles and hats",x+10,y+21,0x626957,false);
        int left=w-178,right=x+w-166;var entries=filtered();Cosmetic hovered=null;
        for(int i=0;i<rows&&page*rows+i<entries.size();i++){
            var e=entries.get(page*rows+i);String id=e.id().toString();int rowY=y+82+i*24;boolean own=WardrobeClient.owned.contains(id),wearing=id.equals(kind==Cosmetic.Kind.TITLE?WardrobeClient.title:WardrobeClient.hat);
            boolean hover=mx>=x+10&&mx<x+10+left&&my>=rowY&&my<rowY+23;if(hover)hovered=e;
            g.fill(x+10,rowY,x+10+left,rowY+23,id.equals(selected)?0xFF899880:hover?0xFFABB0A1:0xFFB4B7AE);
            g.renderItem(e.icon(),x+13,rowY+3);g.drawString(font,font.plainSubstrByWidth(e.name(),left-31),x+34,rowY+3,own?0x28342A:0x60645E,false);
            g.drawString(font,wearing?"Equipped":own?"Unlocked":"Locked",x+34,rowY+13,wearing?0x375D2B:0x6A6D64,false);
        }
        if(entries.isEmpty())g.drawWordWrap(font,Component.literal(onlyOwned?"No unlocked cosmetics in this category yet.":"No matching cosmetics."),x+14,y+90,left-8,0x555C50);
        g.fill(right,y+52,right+154,y+157,0xFF333B3A);
        var selectedEntry=WardrobeClient.entry(selected);
        if(preview!=null){
            var hat=kind==Cosmetic.Kind.HAT?selectedEntry:WardrobeClient.entry(WardrobeClient.hat);
            WardrobeClient.previewEntity=preview;WardrobeClient.previewHat=hat==null?ItemStack.EMPTY:hat.icon();
            try{InventoryScreen.renderEntityInInventoryFollowsMouse(g,right+6,y+53,right+148,y+155,43,.0625f,mx,my,preview);}
            finally{WardrobeClient.previewEntity=null;WardrobeClient.previewHat=ItemStack.EMPTY;}
        }
        if(selectedEntry!=null){
            g.drawWordWrap(font,Component.literal(selectedEntry.name()).withColor(selectedEntry.kind()==Cosmetic.Kind.TITLE?selectedEntry.color():0x353B2F),right,y+163,154,0x353B2F);
            g.drawString(font,WardrobeClient.owned.contains(selected)?"Unlocked":"Locked reward",right,y+186,0x626957,false);
            var lines=font.split(Component.literal(selectedEntry.hint()),154);for(int i=0;i<Math.min(2,lines.size());i++)g.drawString(font,lines.get(i),right,y+199+i*10,0x626957,false);
        }
        int pages=Math.max(1,(entries.size()+rows-1)/rows);g.drawString(font,(page+1)+" / "+pages+"  |  "+WardrobeClient.owned.size()+" unlocked",x+62,y+h-22,0x555C50,false);
        g.drawString(font,"R",x+w-16,y+h-18,0x000000,false);
        super.render(g,mx,my,partial);
        if(!WardrobeClient.error.isEmpty())g.drawString(font,font.plainSubstrByWidth(WardrobeClient.error,w-20),x+10,y+h-38,0x8B3932,false);
        if(hovered!=null)g.renderComponentTooltip(font,List.of(Component.literal(hovered.name()).withColor(hovered.color()),Component.literal(hovered.description()),Component.literal(hovered.hint())),mx,my);
    }
    @Override public void onClose(){minecraft.setScreen(parent);}
    @Override public boolean isPauseScreen(){return false;}
}
