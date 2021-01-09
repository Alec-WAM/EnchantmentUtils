package alec_wam.enchantutils.common.loot;

import alec_wam.enchantutils.EnchantmentUtils;
import net.minecraft.loot.LootConditionType;
import net.minecraft.util.registry.Registry;

public class ModLootModifiers {

	public static LootConditionType UPGRADE;
	
	public static void setupLoot(){
		UPGRADE = Registry.register(Registry.LOOT_CONDITION_TYPE, EnchantmentUtils.resourceL("upgrade_check"), new LootConditionType(new UpgradeLootCondition.Serializer()));        
	}
	
}
