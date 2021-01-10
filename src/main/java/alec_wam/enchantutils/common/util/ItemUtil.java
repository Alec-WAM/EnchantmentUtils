package alec_wam.enchantutils.common.util;

import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.AxeItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.HoeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.item.SwordItem;
import net.minecraft.item.TridentItem;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class ItemUtil {

	public static String getEnchantmentWithLevel(Enchantment ench, int lvl){
		String s = LangUtil.translateToLocal(ench.getName());

        if (ench.isCurse())
        {
            s = TextFormatting.RED + s;
        }
        
        if(lvl == 1 && ench.getMaxLevel() == 1){
        	return s;
        }
        
        String level = LangUtil.canBeTranslated("enchantment.level." + lvl) ? LangUtil.translateToLocal("enchantment.level." + lvl) : ""+lvl;

        return s + " " + level;
	}
	
	public static boolean isDiggingTool(ItemStack stack){
		Item item = stack.getItem();
		if(item instanceof ShovelItem) return true;
		if(item instanceof PickaxeItem) return true;
		if(item instanceof HoeItem) return true;
		if(item instanceof AxeItem) return true;
		return false;
	}
	
	public static boolean isWeapon(ItemStack stack){
		Item item = stack.getItem();
		if(isSword(stack))return true;
		if(item instanceof BowItem) return true;
		if(item instanceof CrossbowItem) return true;
		if(item instanceof TridentItem) return true;
		return false;
	}
	
	public static boolean isSword(ItemStack stack){
		Item item = stack.getItem();
		if(item instanceof SwordItem)return true;
		if(item instanceof AxeItem) return true;
		return false;
	}

    public static int getEnchantmentXp(ItemStack stack) {
       int l = 0;
       Map<Enchantment, Integer> map = EnchantmentHelper.getEnchantments(stack);

       for(Entry<Enchantment, Integer> entry : map.entrySet()) {
          Enchantment enchantment = entry.getKey();
          Integer integer = entry.getValue();
          if (!enchantment.isCurse()) {
             l += enchantment.getMinEnchantability(integer);
          }
       }

       return l;
    }
    
    public static boolean isInventoryFull(IItemHandler itemHandler)
    {
        for (int slot = 0; slot < itemHandler.getSlots(); slot++)
        {
            ItemStack stackInSlot = itemHandler.getStackInSlot(slot);
            if (stackInSlot.isEmpty() || stackInSlot.getCount() < itemHandler.getSlotLimit(slot))
            {
                return false;
            }
        }
        return true;
    }
    
    public static boolean isInventoryEmpty(IItemHandler itemHandler)
    {
        for (int slot = 0; slot < itemHandler.getSlots(); slot++)
        {
            ItemStack stackInSlot = itemHandler.getStackInSlot(slot);
            if (stackInSlot.getCount() > 0)
            {
                return false;
            }
        }
        return true;
    }
    
    public static int doInsertItem(@Nullable IItemHandler inventory, @Nonnull ItemStack item) {
    	if (inventory == null || item.isEmpty()) {
    		return 0;
    	}
    	int startSize = item.getCount();
    	ItemStack res = insertItemStacked(inventory, item.copy(), false);
    	int val = startSize - res.getCount();
    	return val;
    }
    
    @Nonnull
    public static ItemStack insertItemStacked(@Nonnull IItemHandler inventory, @Nonnull ItemStack stack, boolean simulate) {
      if (!stack.isEmpty()) {

        // not stackable -> just insert into a new slot
        if (!stack.isStackable()) {
          return ItemHandlerHelper.insertItem(inventory, stack, simulate);
        }

        int sizeInventory = inventory.getSlots();
        int firstEmptyStack = -1;
        int origSize = stack.getCount();

        // go through the inventory and try to fill up already existing items
        for (int i = 0; i < sizeInventory; i++) {
          ItemStack slot = inventory.getStackInSlot(i);
          if (ItemHandlerHelper.canItemStacksStackRelaxed(slot, stack)) {
            stack = inventory.insertItem(i, stack, simulate);

            if ((simulate && stack.getCount() != origSize) || stack.isEmpty()) {
              // stack has been completely inserted, or we are are simulating and have a partial insert. As inventories may change their acceptance rules after a
              // partial insert, we stop here as the simulated insert doesn't do that.
              return stack;
            }
          } else if (firstEmptyStack < 0 && slot.isEmpty()) {
            firstEmptyStack = i;
          }
        }

        // insert remainder into empty slot
        if (!stack.isEmpty() && firstEmptyStack >= 0) {
          stack = inventory.insertItem(firstEmptyStack, stack, simulate);
          if ((!simulate || stack.getCount() == origSize) && !stack.isEmpty()) {
            // same "partial insert" issue as above
            for (int i = 0; i < sizeInventory; i++) {
              stack = inventory.insertItem(i, stack, simulate);
              if ((simulate && stack.getCount() != origSize) || stack.isEmpty()) {
                return stack;
              }
            }
          }
        }
      }

      return stack;
    }
}
