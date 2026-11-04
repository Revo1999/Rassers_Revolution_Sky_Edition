package dk.revo.revoachievements.compat;

import dk.revo.revoachievements.*;
import dev.ftb.mods.ftblibrary.config.*;
import dev.ftb.mods.ftblibrary.icon.*;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.reward.*;
import dev.ftb.mods.ftbteams.api.FTBTeamsAPI;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import java.util.*;

public final class FtbCosmeticRewards {
    public static RewardType TITLE,HAT;
    public static void register(){
        TITLE=RewardTypes.register(ResourceLocation.fromNamespaceAndPath(RevoAchievements.MOD_ID,"title"),TitleReward::new,()->ItemIcon.getItemIcon(Items.NAME_TAG));
        HAT=RewardTypes.register(ResourceLocation.fromNamespaceAndPath(RevoAchievements.MOD_ID,"hat"),HatReward::new,()->ItemIcon.getItemIcon("simplehats:tophat"));
    }
    public abstract static class UnlockReward extends Reward {
        public String cosmetic="";
        protected UnlockReward(long id,Quest quest){super(id,quest);}
        protected abstract Cosmetic.Kind kind();
        @Override public void writeData(CompoundTag tag,HolderLookup.Provider registries){super.writeData(tag,registries);tag.putString("cosmetic",cosmetic);}
        @Override public void readData(CompoundTag tag,HolderLookup.Provider registries){var data=tag.copy();if(!data.contains("team_reward"))data.putBoolean("team_reward",false);super.readData(data,registries);cosmetic=tag.getString("cosmetic");}
        @Override public void writeNetData(RegistryFriendlyByteBuf b){super.writeNetData(b);b.writeUtf(cosmetic,128);}
        @Override public void readNetData(RegistryFriendlyByteBuf b){super.readNetData(b);cosmetic=b.readUtf(128);}
        @Override public void fillConfigGroup(ConfigGroup group){
            super.fillConfigGroup(group);group.addString("cosmetic",cosmetic,value->cosmetic=value,"revoachievements:"+(kind()==Cosmetic.Kind.TITLE?"title/unpaid_factory_intern":"hat/capbackwards")).setNameKey("revoachievements.reward.cosmetic_id");
        }
        @Override public Component getAltTitle(){var e=CosmeticCatalog.get(cosmetic);return Component.literal("Unlock "+kind().name().toLowerCase(Locale.ROOT)+": "+(e==null?cosmetic:e.name()));}
        @Override public Icon getAltIcon(){var e=CosmeticCatalog.get(cosmetic);return e==null?super.getAltIcon():ItemIcon.getItemIcon(e.icon());}
        @Override public boolean claim(ServerPlayer player,boolean notify){
            var e=CosmeticCatalog.get(cosmetic);if(e==null||e.kind()!=kind()){player.sendSystemMessage(Component.literal("Quest cosmetic is unavailable: "+cosmetic));return false;}
            var targets=new LinkedHashSet<UUID>();targets.add(player.getUUID());
            if(isTeamReward())FTBTeamsAPI.api().getManager().getTeamForPlayer(player).ifPresent(t->targets.addAll(t.getMembers()));
            targets.forEach(id->WardrobeService.grant(player.getServer(),id,cosmetic,notify));return true;
        }
    }
    public static final class TitleReward extends UnlockReward {
        public TitleReward(long id,Quest quest){super(id,quest);cosmetic="revoachievements:title/unpaid_factory_intern";}
        protected Cosmetic.Kind kind(){return Cosmetic.Kind.TITLE;}
        @Override public RewardType getType(){return TITLE;}
    }
    public static final class HatReward extends UnlockReward {
        public HatReward(long id,Quest quest){super(id,quest);cosmetic="revoachievements:hat/capbackwards";}
        protected Cosmetic.Kind kind(){return Cosmetic.Kind.HAT;}
        @Override public RewardType getType(){return HAT;}
    }
}
