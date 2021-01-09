package alec_wam.enchantutils.common.blocks.editor;

import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import alec_wam.enchantutils.common.blocks.ModBlocks;
import alec_wam.enchantutils.common.network.IMessageHandler;
import alec_wam.enchantutils.common.tile.InventoryTileEntity;
import alec_wam.enchantutils.common.util.BlockUtil;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public class EnchantmentEditorTileEntity extends InventoryTileEntity implements IMessageHandler, INamedContainerProvider {

	public EnchantmentEditorTileEntity() {
		super(ModBlocks.TILE_ENCHANTMENT_EDITOR, "EnchantmentEditor", 3);
	}
	
	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if(slot == 0){
			return !stack.isEmpty() && (stack.getItem().isEnchantable(stack) || stack.getItem() == Items.ENCHANTED_BOOK);
		}
		if(slot == 1){
			return !stack.isEmpty() &&stack.getItem() == Items.BOOK;
		}
		if(slot == 2){
			return !stack.isEmpty() && stack.getItem() == Items.ENCHANTED_BOOK;
		}
		return super.isItemValidForSlot(slot, stack);
	}
	
	@Override
	public boolean canInsertItem(int slot, ItemStack stack) {
		if(slot == 0){
			return (stack.isEnchanted()|| stack.getItem() == Items.ENCHANTED_BOOK);
		}
		if(slot == 1){
			return stack.getItem() == Items.BOOK;
		}
		if(slot == 2){
			return false;
		}
		return super.canInsertItem(slot, stack);
	}
	
	@Override
	public boolean canExtract(int slot, int amt) {
		if(slot == 0){
			return true;
		}
		if(slot == 1){
			return false;
		}
		if(slot == 2){
			return true;
		}
		return super.canExtract(slot, amt);
	}
	
	public net.minecraftforge.items.IItemHandler handlerTop = new net.minecraftforge.items.wrapper.InvWrapper(this){
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
	};
	private final LazyOptional<IItemHandler> holderTop = LazyOptional.of(() -> handlerTop);	
	public net.minecraftforge.items.IItemHandler handlerSide = new net.minecraftforge.items.wrapper.InvWrapper(this){
		@Override
		public int getSlots(){
			return 1;
		}
		
		@Override
		public ItemStack getStackInSlot(int slot){
			return getStackInSlot(1);
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
	};
	private final LazyOptional<IItemHandler> holderSide = LazyOptional.of(() -> handlerSide);
	public net.minecraftforge.items.IItemHandler handlerBottom = new net.minecraftforge.items.wrapper.InvWrapper(this){
		@Override
		public int getSlots(){
			return 1;
		}
		
		@Override
		public ItemStack getStackInSlot(int slot){
			return getStackInSlot(2);
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
	};
	private final LazyOptional<IItemHandler> holderBottom = LazyOptional.of(() -> handler);

    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
    {
        if (side != null && cap == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
            return side == Direction.UP ? holderTop.cast() : side == Direction.DOWN ? holderBottom.cast() : holderSide.cast();
        }
        return super.getCapability(cap, side);
    }

	@Override
	public void handleMessage(String messageId, CompoundNBT messageData, boolean client) {
		if(messageId.equalsIgnoreCase("Update")){
			BlockUtil.markBlockForUpdate(getWorld(), getPos());
		}
		if(messageId.equalsIgnoreCase("Transfer")){
			if(getStackInSlot(2).isEmpty()){
				ItemStack editStack = getStackInSlot(0);
				int[] selections = messageData.getIntArray("Selections");
				Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(getStackInSlot(0));
				@SuppressWarnings("unchecked")
				Entry<Enchantment, Integer>[] entries = (Entry<Enchantment, Integer>[]) enchantments.entrySet().toArray(new Entry[0]);
				Map<Enchantment, Integer> bookEnchants = Maps.newHashMap();
				for(int i : selections){
					Entry<Enchantment, Integer> entry = entries[i];
					if(entry !=null){
						enchantments.remove(entry.getKey());
						bookEnchants.put(entry.getKey(), entry.getValue());
					}
				}
				ItemStack newBook = new ItemStack(Items.ENCHANTED_BOOK);
				EnchantmentHelper.setEnchantments(bookEnchants, newBook);
				setInventorySlotContents(2, newBook);
				if(editStack.getItem() == Items.ENCHANTED_BOOK){
					if(enchantments.isEmpty()){
						decrStackSize(0, 1);						
					} else {
						ItemStack newEditBook = new ItemStack(Items.ENCHANTED_BOOK);
						EnchantmentHelper.setEnchantments(enchantments, newEditBook);
						setInventorySlotContents(0, newEditBook);						
					}
				} else {							
					decrStackSize(1, 1);
					EnchantmentHelper.setEnchantments(enchantments, editStack);
				}
			}
		}
	}

	@Override
	public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity player) {
		return new EnchantmentEditorContainer(windowId, playerInventory, this);
	}

	@Override
	public ITextComponent getDisplayName() {
		return ModBlocks.ENCHANTMENT_EDITOR.getTranslatedName();
	}

}
