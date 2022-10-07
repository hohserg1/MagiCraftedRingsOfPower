package hohserg.mcrop.items;

import am2.api.spell.component.interfaces.ISpellComponent;
import am2.api.spell.component.interfaces.ISpellShape;
import am2.api.spell.enums.ContingencyTypes;
import am2.api.spell.enums.SpellModifiers;
import am2.items.ItemsCommonProxy;
import am2.playerextensions.ExtendedProperties;
import am2.spell.SkillManager;
import am2.spell.SpellUtils;
import am2.spell.components.*;
import am2.spell.shapes.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import hohserg.mcrop.Config;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static am2.buffs.BuffList.*;
import static hohserg.mcrop.items.RingOfPowerHelper.TargetGetter.tg;
import static hohserg.mcrop.items.RingOfPowerHelper.TargetType.block;
import static hohserg.mcrop.items.RingOfPowerHelper.TargetType.entity;


public enum RingOfPowerHelper {
    instance;

    private int ticks = 0;

    public boolean haveSenseToApplySpell(ItemStack stack, EntityLivingBase player) {
        ticks++;
        if (ticks < Config.clientSideTrottling)
            return false;
        else {
            ticks = 0;

            World world = player.worldObj;

            ISpellShape secondShape = SpellUtils.instance.getShapeForStage(stack, 1);
            ISpellComponent[] components = secondShape.isPrincipumShape() ?
                    SpellUtils.instance.getComponentsForStage(stack, 2) :
                    SpellUtils.instance.getComponentsForStage(stack, 1);

            Map<TargetType, List<ISpellComponent>> componentsByTypes = Arrays.stream(components)
                    .flatMap(c -> targetTypeByComponent.get(c.getClass()).stream().map(tt -> Pair.of(tt, c)))
                    .collect(Collectors.<Pair<TargetType, ISpellComponent>, TargetType, List<ISpellComponent>, List<ISpellComponent>>groupingBy(Pair::getLeft, new Collector<Pair<TargetType, ISpellComponent>, List<ISpellComponent>, List<ISpellComponent>>() {

                        @Override
                        public Supplier<List<ISpellComponent>> supplier() {
                            return ArrayList::new;
                        }

                        @Override
                        public BiConsumer<List<ISpellComponent>, Pair<TargetType, ISpellComponent>> accumulator() {
                            return (acc, e) -> acc.add(e.getRight());
                        }

                        @Override
                        public BinaryOperator<List<ISpellComponent>> combiner() {
                            return (acc1, acc2) -> {
                                acc1.addAll(acc2);
                                return acc1;
                            };
                        }

                        @Override
                        public Function<List<ISpellComponent>, List<ISpellComponent>> finisher() {
                            return Function.identity();
                        }

                        @Override
                        public Set<Characteristics> characteristics() {
                            return ImmutableSet.of(Collector.Characteristics.IDENTITY_FINISH);
                        }
                    }));

            TargetGetter targetGetter = targetGetters.get(secondShape.getClass());

            if (targetGetter != null) {
                List<ISpellComponent> forLiving = componentsByTypes.get(entity);
                if (forLiving != null && forLiving.size() > 0) {
                    List<EntityLivingBase> targets = targetGetter.findPotionTarget(stack, player);
                    if (targets.size() > 0) {
                        for (ISpellComponent component : forLiving) {
                            Potion potion = getBuff(component);
                            if (potion != null) {
                                for (EntityLivingBase e : targets) {
                                    if (!e.isPotionActive(potion) || e.getActivePotionEffect(potion).getDuration() < 2 || potion == Potion.nightVision && e.getActivePotionEffect(potion).getDuration() < 10 * 20 + 2)
                                        return true;
                                }
                            } else if (isHeal(component)) {
                                int healing = SpellUtils.instance.getModifiedInt_Mul(2, stack, player, player, world, 0, SpellModifiers.HEALING);
                                for (EntityLivingBase e : targets) {
                                    if (e.getMaxHealth() - e.getHealth() >= healing || e.getHealth() < 2)
                                        return true;
                                }
                            } else
                                return true;
                        }
                    }
                }

                if (componentsByTypes.containsKey(TargetType.block) && targetGetter.findBlockTarget(stack, player))
                    return true;
            }

            return false;
        }
    }

    public enum TargetType {
        entity, block
    }

    private Set<TargetType> set(TargetType... t) {
        return ImmutableSet.copyOf(t);
    }

