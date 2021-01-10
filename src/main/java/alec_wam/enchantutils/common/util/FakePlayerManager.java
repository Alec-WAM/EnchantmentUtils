package alec_wam.enchantutils.common.util;

import java.lang.ref.WeakReference;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;

import alec_wam.enchantutils.EnchantmentUtils;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SEntityEquipmentPacket;
import net.minecraft.network.play.server.SEntityStatusPacket;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;

public class FakePlayerManager {
	private static final WeakHashMap<World, FakePlayer> FAKE_PLAYERS = new WeakHashMap<World, FakePlayer>();

	private static WeakReference<FakePlayer> ENCHANTUTILS_PLAYER = null;
	private static final String PLAYER_NAME = "["+EnchantmentUtils.MOD_ID+"]";
	public static final UUID ENCHANTUTILS_UUID = UUID.nameUUIDFromBytes(PLAYER_NAME.getBytes());
	public static final GameProfile ENCHANTUTILS = new GameProfile(ENCHANTUTILS_UUID, PLAYER_NAME);

	private static FakePlayer getEnchantmentUtilsPlayer(ServerWorld world)
	{
		FakePlayer ret = ENCHANTUTILS_PLAYER != null ? ENCHANTUTILS_PLAYER.get() : null;
		if (ret == null)
		{
			ret = FakePlayerFactory.get(world, ENCHANTUTILS);
			ENCHANTUTILS_PLAYER = new WeakReference<FakePlayer>(ret);
		}
		return ret;
	}

	public static FakePlayer getPlayer(final ServerWorld w)
	{
		if(w == null)
		{
			throw new InvalidParameterException( "World is null." );
		}

		final FakePlayer wrp = FAKE_PLAYERS.get(w);
		if(wrp != null)
		{
			return wrp;
		}

		final FakePlayer p = getEnchantmentUtilsPlayer(w);
		FAKE_PLAYERS.put(w, p);
		return p;
	}
	
	public static void updateEquipment(FakePlayer player){
		Map<EquipmentSlotType, ItemStack> map = getItemMap(player);
		if (map != null) {
			updateSlots(player, map);
			if (!map.isEmpty()) {
				syncSlots(player, map);
			}
		}
	}

	@Nullable
	private static Map<EquipmentSlotType, ItemStack> getItemMap(FakePlayer player) {
		Map<EquipmentSlotType, ItemStack> map = null;

		for(EquipmentSlotType equipmentslottype : EquipmentSlotType.values()) {
			ItemStack itemstack;
			switch(equipmentslottype.getSlotType()) {
			case HAND:
				itemstack = player.getItemStackFromSlot(equipmentslottype);
				break;
			case ARMOR:
				itemstack = player.getItemStackFromSlot(equipmentslottype);
				break;
			default:
				continue;
			}

			ItemStack itemstack1 = player.getItemStackFromSlot(equipmentslottype);
			if (!ItemStack.areItemStacksEqual(itemstack1, itemstack)) {
				if (map == null) {
					map = Maps.newEnumMap(EquipmentSlotType.class);
				}

				map.put(equipmentslottype, itemstack1);
				if (!itemstack.isEmpty()) {
					player.getAttributeManager().removeModifiers(itemstack.getAttributeModifiers(equipmentslottype));
				}

				if (!itemstack1.isEmpty()) {
					player.getAttributeManager().reapplyModifiers(itemstack1.getAttributeModifiers(equipmentslottype));
				}
			}
		}

		return map;
	}

	private static void updateSlots(FakePlayer player, Map<EquipmentSlotType, ItemStack> p_241342_1_) {
		ItemStack itemstack = p_241342_1_.get(EquipmentSlotType.MAINHAND);
		ItemStack itemstack1 = p_241342_1_.get(EquipmentSlotType.OFFHAND);
		if (itemstack != null && itemstack1 != null && ItemStack.areItemStacksEqual(itemstack, player.getHeldItemOffhand()) && ItemStack.areItemStacksEqual(itemstack1, player.getHeldItemMainhand())) {
			((ServerWorld)player.world).getChunkProvider().sendToAllTracking(player, new SEntityStatusPacket(player, (byte)55));
			p_241342_1_.remove(EquipmentSlotType.MAINHAND);
			p_241342_1_.remove(EquipmentSlotType.OFFHAND);
			player.setHeldItem(Hand.MAIN_HAND, itemstack.copy());
			player.setHeldItem(Hand.OFF_HAND, itemstack1.copy());
		}
	}
	
	private static void syncSlots(FakePlayer player, Map<EquipmentSlotType, ItemStack> p_241344_1_) {
	      List<Pair<EquipmentSlotType, ItemStack>> list = Lists.newArrayListWithCapacity(p_241344_1_.size());
	      p_241344_1_.forEach((p_241341_2_, p_241341_3_) -> {
	         ItemStack itemstack = p_241341_3_.copy();
	         list.add(Pair.of(p_241341_2_, itemstack));
	         switch(p_241341_2_.getSlotType()) {
	         case HAND:
	        	 player.setItemStackToSlot(p_241341_2_, itemstack);
	            break;
	         case ARMOR:
	        	 player.setItemStackToSlot(p_241341_2_, itemstack);
	         }

	      });
	      ((ServerWorld)player.world).getChunkProvider().sendToAllTracking(player, new SEntityEquipmentPacket(player.getEntityId(), list));
	   }

}

