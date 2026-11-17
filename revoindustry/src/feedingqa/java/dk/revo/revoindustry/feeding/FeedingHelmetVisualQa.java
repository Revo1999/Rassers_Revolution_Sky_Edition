package dk.revo.revoindustry.feeding;

import java.nio.file.*;
import java.util.function.Consumer;
import net.minecraft.client.*;
import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Only loaded by -PrevoFeedingQa and refuses every save except its isolated copy. */
@EventBusSubscriber(modid="revoindustry",value=Dist.CLIENT)
public final class FeedingHelmetVisualQa {
    private static boolean started,done;
    private static volatile boolean ready,verified;
    private static int frame,stage;
    private static volatile int feedingStart=-1;
    private static final Path DIR=Path.of("../../screenshots/feeding-helmets").toAbsolutePath().normalize();
    private static void server(Consumer<ServerPlayer> action) {
        var mc=Minecraft.getInstance();
        mc.getSingleplayerServer().execute(() -> {
            var player=mc.getSingleplayerServer().getPlayerList().getPlayer(mc.player.getUUID());
            action.accept(player);
        });
    }
    private static ItemStack helmet(int tier){return new ItemStack(tier==0?FeedingHelmets.BASIC.get():tier==1?FeedingHelmets.ADVANCED.get():FeedingHelmets.ELITE.get());}
    private static void prepare(){server(p->{
        if(!p.serverLevel().getServer().getWorldPath(LevelResource.ROOT).toString().contains("Revo Feeding Helmet QA"))throw new IllegalStateException("Isolated feeding helmet QA save required");
        var l=p.serverLevel();l.getServer().setDifficulty(Difficulty.NORMAL,true);p.closeContainer();p.setGameMode(GameType.CREATIVE);p.getInventory().clearContent();
        l.setDayTime(1000);l.setWeatherParameters(100000,0,false,false);l.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,l.getServer());
        for(int x=90;x<=110;x++)for(int z=88;z<=108;z++)for(int y=179;y<=184;y++)l.setBlockAndUpdate(new BlockPos(x,y,z),y==179?Blocks.SMOOTH_STONE.defaultBlockState():Blocks.AIR.defaultBlockState());
        l.getEntitiesOfClass(net.minecraft.world.entity.Mob.class,new net.minecraft.world.phys.AABB(90,179,88,111,185,109)).forEach(Entity::discard);
        for(int tier=0;tier<3;tier++){
            var mob=EntityType.ZOMBIE.create(l);mob.setNoAi(true);mob.setSilent(true);mob.setInvulnerable(true);mob.setPersistenceRequired();
            mob.moveTo(97+tier*3,180,102,180,0);mob.setYHeadRot(180);mob.setYBodyRot(180);mob.setItemSlot(EquipmentSlot.HEAD,helmet(tier));l.addFreshEntity(mob);
        }
        p.connection.teleport(100,182.3,91,0,0);ready=true;
    });}
    private static void open(int tier){server(p->{p.closeContainer();var stack=helmet(tier);var items=FeedingHelmets.contents(stack,tier);for(int i=0;i<items.size();i++)items.set(i,new ItemStack(i%2==0?Items.CARROT:Items.BREAD,i+1));FeedingHelmets.save(stack,items);p.getInventory().setItem(0,stack);p.getInventory().selected=0;FeedingHelmets.open(p,0);});}
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        var mc=Minecraft.getInstance();if(done||mc.player==null||mc.level==null||mc.getSingleplayerServer()==null)return;
        if(!started){started=true;mc.options.pauseOnLostFocus=false;mc.options.hideGui=true;mc.options.fov().set(48);prepare();return;}
        if(!ready)return;if(mc.screen instanceof net.minecraft.client.gui.screens.PauseScreen)mc.setScreen(null);
        if(stage>=5){if(verified){done=true;mc.options.hideGui=false;mc.stop();}return;}
        if(stage==0)mc.player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,new Vec3(100,181.3,102));
        if(stage==0&&mc.level.getEntitiesOfClass(net.minecraft.world.entity.monster.Zombie.class,new net.minecraft.world.phys.AABB(93,179,99,107,184,105)).size()<3)return;
        if(stage>0&&stage<4&&!(mc.screen instanceof FeedingHelmetClient.Screen)){if(++frame>300)throw new IllegalStateException("Helmet menu did not open");return;}
        if(++frame<(stage==0?100:45))return;
        try{
            Files.createDirectories(DIR);
            if(stage==0){try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(DIR.resolve("three-tiers-worn.png"));}mc.options.hideGui=false;open(0);}
            else if(stage<4){
                var screen=(FeedingHelmetClient.Screen)mc.screen;int tier=stage-1;
                if(screen.getMenu().foodSlots!=new int[]{1,4,25}[tier]||screen.getMenu().slots.get(0).getItem().isEmpty())throw new IllegalStateException("Menu capacity or food synchronization failed");
                try(var image=Screenshot.takeScreenshot(mc.getMainRenderTarget())){image.writeToFile(DIR.resolve("tier-"+(tier+1)+"-inventory.png"));}
                if(stage<3)open(tier+1);else server(p->{
                    p.closeContainer();p.setGameMode(GameType.SURVIVAL);var stack=helmet(0);var food=FeedingHelmets.contents(stack,0);food.set(0,new ItemStack(Items.CARROT,2));FeedingHelmets.save(stack,food);p.setItemSlot(EquipmentSlot.HEAD,stack);p.getFoodData().setFoodLevel(17);p.getFoodData().setSaturation(0);feedingStart=p.serverLevel().getServer().getTickCount();
                });
            }else{
                // Proof waits for actual server ticks, independently of screenshot/UI timing.
            }
            stage++;frame=0;
        }catch(Exception ex){throw new RuntimeException(ex);}
    }
    @SubscribeEvent public static void serverTick(net.neoforged.neoforge.event.tick.ServerTickEvent.Post event) {
        if(feedingStart<0||verified||event.getServer().getTickCount()-feedingStart<40)return;
        var mc=Minecraft.getInstance();var p=event.getServer().getPlayerList().getPlayer(mc.player.getUUID());
        try{var stack=p.getItemBySlot(EquipmentSlot.HEAD);int count=FeedingHelmets.contents(stack,0).get(0).getCount();
            if(p.getFoodData().getFoodLevel()!=20||count!=1)throw new IllegalStateException("Worn feeding proof: hunger="+p.getFoodData().getFoodLevel()+", carrots="+count+", playerTicks="+p.tickCount);
            Files.writeString(DIR.resolve("result.txt"),"PASS: all three armor models rendered, 1/4/25-slot screens synchronized, equipped survival helmet ate exactly one 3-point carrot at 17 hunger.\n");verified=true;
        }catch(Exception ex){throw new RuntimeException(ex);}
    }
}
