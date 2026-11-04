package dk.revo.revoachievements;

import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(RevoAchievements.MOD_ID)
@PrefixGameTestTemplate(false)
public class AchievementsGameTests {
    private static final String TITLE="revoachievements:title/test",OTHER="revoachievements:title/other",HAT="revoachievements:hat/test";
    private static Cosmetic entry(String id,Cosmetic.Kind kind){return new Cosmetic(ResourceLocation.parse(id),kind,id,"Test","Test",ResourceLocation.parse(kind==Cosmetic.Kind.HAT?"minecraft:carved_pumpkin":"minecraft:name_tag"),0xAABBCC,0);}
    private static void catalogue(){CosmeticCatalog.replace(List.of(entry(TITLE,Cosmetic.Kind.TITLE),entry(OTHER,Cosmetic.Kind.TITLE),entry(HAT,Cosmetic.Kind.HAT)));}
    @GameTest(template="achievements_test",timeoutTicks=100)
    public static void unlocksArePersonalIdempotentAndDoNotAutoEquip(GameTestHelper h){
        catalogue();var data=new WardrobeData();var a=UUID.randomUUID();var b=UUID.randomUUID();
        h.assertTrue(data.unlock(a,TITLE)&&!data.unlock(a,TITLE),"Repeated awards must be idempotent");
        h.assertTrue(data.profile(a).owns(TITLE)&&!data.profile(b).owns(TITLE),"No cross-player unlocks");
        h.assertTrue(data.profile(a).title().isEmpty()&&data.profile(a).hat().isEmpty(),"Awarding must not auto-equip");h.succeed();
    }
    @GameTest(template="achievements_test",timeoutTicks=100)
    public static void lockedAndUnknownEquipmentRejected(GameTestHelper h){
        catalogue();var data=new WardrobeData();var a=UUID.randomUUID();
        h.assertTrue(!data.equip(a,TITLE)&&!data.equip(a,"revoachievements:missing"),"Locked and unknown IDs rejected");data.unlock(a,TITLE);h.assertTrue(data.equip(a,TITLE),"Owned known cosmetic equips");
        h.assertTrue(!data.equip(a,OTHER)&&data.profile(a).title().equals(TITLE),"Rejected equip cannot replace a real selection");h.succeed();
    }
    @GameTest(template="achievements_test",timeoutTicks=100)
    public static void titleAndHatAreIndependentAndRevocationClearsSelection(GameTestHelper h){
        catalogue();var data=new WardrobeData();var a=UUID.randomUUID();data.unlock(a,TITLE);data.unlock(a,HAT);data.equip(a,TITLE);data.equip(a,HAT);
        data.clear(a,Cosmetic.Kind.TITLE);h.assertTrue(data.profile(a).title().isEmpty()&&data.profile(a).hat().equals(HAT),"Clearing title must preserve hat");
        data.equip(a,TITLE);data.revoke(a,HAT);h.assertTrue(data.profile(a).hat().isEmpty()&&data.profile(a).title().equals(TITLE)&&!data.profile(a).owns(HAT),"Revocation clears only its selected slot");h.succeed();
    }
    @GameTest(template="achievements_test",timeoutTicks=100)
    public static void unlocksAndSelectionsSurviveSaveReload(GameTestHelper h){
        catalogue();var data=new WardrobeData();var a=UUID.randomUUID();var b=UUID.randomUUID();for(String id:List.of(TITLE,HAT)){data.unlock(a,id);data.equip(a,id);}data.unlock(b,OTHER);
        var saved=data.save(new CompoundTag(),h.getLevel().registryAccess());var restored=WardrobeData.load(saved,h.getLevel().registryAccess());
        h.assertTrue(restored.profile(a).unlocked().equals(Set.of(TITLE,HAT))&&restored.profile(a).title().equals(TITLE)&&restored.profile(a).hat().equals(HAT),"Personal loadout and unlocks must survive world reload");
        h.assertTrue(restored.profile(b).owns(OTHER)&&!restored.profile(b).owns(TITLE),"Other/offline player is preserved separately");h.succeed();
    }
    @GameTest(template="achievements_test",timeoutTicks=100)
    public static void removedDefinitionsCannotStayEquippedButUnlocksAreRetained(GameTestHelper h){
        catalogue();var data=new WardrobeData();var a=UUID.randomUUID();data.unlock(a,TITLE);data.unlock(a,HAT);data.equip(a,TITLE);data.equip(a,HAT);
        CosmeticCatalog.replace(List.of(entry(HAT,Cosmetic.Kind.HAT)));data.validate(a);
        h.assertTrue(data.profile(a).title().isEmpty()&&data.profile(a).hat().equals(HAT)&&data.profile(a).owns(TITLE),"Missing definitions unequip safely, retaining earned history");
        catalogue();h.assertTrue(data.equip(a,TITLE),"Restored definition remains owned");h.succeed();
    }
    @GameTest(template="achievements_test",timeoutTicks=100)
    public static void offlineRewardServiceAndUnknownRewards(GameTestHelper h){
        catalogue();var server=h.getLevel().getServer();var a=UUID.randomUUID();var b=UUID.randomUUID();
        h.assertTrue(WardrobeService.grant(server,a,TITLE,false)==1&&WardrobeService.grant(server,a,TITLE,false)==0,"Offline awards persist and duplicate claims are harmless");
        h.assertTrue(WardrobeService.grant(server,a,"revoachievements:missing",false)==-1,"Unknown rewards cannot unlock arbitrary IDs");
        h.assertTrue(WardrobeData.get(server).profile(a).owns(TITLE)&&!WardrobeData.get(server).profile(b).owns(TITLE),"Offline team recipients remain individually owned");h.succeed();
    }
    @GameTest(template="achievements_test",timeoutTicks=100)
    public static void ordinaryPlayersCannotGrantThemselvesRewards(GameTestHelper h){
        catalogue();var player=net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(h.getLevel());var server=h.getLevel().getServer();var data=WardrobeData.get(server);data.revoke(player.getUUID(),TITLE);
        boolean denied=false;try{server.getCommands().getDispatcher().execute("revoachievements unlock @s "+TITLE,player.createCommandSourceStack().withPermission(0));}catch(com.mojang.brigadier.exceptions.CommandSyntaxException expected){denied=true;}
        h.assertTrue(denied&&!data.profile(player.getUUID()).owns(TITLE),"Unlock command requires operator permission");
        h.assertTrue(!WardrobeService.equip(player,TITLE),"Normal equip cannot unlock a reward");h.succeed();
    }
    @GameTest(template="achievements_test",timeoutTicks=100)
    public static void publicAppearancePacketsDoNotExposePrivateUnlocks(GameTestHelper h){
        catalogue();var p=net.neoforged.neoforge.common.util.FakePlayerFactory.getMinecraft(h.getLevel());var data=WardrobeData.get(h.getLevel().getServer());
        data.unlock(p.getUUID(),TITLE);data.unlock(p.getUUID(),HAT);data.equip(p.getUUID(),TITLE);data.equip(p.getUUID(),HAT);
        var appearance=WardrobeService.publicRow(p);var personal=WardrobeService.own(p,false,"");
        h.assertTrue(appearance.getUUID("Player").equals(p.getUUID())&&appearance.getString("HatItem").equals("minecraft:carved_pumpkin"),"Public appearance identifies the correct player and visible item");
        h.assertTrue(!appearance.contains("Owned")&&!appearance.contains("Entries")&&!appearance.contains("Unlocked"),"Other clients must not receive private unlock collections");
        h.assertTrue(personal.getList("Owned",net.minecraft.nbt.Tag.TAG_STRING).size()>=2,"Only the owner receives unlock data");h.succeed();
    }
}
