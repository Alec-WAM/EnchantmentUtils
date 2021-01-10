package alec_wam.enchantutils.common.blocks.mobkiller;

import alec_wam.enchantutils.common.blocks.CustomContainerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemGroup;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.network.NetworkHooks;

public class MobKillerBlock extends CustomContainerBlock {

	public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
	
	public MobKillerBlock() {
		super("mob_killer", ItemGroup.REDSTONE, Block.Properties.create(Material.ROCK).setRequiresTool().hardnessAndResistance(3.5F).sound(SoundType.STONE));
	}

	//OnActivated
	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult ray)
	{
		TileEntity tile = worldIn.getTileEntity(pos);
		if(tile == null || !(tile instanceof INamedContainerProvider))return ActionResultType.PASS;
		if(worldIn.isRemote)return ActionResultType.SUCCESS;
		if (player instanceof ServerPlayerEntity && !(player instanceof FakePlayer))
		{
			NetworkHooks.openGui((ServerPlayerEntity) player, (MobKillerTileEntity) worldIn.getTileEntity(pos), pos);
		}
		return ActionResultType.SUCCESS;
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}	
	
	@Override
	public TileEntity createNewTileEntity(IBlockReader worldIn) {
		return new MobKillerTileEntity();
	}
	
	@Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
		Direction enumfacing = context.getPlacementHorizontalFacing().getOpposite();
		return this.getDefaultState().with(FACING, enumfacing);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.with(FACING, rot.rotate(state.get(FACING)));
	}

	@SuppressWarnings("deprecation")
	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.toRotation(state.get(FACING)));
	}	
	
}

