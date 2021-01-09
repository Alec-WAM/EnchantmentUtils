package alec_wam.enchantutils.common.blocks.upgradebench;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import alec_wam.enchantutils.common.blocks.ModBlocks;
import alec_wam.enchantutils.common.feature.upgradepoints.UpgradePointManager;
import alec_wam.enchantutils.common.feature.upgradepoints.upgrade.ElytraTrailUpgrade;
import alec_wam.enchantutils.common.feature.upgradepoints.upgrade.IBaseUpgrade;
import alec_wam.enchantutils.common.items.ModItems;
import alec_wam.enchantutils.common.network.IMessageHandler;
import alec_wam.enchantutils.common.tile.InventoryTileEntity;
import alec_wam.enchantutils.common.util.BlockUtil;
import alec_wam.enchantutils.common.util.LangUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public class UpgradeBenchTileEntity extends InventoryTileEntity implements IMessageHandler, INamedContainerProvider {

	public UpgradeBenchTileEntity() {
		super(ModBlocks.TILE_UPGRADE_BENCH, "UpgradeBench", 2);
	}
	
	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if(slot == 0){
			return !stack.isEmpty() && UpgradePointManager.isUpgradeable(stack);
		}
		if(slot == 1){
			return !stack.isEmpty() && stack.getItem() == ModItems.UPGRADE_MODULE;
		}
		return super.isItemValidForSlot(slot, stack);
	}
	
	@Override
	public boolean canInsertItem(int slot, ItemStack stack) {
		if(slot == 0){
			return UpgradePointManager.isUpgradeable(stack);
		}
		if(slot == 1){
			return stack.getItem() == ModItems.UPGRADE_MODULE;
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
		return super.canExtract(slot, amt);
	}
	
	@Override
	public int getInventoryStackLimit(int slot){
		if(slot == 0){
			return 1;
		}
		return getInventoryStackLimit();
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
		
		@Override
		public int getSlotLimit(int slot){
			return 1;
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

    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
    {
        if (side != null && cap == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY){
            return side == Direction.UP ? holderTop.cast() : holderSide.cast();
        }
        return super.getCapability(cap, side);
    }

	@Override
	public void handleMessage(String messageId, CompoundNBT messageData, boolean client) {
		if(messageId.equalsIgnoreCase("Update")){
			BlockUtil.markBlockForUpdate(getWorld(), getPos());
		}
		if(messageId.equalsIgnoreCase("AddUpgrade")){
			ItemStack tool = getStackInSlot(0);
			if(UpgradePointManager.hasUpgrades(tool)){
				ResourceLocation id = ResourceLocation.tryCreate(messageData.getString("id"));
				if(id !=null){
					IBaseUpgrade upgrade = UpgradePointManager.UPGRADE_REGISTRY.get(id);
					if(upgrade !=null){
						int lvl = messageData.getInt("level");
						int cost = messageData.getInt("cost");
						int currentPoints = UpgradePointManager.getToolPoints(tool);
						if(currentPoints >= cost){
							UpgradePointManager.putUpgrade(tool, upgrade, lvl);
							if(cost > 0){
								UpgradePointManager.addToolPoints(tool, -cost);
							}
						}
					}
				}
			}
		}
		if(messageId.equalsIgnoreCase("ElytraTrail")){
			
			ItemStack tool = getStackInSlot(0);
			if(UpgradePointManager.isUpgradeable(tool)){
				if(UpgradePointManager.hasUpgrades(tool)){
					ElytraTrailUpgrade.ElytraTrail newTrail = ElytraTrailUpgrade.getTrailType(tool);
					if(messageData.contains("Type")){
						String type = messageData.getString("Type");
						for(ElytraTrailUpgrade.ElytraTrail trail : ElytraTrailUpgrade.ElytraTrail.values()){
							if(trail.name().equalsIgnoreCase(type)){
								newTrail = trail;
								break;
							}
						}
					}					
					
					int color = ElytraTrailUpgrade.getTrailColor(tool);
					if(messageData.contains("Color")) color = messageData.getInt("Color");
					ElytraTrailUpgrade.setTrailType(tool, newTrail, color);
				}
			}
		}
		if(messageId.equalsIgnoreCase("InstallModule")){
			ItemStack tool = getStackInSlot(0);
			if(UpgradePointManager.isUpgradeable(tool)){
				if(!UpgradePointManager.hasUpgrades(tool)){
					UpgradePointManager.setupUpgrades(tool);
					if(!messageData.contains("Creative"))
						decrStackSize(1, 1);
				}
			}
		}
	}

	@Override
	public Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity player) {
		return new UpgradeBenchContainer(windowId, playerInventory, this);
	}

	@Override
	public ITextComponent getDisplayName() {
		return new StringTextComponent(LangUtil.localize("gui.upgradebench"));
	}

}

