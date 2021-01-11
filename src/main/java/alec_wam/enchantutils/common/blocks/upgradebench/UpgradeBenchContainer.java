package alec_wam.enchantutils.common.blocks.upgradebench;

import javax.annotation.Nullable;

import alec_wam.enchantutils.client.gui.GuiManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;

public class UpgradeBenchContainer extends Container
{
    public final UpgradeBenchTileEntity tile;

    public UpgradeBenchContainer(int windowId, IInventory playerInventory, UpgradeBenchTileEntity tile)
    {
    	super(GuiManager.UPGRADE_BENCH, windowId);
        this.tile = tile;

        this.addSlot(new Slot(tile, 0, 28, 34) {
        	@Override
        	public boolean isItemValid(ItemStack stack)
	        {
	            return tile.isItemValidForSlot(0, stack);
	        }
        	
        	@Override
        	public int getSlotStackLimit() {
        		return 1;
        	}
        });
        
        this.addSlot(new Slot(tile, 1, 28, 60) {
        	@Override
        	public boolean isItemValid(ItemStack stack)
	        {
	            return tile.isItemValidForSlot(1, stack);
	        }
        });

        for (int k = 0; k < 3; ++k)
        {
            for (int i1 = 0; i1 < 9; ++i1)
            {
                this.addSlot(new Slot(playerInventory, i1 + k * 9 + 9, 48 + i1 * 18, 149 + k * 18));
            }
        }

        for (int l = 0; l < 9; ++l)
        {
            this.addSlot(new Slot(playerInventory, l, 48 + l * 18, 207));
        }
    }
    
    public static UpgradeBenchContainer fromNetwork(int windowId, PlayerInventory playerInventory, PacketBuffer buf) {
		BlockPos pos = buf.readBlockPos();
		UpgradeBenchTileEntity te = (UpgradeBenchTileEntity) playerInventory.player.world.getTileEntity(pos);
		return new UpgradeBenchContainer(windowId, playerInventory, te);
	}
    
    @Override
	public boolean canInteractWith(PlayerEntity playerIn)
    {
        return this.tile.isUsableByPlayer(playerIn);
    }

    /**
     * Take a stack from the specified inventory slot.
     */
    @Override
	@Nullable
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index)
    {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack())
        {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (index <= 1)
            {
            	if (!this.mergeItemStack(itemstack1, 29, 38, false))
                {
	            	if (!this.mergeItemStack(itemstack1, 2, 29, false))
	                {
	                    return ItemStack.EMPTY;
	                }
                }
            }
            else if (!this.mergeItemStack(itemstack1, 0, 2, false))
            {
            	if (index < 29)
                {
	            	if (!this.mergeItemStack(itemstack1, 29, 38, false))
	                {
	            		return ItemStack.EMPTY;
	                }
                } else {
                	if (!this.mergeItemStack(itemstack1, 2, 29, false))
	                {
                		return ItemStack.EMPTY;
	                }
                }
            }

            if (itemstack1.isEmpty())
            {
                slot.putStack(ItemStack.EMPTY);
            }
            else
            {
                slot.onSlotChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount())
            {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, itemstack1);
        }

        return itemstack;
    }
}

