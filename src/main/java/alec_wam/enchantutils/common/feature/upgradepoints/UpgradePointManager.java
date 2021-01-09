package alec_wam.enchantutils.common.feature.upgradepoints;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import alec_wam.enchantutils.Config;
import alec_wam.enchantutils.EnchantmentUtils;
import alec_wam.enchantutils.client.ModSounds;
import alec_wam.enchantutils.common.feature.upgradepoints.upgrade.AutoSmeltUpgrade;
import alec_wam.enchantutils.common.feature.upgradepoints.upgrade.DigSpeedUpgrade;
import alec_wam.enchantutils.common.feature.upgradepoints.upgrade.ElytraTrailUpgrade;
import alec_wam.enchantutils.common.feature.upgradepoints.upgrade.ExplosionProofUpgrade;
import alec_wam.enchantutils.common.feature.upgradepoints.upgrade.FireproofUpgrade;
import alec_wam.enchantutils.common.feature.upgradepoints.upgrade.IBaseUpgrade;
import alec_wam.enchantutils.common.feature.upgradepoints.upgrade.SoulBoundUpgrade;
import alec_wam.enchantutils.common.network.ModNetwork;
import alec_wam.enchantutils.common.network.PacketGuiMessage;
import alec_wam.enchantutils.common.util.ItemNBTHelper;
import alec_wam.enchantutils.common.util.ItemUtil;
import net.minecraft.block.Block;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

public class UpgradePointManager {

	public static final String NBT_TAG = EnchantmentUtils.resourceDot("upgradepoints");
	public static final String TAG_XP = "xp"; //double
	public static final String TAG_POINTS = "points"; //int
	public static final String TAG_UPGRADES = "upgrades"; //compound
	
	public static boolean isFeatureEnabled(){
		return Config.COMMON.upgrades_enabled.get();
	}
	
	public static double getMaxXPDig(){
		return Config.COMMON.upgrades_xp_dig.get();
	}
	
	public static double getMaxXPDamage(){
		return Config.COMMON.upgrades_xp_weapon.get();
	}
	
	public static boolean isUpgradeable(ItemStack stack){
		if(!stack.isEmpty()){
			if(ItemUtil.isDiggingTool(stack)) return true;
			if(ItemUtil.isWeapon(stack)) return true;
			//TODO Add tag list
			Item item = stack.getItem();
			if(item.isShield(stack, null)) return true;
			if(item == Items.ELYTRA) return true;
			
			//Basic Items
			if(item == Items.FILLED_MAP)return true;			
			if(item instanceof BlockItem){
				Block block = ((BlockItem)item).getBlock();
				if(block instanceof ShulkerBoxBlock)return true;
			}
			if(item == Items.ENDER_CHEST) return true;
		}
		return false;
	}
	
	public static boolean isBasicUpgradeItem(ItemStack stack){
		if(!stack.isEmpty()){
			Item item = stack.getItem();
			if(item == Items.FILLED_MAP)return true;
			if(item instanceof BlockItem){
				Block block = ((BlockItem)item).getBlock();
				if(block instanceof ShulkerBoxBlock)return true;
			}
			if(item == Items.ENDER_CHEST) return true;
		}
		return false;
	}
	
	public static int getXPLevelsPerPoint(ItemStack stack){
		return Config.COMMON.upgrades_levels_point.get();
	}
	
	//TODO Make this a registry and make values of levels and points use data file
	
	//UPGRADES
	//TODO Magnet
	//Hoe 3x3
	//Sword Damage Bonus
	//Bow Power, Special Damage Bonus
	//Maybe mess with Fishing Rods for special stuff
	//Figure out Tridents
	
	//DIGGING
	public static final Map<ResourceLocation, IBaseUpgrade> UPGRADE_REGISTRY = Maps.newHashMap();
	public static DigSpeedUpgrade UPGRADE_DIGSPEED = new DigSpeedUpgrade();
	public static AutoSmeltUpgrade UPGRADE_AUTOSMELT = new AutoSmeltUpgrade();	

	//BASIC TOOLS
	public static ElytraTrailUpgrade UPGRADE_ELYTRA_TRAIL = new ElytraTrailUpgrade();	
	
	
	//ANY ITEM
	public static SoulBoundUpgrade UPGRADE_SOULBOUND = new SoulBoundUpgrade();
	
	//SPECIAL ITEMS
	//TODO NoDespawn
	public static FireproofUpgrade UPGRADE_FIREPROOF = new FireproofUpgrade();
	public static ExplosionProofUpgrade UPGRADE_EXPLOSION_PROOF = new ExplosionProofUpgrade();
	
