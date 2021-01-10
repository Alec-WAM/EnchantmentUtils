package alec_wam.enchantutils.common.blocks.mobkiller;

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

public class MobKillerContainer extends Container
{
    public final MobKillerTileEntity tile;

    public MobKillerContainer(int windowId, IInventory playerInventory, MobKillerTileEntity tile)
    {
    	super(GuiManager.MOB_KILLER, windowId);
        this.tile = tile;

        this.addSlot(new Slot(tile, 0, 88, 26) {
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

        for (int k = 0; k < 3; ++k)
        {
            for (int i1 = 0; i1 < 9; ++i1)
            {
                this.addSlot(new Slot(playerInventory, i1 + k * 9 + 9, 16 + i1 * 18, 104 + k * 18));
            }
        }

        for (int l = 0; l < 9; ++l)
        {
            this.addSlot(new Slot(playerInventory, l, 16 + l * 18, 162));
        }
    }
    
    public static MobKillerContainer fromNetwork(int windowId, PlayerInventory playerInventory, PacketBuffer buf) {
		BlockPos pos = buf.readBlockPos();
		MobKillerTileEntity te = (MobKillerTileEntity) playerInventory.player.world.getTileEntity(pos);
		return new MobKillerContainer(windowId, playerInventory, te);
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

            if (index == 0)
            {
                if (!this.mergeItemStack(itemstack1, 1, 37, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if (!this.mergeItemStack(itemstack1, 0, 1, false))
            {
                return ItemStack.EMPTY;
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

