package alec_wam.enchantutils.common.blocks.mobkiller;

import java.util.function.Predicate;

import javax.annotation.Nonnull;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MobKillerFakePlayerInventory extends PlayerInventory {

	private final MobKillerTileEntity mobKillerTile;

	public MobKillerFakePlayerInventory(PlayerEntity playerIn, MobKillerTileEntity mobKillerTile) {
		super(playerIn);
		this.mobKillerTile = mobKillerTile;
	}

	@Override
	public ItemStack getCurrentItem() {
		return mobKillerTile.getStackInSlot(0);
	}

	@Override
	public int getFirstEmptyStack() {
		return -1;
	}

	@Override
	public void setPickedItemStack(ItemStack stack) {
	}

	@Override
	public void pickItem(int index) {
	}

	@Override
	public int getSlotFor(ItemStack stack) {
		return -1;
	}

	@Override
	public int getBestHotbarSlot() {
		return 1;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public void changeCurrentItem(double direction) {
	}

	@Override
	public int func_234564_a_(Predicate<ItemStack> p_234564_1_, int p_234564_2_, IInventory p_234564_3_) {
		return 0;
	}

	@Override
	public boolean addItemStackToInventory(ItemStack itemStackIn) {
		return false;
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		return ItemStack.EMPTY;
	}

	@Override
	public void deleteStack(ItemStack stack) {
	}

	@Override
	public @Nonnull ItemStack removeStackFromSlot(int index) {
		return ItemStack.EMPTY;
	}

	@Override
	public void setInventorySlotContents(int index, @Nonnull ItemStack stack) {
		mobKillerTile.setInventorySlotContents(0, stack);
	}

	@Override
	public float getDestroySpeed(@Nonnull BlockState state) {
		return 1;
	}

	@Override
	public @Nonnull ListNBT write(@Nonnull ListNBT nbtTagListIn) {
		return nbtTagListIn;
	}

	@Override
	public void read(@Nonnull ListNBT nbtTagListIn) {
	}

	@Override
	public int getSizeInventory() {
		return 1;
	}

	@Override
	public boolean isEmpty() {
		return mobKillerTile.getStackInSlot(0).isEmpty();
	}

	@Override
	public @Nonnull ItemStack getStackInSlot(int index) {
		return mobKillerTile.getStackInSlot(0);
	}

	@Override
	public @Nonnull ITextComponent getName() {
		return super.getName();
	}

	@Override
	public boolean hasCustomName() {
		return super.hasCustomName();
	}

	@Override
	public @Nonnull ITextComponent getDisplayName() {
		return super.getDisplayName();
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	public @Nonnull ItemStack armorItemInSlot(int slotIn) {
		return ItemStack.EMPTY;
	}

	@Override
	public void dropAllItems() {
	}

	@Override
	public void markDirty() {
		mobKillerTile.markDirty();
	}

	@Override
	public void setItemStack(@Nonnull ItemStack itemStackIn) {
	}

	@Override
	public @Nonnull ItemStack getItemStack() {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean isUsableByPlayer(@Nonnull PlayerEntity playerIn) {
		return false;
	}

	@Override
	public boolean hasItemStack(@Nonnull ItemStack itemStackIn) {
		return itemStackIn.isItemEqual(mobKillerTile.getStackInSlot(0));
	}

	@Override
	public void openInventory(@Nonnull PlayerEntity playerIn) {
	}

	@Override
	public void closeInventory(@Nonnull PlayerEntity playerIn) {
	}

	@Override
	public boolean isItemValidForSlot(int index, @Nonnull ItemStack stack) {
		return false;
	}

	@Override
	public void copyInventory(@Nonnull PlayerInventory playerInventory) {
	}

	@Override
	public void clear() {
	}

}
