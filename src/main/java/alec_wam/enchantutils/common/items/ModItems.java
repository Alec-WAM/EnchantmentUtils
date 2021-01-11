package alec_wam.enchantutils.common.items;

import alec_wam.enchantutils.common.util.RegistryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;

public class ModItems {

	public static Item UPGRADE_MODULE;
	
	public static Item UPGRADE_RANGE;
	public static Item UPGRADE_VACUUM;
	public static Item UPGRADE_CRIT;
	
	public static void constructItems() {
		UPGRADE_MODULE = new Item(new Item.Properties().group(ItemGroup.MISC));
		RegistryHelper.registerItem(UPGRADE_MODULE, "upgrade_module");
		
		UPGRADE_RANGE = new Item(new Item.Properties().group(ItemGroup.MISC).maxStackSize(16));
		RegistryHelper.registerItem(UPGRADE_RANGE, "upgrade_range");
		UPGRADE_VACUUM = new Item(new Item.Properties().group(ItemGroup.MISC).maxStackSize(16));
		RegistryHelper.registerItem(UPGRADE_VACUUM, "upgrade_vacuum");
		UPGRADE_CRIT = new Item(new Item.Properties().group(ItemGroup.MISC).maxStackSize(16));
		RegistryHelper.registerItem(UPGRADE_CRIT, "upgrade_crit");
	}

}
