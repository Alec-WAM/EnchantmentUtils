package alec_wam.enchantutils.common.feature.upgradepoints.upgrade;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

import alec_wam.enchantutils.Config;
import alec_wam.enchantutils.EnchantmentUtils;
import alec_wam.enchantutils.common.feature.upgradepoints.UpgradePointManager;
import alec_wam.enchantutils.common.util.LangUtil;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class SoulBoundUpgrade implements IBaseUpgrade {

	public static final ResourceLocation ID = EnchantmentUtils.resourceL("soulbound");
	public static final int MAX_LEVEL = 1;
	
	@Override
	public ResourceLocation getID() {
		return ID;
	}

	@Override
	public String getDisplayName(int level) {
		return LangUtil.localize("upgrade.soulbound");
	}

	public static boolean isConsumedOnDeath(){
		return Config.COMMON.upgrade_soulbound_consume.get();
	}
	
	@Override
	public List<ITextComponent> getDescription(ItemStack stack, int level) {
		List<ITextComponent> list = Lists.newArrayList();
		list.add(new TranslationTextComponent("enchantutils.upgrade.soulbound.tooltip"));
		if(isConsumedOnDeath()){
			list.add(new TranslationTextComponent("enchantutils.upgrade.soulbound.tooltip.consume").mergeStyle(TextFormatting.RED));				
		}
		return list;
	}

	@Override
	public int getPointCost(ItemStack stack) {
		return isConsumedOnDeath() ? 1 : 3;
	}

	@Override
	public int getMaxLevel() {
		return MAX_LEVEL;
	}

	@Override
	public boolean canApply(ItemStack stack) {
		return !EnchantmentHelper.hasVanishingCurse(stack);
	}
	
	@Override
	public ResourceLocation getIcon(){
		return EnchantmentUtils.resourceL("textures/gui/upgrade/"+getID().getPath()+".png");
	}

	public static String NBT_SOULBOUND_ITEMS = EnchantmentUtils.resourceDot("soulbound_items");
	
	public static void saveDrops(PlayerEntity player, Collection<ItemEntity> drops) {
		List<ItemEntity> savedDrops = Lists.newArrayList();
		for (ItemEntity drop : drops) {
			ItemStack item = drop.getItem();
			if (UpgradePointManager.getUpgradeLevel(item, UpgradePointManager.UPGRADE_SOULBOUND) > 0) {								
				savedDrops.add(drop);
			}
		}

		savedDrops.forEach(dropItem -> {
			drops.remove(dropItem);
		});
		
		if(!savedDrops.isEmpty()){
			//Save Items to NBT
			CompoundNBT nbt = new CompoundNBT();
			nbt.putInt("Count", savedDrops.size());
			int counter = 0;
	
			for (ItemEntity drop : savedDrops) {
				if (drop.getItem() != null) {					
					ItemStack copyStack = drop.getItem().copy();
					if(isConsumedOnDeath()){
						UpgradePointManager.removeUpgrade(copyStack, UpgradePointManager.UPGRADE_SOULBOUND);
					}
					CompoundNBT dropNBT = copyStack.serializeNBT();
					nbt.put("Stack_" + counter, dropNBT);
					counter++;
				}
			}
	
			player.getPersistentData().put(NBT_SOULBOUND_ITEMS, nbt);
		}
	}
	
	public static boolean hasSavedDrops(PlayerEntity player) {
		return player.getPersistentData().contains(NBT_SOULBOUND_ITEMS);
	}
	
	public static void loadDrops(PlayerEntity oldPlayer, PlayerEntity newPlayer){
		if(hasSavedDrops(oldPlayer)){
			List<ItemStack> items = Lists.newArrayList();
			CompoundNBT nbt = oldPlayer.getPersistentData().getCompound(NBT_SOULBOUND_ITEMS);
			int count = nbt.getInt("Count");
			for(int i = 0; i < count; i++){
				CompoundNBT nbtItem = nbt.getCompound("Stack_"+i);
				ItemStack stack = ItemStack.read(nbtItem);

				if (!stack.isEmpty()) {
					items.add(stack);
				}

				nbt.remove("Stack_"+i);
			}
			
			oldPlayer.getPersistentData().remove(NBT_SOULBOUND_ITEMS);
			
			//Give items
			if (items.isEmpty())
				return;
			for (ItemStack item : items) {
				newPlayer.inventory.addItemStackToInventory(item);
			}
			
			if(isConsumedOnDeath())newPlayer.sendMessage(new TranslationTextComponent(EnchantmentUtils.resourceDot("message.soulbound.consume")).mergeStyle(TextFormatting.YELLOW), Util.DUMMY_UUID);
		}
	}

}
