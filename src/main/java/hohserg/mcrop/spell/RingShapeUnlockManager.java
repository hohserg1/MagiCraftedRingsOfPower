package hohserg.mcrop.spell;

import am2.api.events.SpellCastingEvent;
import am2.api.spell.component.interfaces.ISkillTreeEntry;
import am2.api.spell.enums.SkillPointTypes;
import am2.playerextensions.ExtendedProperties;
import am2.playerextensions.SkillData;
import am2.spell.SkillManager;
import am2.spell.SkillTreeManager;
import am2.spell.SpellUtils;
import am2.spell.components.*;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import lotr.common.world.biome.LOTRBiome;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.BiomeGenBase;

import static am2.api.spell.enums.SpellCastResult.SUCCESS;
import static hohserg.mcrop.Main.ringShape;

public class RingShapeUnlockManager {

    private ISkillTreeEntry Binding = SkillManager.instance.getSkill("Binding");

    @SubscribeEvent
    public void onCast(SpellCastingEvent.Post event) {
        if (event.caster instanceof EntityPlayerMP) {
            EntityPlayerMP player = (EntityPlayerMP) event.caster;
            if (ExtendedProperties.For(player).getCurrentMana() >= event.manaCost) {
                if (event.castResult == SUCCESS) {
                    BiomeGenBase biomeGen = player.worldObj.getBiomeGenForCoords(((int) player.posX), (int) player.posZ);
                    if (biomeGen instanceof LOTRBiome) {
                        ItemStack stack = event.stack;
                        if (biomeGen == LOTRBiome.eregion) {
                            checkEregionSpell(player, stack);

                        } else if (biomeGen == LOTRBiome.gorgoroth) {
                            if (player.getDistanceSq(92718, 128, 60970) < 130 * 130) {
                                checkOrodruinSpell(player, stack);
                            }
                        }
                    }
                }
            }
        }
    }

    private void checkOrodruinSpell(EntityPlayerMP player, ItemStack stack) {
        boolean bindingFound = false;
        boolean charmFound = false;
        boolean invisiblityFound = false;
        boolean trueSightFound = false;
        for (int stage = 0; stage < SpellUtils.instance.numStages(stack); stage++) {
            if (SpellUtils.instance.getShapeForStage(stack, stage) == Binding)
                bindingFound = true;
            if (SpellUtils.instance.componentIsPresent(stack, Charm.class, stage))
                charmFound = true;
            if (SpellUtils.instance.componentIsPresent(stack, Invisiblity.class, stage))
                invisiblityFound = true;
            if (SpellUtils.instance.componentIsPresent(stack, TrueSight.class, stage))
                trueSightFound = true;
        }
        if (bindingFound && charmFound && invisiblityFound && trueSightFound)
            unlock(player);
    }

    private void checkEregionSpell(EntityPlayerMP player, ItemStack stack) {
        boolean bindingFound = false;
        boolean furyFound = false;
        boolean manaShieldFound = false;
        boolean knockbackFound = false;
        for (int stage = 0; stage < SpellUtils.instance.numStages(stack); stage++) {
            if (SpellUtils.instance.getShapeForStage(stack, stage) == Binding)
                bindingFound = true;
            if (SpellUtils.instance.componentIsPresent(stack, Fury.class, stage))
                furyFound = true;
            if (SpellUtils.instance.componentIsPresent(stack, ManaShield.class, stage))
                manaShieldFound = true;
            if (SpellUtils.instance.componentIsPresent(stack, Knockback.class, stage))
                knockbackFound = true;
        }
        if (bindingFound && furyFound && manaShieldFound && knockbackFound)
            unlock(player);
    }

    private void unlock(EntityPlayerMP player) {
        SkillData.For(player).incrementSpellPoints(SkillPointTypes.SILVER);
        SkillData.For(player).learn((SkillTreeManager.instance.getSkillTreeEntry(ringShape)).registeredItem);
        SkillData.For(player).forceSync();
    }
}
