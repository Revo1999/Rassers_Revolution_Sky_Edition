package dk.revo.revostorage;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
public class StorageScreen extends AbstractContainerScreen<StorageMenu> {
 private EditBox search;
 private final java.util.List<Button> terminalButtons=new java.util.ArrayList<>();
 public StorageScreen(StorageMenu menu,Inventory inv,Component title){super(menu,inv,title);imageWidth=212;imageHeight=300;inventoryLabelY=204;inventoryLabelX=25;titleLabelX=9;titleLabelY=8;}
 protected void init(){super.init();terminalButtons.clear();search=new EditBox(font,leftPos+101,topPos+6,102,16,Component.literal("Search storage"));search.setMaxLength(120);search.setBordered(true);search.setValue(menu.search);search.setResponder(v->PacketDistributor.sendToServer(new StorageAction("search",v,false)));search.setHint(Component.literal("Search..."));addWidget(search);
  button(5,28,16,18,"A",2,"Sort by name / amount");button(5,51,16,18,"<",0,"Previous page");button(191,51,16,18,">",1,"Next page");button(5,146,54,18,"Wind",3,"Wind spring: +256 crafting operations");button(135,184,68,18,"Clear",4,"Return crafting ingredients to storage");
 }
 private void button(int x,int y,int w,int h,String label,int id,String tip){terminalButtons.add(addRenderableWidget(Button.builder(Component.literal(label),b->minecraft.gameMode.handleInventoryButtonClick(menu.containerId,id)).bounds(leftPos+x,topPos+y,w,h).tooltip(Tooltip.create(Component.literal(tip))).build()));}
 protected void renderBg(GuiGraphics g,float partial,int mx,int my){int x=leftPos,y=topPos;g.fill(x-1,y-1,x+imageWidth+1,y+imageHeight+1,0xff181a21);g.fill(x,y,x+imageWidth,y+imageHeight,0xffc5c5ce);g.fill(x+2,y+2,x+imageWidth-2,y+imageHeight-2,0xffa9a9b3);g.fill(x+4,y+23,x+imageWidth-4,y+139,0xff73737f);g.fill(x+22,y+25,x+190,y+137,0xff353540);g.fill(x+4,y+140,x+208,y+201,0xffbcbcc6);g.fill(x+4,y+202,x+208,y+298,0xffc6c6ce);
  for(var slot:menu.slots){if(!slot.isActive()||(!menu.terminal()&&slot.index<10)||(!menu.terminal()&&slot.index>=46&&slot.index<100))continue;if(menu.terminal()&&slot.index>=100)continue;int sx=x+slot.x-1,sy=y+slot.y-1;g.fill(sx,sy,sx+18,sy+18,0xff454550);g.fill(sx+1,sy+1,sx+17,sy+17,0xff858591);g.fill(sx+17,sy+1,sx+18,sy+18,0xffeeeeef);g.fill(sx+1,sy+17,sx+18,sy+18,0xffeeeeef);}
  if(menu.terminal()){g.drawString(font,"→",x+141,y+168,0xff474750,false);g.drawString(font,menu.data.get(1)+" spring",x+7,y+170,0xff454550,false);g.drawString(font,"Page "+(menu.data.get(3)+1),x+144,y+141,0xff454550,false);}else{g.drawCenteredString(font,"Install empty chests",x+106,y+35,0xffeeeeff);g.drawCenteredString(font,"9 bays · Iron Chests supported",x+106,y+121,0xffeeeeff);g.drawString(font,"Empty a bay before removing",x+106-font.width("Empty a bay before removing")/2,y+153,0xff454550,false);g.drawString(font,"its chest.",x+106-font.width("its chest.")/2,y+165,0xff454550,false);}
 }
 protected void renderLabels(GuiGraphics g,int mx,int my){g.drawString(font,menu.terminal()?"Crafting Terminal":"Chest Drive",8,8,0xff34343f,false);g.drawString(font,"Inventory",25,204,0xff454550,false);g.drawString(font,menu.data.get(2)+" storage slots",25,291,0xff454550,false);}
 public void render(GuiGraphics g,int mx,int my,float partial){search.visible=menu.terminal();for(var button:terminalButtons)button.visible=menu.terminal();super.render(g,mx,my,partial);if(menu.terminal())search.render(g,mx,my,partial);renderTooltip(g,mx,my);}
 public void setSearchText(String text){search.setValue(text);}
 public boolean mouseScrolled(double x,double y,double dx,double dy){if(x>=leftPos+22&&x<leftPos+190&&y>=topPos+25&&y<topPos+138){minecraft.gameMode.handleInventoryButtonClick(menu.containerId,dy>0?0:1);return true;}return super.mouseScrolled(x,y,dx,dy);}
 public boolean keyPressed(int key,int scan,int mods){if(search.isFocused()&&key!=256)return search.keyPressed(key,scan,mods)||search.canConsumeInput();return super.keyPressed(key,scan,mods);}
}
