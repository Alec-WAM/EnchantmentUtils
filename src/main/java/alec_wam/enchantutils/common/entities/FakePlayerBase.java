package alec_wam.enchantutils.common.entities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;

import alec_wam.enchantutils.common.blocks.mobkiller.FakePlayerNetServerHandler;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.FakePlayer;

public class FakePlayerBase extends FakePlayer {

	@Nonnull
	protected ItemStack prevWeapon = ItemStack.EMPTY;
	private final @Nonnull ServerWorld origWorld;
	private GameProfile owner;

	public FakePlayerBase(ServerWorld world, BlockPos pos, GameProfile profile) {
		super(world, profile);
		origWorld = super.getServerWorld();
		setPosition(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
		// ItemInWorldManager will access this field directly and can crash
		connection = new FakePlayerNetServerHandler(this);
	}

	// These do things with packets...which crashes since the net handler is null. Potion effects are not needed anyways.
	@Override
	protected void onNewPotionEffect(@Nonnull EffectInstance p_70670_1_) {
	}

	@Override
	protected void onChangedPotionEffect(@Nonnull EffectInstance p_70695_1_, boolean p_70695_2_) {
	}

	@Override
	protected void onFinishedPotionEffect(@Nonnull EffectInstance p_70688_1_) {
	}

	@Override
	protected void playEquipSound(@Nullable ItemStack stack) {
	}

	/**
	 * Returns the UUID of the player who is responsible for this FakePlayer or null if no player is responsible or known. May return the UUID of another fake
	 * player if the block was placed by one.
	 */
	public GameProfile getOwner() {
		return owner;
	}

	public @Nonnull FakePlayerBase setOwner(@Nullable GameProfile owner) {
		this.owner = owner;
		return this;
	}

	public void clearOwner() {
		this.owner = null;
	}

	@Override
	public @Nonnull ServerWorld getServerWorld() {
		return origWorld;
	}

	@Override
	public void onItemPickup(@Nonnull Entity entityIn, int quantity) {
		if (world instanceof ServerWorld) {
			// EntityLivingBase will unconditionally cast world to WorldServer
			super.onItemPickup(entityIn, quantity);
			// no else needed, if it's not a server world there will be no attached tracker anyway
		}
	}

}
