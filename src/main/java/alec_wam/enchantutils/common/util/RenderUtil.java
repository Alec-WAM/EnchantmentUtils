package alec_wam.enchantutils.common.util;

import java.util.List;
import java.util.Random;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BreakableBlock;
import net.minecraft.block.StainedGlassPaneBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.world.World;

public class RenderUtil {

	public static void renderScaledText(MatrixStack matrixstack, String text, FontRenderer fontRender, int x, int y, float maxWidth, float maxHeight, int color){
		matrixstack.push();
		float scale = Math.min(maxWidth / (text.length()), maxHeight);
		matrixstack.translate(x, y, 0.0);
		matrixstack.scale(scale, scale, 0.0F);
    	if(scale < maxHeight){
    		matrixstack.translate(0, (scale), 0.0);
    	}
    	fontRender.drawString(matrixstack, text, 0, 0, color);
    	matrixstack.pop();
	}

	@SuppressWarnings("deprecation")
	public static void renderItemInGui(ItemStack stack, int x, int y, ItemRenderer render, float alpha){
		IBakedModel bakedmodel = render.getItemModelWithOverrides(stack, (World)null, (LivingEntity)Minecraft.getInstance().player);
		RenderSystem.pushMatrix();
		Minecraft.getInstance().textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
		Minecraft.getInstance().textureManager.getTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE).setBlurMipmapDirect(false, false);
		RenderSystem.enableRescaleNormal();
		RenderSystem.enableAlphaTest();
		RenderSystem.defaultAlphaFunc();
		RenderSystem.enableBlend();
		RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, alpha);
		RenderSystem.translatef((float)x, (float)y, 100.0F + render.zLevel);
		RenderSystem.translatef(8.0F, 8.0F, 0.0F);
		RenderSystem.scalef(1.0F, -1.0F, 1.0F);
		RenderSystem.scalef(16.0F, 16.0F, 16.0F);
		MatrixStack matrixstack = new MatrixStack();
		IRenderTypeBuffer.Impl irendertypebuffer$impl = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
		boolean flag = !bakedmodel.isSideLit();
		if (flag) {
			RenderHelper.setupGuiFlatDiffuseLighting();
		}

		renderItem(render, stack, ItemCameraTransforms.TransformType.GUI, false, matrixstack, irendertypebuffer$impl, 15728880, OverlayTexture.NO_OVERLAY, bakedmodel, alpha);
		irendertypebuffer$impl.finish();
		RenderSystem.enableDepthTest();
		if (flag) {
			RenderHelper.setupGui3DDiffuseLighting();
		}

		RenderSystem.disableAlphaTest();
		RenderSystem.disableRescaleNormal();
		RenderSystem.popMatrix();
	}

	public static void renderItem(ItemRenderer render, ItemStack itemStackIn, ItemCameraTransforms.TransformType transformTypeIn, boolean leftHand, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn, IBakedModel modelIn, float alpha) {
		/*if (!p_229111_1_.isEmpty()) {
			p_229111_4_.push();
			boolean flag = p_229111_2_ == ItemCameraTransforms.TransformType.GUI;
			boolean flag1 = flag || p_229111_2_ == ItemCameraTransforms.TransformType.GROUND || p_229111_2_ == ItemCameraTransforms.TransformType.FIXED;
			if (p_229111_1_.getItem() == Items.TRIDENT && flag1) {
				p_229111_8_ = render.getItemModelMesher().getModelManager().getModel(new ModelResourceLocation("minecraft:trident#inventory"));
			}

			p_229111_8_ = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(p_229111_4_, p_229111_8_, p_229111_2_, p_229111_3_);
			p_229111_4_.translate(-0.5D, -0.5D, -0.5D);
			if (!p_229111_8_.isBuiltInRenderer() && (p_229111_1_.getItem() != Items.TRIDENT || flag1)) {
				RenderType rendertype = RenderTypeLookup.func_239219_a_(p_229111_1_);
				RenderType rendertype1;
				if (flag && Objects.equals(rendertype, Atlases.func_228784_i_())) {
					rendertype1 = Atlases.func_228785_j_();
				} else {
					rendertype1 = rendertype;
				}

				IVertexBuilder ivertexbuilder = ItemRenderer.func_229113_a_(p_229111_5_, rendertype1, true, p_229111_1_.hasEffect());
				func_229114_a_(render, p_229111_8_, p_229111_1_, p_229111_6_, p_229111_7_, p_229111_4_, ivertexbuilder, alpha);
			} else {
				p_229111_1_.getItem().getItemStackTileEntityRenderer().func_228364_a_(p_229111_1_, p_229111_4_, p_229111_5_, p_229111_6_, p_229111_7_);
			}

			p_229111_4_.func_227865_b_();
		}*/
		if (!itemStackIn.isEmpty()) {
	         matrixStackIn.push();
	         boolean flag = transformTypeIn == ItemCameraTransforms.TransformType.GUI || transformTypeIn == ItemCameraTransforms.TransformType.GROUND || transformTypeIn == ItemCameraTransforms.TransformType.FIXED;
	         if (itemStackIn.getItem() == Items.TRIDENT && flag) {
	            modelIn = render.getItemModelMesher().getModelManager().getModel(new ModelResourceLocation("minecraft:trident#inventory"));
	         }

	         modelIn = net.minecraftforge.client.ForgeHooksClient.handleCameraTransforms(matrixStackIn, modelIn, transformTypeIn, leftHand);
	         matrixStackIn.translate(-0.5D, -0.5D, -0.5D);
	         if (!modelIn.isBuiltInRenderer() && (itemStackIn.getItem() != Items.TRIDENT || flag)) {
	            boolean flag1;
	            if (transformTypeIn != ItemCameraTransforms.TransformType.GUI && !transformTypeIn.isFirstPerson() && itemStackIn.getItem() instanceof BlockItem) {
	               Block block = ((BlockItem)itemStackIn.getItem()).getBlock();
	               flag1 = !(block instanceof BreakableBlock) && !(block instanceof StainedGlassPaneBlock);
	            } else {
	               flag1 = true;
	            }
	            if (modelIn.isLayered()) { net.minecraftforge.client.ForgeHooksClient.drawItemLayered(render, modelIn, itemStackIn, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn, flag1); }
	            else {
	            RenderType rendertype = RenderTypeLookup.func_239219_a_(itemStackIn, flag1);
	            IVertexBuilder ivertexbuilder;
	            if (itemStackIn.getItem() == Items.COMPASS && itemStackIn.hasEffect()) {
	               matrixStackIn.push();
	               MatrixStack.Entry matrixstack$entry = matrixStackIn.getLast();
	               if (transformTypeIn == ItemCameraTransforms.TransformType.GUI) {
	                  matrixstack$entry.getMatrix().mul(0.5F);
	               } else if (transformTypeIn.isFirstPerson()) {
	                  matrixstack$entry.getMatrix().mul(0.75F);
	               }

	               if (flag1) {
	                  ivertexbuilder = ItemRenderer.getDirectGlintVertexBuilder(bufferIn, rendertype, matrixstack$entry);
	               } else {
	                  ivertexbuilder = ItemRenderer.getGlintVertexBuilder(bufferIn, rendertype, matrixstack$entry);
	               }

	               matrixStackIn.pop();
	            } else if (flag1) {
	               ivertexbuilder = ItemRenderer.getEntityGlintVertexBuilder(bufferIn, rendertype, true, itemStackIn.hasEffect());
	            } else {
	               ivertexbuilder = ItemRenderer.getBuffer(bufferIn, rendertype, true, itemStackIn.hasEffect());
	            }

	            renderModel(render, modelIn, itemStackIn, combinedLightIn, combinedOverlayIn, matrixStackIn, ivertexbuilder, alpha);
	            }
	         } else {
	            itemStackIn.getItem().getItemStackTileEntityRenderer().func_239207_a_(itemStackIn, transformTypeIn, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
	         }

	         matrixStackIn.pop();
		}
	
	}

	@SuppressWarnings("deprecation")
	private static void renderModel(ItemRenderer render, IBakedModel p_229114_1_, ItemStack p_229114_2_, int p_229114_3_, int p_229114_4_, MatrixStack p_229114_5_, IVertexBuilder p_229114_6_, float alpha) {
		Random random = new Random();
		//long i = 42L;

		for(Direction direction : Direction.values()) {
			random.setSeed(42L);
			func_229112_a_(render, p_229114_5_, p_229114_6_, p_229114_1_.getQuads((BlockState)null, direction, random), p_229114_2_, p_229114_3_, p_229114_4_, alpha);
		}

		random.setSeed(42L);
		func_229112_a_(render, p_229114_5_, p_229114_6_, p_229114_1_.getQuads((BlockState)null, (Direction)null, random), p_229114_2_, p_229114_3_, p_229114_4_, alpha);
	}

	public static void func_229112_a_(ItemRenderer render, MatrixStack p_229112_1_, IVertexBuilder p_229112_2_, List<BakedQuad> p_229112_3_, ItemStack p_229112_4_, int p_229112_5_, int p_229112_6_, float alpha) {
		boolean flag = !p_229112_4_.isEmpty();
		MatrixStack.Entry matrixstack$entry = p_229112_1_.getLast();

		for(BakedQuad bakedquad : p_229112_3_) {
			int i = -1;
			if (flag && bakedquad.hasTintIndex()) {
				i = Minecraft.getInstance().getItemColors().getColor(p_229112_4_, bakedquad.getTintIndex());
			}

			float f = (float)(i >> 16 & 255) / 255.0F;
			float f1 = (float)(i >> 8 & 255) / 255.0F;
			float f2 = (float)(i & 255) / 255.0F;
			p_229112_2_.addVertexData(matrixstack$entry, bakedquad, f, f1, f2, alpha, p_229112_5_, p_229112_6_, true);
		}

	}
	
	public static void innerBlit(MatrixStack matrixStack, int x1, int x2, int y1, int y2, int blitOffset, int uWidth, int vHeight, float uOffset, float vOffset, int textureWidth, int textureHeight) {
      innerBlit(matrixStack.getLast().getMatrix(), x1, x2, y1, y2, blitOffset, (uOffset + 0.0F) / (float)textureWidth, (uOffset + (float)uWidth) / (float)textureWidth, (vOffset + 0.0F) / (float)textureHeight, (vOffset + (float)vHeight) / (float)textureHeight);
	}
	
	@SuppressWarnings("deprecation")
	public static void innerBlit(Matrix4f matrix, int x1, int x2, int y1, int y2, int blitOffset, float minU, float maxU, float minV, float maxV) {
      BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
      bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
      bufferbuilder.pos(matrix, (float)x1, (float)y2, (float)blitOffset).tex(minU, maxV).endVertex();
      bufferbuilder.pos(matrix, (float)x2, (float)y2, (float)blitOffset).tex(maxU, maxV).endVertex();
      bufferbuilder.pos(matrix, (float)x2, (float)y1, (float)blitOffset).tex(maxU, minV).endVertex();
      bufferbuilder.pos(matrix, (float)x1, (float)y1, (float)blitOffset).tex(minU, minV).endVertex();
      bufferbuilder.finishDrawing();
      RenderSystem.enableAlphaTest();
      WorldVertexBufferUploader.draw(bufferbuilder);
   }
	
}
