package alec_wam.enchantutils.common.util;

import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.container.GrindstoneContainer;
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
import net.minecraft.world.World;

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
		if(stack.getItem() instanceof SwordItem)return true;
		if(stack.getItem() instanceof BowItem) return true;
		if(stack.getItem() instanceof CrossbowItem) return true;
		if(stack.getItem() instanceof TridentItem) return true;
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
}
