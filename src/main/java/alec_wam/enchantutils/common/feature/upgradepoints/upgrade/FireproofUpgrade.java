package alec_wam.enchantutils.common.feature.upgradepoints.upgrade;

import java.util.List;

import com.google.common.collect.Lists;

import alec_wam.enchantutils.EnchantmentUtils;
import alec_wam.enchantutils.common.util.LangUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class FireproofUpgrade implements IBaseUpgrade {

	public static final ResourceLocation ID = EnchantmentUtils.resourceL("fireproof");
	public static final int MAX_LEVEL = 1;
	
	@Override
	public ResourceLocation getID() {
		return ID;
	}

	@Override
	public String getDisplayName(int level) {
		return LangUtil.localize("upgrade.fireproof");
	}

	@Override
	public List<ITextComponent> getDescription(ItemStack stack, int level) {
		return Lists.newArrayList(new TranslationTextComponent("enchantutils.upgrade.fireproof.tooltip"));
	}

	@Override
	public int getPointCost(ItemStack stack) {
		return 1;
	}

	@Override
	public int getMaxLevel() {
		return MAX_LEVEL;
	}

	@Override
	public boolean canApply(ItemStack stack) {
		return true;
	}
	
	@Override
	public ResourceLocation getIcon(){
		return EnchantmentUtils.resourceL("textures/gui/upgrade/"+getID().getPath()+".png");
	}

}
