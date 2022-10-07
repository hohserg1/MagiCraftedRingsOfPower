package hohserg.mcrop;

import am2.AMCore;
import am2.api.ArsMagicaApi;
import am2.api.spell.enums.SkillPointTypes;
import am2.api.spell.enums.SkillTrees;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import hohserg.mcrop.items.RingOfPower;
import hohserg.mcrop.render.ItemRingRenderer;
import hohserg.mcrop.spell.RingShape;
import hohserg.mcrop.spell.RingShapeUnlockManager;
import lotr.common.LOTRMod;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.oredict.OreDictionary;

@Mod(modid = "mcrop", name = "MagiCraftedRingsOfPower", dependencies = "required-after:arsmagica2;required-after:lotr;required-after:Baubles")
public class Main {


    public static boolean xpRewardByWearingRings = false;

    @Mod.EventHandler
    public void init(FMLPreInitializationEvent event) {
        Configuration cfg = new Configuration(event.getSuggestedConfigurationFile());
        xpRewardByWearingRings = cfg.get(Configuration.CATEGORY_GENERAL, "xpRewardByWearingRings", false, "if true using rings will give xp and affinity").getBoolean(false);

        ringItem = new RingOfPower();
        GameRegistry.registerItem(ringItem.setUnlocalizedName("ring_of_power"), "ring_of_power");
    }

    @SideOnly(Side.CLIENT)
    @Mod.EventHandler
    public void initClient(FMLPreInitializationEvent event) {
        MinecraftForgeClient.registerItemRenderer(ringItem, new ItemRingRenderer());
    }

    public static RingOfPower ringItem;
    public static final RingShape ringShape = new RingShape();

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        ArsMagicaApi.instance.registerSkillTreeEntry(ringShape, "Ring", SkillTrees.Utility, 75, 135 + 45, SkillPointTypes.SILVER);
        AMCore.skillConfig.save();
        MinecraftForge.EVENT_BUS.register(new RingShapeUnlockManager());

        OreDictionary.registerOre("lotrRing", LOTRMod.mithrilRing);
        OreDictionary.registerOre("lotrRing", LOTRMod.goldRing);
        OreDictionary.registerOre("lotrRing", LOTRMod.silverRing);
    }
}
