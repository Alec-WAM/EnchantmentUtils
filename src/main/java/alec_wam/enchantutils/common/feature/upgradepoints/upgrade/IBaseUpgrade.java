package alec_wam.enchantutils.common.feature.upgradepoints.upgrade;

import java.util.List;

import com.google.common.collect.Lists;

import alec_wam.enchantutils.common.feature.upgradepoints.UpgradePointManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public interface IBaseUpgrade {

	public ResourceLocation getID();
	
	public String getDisplayName(int level);
	
	public List<ITextComponent> getDescription(ItemStack stack, int level);
	
	public int getPointCost(ItemStack stack);
	
	public int getMaxLevel();
	
	public boolean canApply(ItemStack stack);

	public ResourceLocation getIcon();

	public default List<? extends ITextComponent> getToolTipName(PlayerEntity player, ItemStack stack, int lvl){
		return Lists.newArrayList(new StringTextComponent(TextFormatting.DARK_PURPLE + getDisplayName(lvl)));
	}
	
	public default boolean equalUpgrades(ItemStack stack1, ItemStack stack2){
		
		int lvl1 = UpgradePointManager.getUpgradeLevel(stack1, this);
		int lvl2 = UpgradePointManager.getUpgradeLevel(stack2, this);
		
		return lvl1 == lvl2;
	}
}
