package alec_wam.enchantutils.common.blocks.mobkiller;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;

import alec_wam.enchantutils.common.blocks.ModBlocks;
import alec_wam.enchantutils.common.items.ModItems;
import alec_wam.enchantutils.common.network.IMessageHandler;
import alec_wam.enchantutils.common.tile.InventoryTileEntity;
import alec_wam.enchantutils.common.util.BlockUtil;
import alec_wam.enchantutils.common.util.ItemUtil;
import alec_wam.enchantutils.common.util.LangUtil;
import alec_wam.enchantutils.common.util.XPUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class MobKillerTileEntity extends InventoryTileEntity implements IMessageHandler, INamedContainerProvider {

	//TODO Upgrades
	//Range, Item Pickup, Critical
	//Keep almost broken item in
	
	public static final int SLOT_SWORD = 0;
	public static final int SLOT_RANGE = 1;
	public static final int SLOT_VACUUM = 2;
	public static final int SLOT_CRIT = 3;
	//3x3, 5x5, 7x7
	public static final int MAX_RANGE_ITEMS = 3;
	public static final int SLOTS = 4;
	
	public static final int MAX_LEVELS = 100;
	public long experienceTotal;
	public int xpCooldown;	

	public int attackCooldown;
	protected MobKillerFakePlayer fakePlayer;
	
	private AxisAlignedBB killBox;
	public boolean isKillBoxVisible;
	private Direction lastFacing;
	
	public MobKillerTileEntity() {
		super(ModBlocks.TILE_MOB_KILLER, "MobKiller", SLOTS);
	}
	
	@Override
	public void writeCustomNBT(CompoundNBT nbt){
		super.writeCustomNBT(nbt);
		nbt.putLong("XpTotal", experienceTotal);
		nbt.putBoolean("ShowKillBox", isKillBoxVisible);
	}
	
	@Override
	public void readCustomNBT(CompoundNBT nbt){
		super.readCustomNBT(nbt);
		
		this.experienceTotal = nbt.getLong("XpTotal");
		this.isKillBoxVisible = nbt.getBoolean("ShowKillBox");
	}

	public Direction getFacing(){
		BlockState block = getWorld().getBlockState(getPos());
		if(block.hasProperty(HorizontalBlock.HORIZONTAL_FACING)){
			return block.get(HorizontalBlock.HORIZONTAL_FACING);
		}
		return Direction.NORTH;
	}
	
	private MobKillerFakePlayer getFakePlayer() {
		if (fakePlayer == null) {
			fakePlayer = new MobKillerFakePlayer(this);
		}
		return fakePlayer;
	}
	
	public boolean hasVacuumUpgrade(){
		ItemStack upgrade = getStackInSlot(SLOT_VACUUM);
		return !upgrade.isEmpty();
	}
	
	public void refreshKillBox(){
		killBox = null;
	}
	
	public AxisAlignedBB getKillBox() {
		if(killBox == null){
			ItemStack upgrade = getStackInSlot(SLOT_RANGE);
			int range = 0;
			if(!upgrade.isEmpty()){
				range = upgrade.getCount();
			}
			Direction facing = getFacing();
			BlockPos facingPos = getPos().offset(facing);
			facingPos = facingPos.offset(facing, range);
			AxisAlignedBB bbKill = AxisAlignedBB.fromVector(Vector3d.copy(facingPos)).grow(range, 0, range);
			killBox = bbKill;
		}
		return killBox;
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox(){
		AxisAlignedBB blockBox = AxisAlignedBB.fromVector(Vector3d.copy(getPos()));
		return getKillBox().union(blockBox);
	}
	
	@Override
	public void tick(){
		super.tick();
		
		Direction facing = getFacing();
		if(lastFacing == null){
			lastFacing = facing;
		} else {
			if(facing !=lastFacing){
				refreshKillBox();
				lastFacing = facing;
			}
		}
		
		if(!this.world.isRemote){
			if(xpCooldown > 0){
				xpCooldown--;
			}
			if(attackCooldown > 0){
				attackCooldown--;
			}
			
			//Redstone Check
			if(!world.isBlockPowered(getPos())){
				return;
			}			
			
			if(facing == null)return;
			
			boolean vacuum = hasVacuumUpgrade();
			
			if(vacuum){
				AxisAlignedBB bbVacuum = getKillBox();
				List<ExperienceOrbEntity> xpOrbs = world.getEntitiesWithinAABB(ExperienceOrbEntity.class, bbVacuum, EntityPredicates.IS_ALIVE);
				ItemStack upgrade = getStackInSlot(SLOT_RANGE);
				int range = 0;
				if(!upgrade.isEmpty()){
					range = upgrade.getCount();
				}
				double maxDist = 1.5D + (range) * 2;
				
				for (ExperienceOrbEntity entity : xpOrbs) {
					double xDist = (pos.getX() + 0.5D - entity.getPosX());
					double yDist = (pos.getY() + 0.5D - entity.getPosY());
					double zDist = (pos.getZ() + 0.5D - entity.getPosZ());
	
					double totalDistance = Math.sqrt(xDist * xDist + yDist * yDist + zDist * zDist);
					
					if(totalDistance < 1.0){
						pickupXP(entity);
					}else {
					
						double d = 1 - (Math.max(0.1, totalDistance) / maxDist);
						double speed = 0.01 + (d * 0.02);
						double x = entity.getMotion().x + (xDist / totalDistance * speed);
						double z = entity.getMotion().z + (zDist / totalDistance * speed);
						double y = entity.getMotion().y + (yDist / totalDistance * speed);
						if (yDist > 0.5) {
							y = 0.12;
						}
						entity.setMotion(x, y, z);
						
						// force client sync because this movement is server-side only
				        boolean silent = entity.isSilent();
				        entity.setSilent(!silent);
				        entity.setSilent(silent);
					}
				}
			}
			
			
			ItemStack sword = getStackInSlot(0);
			
			if(!sword.isEmpty()){
				MobKillerFakePlayer killer = getFakePlayer();
				
				if(killer !=null){
					killer.tick();
					
					if (killer.getTicksSinceLastSwing() > killer.getCooldownPeriod()) {
						AxisAlignedBB bbAttack = getKillBox();
						List<LivingEntity> attackEntities = getWorld().getEntitiesWithinAABB(LivingEntity.class, bbAttack, this::canAttackEntity);
						if(!attackEntities.isEmpty()){
							LivingEntity attackTarget = attackEntities.get(world.rand.nextInt(attackEntities.size()));
							if(attackTarget !=null){
								killer.attackTargetEntityWithCurrentItem(attackTarget);
								killer.resetCooldown();
							}
						}
					}
				}
			}
		}
	}
	
	private boolean canAttackEntity(LivingEntity entity) {
		if(!entity.isAlive()) return false;
		if(!entity.canBeAttackedWithItem()) return false;
		if(entity.isInvulnerable()) return false;
		if(entity instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity)entity;
			if(player.abilities.isCreativeMode)return false;
			if(player.isSpectator()) return false;
			return false; //TODO Add Config for player attack
		}
		return true;
	}
	
	public void pickupXP(ExperienceOrbEntity xp){
		if (!this.world.isRemote) {
			if (xp.delayBeforeCanPickup == 0 && xpCooldown == 0) {
				xpCooldown = 2;
				
				int xpValue = xp.xpValue;
				ItemStack sword = getStackInSlot(0);
				
				if(!sword.isEmpty() && sword.isDamaged() && EnchantmentHelper.getEnchantmentLevel(Enchantments.MENDING, sword) > 0){
					int i = Math.min((int)(xpValue * sword.getXpRepairRatio()), sword.getDamage());
					xpValue -= this.durabilityToXp(i);
					sword.setDamage(sword.getDamage() - i);
				}

				if (xpValue > 0) {
					xp.xpValue -= addExperience(xpValue);
				}

				if(xp.xpValue <= 0)xp.remove();
			}

		}
	}

	private int durabilityToXp(int durability) {
		return durability / 2;
	}
	
	public void givePlayerXp(@Nonnull PlayerEntity player, int levels){
		for (int i = 0; i < levels && experienceTotal > 0; i++) {
			givePlayerXpLevel(player);
		}
	}

	public void givePlayerXpLevel(@Nonnull PlayerEntity player) {
		long currentXP = XPUtil.getPlayerXPL(player);
		long nextLevelXP = XPUtil.getExperienceForLevelL(player.experienceLevel + 1);
		long requiredXP = nextLevelXP - currentXP;

		long removed = removeExperience(requiredXP);
		XPUtil.addPlayerXP(player, removed);
	}
	
	public int addExperience(int xpToAdd) {
		return XPUtil.limit(addExperience((long) xpToAdd));
	}

	public long addExperience(long xpToAdd) {
		long maxXP = XPUtil.getExperienceForLevelL(MAX_LEVELS);
		long j = MathHelper.clamp(xpToAdd, 0, maxXP - experienceTotal);
		experienceTotal += j;
	    BlockUtil.markBlockForUpdate(getWorld(), getPos());
		return j;
	}
	
	public long removeExperience(long xpToRemove) {
	    long j = MathHelper.clamp(xpToRemove, 0, experienceTotal);
	    this.experienceTotal -= j;
	    BlockUtil.markBlockForUpdate(getWorld(), getPos());
	    return j;
	}
	
	public int getExperienceLevel() {
		return XPUtil.getLevelForExperience(experienceTotal);
	}

	public float getExperience() {
		return (experienceTotal - XPUtil.getExperienceForLevelL(getExperienceLevel())) / (float) getXpBarCapacity();
	}
	
	public int getXpBarCapacity() {
		return XPUtil.getXpBarCapacity(getExperienceLevel());
	}
	
	public ItemStack suckInStack(ItemStack stack){
		BlockPos backPos = getPos().offset(getFacing().getOpposite());
		TileEntity tileBack = getWorld().getTileEntity(backPos);
		if(tileBack !=null && hasVacuumUpgrade()){
			LazyOptional<IItemHandler> cap = tileBack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, getFacing());
			if(cap.isPresent()){
				IItemHandler inv = cap.orElse(null);
				if(inv !=null){					
					int numInserted = ItemUtil.doInsertItem(inv, stack);
					stack.shrink(numInserted);
				}
			}
		}
		
		return stack;
	}
	
	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if(slot == SLOT_SWORD){
			return !stack.isEmpty() && ItemUtil.isSword(stack);
		}
		if(slot == SLOT_RANGE){
			return !stack.isEmpty() && stack.getItem() == ModItems.UPGRADE_RANGE;
		}
		if(slot == SLOT_VACUUM){
			return !stack.isEmpty() && stack.getItem() == ModItems.UPGRADE_VACUUM;
		}
		if(slot == SLOT_CRIT){
			return !stack.isEmpty() && stack.getItem() == ModItems.UPGRADE_CRIT;
		}
		return super.isItemValidForSlot(slot, stack);
	}
	
	@Override
	public boolean canInsertItem(int slot, ItemStack stack) {
		if(slot == SLOT_SWORD){
			return ItemUtil.isSword(stack);
		}
		if(slot == SLOT_RANGE){
			return !stack.isEmpty() && stack.getItem() == ModItems.UPGRADE_RANGE;
		}
		if(slot == SLOT_VACUUM){
			return !stack.isEmpty() && stack.getItem() == ModItems.UPGRADE_VACUUM;
		}
		if(slot == SLOT_CRIT){
			return !stack.isEmpty() && stack.getItem() == ModItems.UPGRADE_CRIT;
		}
		return super.canInsertItem(slot, stack);
	}
	
	@Override
	public boolean canExtract(int slot, int amt) {
		if(slot == SLOT_SWORD){
			return true;
		}
		if(slot == SLOT_RANGE || slot == SLOT_VACUUM || slot == SLOT_CRIT){
			return false;
		}
		return super.canExtract(slot, amt);
	}
	
	@Override
	public int getInventoryStackLimit(int slot){
		if(slot == SLOT_SWORD){
			return 1;
		}
		if(slot == SLOT_RANGE){
			return MAX_RANGE_ITEMS;
		}
		if(slot == SLOT_VACUUM){
			return 1;
		}
		if(slot == SLOT_CRIT){
			return 1;
		}
		return getInventoryStackLimit();
	}
	
	@Override
	public void onItemChanged(int slot){
		if(slot == SLOT_RANGE){
			refreshKillBox();
		}
	}
	
	public net.minecraftforge.items.IItemHandler handlerSword = new net.minecraftforge.items.wrapper.InvWrapper(this){
		@Override
		public int getSlots(){
			return 1;
		}
		
		@Override
		public ItemStack getStackInSlot(int slot){
			return getStackInSlot(0);
		}
		
		@Override
	    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
	    {
			if(!stack.isEmpty()){
				if(!canInsertItem(slot, stack)){
					return ItemStack.EMPTY;
				}
			}
			return super.insertItem(slot, stack, simulate);
		}
		
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate)
		{
			if(amount > 0){
				if(!canExtract(slot, amount)){
					return ItemStack.EMPTY;
				}
			}
			return super.extractItem(slot, amount, simulate);
		}
		
		@Override
		public int getSlotLimit(int slot){
			return getInventoryStackLimit(slot);
		}
	};
	private final LazyOptional<IItemHandler> holderSword = LazyOptional.of(() -> handlerSword);	
	public net.minecraftforge.items.IItemHandler handlerUpgrades = new net.minecraftforge.items.wrapper.InvWrapper(this){
		@Override
		public int getSlots(){
			return SLOTS - 1;
		}
		
		@Override
		public ItemStack getStackInSlot(int slot){
			return getStackInSlot(slot + 1);
		}
		
		@Override
	    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
	    {
			if(!stack.isEmpty()){
				if(!canInsertItem(slot + 1, stack)){
					return ItemStack.EMPTY;
				}
			}
			return super.insertItem(slot, stack, simulate);
		}
		
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate)
		{
			if(amount > 0){
				if(!canExtract(slot + 1, amount)){
					return ItemStack.EMPTY;
				}
			}
			return super.extractItem(slot, amount, simulate);
		}
		
		@Override
		public int getSlotLimit(int slot){
			return getInventoryStackLimit(slot + 1);
		}
	};
	private final LazyOptional<IItemHandler> holderUpgrades = LazyOptional.of(() -> handlerUpgrades);	
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
    {
        if (cap == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
            if(side !=null && side.getAxis().isHorizontal()){
            	return holderUpgrades.cast();
            }
            return holderSword.cast();
        }
        return super.getCapability(cap, side);
    }

	@Override
	public void handleMessage(String messageId, CompoundNBT messageData, boolean client) {
		if(messageId.equalsIgnoreCase("TransferXP")){
			int levels = messageData.getInt("Levels");
			UUID uuid = messageData.getUniqueId("PlayerUUID");
			PlayerEntity player = getWorld().getPlayerByUuid(uuid);
			if(player !=null){
				givePlayerXp(player, levels);
			}
		}
		if(messageId.equalsIgnoreCase("ShowKillbox")){
			this.isKillBoxVisible = messageData.getBoolean("Show");
		}
	}

	@Override
	public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity player) {
		return new MobKillerContainer(windowId, playerInventory, this);
	}

	@Override
	public ITextComponent getDisplayName() {
		return new StringTextComponent(LangUtil.localize("gui.mob_killer"));
	}

	public GameProfile getOwner() {
		return null;
	}

}