	public static void registerUpgrades(){
		UPGRADE_REGISTRY.put(UPGRADE_DIGSPEED.getID(), UPGRADE_DIGSPEED);
		UPGRADE_REGISTRY.put(UPGRADE_AUTOSMELT.getID(), UPGRADE_AUTOSMELT);
		UPGRADE_REGISTRY.put(UPGRADE_ELYTRA_TRAIL.getID(), UPGRADE_ELYTRA_TRAIL);

		UPGRADE_REGISTRY.put(UPGRADE_SOULBOUND.getID(), UPGRADE_SOULBOUND);
		
		UPGRADE_REGISTRY.put(UPGRADE_FIREPROOF.getID(), UPGRADE_FIREPROOF);
		UPGRADE_REGISTRY.put(UPGRADE_EXPLOSION_PROOF.getID(), UPGRADE_EXPLOSION_PROOF);
		
		EnchantmentUtils.LOGGER.info("Added " + UPGRADE_REGISTRY.size() + " tool upgrades to the registry");
	}
	
	public static List<IBaseUpgrade> getUpgradesAvailable(ItemStack stack){
		List<IBaseUpgrade> list = Lists.newArrayList();
		if(isUpgradeable(stack)){
			for(IBaseUpgrade upgrade : UPGRADE_REGISTRY.values()){
				if(upgrade.canApply(stack)){
					list.add(upgrade);
				}
			}
		}
		return list;
	}
	
	public static boolean hasUpgrades(ItemStack stack){
		if(!isUpgradeable(stack)) return false;
		CompoundNBT nbt = ItemNBTHelper.getCompound(stack).getCompound(NBT_TAG);
		return nbt.contains(TAG_UPGRADES);
	}
	
	public static void setupUpgrades(ItemStack stack){
		CompoundNBT nbt = ItemNBTHelper.getCompound(stack);
		CompoundNBT upgradesNBT = new CompoundNBT();
		upgradesNBT.put(TAG_UPGRADES, new ListNBT());
		nbt.put(NBT_TAG, upgradesNBT);
	}
	
	public static Map<IBaseUpgrade, Integer> getUpgrades(ItemStack stack){
		if(!isUpgradeable(stack))return Maps.newHashMap();
		Map<IBaseUpgrade, Integer> upgrades = Maps.newHashMap();
		CompoundNBT nbt = ItemNBTHelper.getCompound(stack).getCompound(NBT_TAG);
		if(nbt.contains(TAG_UPGRADES)){
			ListNBT list = nbt.getList(TAG_UPGRADES, Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < list.size(); i++){
				CompoundNBT upgradeNBT = list.getCompound(i);
				ResourceLocation id = ResourceLocation.tryCreate(upgradeNBT.getString("id"));
				if(id !=null){
					IBaseUpgrade upgrade = UPGRADE_REGISTRY.get(id);
					if(upgrade !=null){
						upgrades.put(upgrade, upgradeNBT.getInt("level"));
					}
				}
			}
		}
		return upgrades;
	}
	
	public static void setUpgrades(ItemStack stack, Map<IBaseUpgrade, Integer> upgrades){
		if(!isUpgradeable(stack))return;
		CompoundNBT nbt = ItemNBTHelper.getCompound(stack).getCompound(NBT_TAG);
		ListNBT list = new ListNBT();
		for(IBaseUpgrade upgrade : upgrades.keySet()){
			int lvl = upgrades.get(upgrade);
			CompoundNBT upgradeNBT = new CompoundNBT();
			upgradeNBT.putString("id", upgrade.getID().toString());
			upgradeNBT.putInt("level", lvl);
			list.add(upgradeNBT);
		}
		nbt.put(TAG_UPGRADES, list);
		ItemNBTHelper.getCompound(stack).put(NBT_TAG, nbt);
	}
	
	public static void putUpgrade(ItemStack stack, IBaseUpgrade upgrade, int level){
		if(!isUpgradeable(stack))return;
		Map<IBaseUpgrade, Integer> upgrades = getUpgrades(stack);
		upgrades.put(upgrade, level);
		setUpgrades(stack, upgrades);
	}
	
	public static boolean removeUpgrade(ItemStack stack, IBaseUpgrade upgrade){
		Map<IBaseUpgrade, Integer> upgrades = getUpgrades(stack);
		if(upgrades.containsKey(upgrade)){
			upgrades.remove(upgrade);
			setUpgrades(stack, upgrades);
			return true;
		}
		return false;
	}
	
	public static int getUpgradeLevel(ItemStack stack, IBaseUpgrade upgrade){
		if(!hasUpgrades(stack))return 0;
		Map<IBaseUpgrade, Integer> upgrades = getUpgrades(stack);
		return upgrades.getOrDefault(upgrade, 0);
	}
	
