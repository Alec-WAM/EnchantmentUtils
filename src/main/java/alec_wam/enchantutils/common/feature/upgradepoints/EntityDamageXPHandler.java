package alec_wam.enchantutils.common.feature.upgradepoints;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

//https://github.com/SlimeKnights/TinkersToolLeveling/blob/master/src/main/java/slimeknights/toolleveling/capability/DamageXpHandler.java
public class EntityDamageXPHandler implements IDamageEntityXP, ICapabilitySerializable<ListNBT> {

	private static String TAG_PLAYER_UUID = "player_uuid";
	private static String TAG_DAMAGE_LIST = "damage_data";
	private static String TAG_ITEM = "item";
	private static String TAG_DAMAGE = "damage";

	private Map<UUID, Map<ItemStack, Double>> playerToDamageMap = new HashMap<>();

	public void addDamageFromTool(double damage, ItemStack tool, PlayerEntity player) {
		Map<ItemStack, Double> damageMap = playerToDamageMap.getOrDefault(player.getUniqueID(), new HashMap<ItemStack, Double>());

		damage += getDamageDealtByTool(tool, player);

		damageMap.put(tool, damage);
		playerToDamageMap.put(player.getUniqueID(), damageMap);
	}

	public double getDamageDealtByTool(ItemStack tool, PlayerEntity player) {
		Map<ItemStack, Double> damageMap = playerToDamageMap.getOrDefault(player.getUniqueID(), new HashMap<ItemStack, Double>());

		return damageMap.entrySet().stream()
				.filter(itemStackFloatEntry -> UpgradePointManager.areUpgradeItemsEqual(tool, itemStackFloatEntry.getKey()))
				.findFirst()
				.map(Map.Entry::getValue)
				.orElse(0.0D);
	}

	public void distributeXpToTools(LivingEntity deadEntity) {
		playerToDamageMap.forEach((uuid, itemStackFloatMap) -> distributeXpForPlayer(deadEntity.getEntityWorld(), uuid, itemStackFloatMap));
	}

	private void distributeXpForPlayer(World world, UUID playerUuid, Map<ItemStack, Double> damageMap) {
		Optional.ofNullable(world.getPlayerByUuid(playerUuid))
		.ifPresent(
				player -> damageMap.forEach(
						(itemStack, damage) -> distributeXpToPlayerForTool(player, itemStack, damage)
						)
				);
	}

	private void distributeXpToPlayerForTool(PlayerEntity player, ItemStack tool, double damage) {
		if(!tool.isEmpty() && player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).isPresent()) {
			IItemHandler itemHandler = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null).orElse(null);

			// check for identity. should work in most cases because the entity was killed without loading/unloading
			for(int i = 0; i < itemHandler.getSlots(); i++) {
				if(itemHandler.getStackInSlot(i) == tool) {
					UpgradePointManager.earnToolXP(player, tool, Math.round(damage), UpgradePointManager.getMaxXPDamage());
					return;
				}
			}

			// check for equal stack in case instance equality didn't find it
			for(int i = 0; i < itemHandler.getSlots(); i++) {
				if(UpgradePointManager.areUpgradeItemsEqual(itemHandler.getStackInSlot(i), tool)) {
					UpgradePointManager.earnToolXP(player, itemHandler.getStackInSlot(i), Math.round(damage), UpgradePointManager.getMaxXPDamage());
					return;
				}
			}
		}
	}

	@Override
	public ListNBT serializeNBT() {
		ListNBT playerList = new ListNBT();

		playerToDamageMap.forEach((uuid, itemStackFloatMap) -> playerList.add(convertPlayerDataToTag(uuid, itemStackFloatMap)));

		return playerList;
	}

	private CompoundNBT convertPlayerDataToTag(UUID uuid, Map<ItemStack, Double> itemStackFloatMap) {
		CompoundNBT tag = new CompoundNBT();
		tag.putUniqueId(TAG_PLAYER_UUID, uuid);

		ListNBT damageTag = new ListNBT();

		itemStackFloatMap.forEach((itemStack, damage) -> damageTag.add(convertItemDamageDataToTag(itemStack, damage)));

		tag.put(TAG_DAMAGE_LIST, damageTag);
		return tag;
	}

	private CompoundNBT convertItemDamageDataToTag(ItemStack stack, Double damage) {
		CompoundNBT tag = new CompoundNBT();

		CompoundNBT itemTag = stack.write(new CompoundNBT());
		tag.put(TAG_ITEM, itemTag.copy());
		tag.putDouble(TAG_DAMAGE, damage);

		return tag;
	}


	@Override
	public void deserializeNBT(ListNBT nbt) {
		playerToDamageMap = new HashMap<>();
		for(int i = 0; i < nbt.size(); i++) {
			CompoundNBT tag = nbt.getCompound(i);

			UUID playerUuid = tag.getUniqueId(TAG_PLAYER_UUID);
			ListNBT data = tag.getList(TAG_DAMAGE_LIST, 10);

			Map<ItemStack, Double> damageMap = new HashMap<>();

			for(int j = 0; j < data.size(); j++) {
				deserializeTagToMapEntry(damageMap, data.getCompound(j));
			}

			playerToDamageMap.put(playerUuid, damageMap);
		}
	}

	private void deserializeTagToMapEntry(Map<ItemStack, Double> damageMap, CompoundNBT tag) {
		ItemStack stack = ItemStack.read(tag.getCompound(TAG_ITEM));
		if(!stack.isEmpty()) {
			damageMap.put(stack, tag.getDouble(TAG_DAMAGE));
		}
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if(cap == CapabilityEntityDamageXP.CAPABILITY) {
			return LazyOptional.of(() -> this).cast();
		}
		return null;
	}
}
