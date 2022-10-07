package hohserg.mcrop.items;

import am2.api.spell.enums.SpellCastResult;
import am2.spell.SpellHelper;
import am2.spell.SpellUtils;
import baubles.common.lib.PlayerHandler;
import hohserg.elegant.networking.api.ClientToServerPacket;
import hohserg.elegant.networking.api.ElegantPacket;
import lombok.Value;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;

import static am2.buffs.BuffList.silence;
import static hohserg.mcrop.Main.xpRewardByWearingRings;

public class RingOfPower extends RingBauble {

    @Override
    public void onEquipped(ItemStack itemStack, EntityLivingBase entityLivingBase) {
        SpellHelper.instance.applyStageToEntity(itemStack, entityLivingBase, entityLivingBase.worldObj, entityLivingBase, 0, xpRewardByWearingRings);
    }

    @Override
    public void onWornTick(ItemStack stack, EntityLivingBase player) {
        if (player.worldObj.isRemote)
            if (player instanceof EntityPlayer)
                if (RingOfPowerHelper.instance.haveSenseToApplySpell(SpellUtils.instance.constructSpellStack(stack), player))
                    new PacketApplyRingSpell(getSlot(stack, (EntityPlayer) player)).sendToServer();
    }

    private int getSlot(ItemStack stack, EntityPlayer player) {
        if (PlayerHandler.getPlayerBaubles(player).getStackInSlot(1) == stack) {
            return 1;
        } else
            return 2;
    }


    @ElegantPacket
    @Value
    public static class PacketApplyRingSpell implements ClientToServerPacket {
        public int slot;

        @Override
        public void onReceive(EntityPlayerMP player) {
            if (slot == 1 || slot == 2) {
                ItemStack stack = PlayerHandler.getPlayerBaubles(player).getStackInSlot(slot);

                long currentTime = player.worldObj.getTotalWorldTime();

                if (getNextFreeUsingTime(stack) < currentTime) {

                    PotionEffect activeSilenceEffect = player.getActivePotionEffect(silence);
                    player.removePotionEffect(silence.id);

                    SpellCastResult result = SpellHelper.instance.applyStackStage(stack, player, null, player.posX, player.posY, player.posZ, 0, player.worldObj, true, xpRewardByWearingRings, 0);

                    if (activeSilenceEffect != null)
                        player.addPotionEffect(activeSilenceEffect);

                    if (result == SpellCastResult.SUCCESS || result == SpellCastResult.SUCCESS_REDUCE_MANA)
                        setNextFreeUsingTime(stack, currentTime + getCooldown(stack));
                }

            }
        }
    }

    public static long getCooldown(ItemStack stack) {
        return 10;
    }

    public static long getNextFreeUsingTime(ItemStack stack) {
        return stack.hasTagCompound() ? stack.getTagCompound().getLong("nextFreeUsingTime") : 0;
    }

    public static void setNextFreeUsingTime(ItemStack stack, long v) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());

        stack.getTagCompound().setLong("nextFreeUsingTime", v);
    }
}