	//TOOL XP
	public static double getToolXP(ItemStack stack){
		if(stack.isEmpty()) return 0.0;
		CompoundNBT nbt = ItemNBTHelper.getCompound(stack).getCompound(NBT_TAG);
		if(nbt.contains(TAG_XP)){
			return nbt.getDouble(TAG_XP);
		}		
		return 0.0;
	}
	
	public static void addToolXP(ItemStack stack, double xp){
		if(stack.isEmpty()) return;
		CompoundNBT nbt = ItemNBTHelper.getCompound(stack).getCompound(NBT_TAG);
		double currentXP = getToolXP(stack);
		nbt.putDouble(TAG_XP, currentXP + xp);
		ItemNBTHelper.getCompound(stack).put(NBT_TAG, nbt);
	}
	
	public static void setToolXP(ItemStack stack, double xp){
		if(stack.isEmpty()) return;
		CompoundNBT nbt = ItemNBTHelper.getCompound(stack).getCompound(NBT_TAG);
		nbt.putDouble(TAG_XP, xp);
		ItemNBTHelper.getCompound(stack).put(NBT_TAG, nbt);
	}
	
	public static void earnToolXP(PlayerEntity player, ItemStack stack, double xpEarned, double maxXP){
		double xp = UpgradePointManager.getToolXP(stack);
		if(xp + xpEarned >= maxXP){
			UpgradePointManager.earnToolPoint(stack, player);
			xpEarned -= maxXP;
		} 
		UpgradePointManager.addToolXP(stack, Math.max(0, xpEarned));
	}
	
	public static void earnToolPoint(ItemStack stack, PlayerEntity player){
		setToolXP(stack, 0.0);
		addToolPoints(stack, 1);
		
		player.world.playSound((PlayerEntity)null, player.getPosX(), player.getPosY(), player.getPosZ(), ModSounds.UPGRADEPOINT_UNLOCK, player.getSoundCategory(), 0.75F, 1.0F);
		
		if(player instanceof ServerPlayerEntity){
			CompoundNBT nbt = new CompoundNBT();
			nbt.put("Item", stack.write(new CompoundNBT()));
			nbt.putInt("Time", 10 * 20); //10 Seconds
			ModNetwork.sendTo((ServerPlayerEntity)player, new PacketGuiMessage(PacketGuiMessage.CUSTOM_PACKET_UPGRADEPOINT, nbt));
		}
	}
	
	//TOOL POINTS
	public static int getToolPoints(ItemStack stack){
		if(stack.isEmpty()) return 0;
		CompoundNBT nbt = ItemNBTHelper.getCompound(stack).getCompound(NBT_TAG);
		if(nbt.contains(TAG_POINTS)){
			return nbt.getInt(TAG_POINTS);
		}		
		return 0;
	}

	public static void addToolPoints(ItemStack stack, int points){
		if(stack.isEmpty()) return;
		CompoundNBT nbt = ItemNBTHelper.getCompound(stack).getCompound(NBT_TAG);
		int currentPoints = getToolPoints(stack);
		nbt.putInt(TAG_POINTS, currentPoints + points);
		ItemNBTHelper.getCompound(stack).put(NBT_TAG, nbt);
	}

	public static void setToolPoints(ItemStack stack, int points){
		if(stack.isEmpty()) return;
		CompoundNBT nbt = ItemNBTHelper.getCompound(stack).getCompound(NBT_TAG);
		nbt.putInt(TAG_POINTS, points);
		ItemNBTHelper.getCompound(stack).put(NBT_TAG, nbt);
	}
	
	public static boolean areUpgradeItemsEqual(ItemStack stack1, ItemStack stack2){
		if(stack1 == null || stack2 == null || stack1.isEmpty() || stack2.isEmpty()){
			return false;
		}
		
		if(stack1.getItem() != stack2.getItem()){
			return false;
		}
		
		if(!hasUpgrades(stack1) || !hasUpgrades(stack2)){
			return false;
		}
		
		List<IBaseUpgrade> upgradeList1 = getUpgradesAvailable(stack1);
		List<IBaseUpgrade> upgradeList2 = getUpgradesAvailable(stack2);
		
		if(upgradeList1.size() != upgradeList2.size()){
			return false;
		}
		
		for(int u = 0; u < upgradeList1.size(); u++){
			IBaseUpgrade upgrade1 = upgradeList1.get(u);
			IBaseUpgrade upgrade2 = upgradeList2.get(u);
			if(upgrade1 != upgrade2){
				return false;
			}
			
			if(!upgrade1.equalUpgrades(stack1, stack2) || !upgrade2.equalUpgrades(stack1, stack2)){
				return false;
			}
		}
		
		return true;
	}
}
