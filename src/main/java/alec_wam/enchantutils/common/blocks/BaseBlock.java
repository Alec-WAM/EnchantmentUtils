package alec_wam.enchantutils.common.blocks;

import alec_wam.enchantutils.common.util.RegistryHelper;
import net.minecraft.block.Block;
import net.minecraft.item.ItemGroup;

public class BaseBlock extends Block {

	public BaseBlock(String regname, ItemGroup creativeTab, Properties properties) {
		super(properties);
		
		RegistryHelper.registerBlock(this, regname, creativeTab);
	}
	
}
