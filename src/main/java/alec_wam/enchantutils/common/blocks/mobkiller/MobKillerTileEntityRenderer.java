package alec_wam.enchantutils.common.blocks.mobkiller;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.BlockPos;

public class MobKillerTileEntityRenderer extends TileEntityRenderer<MobKillerTileEntity> {
	
	public MobKillerTileEntityRenderer(TileEntityRendererDispatcher p_i226006_1_) {
		super(p_i226006_1_);
	}

	@Override
	public void render(MobKillerTileEntity te, float pticks, MatrixStack matrix, IRenderTypeBuffer buffer, int light, int overlay) {
		if(te == null)return;
		if(te.isKillBoxVisible){
			//Push
			matrix.push();
			BlockPos pos = te.getPos();
			matrix.translate(-pos.getX(), -pos.getY(), -pos.getZ());
			WorldRenderer.drawBoundingBox(matrix, buffer.getBuffer(RenderType.getLines()), te.getKillBox(), 1.0F, 1.0F, 1.0F, 1.0F);
			matrix.pop();		
		}
	}
}


