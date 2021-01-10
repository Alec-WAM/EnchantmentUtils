package alec_wam.enchantutils.common.blocks.mobkiller;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;

import alec_wam.enchantutils.common.entities.FakePlayerBase;
import alec_wam.enchantutils.common.util.FakePlayerManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.server.ServerWorld;

public class MobKillerFakePlayer extends FakePlayerBase {
	public final @Nonnull MobKillerTileEntity mobKillerTile;
	public MobKillerFakePlayerInventory fakeInventory;
	
	public MobKillerFakePlayer(MobKillerTileEntity tile){
		super((ServerWorld)tile.getWorld(), tile.getPos(), makeGameProfile(tile.getOwner()));
		mobKillerTile = tile;
		fakeInventory = new MobKillerFakePlayerInventory(this, mobKillerTile);
		setOwner(tile.getOwner());
	}

	private static @Nonnull GameProfile makeGameProfile(GameProfile owner) {
		return (owner == null || owner.getName().isEmpty()) ? FakePlayerManager.ENCHANTUTILS
				: new GameProfile(FakePlayerManager.ENCHANTUTILS_UUID, "[" + owner.getName() + "'s Mob Killer]");
	}

	@Override
	public void tick() {
		ItemStack prev = prevWeapon;
		ItemStack cur = getHeldItemMainhand();
		if (!ItemStack.areItemStacksEqual(cur, prev)) {
			if (!prev.isEmpty()) {
				getAttributeManager().removeModifiers(prev.getAttributeModifiers(EquipmentSlotType.MAINHAND));
			}

			if (!cur.isEmpty()) {
				getAttributeManager().reapplyModifiers(cur.getAttributeModifiers(EquipmentSlotType.MAINHAND));
			}
			prevWeapon = cur.copy();
		}
		ticksSinceLastSwing++;
	}
	
	public int getTicksSinceLastSwing() {
		return ticksSinceLastSwing;
	}

	@Override
	public ItemStack getItemStackFromSlot(EquipmentSlotType slotIn){
		if(slotIn == EquipmentSlotType.MAINHAND && mobKillerTile != null){
			return mobKillerTile.getStackInSlot(0);
		}
		return super.getItemStackFromSlot(slotIn);
	}
	
	@Override
	public void setItemStackToSlot(EquipmentSlotType slot, ItemStack stack){
		if(slot == EquipmentSlotType.MAINHAND){
			mobKillerTile.setInventorySlotContents(0, stack);
		}
		else super.setItemStackToSlot(slot, stack);
	}
	
	private final net.minecraftforge.common.util.LazyOptional<net.minecraftforge.items.IItemHandler>
	playerMainHandler = net.minecraftforge.common.util.LazyOptional.of(
			() -> new net.minecraftforge.items.wrapper.PlayerMainInvWrapper(fakeInventory));

	private final net.minecraftforge.common.util.LazyOptional<net.minecraftforge.items.IItemHandler>
	playerEquipmentHandler = net.minecraftforge.common.util.LazyOptional.of(
			() -> new net.minecraftforge.items.wrapper.CombinedInvWrapper(
					new net.minecraftforge.items.wrapper.PlayerArmorInvWrapper(fakeInventory),
					new net.minecraftforge.items.wrapper.PlayerOffhandInvWrapper(fakeInventory)));

	private final net.minecraftforge.common.util.LazyOptional<net.minecraftforge.items.IItemHandler>
	playerJoinedHandler = net.minecraftforge.common.util.LazyOptional.of(
			() -> new net.minecraftforge.items.wrapper.PlayerInvWrapper(fakeInventory));

	@Override
	public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> capability, @Nullable Direction facing) {
		if (this.isAlive() && capability == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			if (facing == null) return playerJoinedHandler.cast();
			else if (facing.getAxis().isVertical()) return playerMainHandler.cast();
			else if (facing.getAxis().isHorizontal()) return playerEquipmentHandler.cast();
		}
		return super.getCapability(capability, facing);
	}
	
	@Override
	public void attackTargetEntityWithCurrentItem(@Nonnull Entity targetEntity) {
		onGround = true; // sweep attacks need this
		faceEntity(targetEntity); // sweep attack particles use this
		super.attackTargetEntityWithCurrentItem(targetEntity);
	}

	// taken from EntityLiving and simplified
	public void faceEntity(Entity entityIn) {
		double d0 = entityIn.getPosX() - this.getPosX();
		double d2 = entityIn.getPosZ() - this.getPosZ();
		double d1;

		if (entityIn instanceof LivingEntity) {
			LivingEntity entitylivingbase = (LivingEntity) entityIn;
			d1 = entitylivingbase.getPosY() + entitylivingbase.getEyeHeight() - (this.getPosY() + this.getEyeHeight());
		} else {
			d1 = (entityIn.getBoundingBox().minY + entityIn.getBoundingBox().maxY) / 2.0D - (this.getPosY() + this.getEyeHeight());
		}

		double d3 = MathHelper.sqrt(d0 * d0 + d2 * d2);
		rotationPitch = MathHelper.wrapDegrees((float) (-(MathHelper.atan2(d1, d3) * (180D / Math.PI))));
		rotationYaw = MathHelper.wrapDegrees((float) (MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F);
	}

	//TODO don't let Creepers blow us up

	/*@Override
	public boolean isCreeperTarget(@Nonnull EntityCreeper swellingCreeper) {
		return KillerJoeConfig.killerProvokesCreeperExplosions.get();
	}*/

	// don't let Zombies summon aid

}
