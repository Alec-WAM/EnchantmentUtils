package alec_wam.enchantutils.common.entities.fireproof;

import java.util.Random;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class UpgradedItemRenderer extends EntityRenderer<UpgradedItemEntity> {
   private final net.minecraft.client.renderer.ItemRenderer itemRenderer;
   private final Random random = new Random();

   public UpgradedItemRenderer(EntityRendererManager renderManagerIn, net.minecraft.client.renderer.ItemRenderer p_i46167_2_) {
      super(renderManagerIn);
      this.itemRenderer = p_i46167_2_;
      this.shadowSize = 0.15F;
      this.shadowOpaque = 0.75F;
   }

   protected int getModelCount(ItemStack stack) {
      int i = 1;
      if (stack.getCount() > 48) {
         i = 5;
      } else if (stack.getCount() > 32) {
         i = 4;
      } else if (stack.getCount() > 16) {
         i = 3;
      } else if (stack.getCount() > 1) {
         i = 2;
      }

      return i;
   }

   @SuppressWarnings("deprecation")
   @Override
   public void render(UpgradedItemEntity p_225623_1_, float p_225623_2_, float p_225623_3_, MatrixStack p_225623_4_, IRenderTypeBuffer p_225623_5_, int p_225623_6_) {
      p_225623_4_.push();
      ItemStack itemstack = p_225623_1_.getItem();
      int i = itemstack.isEmpty() ? 187 : Item.getIdFromItem(itemstack.getItem()) + itemstack.getDamage();
      this.random.setSeed((long)i);
      IBakedModel ibakedmodel = this.itemRenderer.getItemModelWithOverrides(itemstack, p_225623_1_.world, (LivingEntity)null);
      boolean flag = ibakedmodel.isGui3d();
      int j = this.getModelCount(itemstack);
      //float f = 0.25F;
      float f1 = shouldBob() ? MathHelper.sin(((float)p_225623_1_.getAge() + p_225623_3_) / 10.0F + p_225623_1_.hoverStart) * 0.1F + 0.1F : 0;
      float f2 = ibakedmodel.getItemCameraTransforms().getTransform(ItemCameraTransforms.TransformType.GROUND).scale.getY();
      p_225623_4_.translate(0.0D, (double)(f1 + 0.25F * f2), 0.0D);
      float f3 = ((float)p_225623_1_.getAge() + p_225623_3_) / 20.0F + p_225623_1_.hoverStart;
      p_225623_4_.rotate(Vector3f.YP.rotation(f3));
      if (!flag) {
         float f7 = -0.0F * (float)(j - 1) * 0.5F;
         float f8 = -0.0F * (float)(j - 1) * 0.5F;
         float f9 = -0.09375F * (float)(j - 1) * 0.5F;
         p_225623_4_.translate((double)f7, (double)f8, (double)f9);
      }

      for(int k = 0; k < j; ++k) {
         p_225623_4_.push();
         if (k > 0) {
            if (flag) {
               float f11 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
               float f13 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
               float f10 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F;
               p_225623_4_.translate(shouldSpreadItems() ? f11 : 0, shouldSpreadItems() ? f13 : 0, shouldSpreadItems() ? f10 : 0);
            } else {
               float f12 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
               float f14 = (this.random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
               p_225623_4_.translate(shouldSpreadItems() ? f12 : 0, shouldSpreadItems() ? f14 : 0, 0.0D);
            }
         }

         this.itemRenderer.renderItem(itemstack, ItemCameraTransforms.TransformType.GROUND, false, p_225623_4_, p_225623_5_, p_225623_6_, OverlayTexture.NO_OVERLAY, ibakedmodel);
         p_225623_4_.pop();
         if (!flag) {
            p_225623_4_.translate(0.0, 0.0, 0.09375F);
         }
      }

      p_225623_4_.pop();
      super.render(p_225623_1_, p_225623_2_, p_225623_3_, p_225623_4_, p_225623_5_, p_225623_6_);
   }

   @SuppressWarnings("deprecation")
   @Override
   public ResourceLocation getEntityTexture(UpgradedItemEntity entity) {
      return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
   }
   
   /*==================================== FORGE START ===========================================*/

   /**
    * @return If items should spread out when rendered in 3D
    */
   public boolean shouldSpreadItems() {
      return true;
   }

   /**
    * @return If items should have a bob effect
    */
   public boolean shouldBob() {
      return true;
   }
   /*==================================== FORGE END =============================================*/

}