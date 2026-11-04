package dk.revo.revoachievements;

import dk.revo.revoachievements.client.*;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.client.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Real FTB claims, network equipment actions and world-reload QA, only in a copied named save. */
@EventBusSubscriber(modid=RevoAchievements.MOD_ID,value=Dist.CLIENT)
public final class AchievementsUiQa {
    static final String INTERN="revoachievements:title/unpaid_factory_intern",CAP="revoachievements:hat/capbackwards",CRANK="revoachievements:title/certified_crank_operator",MINER="revoachievements:hat/minerhat";
    static final boolean RELOAD=Boolean.getBoolean("revo.wardrobe.reloadqa");
    static boolean started,done;static volatile boolean ready;static volatile Throwable failure;static int stage,frames,timeout;
    static final Path DIR=Path.of("../../art/revoachievements/ingame").toAbsolutePath().normalize();
    static final String[] NAMES={"initial-titles","title-equipped","hats","hat-equipped","locked-hat-preview","quest-hat-unlocked","miner-hat-equipped","crank-title-equipped","third-person-hat","ftb-quest-rewards","hat-cleared","saved-loadout"};
    static List<ItemStack> inventoryBefore;
    static void check(boolean ok,String message){if(!ok)throw new IllegalStateException(message);}
    static void note(String message){try{Files.createDirectories(DIR);Files.writeString(DIR.resolve("qa-results.txt"),message+"\n",StandardOpenOption.CREATE,StandardOpenOption.APPEND);}catch(Exception e){throw new RuntimeException(e);}}
    static void server(Consumer<ServerPlayer> action){var mc=Minecraft.getInstance();var id=mc.player.getUUID();mc.getSingleplayerServer().execute(()->{try{action.accept(mc.getSingleplayerServer().getPlayerList().getPlayer(id));}catch(Throwable e){failure=e;note("FAIL: "+e);}});}
    static void click(String name){var mc=Minecraft.getInstance();var button=mc.screen.children().stream().filter(w->w instanceof Button b&&b.getMessage().getString().equals(name)).map(w->(Button)w).findFirst().orElseThrow();check(button.active,"Inactive button "+name);mc.screen.mouseClicked(button.getX()+button.getWidth()/2.,button.getY()+button.getHeight()/2.,0);}
    static void row(int index){var mc=Minecraft.getInstance();int w=Math.min(420,mc.screen.width-12),h=Math.min(274,mc.screen.height-12);mc.screen.mouseClicked((mc.screen.width-w)/2.+60,(mc.screen.height-h)/2.+82+index*24+10,0);}
    static void claim(ServerPlayer p,int n){
        var file=ServerQuestFile.INSTANCE;check(file!=null,"FTB server file missing");var quest=file.getQuest(0x5E70000000000000L+n);check(quest!=null,"Example quest did not load: "+n);
        var data=file.getOrCreateTeamData(p);for(var reward:quest.getRewards())data.resetReward(p.getUUID(),reward);
        for(var task:quest.getTasks()){data.resetProgress(task);data.setProgress(task,task.getMaxProgress());}
        for(var reward:quest.getRewards()){data.claimReward(p,reward,true);check(data.isRewardClaimed(p.getUUID(),reward),"Actual FTB reward was not claimable/claimed "+reward.id);}
    }
    static void init(){
        server(p->{
            check(p.getServer().getWorldPath(LevelResource.ROOT).toString().contains("Revo Wardrobe QA"),"Only the named copied QA save can be changed");
            check(CosmeticCatalog.all().size()==17,"Expected 9 titles and 8 real Simple Hats entries, got "+CosmeticCatalog.all().size());
            var data=WardrobeData.get(p.getServer());
            if(RELOAD){
                var state=data.profile(p.getUUID());check(state.title().equals(CRANK)&&state.hat().equals(MINER)&&state.owns(INTERN)&&state.owns(CAP),"Loadout/unlocks did not survive stopping and reopening the world");
                note("PASS: actual second client/world launch retained unlocks, equipped title and hat.");
            }else{
                try{Files.createDirectories(DIR);Files.writeString(DIR.resolve("qa-results.txt"),"");}catch(Exception e){throw new RuntimeException(e);}
                for(var id:data.profile(p.getUUID()).unlocked())data.revoke(p.getUUID(),id);
                inventoryBefore=new ArrayList<>();for(int i=0;i<p.getInventory().getContainerSize();i++)inventoryBefore.add(p.getInventory().getItem(i).copy());
                claim(p,1);var state=data.profile(p.getUUID());
                check(state.owns(INTERN)&&state.owns(CAP)&&state.title().isEmpty()&&state.hat().isEmpty(),"FTB claims must unlock two cosmetics without auto-equipping");
                for(int i=0;i<inventoryBefore.size();i++)check(ItemStack.matches(inventoryBefore.get(i),p.getInventory().getItem(i)),"FTB cosmetic reward changed physical inventory");
                note("PASS: real FTB task completion and reward-claim pipeline unlocked title + Simple Hats cosmetic; no items and no auto-equip.");
            }
            p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);p.teleportTo(10.5,128,-3);p.getAbilities().flying=true;p.onUpdateAbilities();ready=true;
            WardrobeService.sendOwn(p,true,"");
        });
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        if(failure!=null)throw new RuntimeException("Wardrobe QA server check failed",failure);
        var mc=Minecraft.getInstance();if(done||mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;
        if(!started){started=true;init();return;}if(!ready)return;
        if(++timeout>1600)throw new IllegalStateException("Wardrobe QA timed out at stage "+stage+" screen="+mc.screen);
        boolean expected=RELOAD?mc.screen instanceof WardrobeScreen:stage==8?mc.screen==null:stage==9?mc.screen!=null:mc.screen instanceof WardrobeScreen;
        if(!expected)return;if(frames==5)org.lwjgl.glfw.GLFW.glfwSetCursorPos(mc.getWindow().getWindow(),5,55);
        if(++frames<(stage==0?120:60))return;
        try{
            Files.createDirectories(DIR);String name=RELOAD?new String[]{"reloaded-title","reloaded-hat","keybinding-open","inventory-button-open","command-open"}[stage]:stage>=12?"final-persisted-loadout":NAMES[stage];
            try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(DIR.resolve(name+".png"));}
            if(RELOAD){switch(stage){
                case 0->click("Hats");
                case 1->{check(WardrobeClient.title.equals(CRANK)&&WardrobeClient.hat.equals(MINER),"Reloaded selection was not synchronized to client");note("PASS: reloaded wardrobe and native hat preview captured.");mc.setScreen(null);KeyMapping.click(WardrobeClientEvents.OPEN.getKey());}
                case 2->{note("PASS: real keybinding opened the wardrobe through a server request.");mc.setScreen(new net.minecraft.client.gui.screens.inventory.InventoryScreen(mc.player));click("R");}
                case 3->{mc.screen.onClose();check(mc.screen instanceof net.minecraft.client.gui.screens.inventory.InventoryScreen||mc.screen instanceof net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen,"Wardrobe Back failed to restore inventory");mc.setScreen(null);server(p->{try{check(p.getServer().getCommands().getDispatcher().execute("revoachievements wardrobe",p.createCommandSourceStack().withPermission(0))==1,"Non-op wardrobe command failed");}catch(Exception ex){throw new RuntimeException(ex);}});}
                case 4->{
                    var screen=(WardrobeScreen)mc.screen;click("All");check(screen.filtered().size()==2,"Owned-only filter failed");click("Owned");check(screen.filtered().size()==9,"All filter failed");click(">");click("<");
                    var search=screen.children().stream().filter(w->w instanceof net.minecraft.client.gui.components.EditBox).map(w->(net.minecraft.client.gui.components.EditBox)w).findFirst().orElseThrow();search.setValue("Crank");check(screen.filtered().size()==1,"Search failed");search.setValue("");
                    note("PASS: actual keybinding, inventory R button, Back navigation, non-op command, owned filter, paging and search.");done=true;mc.stop();
                }
            }}
            else switch(stage){
                case 0->{check(WardrobeClient.owned.containsAll(Set.of(INTERN,CAP)),"Private unlock sync missing");click("Equip");}
                case 1->{check(WardrobeClient.title.equals(INTERN),"Title equip packet did not persist");click("Hats");}
                case 2->click("Equip");
                case 3->{check(WardrobeClient.hat.equals(CAP),"Hat equip packet did not persist");row(1);note("PASS: independent title/hat equip actions through real UI buttons and server packets.");}
                case 4->{
                    check(WardrobeClient.hat.equals(CAP)&&!WardrobeClient.owned.contains(MINER),"Locked preview changed equipped state");
                    check(mc.screen.children().stream().anyMatch(w->w instanceof Button b&&b.getMessage().getString().equals("Locked")&&!b.active),"Locked preview must disable equip");
                    server(p->{claim(p,2);var profile=WardrobeData.get(p.getServer()).profile(p.getUUID());check(profile.owns(CRANK)&&profile.owns(MINER)&&profile.title().equals(INTERN)&&profile.hat().equals(CAP),"Further FTB rewards changed the chosen loadout");});
                }
                case 5->{check(WardrobeClient.owned.contains(MINER),"New FTB reward was not synchronized into open wardrobe");click("Equip");}
                case 6->{check(WardrobeClient.hat.equals(MINER),"Miner hat failed to equip");click("Titles");row(1);click("Equip");}
                case 7->{
                    check(WardrobeClient.title.equals(CRANK),"Crank title failed to equip");
                    server(p->{
                        var chat=sierra.thing.playernamestyler.util.ChatFormatUtil.formatChat(p,"Wardrobe test",sierra.thing.playernamestyler.PlayerNameStyler.nicknameManager).getString();
                        check(chat.contains("Certified Crank Operator")&&chat.contains("Wardrobe test"),"PlayerName Styler chat placeholder did not resolve");
                        check(p.getTabListDisplayName()!=null&&p.getTabListDisplayName().getString().contains("Certified Crank Operator"),"Title missing from tab-name formatter");
                        note("PASS: real PlayerName Styler chat and tab/name formatting resolve the equipped Revo title.");
                        for(int i=0;i<inventoryBefore.size();i++)check(ItemStack.matches(inventoryBefore.get(i),p.getInventory().getItem(i)),"Cosmetics modified physical equipment/inventory");
                    });
                    mc.screen.onClose();mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
                }
                case 8->{check(!WardrobeClient.hatFor(mc.player).isEmpty(),"Equipped native hat is missing for world renderer");note("PASS: world player hat layer rendered; armor/inventory remain unchanged.");mc.options.setCameraType(CameraType.FIRST_PERSON);ClientQuestFile.openBookToQuestObject(0x5E70000000000001L);}
                case 9->{mc.setScreen(null);WardrobeClient.request(0,"");}
                case 10->{click("Hats");click("Clear");}
                case 11->{
                    check(WardrobeClient.hat.isEmpty()&&WardrobeClient.title.equals(CRANK),"Clear hat affected title or failed");row(1);click("Equip");
                }
                case 12->{check(WardrobeClient.hat.equals(MINER),"Final equip not acknowledged");ready=false;server(p->{check(WardrobeData.get(p.getServer()).profile(p.getUUID()).hat().equals(MINER),"Final saved hat missing");p.serverLevel().getDataStorage().save();note("PASS: clear/re-equip preserved independent selections; save written for a second real client launch.");ready=true;});}
                case 13->{done=true;mc.stop();}
            }
            stage++;frames=0;timeout=0;
        }catch(Exception e){throw new RuntimeException(e);}
    }
}
