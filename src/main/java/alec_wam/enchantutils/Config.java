package alec_wam.enchantutils;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

public class Config {

	@Mod.EventBusSubscriber(modid = EnchantmentUtils.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Common
    {
        public final ForgeConfigSpec.BooleanValue upgrades_enabled;
        public final ForgeConfigSpec.DoubleValue upgrades_xp_dig; //500
        public final ForgeConfigSpec.DoubleValue upgrades_xp_weapon; //1000
        public final ForgeConfigSpec.DoubleValue upgrades_xp_mulit_dig_axe; //2
        public final ForgeConfigSpec.IntValue upgrades_levels_point; //10
        public final ForgeConfigSpec.BooleanValue upgrades_fakeplayer_digging; //true
        public final ForgeConfigSpec.BooleanValue upgrades_fakeplayer_weapon; //true
        public final ForgeConfigSpec.BooleanValue upgrades_shield_xp_proj; //false

        public final ForgeConfigSpec.BooleanValue upgrade_soulbound_consume; //false
        
        public Common(ForgeConfigSpec.Builder builder)
        {
            builder.comment("EnchantmentUtils Config");            

            builder.push("General Tool Upgrade Settings");

            upgrades_enabled = builder
                    .comment("If Tool Upgrades are enabled")
                    .translation(EnchantmentUtils.resourceDot("config.upgrade.enabled"))
                    .define("enableToolUpgrades", true);
            
            upgrades_xp_dig = builder
                    .comment("Digging XP needed to unlock an Upgrade Point")
                    .translation(EnchantmentUtils.resourceDot("config.upgrade.xpdig"))
                    .defineInRange("xpNeededDigTool", 500.0D, 0.0D, Double.MAX_VALUE);

            upgrades_xp_mulit_dig_axe = builder
                    .comment("Digging Bonus for Axe since it is a weapon as well")
                    .translation(EnchantmentUtils.resourceDot("config.upgrade.axe.multi"))
                    .defineInRange("xpAxeDigMulti", 2.0D, 0.0D, Double.MAX_VALUE);
            
            upgrades_xp_weapon = builder
                    .comment("Weapon XP needed to unlock an Upgrade Point")
                    .translation(EnchantmentUtils.resourceDot("config.upgrade.xpweapon"))
                    .defineInRange("xpNeededWeapon", 1000.0D, 0.0D, Double.MAX_VALUE);
            
            upgrades_levels_point = builder
                    .comment("Experience Levels per Upgrade Point")
                    .translation(EnchantmentUtils.resourceDot("config.upgrade.levels_point"))
                    .defineInRange("realXPForPoint", 10, 0, Integer.MAX_VALUE);
            
            upgrades_fakeplayer_digging = builder
                    .comment("If Fake Players are allowed to gain tool xp")
                    .translation(EnchantmentUtils.resourceDot("config.upgrade.fakeplayer.digging"))
                    .define("diggingFakePlayerAllowed", true);
            
            upgrades_fakeplayer_weapon = builder
                    .comment("If Fake Players are allowed to gain weapon xp")
                    .translation(EnchantmentUtils.resourceDot("config.upgrade.fakeplayer.weapon"))
                    .define("weaponFakePlayerAllowed", true);
            
            upgrades_shield_xp_proj = builder
                    .comment("If Sheilds gain tool xp from only projectiles")
                    .translation(EnchantmentUtils.resourceDot("config.upgrade.shield.proj"))
                    .define("shieldXPOnlyProj", false);
            
            builder.pop();
            
            builder.push("Upgrade Settings");

            upgrade_soulbound_consume = builder
                    .comment("If Soulbound upgrade is consumed on death")
                    .translation(EnchantmentUtils.resourceDot("config.upgrade.soulbound.consume"))
                    .define("soulboundConsumeUpgrade", false);
            
            builder.pop();
            
            /*cursesApplicable = builder
                    .comment("If curses can be applied with the enchant. **NOT RECOMMENDED**")
                    .translation("soulbound.config.curses_applicable")
                    .define("curses_applicable", false);

            maximumLevel = builder
                    .comment("Maximum level the enchant can be.")
                    .translation("soulbound.config.maximum_level")
                    .defineInRange("maximum_level", 3, 1, 32767);

            rarity = builder
                    .comment(("How rare the enchantment is at the enchantment table (some loot spawns included).\n" +
                            "Acceptable values are listed below.\n" +
                            "1 - COMMON\n" +
                            "2 - UNCOMMON\n" +
                            "3 - RARE\n" +
                            "4 - VERY RARE"))
                    .translation("soulbound.config.rarity")
                    .defineInRange("rarity", 4, 1, 4);

            builder.pop();
            
            builder.push("Level Drop Values");

            dropLevel = builder
                    .comment("Chance for the enchant to drop down 1 level on death from 0.00 to 1.00.")
                    .translation("soulbound.config.drop_level")
                    .defineInRange("drop_level", 1.0, 0.0, 1.0);

            additiveDropChance = builder
                    .comment("Chance for enchant to drop down 1 level with every added level\n" +
                            "So if someone with Soulbound 3 dies the chance of the enchant being downgraded would be:\n" +
                            "(dropLevel) - ((level - 1) * additiveDropChance)\n" +
                            "Remember if you've set a lot of levels this could lead to the higher levels never dropping down")
                    .translation("soulbound.config.additive_drop_chance")
                    .defineInRange("additive_drop_chance", 0.0, 0.0, 1.0);

            builder.pop();
            builder.push("Save Chance Values");

            additiveSaveChance = builder
                    .comment("Chance for item to be kept from 0.00 to 1.00 with every added level.\n" +
                            "So if someone with Soulbound 3 dies their chances of keeping the item would be:\n" +
                            ("(saveChance) + ((level - 1) * additiveSaveChance)\n" +
                                    "Remember if you've set a lot of levels this could lead to the higher levels always being saved."))
                    .translation("soulbound.config.additive_save_chance")
                    .defineInRange("additive_save_chance", 0.0, 0.0, 1.0);

            saveChance = builder // done
                    .comment("Chance for item with enchant to be kept from 0.00 to 1.00.")
                    .translation("soulbound.config.save_chance")
                    .defineInRange("save_chance", 1.0, 0.0, 1.0);

            builder.pop();
            builder.push("Durability Drop Values");

            durabilityDrop = builder
                    .comment("Set whether durability drop is enabled or not. Durability drop is calculated with the min and max \n"
                            + "variables. The values chosen will most likely be in the middle and get rarer towards the ends. (triangular distribution)")
                    .translation("soulbound.config.durability_drop")
                    .define("durability_drop", false);

            breakItemOnZeroDurability = builder
                    .comment("If set to true, the item will be broken if the durability reaches 0 on it's durabilityDrop")
                    .translation("soulbound.config.break_item_on_zero_durability")
                    .define("break_item_on_zero_durability", false);

            additiveDurabilityDrop = builder
                    .comment("Subtracts this number from the max, min, and mode each level effectively making the durability \n" +
                            "drop go down the higher the level")
                    .translation("soulbound.config.additive_durability_drop")
                    .defineInRange("additive_durability_drop", 0.05, 0.0, 1.0);

            maximumDurabilityDrop = builder
                    .comment("Defines the minimum percentage that the durability goes down when returned (this percentage is of\n" +
                            "the items max durability NOT the actual durability)")
                    .translation("soulbound.config.maximum_durability_drop")
                    .defineInRange("maximum_durability_drop", 0.35, 0.0, 1.0);

            minimumDurabilityDrop = builder
                    .comment("Defines the maximum percentage that the durability goes down when returned (this percentage is of\n" +
                            "the items max durability NOT the actual durability)")
                    .translation("soulbound.config.minimum_durability_drop")
                    .defineInRange("minimum_durability_drop", 0.20, 0.0, 1.0);

            modeDurabilityDrop = builder
                    .comment("Defines the mode (average value) percentage that the durability goes down when returned. This value\n" +
                            "cannot be more than the maximum or less than the minimum. (this percentage is of\n" +
                            "the items max durability NOT the actual durability)")
                    .translation("soulbound.config.mode_durability_drop")
                    .defineInRange("mode_durability_drop", 0.25, 0.0, 1.0);

            builder.pop();*/
        }
    }

    public static final ForgeConfigSpec COMMON_SPEC;
    public static final Common COMMON;
    static {
        final Pair<Common, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Common::new);
        COMMON_SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    @SubscribeEvent
    public static void onLoad(final ModConfig.Loading event)
    {

    }

    @SubscribeEvent
    public static void onFileChange(final ModConfig.Reloading event)
    {

    }
}
