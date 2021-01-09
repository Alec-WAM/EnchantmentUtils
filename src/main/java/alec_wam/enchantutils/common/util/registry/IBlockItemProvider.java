package alec_wam.enchantutils.common.util.registry;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;

public interface IBlockItemProvider {

	BlockItem provideItemBlock(Block block, Item.Properties props);
	
}
