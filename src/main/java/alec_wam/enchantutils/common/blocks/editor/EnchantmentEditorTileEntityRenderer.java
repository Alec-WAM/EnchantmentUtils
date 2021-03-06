package alec_wam.enchantutils.common.blocks.editor;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3f;

public class EnchantmentEditorTileEntityRenderer extends TileEntityRenderer<EnchantmentEditorTileEntity> {
	
	public EnchantmentEditorTileEntityRenderer(TileEntityRendererDispatcher p_i226006_1_) {
		super(p_i226006_1_);
	}

	@Override
	public void render(EnchantmentEditorTileEntity te, float pticks, MatrixStack matrix, IRenderTypeBuffer buffer, int light, int overlay) {
		if(te == null || te.getStackInSlot(0).isEmpty())return;
		//Push
		matrix.push();
		float pixel = 1.0F / 16.0F;
		//Translate
		matrix.translate(8 * pixel, 14 * pixel, 8 * pixel);
		float rotation = 0.0F;
		Direction dir = te.getBlockState().get(EnchantmentEditorBlock.FACING);
		if(dir == Direction.SOUTH)rotation = 180;
		if(dir == Direction.WEST)rotation = 90;
		if(dir == Direction.EAST)rotation = 270;
		//Rotate Y+
		matrix.rotate(Vector3f.YP.rotationDegrees(rotation));
		//Rotate X+
		matrix.rotate(Vector3f.XP.rotationDegrees(45.0F));
		//Translate
		matrix.translate(0 * pixel, 0.5 * pixel, -2 * pixel);
		//Scale
		matrix.scale(0.4f, 0.4f, 0.4f);		
		ItemRenderer render = Minecraft.getInstance().getItemRenderer();
		//RenderItem
		render.renderItem(te.getStackInSlot(0), ItemCameraTransforms.TransformType.FIXED, light, overlay, matrix, buffer);		
		//Pop
		matrix.pop();
		
		
	}
}

