package alec_wam.enchantutils.common.feature.upgradepoints.upgrade;

import java.util.List;

import com.google.common.collect.Lists;

import alec_wam.enchantutils.EnchantmentUtils;
import alec_wam.enchantutils.common.util.ItemUtil;
import alec_wam.enchantutils.common.util.LangUtil;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class DigSpeedUpgrade implements IBaseUpgrade {

	public static final ResourceLocation ID = EnchantmentUtils.resourceL("digspeed");
	public static final int MAX_LEVEL = 3;
	
	@Override
	public ResourceLocation getID() {
		return ID;
	}

	@Override
	public String getDisplayName(int level) {
		return LangUtil.localizeFormat("upgrade.digspeed", I18n.format("enchantment.level." + (level)));
	}

	@Override
	public List<ITextComponent> getDescription(ItemStack stack, int level) {
		List<ITextComponent> list = Lists.newArrayList();
		ITextComponent mainLine = new TranslationTextComponent("enchantutils.upgrade.digspeed.tooltip");
		list.add(mainLine);
		for(int i = 1; i <= getMaxLevel(); i++){
			float multi = 1.0F + (float)(i + 1) * 0.2F;
			String str = I18n.format("enchantment.level." + (i)) + ": x" + multi;
			list.add(new StringTextComponent(str));
		}
		return list;
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
		return ItemUtil.isDiggingTool(stack);
	}
	
	@Override
	public ResourceLocation getIcon(){
		return EnchantmentUtils.resourceL("textures/gui/upgrade/"+getID().getPath()+".png");
	}

}
