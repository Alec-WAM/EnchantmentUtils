package alec_wam.enchantutils.common.entities;

import alec_wam.enchantutils.common.entities.fireproof.UpgradedItemEntity;
import alec_wam.enchantutils.common.util.RegistryHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ModEntities {

	public static EntityType<UpgradedItemEntity> UPGRADED_ITEM;
	
	public static void constructEntites() {	
		UPGRADED_ITEM = EntityType.Builder.<UpgradedItemEntity>create(UpgradedItemEntity::new, EntityClassification.MISC)
				.size(0.25F, 0.25F)
				.immuneToFire()
				.build("upgraded_item");
		RegistryHelper.ENTITIES.register("upgraded_item", () -> UPGRADED_ITEM);
	}

	@OnlyIn(Dist.CLIENT)
	public static void clientSetup() {				
		/*RenderingRegistry.registerEntityRenderingHandler(FIREPROOF_ITEM, new IRenderFactory<UpgradedItemEntity>(){

			@Override
			public EntityRenderer<UpgradedItemEntity> createRenderFor(EntityRendererManager manager) {
				return new UpgradedItemRenderer(manager, Minecraft.getInstance().getItemRenderer());
			}
			
		});*/
		RenderingRegistry.registerEntityRenderingHandler(UPGRADED_ITEM, new IRenderFactory<ItemEntity>(){

			@Override
			public EntityRenderer<ItemEntity> createRenderFor(EntityRendererManager manager) {
				return new ItemRenderer(manager, Minecraft.getInstance().getItemRenderer());
			}
			
		});
	}
}
