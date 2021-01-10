package alec_wam.enchantutils.common.blocks.mobkiller;

import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;

import alec_wam.enchantutils.common.blocks.ModBlocks;
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
	
	public static final int MAX_LEVELS = 100;
	public long experienceTotal;
	public int xpCooldown;	

	public int attackCooldown;
	protected MobKillerFakePlayer fakePlayer;
	
	public MobKillerTileEntity() {
		super(ModBlocks.TILE_MOB_KILLER, "MobKiller", 1);
	}
	
	@Override
	public void writeCustomNBT(CompoundNBT nbt){
		super.writeCustomNBT(nbt);
		nbt.putLong("XpTotal", experienceTotal);
	}
	
	@Override
	public void readCustomNBT(CompoundNBT nbt){
		super.readCustomNBT(nbt);
		
		this.experienceTotal = nbt.getLong("XpTotal");
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
	
	@Override
	public void tick(){
		super.tick();
		
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
			
			Direction facing = getFacing();
			if(facing == null)return;
			
			BlockPos facingPos = getPos().offset(facing);
			
			boolean vacuum = true;
			
			if(vacuum){
				BlockPos vacuumCenter = facingPos.offset(facing, 1);
				AxisAlignedBB bbVacuum = AxisAlignedBB.fromVector(Vector3d.copy(vacuumCenter)).grow(1.0, 0.0, 1.0);
				List<ExperienceOrbEntity> xpOrbs = world.getEntitiesWithinAABB(ExperienceOrbEntity.class, bbVacuum, EntityPredicates.IS_ALIVE);
				double maxDist = 1.5D * 2;
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
						BlockPos attackCenter = facingPos.offset(facing, 1);
						AxisAlignedBB bbAttack = AxisAlignedBB.fromVector(Vector3d.copy(attackCenter)).grow(1.0, 0.0, 1.0);
						//TODO Expand this with upgrades
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

	/*public int xpBarCap() {
		if (this.experienceLevel >= 30) {
			return 112 + (this.experienceLevel - 30) * 9;
		} else {
			return this.experienceLevel >= 15 ? 37 + (this.experienceLevel - 15) * 5 : 7 + this.experienceLevel * 2;
		}
	}*/
	
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
		if(tileBack !=null){
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
		if(slot == 0){
			return !stack.isEmpty() && ItemUtil.isSword(stack);
		}
		return super.isItemValidForSlot(slot, stack);
	}
	
	@Override
	public boolean canInsertItem(int slot, ItemStack stack) {
		if(slot == 0){
			return ItemUtil.isSword(stack);
		}
		return super.canInsertItem(slot, stack);
	}
	
	@Override
	public boolean canExtract(int slot, int amt) {
		if(slot == 0){
			return true;
		}
		return super.canExtract(slot, amt);
	}
	
	@Override
	public int getInventoryStackLimit(int slot){
		if(slot == 0){
			return 1;
		}
		return getInventoryStackLimit();
	}
	
	public net.minecraftforge.items.IItemHandler handlerInv = new net.minecraftforge.items.wrapper.InvWrapper(this){
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
			return 1;
		}
	};
	private final LazyOptional<IItemHandler> holderInv = LazyOptional.of(() -> handlerInv);	
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
    {
        if (cap == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
            return holderInv.cast();
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


