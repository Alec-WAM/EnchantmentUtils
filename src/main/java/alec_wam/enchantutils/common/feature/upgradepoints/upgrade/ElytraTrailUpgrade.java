package alec_wam.enchantutils.common.feature.upgradepoints.upgrade;

import java.awt.Color;
import java.util.List;

import com.google.common.collect.Lists;

import alec_wam.enchantutils.EnchantmentUtils;
import alec_wam.enchantutils.common.feature.upgradepoints.UpgradePointManager;
import alec_wam.enchantutils.common.util.ItemNBTHelper;
import alec_wam.enchantutils.common.util.LangUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ElytraItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class ElytraTrailUpgrade implements IBaseUpgrade {

	public static final ResourceLocation ID = EnchantmentUtils.resourceL("elytra_trail");
	public static final int MAX_LEVEL = 1;
	
	@Override
	public ResourceLocation getID() {
		return ID;
	}

	@Override
	public String getDisplayName(int level) {
		return LangUtil.localize("upgrade.elytra_trail");
	}
	
	@Override
	public List<? extends ITextComponent> getToolTipName(PlayerEntity player, ItemStack stack, int lvl){
		List<ITextComponent> list = Lists.newArrayList();
		String name = getDisplayName(lvl);
		ElytraTrail trail = getTrailType(stack);
		if(trail !=null){
			name += " (" + trail.getDisplayName();
			
			IFormattableTextComponent nameComp = new StringTextComponent(name).mergeStyle(TextFormatting.DARK_PURPLE);
			Style colorStyle = null;
			if(trail == ElytraTrail.COLOR){
				int colorValue = getTrailColor(stack);
				String value = String.format("%06X", colorValue);
				
				colorStyle = Style.EMPTY.setColor(net.minecraft.util.text.Color.fromInt(colorValue));
				IFormattableTextComponent colorText = new StringTextComponent(" " + value).mergeStyle(colorStyle); 
				nameComp.append(colorText);
			}
			
			nameComp.appendString(")");
			list.add(nameComp);
		}
		
		return list;
	}

	@Override
	public List<ITextComponent> getDescription(ItemStack stack, int level) {
		return Lists.newArrayList(new TranslationTextComponent("enchantutils.upgrade.elytra_trail.tooltip"));
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
		return stack.getItem() instanceof ElytraItem;
	}
	
	@Override
	public ResourceLocation getIcon(){
		return EnchantmentUtils.resourceL("textures/gui/upgrade/"+getID().getPath()+".png");
	}
	
	@Override
	public boolean equalUpgrades(ItemStack stack1, ItemStack stack2){
		
		int lvl1 = UpgradePointManager.getUpgradeLevel(stack1, this);
		int lvl2 = UpgradePointManager.getUpgradeLevel(stack2, this);
		
		if(lvl1 == lvl2){
			return getTrailType(stack1) == getTrailType(stack2) && getTrailColor(stack1) == getTrailColor(stack2);
		}
		
		return false;
	}
	
	public static void setTrailType(ItemStack stack, ElytraTrail trail, int color){
		CompoundNBT nbt = ItemNBTHelper.getCompound(stack).getCompound(UpgradePointManager.NBT_TAG);
		CompoundNBT trailNBT = new CompoundNBT();
		trailNBT.putString("Type", trail.name().toLowerCase());
		if(trail == ElytraTrail.COLOR){
			trailNBT.putInt("Color", color);
		}
		nbt.put("ElytraTrail", trailNBT);
		ItemNBTHelper.getCompound(stack).put(UpgradePointManager.NBT_TAG, nbt);
	}
	
	public static ElytraTrail getTrailType(ItemStack stack){
		CompoundNBT nbt = ItemNBTHelper.getCompound(stack).getCompound(UpgradePointManager.NBT_TAG);
		if(nbt.contains("ElytraTrail")){
			CompoundNBT trailNBT = nbt.getCompound("ElytraTrail");
			if(!trailNBT.isEmpty()){
				String type = trailNBT.getString("Type");
				for(ElytraTrail trail : ElytraTrail.values()){
					if(trail.name().equalsIgnoreCase(type)){
						return trail;
					}
				}
			}
		}
		return null;
	}
	
	public static int getTrailColor(ItemStack stack){
		CompoundNBT nbt = ItemNBTHelper.getCompound(stack).getCompound(UpgradePointManager.NBT_TAG);
		if(nbt.contains("ElytraTrail")){
			CompoundNBT trailNBT = nbt.getCompound("ElytraTrail");
			if(!trailNBT.isEmpty()){
				return trailNBT.getInt("Color");
			}
		}
		return Color.WHITE.getRGB();
	}
	
	public static enum ElytraTrail {
		BUBBLE, FLAME, GLINT, SPARKLE, HEART, COLOR;
		
		public String getDisplayName(){
			return LangUtil.localize("upgrade.elytra_trail."+name().toLowerCase());
		}
	}

}
