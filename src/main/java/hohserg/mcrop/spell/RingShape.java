package hohserg.mcrop.spell;

import am2.api.spell.ItemSpellBase;
import am2.api.spell.component.interfaces.ISpellShape;
import am2.api.spell.enums.Affinity;
import am2.api.spell.enums.SpellCastResult;
import am2.items.ItemsCommonProxy;
import am2.spell.SpellHelper;
import am2.spell.SpellUtils;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class RingShape implements ISpellShape {

    @Override
    public int getID() {
        return 30;
    }

    public SpellCastResult beginStackStage(ItemSpellBase item, ItemStack stack, EntityLivingBase caster, EntityLivingBase target, World world, double x, double y, double z, int side, boolean giveXP, int useCount) {
        ItemStack newItemStack = SpellUtils.instance.popStackStage(stack);
        return SpellHelper.instance.applyStackStage(newItemStack, caster, target, x, y, z, 0, world, true, giveXP, 0);
    }

    public boolean isChanneled() {
        return false;
    }

    //Lists.newArrayList(new ItemStack(LOTRMod.goldRing), new ItemStack(LOTRMod.silverRing), new ItemStack(LOTRMod.mithrilRing))
    public Object[] getRecipeItems() {
        return new Object[]{
                "lotrRing",
                new ItemStack(ItemsCommonProxy.itemOre, 1, 0),
                "E:*",
                500
        };
    }

    public float manaCostMultiplier(ItemStack spellStack) {
        return 0.5F;
    }

    public boolean isTerminusShape() {
        return false;
    }

    public boolean isPrincipumShape() {
        return true;
    }

    public String getSoundForAffinity(Affinity affinity, ItemStack stack, World world) {
        return "arsmagica2:spell.cast.none";
    }
}
