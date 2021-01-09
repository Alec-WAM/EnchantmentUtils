package alec_wam.enchantutils.common.feature.upgradepoints;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.INBTSerializable;

public interface IDamageEntityXP extends INBTSerializable<ListNBT> {

	void addDamageFromTool(double damage, ItemStack tool, PlayerEntity player);

	double getDamageDealtByTool(ItemStack tool, PlayerEntity player);

	void distributeXpToTools(LivingEntity deadEntity);
}
