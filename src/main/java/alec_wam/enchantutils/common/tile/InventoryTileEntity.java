package alec_wam.enchantutils.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public class InventoryTileEntity extends BaseTileEntity implements IInventory {

	private String name;
	public NonNullList<ItemStack> inventory;
	
	public InventoryTileEntity(TileEntityType<?> tileEntityTypeIn, String name, int size){
		super(tileEntityTypeIn);
		this.name = name;
		this.inventory = NonNullList.<ItemStack>withSize(size, ItemStack.EMPTY);
	}
	
	@Override
	public void writeCustomNBT(CompoundNBT nbt){
		super.writeCustomNBT(nbt);
		nbt.putString("InvName", name);
		ItemStackHelper.saveAllItems(nbt, inventory);
	}
	
	@Override
	public void readCustomNBT(CompoundNBT nbt){
		super.readCustomNBT(nbt);
		name = nbt.getString("InvName");
		ItemStackHelper.loadAllItems(nbt, inventory);
	}
	
	@Override
	public int getSizeInventory() {
		return inventory.size();
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		if(slot < 0 || slot >= inventory.size()) {
			return ItemStack.EMPTY;
		}

		//System.out.println("" + inventory);
		
	    return this.inventory.get(slot);
	}

	@Override
	public ItemStack decrStackSize(int slot, int quantity) {
		ItemStack itemStack = getStackInSlot(slot);

	    if(itemStack.isEmpty()) {
	      return ItemStack.EMPTY;
	    }

	    // whole itemstack taken out
	    if(itemStack.getCount() <= quantity) {
	      setInventorySlotContents(slot, ItemStack.EMPTY);
	      onItemChanged(slot);
	      markDirty();
	      return itemStack;
	    }

	    // split itemstack
	    itemStack = itemStack.split(quantity);
	    // slot is empty, set to null
	    if(getStackInSlot(slot).isEmpty()) {
	      setInventorySlotContents(slot, ItemStack.EMPTY);
	    }
	    onItemChanged(slot);
	    markDirty();
	    // return remainder
	    return itemStack;
	}

	@Override
	public ItemStack removeStackFromSlot(int slot) {
		ItemStack itemStack = getStackInSlot(slot);
	    setInventorySlotContents(slot, ItemStack.EMPTY);
	    return itemStack;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack itemstack) {
		if(slot < 0 || slot >= inventory.size()) {
	      return;
	    }
		
		this.inventory.set(slot, itemstack);
		if(itemstack.getCount() > getInventoryStackLimit(slot)) {
	    	itemstack.setCount(getInventoryStackLimit(slot));
	    }
	    onItemChanged(slot);
	    markDirty();
	}
	
	public void onItemChanged(int slot){
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	public int getInventoryStackLimit(int slot){
		return getInventoryStackLimit();
	}
	
	@Override
	public boolean isUsableByPlayer(PlayerEntity player) {
		if(getWorld().getTileEntity(pos) != this || getWorld().getBlockState(pos).getBlock() == Blocks.AIR) {
	      return false;
	    }

	    return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64D;
	}

	@Override
	public void openInventory(PlayerEntity player) {}

	@Override
	public void closeInventory(PlayerEntity player) {}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return true;
	}

	@Override
	public void clear() {
		inventory.clear();
	}
	
	public final net.minecraftforge.items.IItemHandler handler = new net.minecraftforge.items.wrapper.InvWrapper(this){
		@Override
	    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
	    {
			if(!stack.isEmpty()){
				if(!InventoryTileEntity.this.canInsertItem(slot, stack)){
					return stack;
				}
			}
			return super.insertItem(slot, stack, simulate);
		}
		
		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate)
		{
			if(amount > 0){
				if(!InventoryTileEntity.this.canExtract(slot, amount)){
					return ItemStack.EMPTY;
				}
			}
			return super.extractItem(slot, amount, simulate);
		}
		
		@Override
	    public int getSlotLimit(int slot)
	    {
			return InventoryTileEntity.this.getInventoryStackLimit(slot);
	    }
	};
	private final LazyOptional<IItemHandler> holder = LazyOptional.of(() -> handler);
	
	@Override
    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
    {
        if (cap == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
            return holder.cast();
        return super.getCapability(cap, side);
    }

	public boolean canExtract(int slot, int amount) {
		return true;
	}

	public boolean canInsertItem(int slot, ItemStack stack) {
		return true;
	}
	
	@Override
	public boolean isEmpty() {
		for(ItemStack stack : inventory){
			if(!stack.isEmpty()){
				return false;
			}
		}
		return true;
	}

}

