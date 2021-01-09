package alec_wam.enchantutils.common.blocks.editor;

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

public class EnchantmentEditorContainer extends Container
{
    public final EnchantmentEditorTileEntity editor;

    public EnchantmentEditorContainer(int windowId, IInventory playerInventory, EnchantmentEditorTileEntity editor)
    {
    	super(GuiManager.ENCHANTMENT_EDITOR, windowId);
        this.editor = editor;

        this.addSlot(new Slot(editor, 0, 39, 27) {
        	@Override
        	public boolean isItemValid(ItemStack stack)
	        {
	            return editor.isItemValidForSlot(0, stack);
	        }
        });
        
        this.addSlot(new Slot(editor, 1, 17, 63) {
        	@Override
        	public boolean isItemValid(ItemStack stack)
	        {
	            return editor.isItemValidForSlot(1, stack);
	        }
        });
        
        this.addSlot(new Slot(editor, 2, 62, 63) {
        	@Override
        	public boolean isItemValid(ItemStack stack)
	        {
	            return false;
	        }
        });

        for (int k = 0; k < 3; ++k)
        {
            for (int i1 = 0; i1 < 9; ++i1)
            {
                this.addSlot(new Slot(playerInventory, i1 + k * 9 + 9, 8 + i1 * 18, 108 + k * 18));
            }
        }

        for (int l = 0; l < 9; ++l)
        {
            this.addSlot(new Slot(playerInventory, l, 8 + l * 18, 166));
        }
    }
    
    public static EnchantmentEditorContainer fromNetwork(int windowId, PlayerInventory playerInventory, PacketBuffer buf) {
		BlockPos pos = buf.readBlockPos();
		EnchantmentEditorTileEntity te = (EnchantmentEditorTileEntity) playerInventory.player.world.getTileEntity(pos);
		return new EnchantmentEditorContainer(windowId, playerInventory, te);
	}
    
    @Override
	public boolean canInteractWith(PlayerEntity playerIn)
    {
        return this.editor.isUsableByPlayer(playerIn);
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

            if (index <= 2)
            {
                if (!this.mergeItemStack(itemstack1, 3, 39, false))
                {
                    return ItemStack.EMPTY;
                }
            }
            else if (!this.mergeItemStack(itemstack1, 0, 3, false))
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
