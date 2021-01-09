package alec_wam.enchantutils.common.blocks;

import net.minecraft.item.ItemStack;

public interface INBTDrop {

	public void writeToItemNBT(ItemStack stack);
	
	public void readFromItemNBT(ItemStack stack);
	
}
