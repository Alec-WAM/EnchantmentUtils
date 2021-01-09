package alec_wam.enchantutils.common.items;

import alec_wam.enchantutils.common.util.RegistryHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;

public class ModItems {

	public static Item UPGRADE_MODULE;
	
	public static void constructItems() {
		UPGRADE_MODULE = new Item(new Item.Properties().group(ItemGroup.MISC));
		RegistryHelper.registerItem(UPGRADE_MODULE, "upgrade_module");
	}

}
