package hohserg.mcrop;

import com.google.common.collect.Sets;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.config.Configuration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Config {

    public static boolean xpRewardByWearingRings = false;
    public static int clientSideTrottling = 0;
    public static Set<String> allowedSpellParts = new HashSet<>();
    public static Map<String, Integer> spellPartCooldowns = new HashMap<>();

    public static void load(FMLPreInitializationEvent event) {
        Configuration cfg = new Configuration(event.getSuggestedConfigurationFile());
        xpRewardByWearingRings = cfg.get(Configuration.CATEGORY_GENERAL, "xpRewardByWearingRings", false, "if true using rings will give xp and affinity").getBoolean(false);
        clientSideTrottling = cfg.get(Configuration.CATEGORY_GENERAL, "clientSideTrottling", 0, "how many tick will be missed for check spell application. higher values may increase fps").getInt(0);
        allowedSpellParts = Sets.newHashSet(cfg.get(Configuration.CATEGORY_GENERAL, "allowedSpellParts", new String[]{
                "AoE", "Beam", "Chain", "Channel", "Projectile", "Rune", "Self", "Summon", "Touch", "Zone",
                "Contingency_Fall", "Contingency_Damage", "Contingency_Fire", "Contingency_Health", "Contingency_Death",
                "Wall", "Wave", "Accelerate", "AstralDistortion", "Attract", "BanishRain", "Blind", "Blink",
                "ChronoAnchor", "CreateWater", "Dig", "Disarm", "Dispel", "DivineIntervention", "Drought",
                "EnderIntervention", "Entangle", "FireDamage", "Flight", "Fling", "Forge", "Freeze", "FrostDamage",
                "GravityWell", "Grow", "HarvestPlants", "Haste", "Heal", "Ignition", "Invisibility", "Knockback", "Leap",
                "Levitate", "LifeDrain", "LifeTap", "Light", "LightningDamage", "MagicDamage", "ManaDrain", "Mark",
                "NightVision", "PhysicalDamage", "Plant", "Plow", "RandomTeleport", "Recall", "Reflect", "Regeneration",
                "Repel", "Rift", "Shield", "Slow", "Slowfall", "Storm", "SwiftSwim", "Telekinesis", "Transplace",
                "TrueSight", "WaterBreathing", "WateryGrave", "Charm", "Drown", "Blizzard", "Daylight", "FallingStar",
                "FireRain", "ManaLink", "ManaShield", "Moonrise", "WizardsAutumn", "Appropriation", "Fury", "Silence",
                "PlaceBlock", "Shrink", "Bounce", "Speed", "Gravity", "Damage", "Healing", "VelocityAdded", "Radius",
                "Duration", "RuneProcs", "Range", "Lunar", "TargetNonSolid", "Solar", "Piercing", "Colour",
                "MiningPower", "Prosperity", "BuffPower", "Dismembering", "FeatherTouch"
        }, "list of spell parts which allow to use with ring shape").getStringList());

        for (String line : cfg.get(Configuration.CATEGORY_GENERAL, "spellPartCooldowns", new String[]{"Beam|10", "LightningDamage|100"}, "special ring cooldowns(in ticks) for spell parts").getStringList()) {
            String[] v = line.split("\\|");
            if (v.length == 2) {
                String spellPart = v[0];
                int cooldown = Integer.parseInt(v[1]);
                spellPartCooldowns.put(spellPart, cooldown);
            }
        }

        cfg.save();
    }

}
