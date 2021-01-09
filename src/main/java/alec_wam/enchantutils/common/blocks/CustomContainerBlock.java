package alec_wam.enchantutils.common.blocks;

import java.util.List;

import javax.annotation.Nullable;

import alec_wam.enchantutils.EnchantmentUtils;
import alec_wam.enchantutils.common.util.BlockUtil;
import alec_wam.enchantutils.common.util.RegistryHelper;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public abstract class CustomContainerBlock extends ContainerBlock {
	public static final ResourceLocation CONTENTS = EnchantmentUtils.resourceL("contents");
	
	public CustomContainerBlock(String regname, ItemGroup creativeTab, Properties properties) {
		super(properties);
		
		RegistryHelper.registerBlock(this, regname, creativeTab);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			TileEntity tileentity = worldIn.getTileEntity(pos);
			if (tileentity instanceof IInventory && !(tileentity instanceof INBTDrop)) {
				InventoryHelper.dropInventoryItems(worldIn, pos, ((IInventory)tileentity));
				worldIn.updateComparatorOutputLevel(pos, this);
			}

			super.onReplaced(state, worldIn, pos, newState, isMoving);
		} 
	}
	
	@Override
    public void onBlockPlacedBy(World world, BlockPos pos, BlockState blockState, LivingEntity entityliving, ItemStack itemStack)
    {
        TileEntity te = world.getTileEntity(pos);
        if (te != null && te instanceof INBTDrop)
        {
        	INBTDrop nbtTile = (INBTDrop) te;
            if(itemStack.hasTag()){
            	nbtTile.readFromItemNBT(itemStack);
                BlockUtil.markBlockForUpdate(world, pos);
            }
        }
    }
	
	@SuppressWarnings("deprecation")
	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		ServerWorld world = builder.getWorld();
		TileEntity tile = builder.get(LootParameters.BLOCK_ENTITY);
		builder = builder.withDynamicDrop(CONTENTS, (context, stackConsumer) -> {
             stackConsumer.accept(getNBTDrop(world, tile));
		});
		return super.getDrops(state, builder);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public ItemStack getItem(IBlockReader worldIn, BlockPos pos, BlockState state) {
		TileEntity tileentity = worldIn.getTileEntity(pos);
		return tileentity instanceof INBTDrop ? getNBTDrop(worldIn, tileentity) : super.getItem(worldIn, pos, state);
	}
	
	@Override	
	public boolean removedByPlayer(BlockState state, World world, BlockPos pos, PlayerEntity player, boolean willHarvest, FluidState fluid)
    {
		if(willHarvest){
			return true;
		}
        return super.removedByPlayer(state, world, pos, player, willHarvest, fluid);
    }
	
	@Override
	public void harvestBlock(World worldIn, PlayerEntity player, BlockPos pos, BlockState state, @Nullable TileEntity te, ItemStack stack) {
		super.harvestBlock(worldIn, player, pos, state, te, stack);
		worldIn.removeBlock(pos, false);
	}
	
	protected ItemStack getNBTDrop(IBlockReader world, TileEntity tileEntity) {
		ItemStack stack = new ItemStack(this);
		if(tileEntity instanceof INBTDrop){
			INBTDrop nbtTile = (INBTDrop)tileEntity;
			nbtTile.writeToItemNBT(stack);
		}
		return stack;
	}
	
}

