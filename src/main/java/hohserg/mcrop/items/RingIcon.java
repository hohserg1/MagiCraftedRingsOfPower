package hohserg.mcrop.items;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lotr.common.LOTRMod;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;

import java.util.HashMap;
import java.util.Map;

import static hohserg.mcrop.items.RingBase.Type.*;

public class RingIcon extends RingBase {

    public static final int inscriptionsCount = 2;

    private Map<Type, IIcon> icons = new HashMap<>();
    private IIcon[] inscriptions = new IIcon[inscriptionsCount];

    @SideOnly(Side.CLIENT)
    @Override
    public void registerIcons(IIconRegister iconRegister) {
        icons.put(silver, LOTRMod.silverRing.getIcon(new ItemStack(LOTRMod.silverRing), 0));
        icons.put(gold, LOTRMod.goldRing.getIcon(new ItemStack(LOTRMod.goldRing), 0));
        icons.put(mithril, LOTRMod.mithrilRing.getIcon(new ItemStack(LOTRMod.mithrilRing), 0));

        for (int i = 0; i < inscriptionsCount; i++) {
            inscriptions[i] = iconRegister.registerIcon("mcrop:inscriptions/" + i);
        }
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon(ItemStack stack, int pass) {
        return pass == 0 ? icons.get(getType(stack)) : getOverlayIcon(stack);
    }

    private IIcon getOverlayIcon(ItemStack stack) {
        int inscription = getInscription(stack);
        return inscriptions[inscription];
    }

    public int getInscription(ItemStack stack) {
        if (true)
            return 1;
        int inscriptionType = stack.hasTagCompound() ? stack.getTagCompound().getInteger("inscription") : 1;
        return 0 <= inscriptionType && inscriptionType < inscriptionsCount ? inscriptionType : 0;
    }

    public void setInscription(ItemStack stack, int inscription) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());

        stack.getTagCompound().setInteger("inscription", inscription);
    }

    @Override
    public boolean hasEffect(ItemStack par1ItemStack, int pass) {
        return pass == 1;
    }

    @SideOnly(Side.CLIENT)
    public boolean requiresMultipleRenderPasses() {
        return true;
    }
}
