package hohserg.mcrop.items;

import com.google.common.collect.ImmutableMap;
import lotr.common.LOTRMod;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Map;

import static hohserg.mcrop.items.RingBase.Type.*;


public class RingBase extends Item {
    {
        setMaxStackSize(1);
    }

    public enum Type {
        silver, gold, mithril
    }

    public Map<Item, Type> typeByIngredient = ImmutableMap.of(
            LOTRMod.silverRing, silver,
            LOTRMod.goldRing, gold,
            LOTRMod.mithrilRing, mithril
    );

    public Type getType(ItemStack stack) {
        int ringType = stack.hasTagCompound() ? stack.getTagCompound().getInteger("ringType") : 0;
        return 0 <= ringType && ringType < Type.values().length ? Type.values()[ringType] : silver;
    }

    public void setType(ItemStack stack, Type type) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());

        stack.getTagCompound().setInteger("ringType", type.ordinal());
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        if (stack.stackTagCompound == null) {
            return "§bMalformed Ring";
        } else {
            return "Unnamed Ring";
        }
    }

}
