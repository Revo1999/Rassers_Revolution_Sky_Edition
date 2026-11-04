package dk.revo.revolib.client;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import java.io.*;
import java.nio.*;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

/** Opt-in, real-time capture tooling; excluded from normal builds. */
@EventBusSubscriber(modid="revolib", value=Dist.CLIENT)
public final class CinematicCapture {
    private static final int FPS=30, FRAMES=600;
    private static ArmorStand camera;
    private static BlockPos spawn;
    private static long readyAt, started;
    private static int frame, samples, repeated, previousFov;
    private static boolean done, previousPause, previousBob, previousGui;
    private static GameType previousMode;
    private static Process encoder;
    private static OutputStream pipe;
    private static byte[] rgba;
    private static Path output;

    @SubscribeEvent
    public static void before(RenderFrameEvent.Pre event) throws Exception {
        Minecraft mc=Minecraft.getInstance();
        if(done || System.getProperty("revo.capture")==null || mc.level==null || mc.player==null) return;
        if(camera==null) {
            output=Path.of(System.getProperty("revo.capture"));Files.createDirectories(output);
            Files.deleteIfExists(output.resolve("complete.txt"));
            spawn=mc.level.getSharedSpawnPos();
            camera=new ArmorStand(mc.level,spawn.getX(),spawn.getY(),spawn.getZ());
            previousMode=mc.gameMode.getPlayerMode();
            previousFov=mc.options.fov().get();previousPause=mc.options.pauseOnLostFocus;
            previousBob=mc.options.bobView().get();previousGui=mc.options.hideGui;
            mc.options.fov().set(60);mc.options.pauseOnLostFocus=false;mc.options.bobView().set(false);
            readyAt=System.nanoTime()+15_000_000_000L;
            var id=mc.player.getUUID();var server=mc.getSingleplayerServer();
            if(server!=null) server.execute(()->{
                var world=server.overworld();var player=server.getPlayerList().getPlayer(id);
                // Spectator hides the player and its shadow; no source-image retouching.
                if(player!=null) player.setGameMode(GameType.SPECTATOR);
                world.setDayTime(10500);world.setWeatherParameters(120000,0,false,false);
                world.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(false,server);
                world.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(false,server);
                world.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,server);
            });
        }
        if(mc.screen!=null || System.nanoTime()<readyAt || !mc.player.isSpectator()) return;
        if(mc.level.getBlockState(spawn.below()).isAir()) return;
        if(started==0) {
            String executable=Files.readString(output.resolve("ffmpeg-path.txt")).trim();
            int width=mc.getMainRenderTarget().width,height=mc.getMainRenderTarget().height;
            encoder=new ProcessBuilder(executable,"-y","-f","rawvideo","-pixel_format","rgba",
                "-video_size",width+"x"+height,"-framerate",String.valueOf(FPS),"-i","pipe:0",
                "-an","-c:v","libx264","-preset","veryfast","-crf","18","-pix_fmt","yuv420p",
                "-movflags","+faststart",output.resolve("minecraft-realtime.mp4").toString())
                .redirectError(output.resolve("encoder.log").toFile()).start();
            pipe=new BufferedOutputStream(encoder.getOutputStream(),8*1024*1024);
            rgba=new byte[width*height*4];started=System.nanoTime();
            Files.writeString(output.resolve("spawn-audit.txt"),"spawn="+spawn+" floor="+
                mc.level.getBlockState(spawn.below())+" spectator="+mc.player.isSpectator()+"\n");
        }
        mc.options.hideGui=true;mc.setCameraEntity(camera);
        double seconds=(System.nanoTime()-started)/1_000_000_000.0;
        double angle=2*Math.PI*Math.min(seconds/(FRAMES/(double)FPS),1);
        double cx=spawn.getX()-2,cy=spawn.getY()+1,cz=spawn.getZ()-3;
        double x=cx+19*Math.sin(angle),y=cy+8,z=cz+19*Math.cos(angle);
        float yaw=(float)Math.toDegrees(Math.atan2(cz-z,cx-x))-90;
        float pitch=(float)Math.toDegrees(Math.atan2(y+camera.getEyeHeight()-cy,19));
        camera.setPos(x,y,z);camera.xo=x;camera.yo=y;camera.zo=z;
        camera.xOld=x;camera.yOld=y;camera.zOld=z;
        camera.setYRot(yaw);camera.yRotO=yaw;camera.setXRot(pitch);camera.xRotO=pitch;
        camera.setYHeadRot(yaw);camera.yHeadRotO=yaw;camera.yBodyRot=yaw;camera.yBodyRotO=yaw;
    }

    @SubscribeEvent
    public static void after(RenderFrameEvent.Post event) throws Exception {
        Minecraft mc=Minecraft.getInstance();
        if(done || started==0 || mc.screen!=null) return;
        long elapsed=System.nanoTime()-started;
        int due=Math.min(FRAMES,(int)(elapsed*FPS/1_000_000_000L)+1);
        if(due<=frame) return;
        try(NativeImage image=Screenshot.takeScreenshot(mc.getMainRenderTarget())) {
            ByteBuffer.wrap(rgba).order(ByteOrder.LITTLE_ENDIAN).asIntBuffer().put(image.getPixelsRGBA());
        }
        samples++;repeated+=Math.max(0,due-frame-1);
        while(frame<due) {pipe.write(rgba);frame++;}
        if(frame>=FRAMES) {
            pipe.close();
            if(!encoder.waitFor(20,TimeUnit.SECONDS) || encoder.exitValue()!=0) throw new IOException("Video encoding failed");
            done=true;mc.setCameraEntity(mc.player);mc.options.hideGui=previousGui;
            mc.options.fov().set(previousFov);mc.options.pauseOnLostFocus=previousPause;mc.options.bobView().set(previousBob);
            var id=mc.player.getUUID();var server=mc.getSingleplayerServer();
            if(server!=null) server.execute(()->{var player=server.getPlayerList().getPlayer(id);if(player!=null)player.setGameMode(previousMode);});
            String audit="frames="+frame+" fps="+FPS+" wallSeconds="+(elapsed/1_000_000_000.0)+
                " playbackSeconds="+(frame/(double)FPS)+" uniqueSamples="+samples+" repeatedFrames="+repeated+"\n";
            Files.writeString(output.resolve("complete.txt"),audit);
            System.out.println("[REVO CAPTURE] COMPLETE "+audit);
        }
    }
}
