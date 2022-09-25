package hohserg.mcrop.items;

import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.component.interfaces.ISpellShape;
import am2.api.spell.enums.SpellModifiers;
import am2.buffs.BuffList;
import am2.spell.SkillManager;
import am2.spell.SpellHelper;
import am2.spell.SpellUtils;
import am2.spell.components.*;
import am2.spell.shapes.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;

import static hohserg.mcrop.Main.xpRewardByWearingRings;

public class RingOfPower extends RingBauble {

    @Override
    public void onEquipped(ItemStack itemStack, EntityLivingBase entityLivingBase) {
        SpellHelper.instance.applyStageToEntity(itemStack, entityLivingBase, entityLivingBase.worldObj, entityLivingBase, 0, xpRewardByWearingRings);
    }

    @Override
    public void onWornTick(ItemStack stack, EntityLivingBase player) {
        World world = player.worldObj;
        if (haveSenseToApplySpell(SpellUtils.instance.constructSpellStack(stack), player)) {
            PotionEffect activeSilenceEffect = player.getActivePotionEffect(BuffList.silence);
            player.removePotionEffect(BuffList.silence.id);

            SpellHelper.instance.applyStackStage(stack, player, null, player.posX, player.posY, player.posZ, 0, world, true, xpRewardByWearingRings, 0);

            if (activeSilenceEffect != null)
                player.addPotionEffect(activeSilenceEffect);

        }
    }

    public boolean haveSenseToApplySpell(ItemStack stack, EntityLivingBase player) {
        World world = player.worldObj;

        ISpellShape secondShape = SpellUtils.instance.getShapeForStage(stack, 1);
        ISpellComponent[] components = secondShape.isPrincipumShape() ?
                SpellUtils.instance.getComponentsForStage(stack, 2) :
                SpellUtils.instance.getComponentsForStage(stack, 1);

        TargetGetter targetGetter = targetGetters.get(secondShape.getClass());
        if (targetGetter == null)
            return player.ticksExisted % 20 == 0;
        else {
            List<EntityLivingBase> targets = targetGetter.apply(stack, player);

            if (targets.size() > 0) {
                for (ISpellComponent component : components) {
                    Potion potion = getBuff(component);
                    if (potion != null) {
                        for (EntityLivingBase target : targets) {
                            if (!target.isPotionActive(potion) || target.getActivePotionEffect(potion).getDuration() < 2 || potion == Potion.nightVision && target.getActivePotionEffect(potion).getDuration() < 10 * 20 + 2)
                                return true;
                        }

                    } else if (isHeal(component)) {
                        int healing = SpellUtils.instance.getModifiedInt_Mul(2, stack, player, player, world, 0, SpellModifiers.HEALING);
                        for (EntityLivingBase target : targets) {
                            if (target.getMaxHealth() - target.getHealth() >= healing || target.getHealth() < 2)
                                return true;
                        }

                    } else if (isChannelable(component)) {
                        return true;

                    } else if (player.ticksExisted % 20 == 0) {
                        return true;

                    }
                }
            }
            return false;
        }
    }

    private interface TargetGetter {
        List<EntityLivingBase> apply(ItemStack stack, EntityLivingBase player);
    }

    private Map<Class<? extends ISpellShape>, TargetGetter> targetGetters = ImmutableMap.<Class<? extends ISpellShape>, TargetGetter>builder()
            .put(AoE.class, (stack, player) -> {
                double radius = SpellUtils.instance.getModifiedDouble_Add(1.0D, stack, player, player, player.worldObj, 0, SpellModifiers.RADIUS);
                return player.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, AxisAlignedBB.getBoundingBox(player.posX - radius, player.posY - radius, player.posZ - radius, player.posX + radius, player.posY + radius, player.posZ + radius));
            })
            .put(Channel.class, (TargetGetter) (stack, player) -> ImmutableList.of(player))
            .put(MissingShape.class, (TargetGetter) (stack, player) -> ImmutableList.of())
            .put(Self.class, (TargetGetter) (stack, player) -> ImmutableList.of(player))
            .put(Zone.class, (TargetGetter) (stack, player) -> {
                int radius = SpellUtils.instance.getModifiedInt_Add(2, stack, player, player, player.worldObj, 0, SpellModifiers.RADIUS);
                return player.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, AxisAlignedBB.getBoundingBox(player.posX - radius, player.posY - 1.0D, player.posZ - radius, player.posX + radius, player.posY + 3, player.posZ + radius));
            })
            .build();

    private Map<Class<? extends ISpellComponent>, Potion> potionByComponent = ImmutableMap.<Class<? extends ISpellComponent>, Potion>builder()
            .put(AstralDistortion.class, BuffList.astralDistortion)
            .put(Blind.class, Potion.blindness)
            .put(Charm.class, BuffList.charmed)
            .put(ChronoAnchor.class, BuffList.temporalAnchor)
            .put(Entangle.class, BuffList.entangled)
            .put(Flight.class, BuffList.flight)
            .put(Freeze.class, BuffList.frostSlowed)
            .put(Fury.class, BuffList.fury)
            .put(GravityWell.class, BuffList.gravityWell)
            .put(Haste.class, BuffList.haste)
            .put(Invisiblity.class, Potion.invisibility)
            .put(Leap.class, BuffList.leap)
            .put(Levitation.class, BuffList.levitation)
            .put(ManaShield.class, BuffList.manaShield)
            .put(NightVision.class, Potion.nightVision)
            .put(Reflect.class, BuffList.spellReflect)
            .put(Regeneration.class, BuffList.regeneration)
            .put(Shield.class, BuffList.magicShield)
            .put(Shrink.class, BuffList.shrink)
            .put(Silence.class, BuffList.silence)
            .put(Slow.class, BuffList.frostSlowed)
            .put(Slowfall.class, BuffList.slowfall)
            .put(SwiftSwim.class, BuffList.swiftSwim)
            .put(TrueSight.class, BuffList.trueSight)
            .put(WaterBreathing.class, BuffList.waterBreathing)
            .put(WateryGrave.class, BuffList.wateryGrave)
            .build();

    private Potion getBuff(ISpellComponent component) {
        return potionByComponent.get(component.getClass());
    }

    private boolean isChannelable(ISpellComponent component) {
        return component instanceof Telekinesis || component instanceof Attract || component instanceof Repel;
    }

    private boolean isHeal(ISpellComponent component) {
        return component == SkillManager.instance.getSkill("Heal");
    }
}
