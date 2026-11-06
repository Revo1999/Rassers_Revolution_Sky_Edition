package dk.revo.revocultivation;

import net.minecraft.core.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(RevoCultivation.MOD_ID)
@PrefixGameTestTemplate(false)
public class FarmLightingGameTests {
    private static void tick(FarmMachine m){FarmMachine.tick(m.getLevel(),m.getBlockPos(),m.getBlockState(),m);}
    @GameTest(template="farm_test",timeoutTicks=160)
    public static void ledStripsFollowWorldTimeRainAndPower(GameTestHelper h){
        var level=h.getLevel();level.setDayTime(1000);level.updateSkyBrightness();
        var kind=MachineKind.IRON_GREENHOUSE;
        var pos=h.absolutePos(new BlockPos(4,2,3));
        level.setBlockAndUpdate(pos,kind.controller().defaultBlockState().setValue(FarmController.FACING,Direction.NORTH));
        var m=(FarmMachine)level.getBlockEntity(pos);
        for(int x=-2;x<=2;x++)for(int y=0;y<3;y++)for(int z=-2;z<=2;z++){
            var part=kind.part(x,y,z);var state=kind.block(part).defaultBlockState();
            if(part==MachineKind.Part.CONTROLLER)state=state.setValue(FarmController.FACING,Direction.NORTH);
            if(part==MachineKind.Part.HATCH)state=state.setValue(FarmHatch.FACING,Direction.EAST);
            if(part==MachineKind.Part.POWER)state=state.setValue(BlockStateProperties.FACING,Direction.SOUTH);
            level.setBlockAndUpdate(m.at(x,y,z),state);
        }
        h.assertTrue(m.checkStructure(),"Wide greenhouse must form");m.receiveEu(1600,false);
        level.setWeatherParameters(100000,0,false,false);level.setRainLevel(0);
        h.runAfterDelay(8,()->{
            tick(m);h.assertTrue(!m.needsGrowLights()&&!m.growLightsOn(),"Clear daytime leaves LEDs off");
            int daytimeEnergy=m.energy();tick(m);h.assertTrue(m.energy()==daytimeEnergy,"Idle daytime consumes no LED energy");
            level.setDayTime(12999);tick(m);h.assertTrue(!m.growLightsOn(),"LEDs stay off just before dusk");
            level.setDayTime(13000);tick(m);h.assertTrue(m.growLightsOn(),"LEDs switch on exactly at tick 13000");
            int nighttimeEnergy=m.energy();tick(m);h.assertTrue(m.energy()==nighttimeEnergy-1,"Idle LEDs consume 1 EU/t");
            level.setDayTime(22999);tick(m);h.assertTrue(m.growLightsOn(),"LEDs stay on just before dawn");
            level.setDayTime(23000);tick(m);h.assertTrue(!m.growLightsOn(),"Clear dawn switches LEDs off exactly at tick 23000");
            level.setDayTime(1000);level.setWeatherParameters(0,100000,true,false);level.setRainLevel(1);tick(m);
            h.assertTrue(m.growLightsOn(),"Daytime rain switches LEDs on");
            level.setWeatherParameters(100000,0,false,false);level.setRainLevel(0);tick(m);
            h.assertTrue(!m.growLightsOn(),"Rain stopping during daytime switches LEDs off");
            for(int x=-3;x<=3;x++)for(int z=-3;z<=3;z++)level.setBlockAndUpdate(m.at(x,3,z),Blocks.STONE.defaultBlockState());
            h.runAfterDelay(8,()->{
                tick(m);h.assertTrue(!m.growLightsOn(),"Indoor shade does not override the clear daytime schedule");
                level.setDayTime(18000);tick(m);h.assertTrue(m.growLightsOn(),"Nighttime still switches LEDs on under cover");
                int stored=m.energy();for(int i=0;i<=stored;i++)tick(m);
                h.assertTrue(m.energy()==0&&!m.growLightsOn(),"Power loss switches LEDs off without negative energy");
                m.receiveEu(20,false);tick(m);h.assertTrue(m.growLightsOn(),"Restored energy switches LEDs back on");
                level.setBlockAndUpdate(m.at(-2,0,-2),Blocks.AIR.defaultBlockState());tick(m);
                h.assertTrue(!m.formed()&&!m.growLightsOn()&&!level.getBlockState(m.at(0,2,0)).getValue(FarmGrowLight.LIT),"Dismantling clears both visible LEDs and emitted light");
                h.succeed();
            });
        });
    }
}