    private Map<Class<? extends ISpellComponent>, Set<TargetType>> targetTypeByComponent = ImmutableMap.<Class<? extends ISpellComponent>, Set<TargetType>>builder()
            .put(Accelerate.class, set(block, entity))
            .put(Appropriation.class, set(block, entity))
            .put(AstralDistortion.class, set(entity))
            .put(Attract.class, set(entity))
            .put(BanishRain.class, set(block, entity))
            .put(Blind.class, set(entity))
            .put(Blink.class, set(entity))
            .put(Blizzard.class, set(block, entity))
            .put(Charm.class, set(entity))
            .put(ChronoAnchor.class, set(entity))
            .put(CreateWater.class, set(block))
            .put(Daylight.class, set(block, entity))
            .put(Dig.class, set(block))
            .put(Disarm.class, set(entity))
            .put(Dispel.class, set(entity))
            .put(DivineIntervention.class, set(entity))
            .put(Drought.class, set(block))
            .put(Drown.class, set(entity))
            .put(EnderIntervention.class, set(entity))
            .put(Entangle.class, set(entity))
            .put(FallingStar.class, set(block, entity))
            .put(FireDamage.class, set(entity))
            .put(FireRain.class, set(block, entity))
            .put(Flight.class, set(entity))
            .put(Fling.class, set(entity))
            .put(Forge.class, set(block, entity))
            .put(Freeze.class, set(block, entity))
            .put(FrostDamage.class, set(entity))
            .put(Fury.class, set(entity))
            .put(GravityWell.class, set(entity))
            .put(Grow.class, set(block))
            .put(HarvestPlants.class, set(block))
            .put(Haste.class, set(entity))
            .put(Heal.class, set(entity))
            .put(Ignition.class, set(block, entity))
            .put(Invisiblity.class, set(entity))
            .put(Knockback.class, set(entity))
            .put(Leap.class, set(entity))
            .put(Levitation.class, set(entity))
            .put(LifeDrain.class, set(entity))
            .put(LifeTap.class, set(entity))
            .put(Light.class, set(block, entity))
            .put(LightningDamage.class, set(entity))
            .put(MagicDamage.class, set(entity))
            .put(ManaDrain.class, set(entity))
            .put(ManaLink.class, set(entity))
            .put(ManaShield.class, set(entity))
            .put(Mark.class, set(block, entity))
            .put(MissingComponent.class, set())
            .put(Moonrise.class, set(block, entity))
            .put(NightVision.class, set(entity))
            .put(PhysicalDamage.class, set(entity))
            .put(PlaceBlock.class, set(block))
            .put(Plant.class, set(block))
            .put(Plow.class, set(block))
            .put(RandomTeleport.class, set(entity))
            .put(Recall.class, set(entity))
            .put(Reflect.class, set(entity))
            .put(Regeneration.class, set(entity))
            .put(Repel.class, set(entity))
            .put(Rift.class, set(block))
            .put(ScrambleSynapses.class, set(entity))
            .put(Shield.class, set(entity))
            .put(Shrink.class, set(entity))
            .put(Silence.class, set(entity))
            .put(Slow.class, set(entity))
            .put(Slowfall.class, set(entity))
            .put(Storm.class, set(block, entity))
            .put(Summon.class, set(block, entity))
            .put(SwiftSwim.class, set(entity))
            .put(Telekinesis.class, set(block, entity))
            .put(Transplace.class, set(entity))
            .put(TrueSight.class, set(entity))
            .put(WaterBreathing.class, set(entity))
            .put(WateryGrave.class, set(entity))
            .put(WizardsAutumn.class, set(block, entity))
            .put(MeltArmor.class, set(entity))
            .put(Nauseate.class, set(entity))
            .build();

    public interface TargetGetter {
        boolean findBlockTarget(ItemStack stack, EntityLivingBase player);

        List<EntityLivingBase> findPotionTarget(ItemStack stack, EntityLivingBase player);

        static TargetGetter tg(FindBlockTarget findBlockTarget, FindPotionTarget findPotionTarget) {
            return new TargetGetter() {
                @Override
                public boolean findBlockTarget(ItemStack stack, EntityLivingBase player) {
                    return findBlockTarget.apply(stack, player);
                }

                @Override
                public List<EntityLivingBase> findPotionTarget(ItemStack stack, EntityLivingBase player) {
                    return findPotionTarget.apply(stack, player);
                }
            };
        }

        interface FindBlockTarget {
            boolean apply(ItemStack stack, EntityLivingBase player);
        }

