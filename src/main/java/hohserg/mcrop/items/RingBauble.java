package hohserg.mcrop.items;

import am2.AMCore;
import baubles.api.BaubleType;
import baubles.api.IBauble;
import baubles.common.container.InventoryBaubles;
import baubles.common.lib.PlayerHandler;
import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;

import java.util.regex.Pattern;

public abstract class RingBauble extends RingIcon implements IBauble {
    @Override
    public BaubleType getBaubleType(ItemStack itemStack) {
        return BaubleType.RING;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
        if (isTruePlayer(player))
            if (!stack.hasDisplayName()) {
                if (!world.isRemote)
                    FMLNetworkHandler.openGui(player, AMCore.instance, 16, world, (int) player.posX, (int) player.posY, (int) player.posZ);
            } else
                return equip(stack, player);

        return stack;
    }

    private static final Pattern FAKE_PLAYER_PATTERN = Pattern.compile("^\\[.*]|ComputerCraft$");

    public static boolean isTruePlayer(EntityPlayer player) {
        return !(player instanceof FakePlayer || FAKE_PLAYER_PATTERN.matcher(player.getCommandSenderName()).matches());
    }

    private ItemStack equip(ItemStack stack, EntityPlayer player) {
        if (canEquip(stack, player)) {
            InventoryBaubles baubles = PlayerHandler.getPlayerBaubles(player);
            for (int i = 1; i <= 2; i++) {
                if (baubles.isItemValidForSlot(i, stack)) {
                    ItemStack stackInSlot = baubles.getStackInSlot(i);
                    if (stackInSlot == null || ((IBauble) stackInSlot.getItem()).canUnequip(stackInSlot, player)) {
                        if (!player.worldObj.isRemote) {
                            baubles.setInventorySlotContents(i, stack.copy());
                            if (!player.capabilities.isCreativeMode)
                                player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
                        }

                        if (stackInSlot != null) {
                            ((IBauble) stackInSlot.getItem()).onUnequipped(stackInSlot, player);
                            return stackInSlot.copy();
                        }
                        break;
                    }
                }
            }
        }
        return stack;
    }

    @Override
    public void onUnequipped(ItemStack itemStack, EntityLivingBase entityLivingBase) {

    }

    @Override
    public boolean canEquip(ItemStack itemStack, EntityLivingBase entityLivingBase) {
        return true;
    }

    @Override
    public boolean canUnequip(ItemStack itemStack, EntityLivingBase entityLivingBase) {
        return true;
    }
}
