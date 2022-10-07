package hohserg.mcrop.hooklib;

import am2.api.spell.component.interfaces.ISkillTreeEntry;
import am2.api.spell.component.interfaces.ISpellPart;
import am2.api.spell.component.interfaces.ISpellShape;
import am2.blocks.tileentities.TileEntityCraftingAltar;
import am2.blocks.tileentities.TileEntityInscriptionTable;
import am2.containers.ContainerInscriptionTable;
import am2.containers.slots.SlotInscriptionTable;
import am2.guis.GuiInscriptionTable;
import am2.spell.SkillManager;
import am2.spell.SpellUtils;
import am2.utility.InventoryUtilities;
import am2.utility.KeyValuePair;
import cpw.mods.fml.relauncher.ReflectionHelper;
import hohserg.mcrop.Config;
import hohserg.mcrop.hooklib.asm.Hook;
import hohserg.mcrop.hooklib.asm.ReturnCondition;
import hohserg.mcrop.items.RingBase;
import lotr.common.LOTRMod;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;

import static hohserg.mcrop.Main.ringItem;
import static hohserg.mcrop.Main.ringShape;

public class Hooks {

    private static boolean isRingIngredient(ItemStack target) {
        return target.getItem() == LOTRMod.goldRing || target.getItem() == LOTRMod.mithrilRing || target.getItem() == LOTRMod.silverRing;
    }

    @Hook(targetMethod = "compareItemStacks", returnCondition = ReturnCondition.ON_TRUE, booleanReturnConstant = true)
    public static boolean acceptAllRings(TileEntityCraftingAltar tileEntityCraftingAltar, ItemStack target, ItemStack input) {
        if (isRingIngredient(target))
            return isRingIngredient(input);
        else
            return false;
    }

    @Hook(targetMethod = "spellPartIsValidAddition", returnCondition = ReturnCondition.ON_TRUE, booleanReturnConstant = false)
    public static boolean ringShapeOnlyFirst(GuiInscriptionTable guiInscriptionTable, ISkillTreeEntry part) {
        ContainerInscriptionTable containerInscriptionTable = (ContainerInscriptionTable) guiInscriptionTable.inventorySlots;
        if (part == ringShape) {
            if (containerInscriptionTable.getCurrentRecipeSize() > 0)
                return true;

            for (int shapeGroup = 0; shapeGroup < containerInscriptionTable.getNumStageGroups(); shapeGroup++) {
                for (int i = 0; i < containerInscriptionTable.getShapeGroupSize(shapeGroup); i++) {
                    if (containerInscriptionTable.getShapeGroupPartAt(shapeGroup, i) instanceof ISpellShape) {
                        return true;
                    }
                }
            }
        } else if (containerInscriptionTable.currentRecipeContains(ringShape))
            return !Config.allowedSpellParts.contains(SkillManager.instance.getSkillName(part));

        return false;
    }

    @Hook(targetMethod = "isItemValid", returnCondition = ReturnCondition.ON_TRUE, booleanReturnConstant = true)
    public static boolean allowToSeeRingsByTable(SlotInscriptionTable slotInscriptionTable, ItemStack par1ItemStack) {
        return par1ItemStack.getItem() == ringItem;
    }

    @Hook(targetMethod = "onSlotChanged")
    public static void allowToSeeRingsByTable2(SlotInscriptionTable slotInscriptionTable) {
        if (slotInscriptionTable.getStack() != null)
            if (slotInscriptionTable.getStack().getItem() == ringItem)
                ((TileEntityInscriptionTable) slotInscriptionTable.inventory).reverseEngineerSpell(slotInscriptionTable.getStack());
    }

    @Hook(targetMethod = "onPickupFromSlot", returnCondition = ReturnCondition.ALWAYS)
    public static void maybeRingRecipe(SlotInscriptionTable slotInscriptionTable, EntityPlayer par1EntityPlayer, ItemStack par2ItemStack) {
        TileEntityInscriptionTable table = (TileEntityInscriptionTable) slotInscriptionTable.inventory;
        if (par2ItemStack.getItem() == Items.written_book) {
            ISpellPart shapeGroupPartAt = table.getShapeGroupPartAt(0, 0);
            table.writeRecipeAndDataToBook(par2ItemStack, par1EntityPlayer,
                    shapeGroupPartAt == ringShape ? "Ring Recipe" : "Spell Recipe"
            );
        } else
            table.clearCurrentRecipe();

        slotInscriptionTable.onSlotChanged();
    }

    @Hook(targetMethod = "writeRecipeAndDataToBook")
    public static void maybeRingRecipe2(TileEntityInscriptionTable table, ItemStack bookstack, EntityPlayer player, String title) {
        ISpellPart shapeGroupPartAt = table.getShapeGroupPartAt(0, 0);

        if (shapeGroupPartAt == ringShape)
            if (getCurrentSpellName(table).isEmpty())
                setCurrentSpellName(table, "Ring Recipe");

    }

    private static String getCurrentSpellName(TileEntityInscriptionTable table) {
        return ReflectionHelper.getPrivateValue(TileEntityInscriptionTable.class, table, "currentSpellName");
    }

    private static void setCurrentSpellName(TileEntityInscriptionTable table, String v) {
        ReflectionHelper.setPrivateValue(TileEntityInscriptionTable.class, table, v, "currentSpellName");
    }

    @Hook(targetMethod = "createSpellStack", returnCondition = ReturnCondition.ALWAYS, injectOnExit = true)
    public static ItemStack replaceSpellByRing(SpellUtils spellUtils, ArrayList<ArrayList<KeyValuePair<ISpellPart, byte[]>>> shapeGroups, ArrayList<KeyValuePair<ISpellPart, byte[]>> spell, @Hook.ReturnValue ItemStack stack) {
        if (isRingSpell(shapeGroups)) {
            ItemStack itemStack = InventoryUtilities.replaceItem(stack, ringItem);
            return itemStack;
        } else
            return stack;
    }

    @Hook(targetMethod = "AddSpecialMetadata")
    public static void setRingType(TileEntityCraftingAltar craftingAltar, ItemStack craftStack) {
        if (craftStack.getItem() == ringItem) {
            for (ItemStack ingredient : ReflectionHelper.<ArrayList<ItemStack>, TileEntityCraftingAltar>getPrivateValue(TileEntityCraftingAltar.class, craftingAltar, "allAddedItems")) {
                if (isRingIngredient(ingredient))
                    ringItem.setType(craftStack, ringItem.typeByIngredient.getOrDefault(ingredient.getItem(), RingBase.Type.silver));
            }
        }
    }

    private static boolean isRingSpell(ArrayList<ArrayList<KeyValuePair<ISpellPart, byte[]>>> shapeGroups) {
        for (ArrayList<KeyValuePair<ISpellPart, byte[]>> shapeGroup : shapeGroups) {
            for (KeyValuePair<ISpellPart, byte[]> entry : shapeGroup) {
                if (entry.getKey() == ringShape)
                    return true;
            }
        }
        return false;
    }
}