        interface FindPotionTarget {
            List<EntityLivingBase> apply(ItemStack stack, EntityLivingBase player);
        }
    }

    private TargetGetter contingencyTargetGetter(ContingencyTypes contingencyType) {
        return tg((stack, player) -> false, (stack, player) -> {
            if (ExtendedProperties.For(player).getContingencyType() != contingencyType)
                return ImmutableList.of(player);
            else
                return ImmutableList.of();
        });
    }

    private TargetGetter directLineTarget(double rangeModifier) {
        return tg((stack, player) -> {
            double range = rangeModifier * SpellUtils.instance.getModifiedDouble_Add(SpellModifiers.RANGE, stack, player, player, player.worldObj, 0);
            boolean targetWater = SpellUtils.instance.modifierIsPresent(SpellModifiers.TARGET_NONSOLID_BLOCKS, stack, 0);
            MovingObjectPosition mop = ItemsCommonProxy.spell.getMovingObjectPosition(player, player.worldObj, range, true, targetWater);
            return mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK;
        }, (stack, player) -> {
            double range = rangeModifier * SpellUtils.instance.getModifiedDouble_Add(SpellModifiers.RANGE, stack, player, player, player.worldObj, 0);
            boolean targetWater = SpellUtils.instance.modifierIsPresent(SpellModifiers.TARGET_NONSOLID_BLOCKS, stack, 0);
            MovingObjectPosition mop = ItemsCommonProxy.spell.getMovingObjectPosition(player, player.worldObj, range, true, targetWater);
            if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                Entity e = mop.entityHit;
                if (e instanceof EntityDragonPart && ((EntityDragonPart) e).entityDragonObj instanceof EntityLivingBase) {
                    e = (EntityLivingBase) ((EntityDragonPart) e).entityDragonObj;
                }
                if (e instanceof EntityLivingBase)
                    return ImmutableList.of((EntityLivingBase) e);
            }
            return ImmutableList.of();
        });
    }

    private Map<Class<? extends ISpellShape>, TargetGetter> targetGetters = ImmutableMap.<Class<? extends ISpellShape>, TargetGetter>builder()
            .put(AoE.class, tg((stack, player) -> {
                int radius = (int) Math.floor(SpellUtils.instance.getModifiedDouble_Add(1.0D, stack, player, player, player.worldObj, 0, SpellModifiers.RADIUS));
                for (int i = -radius; i <= radius; ++i) {
                    for (int j = -radius; j <= radius; ++j) {
                        Block block = player.worldObj.getBlock(((int) player.posX), ((int) player.posY) + i, ((int) player.posZ) + j);
                        if (block != Blocks.air) {
                            return true;
                        }
                    }
                }
                return false;
            }, (stack, player) -> {
                double radius = SpellUtils.instance.getModifiedDouble_Add(1.0D, stack, player, player, player.worldObj, 0, SpellModifiers.RADIUS);
                return player.worldObj.getEntitiesWithinAABBExcludingEntity(
                        player,
                        AxisAlignedBB.getBoundingBox(player.posX - radius, player.posY - radius, player.posZ - radius, player.posX + radius, player.posY + radius, player.posZ + radius),
                        e -> e instanceof EntityLivingBase
                );
            }))
            .put(Channel.class, tg((stack, player) -> false, (stack, player) -> ImmutableList.of(player)))
            .put(MissingShape.class, tg((stack, player) -> false, (stack, player) -> ImmutableList.of()))
            .put(Self.class, tg((stack, player) -> false, (stack, player) -> ImmutableList.of(player)))
            .put(Zone.class, tg((stack, player) -> false, (stack, player) -> {
                int radius = SpellUtils.instance.getModifiedInt_Add(2, stack, player, player, player.worldObj, 0, SpellModifiers.RADIUS);
                return player.worldObj.getEntitiesWithinAABB(EntityLivingBase.class, AxisAlignedBB.getBoundingBox(player.posX - radius, player.posY - 1.0D, player.posZ - radius, player.posX + radius, player.posY + 3, player.posZ + radius));
            }))
            .put(Beam.class, directLineTarget(1))
            .put(Binding.class, tg((__, ___) -> false, (__, ___) -> ImmutableList.of()))
            .put(Chain.class, tg((stack, player) -> false, (stack, player) -> {
                MovingObjectPosition mop = ItemsCommonProxy.spell.getMovingObjectPosition(player, player.worldObj, 8.0D, true, false);

                if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY && mop.entityHit instanceof EntityLivingBase) {
                    return ImmutableList.of(((EntityLivingBase) mop.entityHit));
                } else
                    return ImmutableList.of();
            }))
            .put(Contingency_Death.class, contingencyTargetGetter(ContingencyTypes.DEATH))
            .put(Contingency_Fall.class, contingencyTargetGetter(ContingencyTypes.FALL))
            .put(Contingency_Fire.class, contingencyTargetGetter(ContingencyTypes.ON_FIRE))
            .put(Contingency_Health.class, contingencyTargetGetter(ContingencyTypes.HEALTH_LOW))
            .put(Contingency_Hit.class, contingencyTargetGetter(ContingencyTypes.DAMAGE_TAKEN))
            .put(Projectile.class, directLineTarget(10))
            .put(Rune.class, tg((stack, player) -> {
                boolean targetWater = SpellUtils.instance.modifierIsPresent(SpellModifiers.TARGET_NONSOLID_BLOCKS, stack, 0);
                MovingObjectPosition mop = ItemsCommonProxy.spell.getMovingObjectPosition(player, player.worldObj, 8.0D, true, targetWater);
                return mop != null && mop.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY;
            }, (stack, player) -> ImmutableList.of()))
            .put(Touch.class, tg((stack, player) -> {
                boolean targetWater = SpellUtils.instance.modifierIsPresent(SpellModifiers.TARGET_NONSOLID_BLOCKS, stack, 0);
                MovingObjectPosition mop = ItemsCommonProxy.spell.getMovingObjectPosition(player, player.worldObj, 2.5D, true, targetWater);
                return mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK;
            }, (stack, player) -> {
                boolean targetWater = SpellUtils.instance.modifierIsPresent(SpellModifiers.TARGET_NONSOLID_BLOCKS, stack, 0);
                MovingObjectPosition mop = ItemsCommonProxy.spell.getMovingObjectPosition(player, player.worldObj, 2.5D, true, targetWater);
                if (mop != null && mop.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                    Entity e = mop.entityHit;
                    if (e instanceof EntityDragonPart && ((EntityDragonPart) e).entityDragonObj instanceof EntityLivingBase)
                        e = (EntityLivingBase) ((EntityDragonPart) e).entityDragonObj;
                    if (e instanceof EntityLivingBase)
                        return ImmutableList.of(((EntityLivingBase) e));
                }
                return ImmutableList.of();
            }))
            .put(Wall.class, tg((stack, player) -> false, (stack, player) -> {
                double radius = SpellUtils.instance.getModifiedInt_Mul(3, stack, player, player, player.worldObj, 0, SpellModifiers.RADIUS);
                return player.worldObj.getEntitiesWithinAABBExcludingEntity(
                        player,
                        AxisAlignedBB.getBoundingBox(player.posX - radius, player.posY - 1.0D, player.posZ - radius, player.posX + radius, player.posY + 3.0D, player.posZ + radius),
                        e -> e instanceof EntityLivingBase
                );
            }))
            .put(Wave.class, tg((stack, player) -> player.isSneaking(), (stack, player) -> ImmutableList.of()))
            .build();

    private Map<Class<? extends ISpellComponent>, Potion> potionByComponent = ImmutableMap.<Class<? extends ISpellComponent>, Potion>builder()
            .put(AstralDistortion.class, astralDistortion)
            .put(Blind.class, Potion.blindness)
            .put(Charm.class, charmed)
            .put(ChronoAnchor.class, temporalAnchor)
            .put(Entangle.class, entangled)
            .put(Flight.class, flight)
            .put(Freeze.class, frostSlowed)
            .put(Fury.class, fury)
            .put(GravityWell.class, gravityWell)
            .put(Haste.class, haste)
            .put(Invisiblity.class, Potion.invisibility)
            .put(Leap.class, leap)
            .put(Levitation.class, levitation)
            .put(ManaShield.class, manaShield)
            .put(NightVision.class, Potion.nightVision)
            .put(Reflect.class, spellReflect)
            .put(Regeneration.class, regeneration)
            .put(Shield.class, magicShield)
            .put(Shrink.class, shrink)
            .put(Silence.class, silence)
            .put(Slow.class, frostSlowed)
            .put(Slowfall.class, slowfall)
            .put(SwiftSwim.class, swiftSwim)
            .put(TrueSight.class, trueSight)
            .put(WaterBreathing.class, waterBreathing)
            .put(WateryGrave.class, wateryGrave)
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
