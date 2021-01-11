package alec_wam.enchantutils.client;

import java.awt.Color;
import java.util.Map;

import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import alec_wam.enchantutils.EnchantmentUtils;
import alec_wam.enchantutils.client.ModParticles.TrailParticle;
import alec_wam.enchantutils.client.gui.GuiManager;
import alec_wam.enchantutils.common.CommonProxy;
import alec_wam.enchantutils.common.ContentLoader;
import alec_wam.enchantutils.common.feature.upgradepoints.UpgradePointManager;
import alec_wam.enchantutils.common.feature.upgradepoints.upgrade.IBaseUpgrade;
import alec_wam.enchantutils.common.util.ItemUtil;
import alec_wam.enchantutils.common.util.LangUtil;
import alec_wam.enchantutils.common.util.RenderUtil;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.util.InputMappings;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.GrindstoneContainer;
import net.minecraft.inventory.container.RepairContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientProxy extends CommonProxy {

	@Override
	public void start(){
		super.start();
	}
	
	@Override
	public void registerListeners(IEventBus bus) {
		super.registerListeners(bus);

		bus.addListener(this::clientSetup);

		bus.addListener(this::registerParticleFactories);
	}
	
	public void clientSetup(FMLClientSetupEvent event) {
		GuiManager.clientSetup();
		ContentLoader.INSTANCE.clientSetup();
	}

	@SubscribeEvent
    @OnlyIn(Dist.CLIENT)
	public static void addCustomTooltips(ItemTooltipEvent event){
		handleEnchantmentChecker(event);
		handleGrindstone(event);
		handleUpgradePoints(event);
	}
	
	@SubscribeEvent
	public void registerParticleFactories(ParticleFactoryRegisterEvent event){
		Minecraft mc = Minecraft.getInstance();
		mc.particles.registerFactory(ModParticles.BUBBLE_TRAIL.get(), TrailParticle.Factory::new);
		mc.particles.registerFactory(ModParticles.FLAME_TRAIL.get(), TrailParticle.Factory::new);
		mc.particles.registerFactory(ModParticles.GLINT_TRAIL.get(), TrailParticle.Factory::new);
		mc.particles.registerFactory(ModParticles.SPARKLE_TRAIL.get(), TrailParticle.Factory::new);
		mc.particles.registerFactory(ModParticles.HEART_TRAIL.get(), TrailParticle.Factory::new);
		mc.particles.registerFactory(ModParticles.COLOR_TRAIL.get(), TrailParticle.ColorFactory::new);
	}

	private static int UPGRADE_ANIMATION_TIMER_START;
	private static int UPGRADE_ANIMATION_TIMER;
	private static ItemStack UPGRADE_OVERLAY_STACK = ItemStack.EMPTY;
	
	public static void startUpgradePointAnimation(ItemStack stack, int length){
		UPGRADE_ANIMATION_TIMER_START = length;
		UPGRADE_ANIMATION_TIMER = length;
		UPGRADE_OVERLAY_STACK = stack;
	}
	
	@SubscribeEvent
    @OnlyIn(Dist.CLIENT)
	public static void clientTick(ClientTickEvent event){
		if(Minecraft.getInstance().currentScreen == null && InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), GLFW.GLFW_KEY_U)){
			//U
			ItemStack stack = new ItemStack(Items.DIAMOND_PICKAXE);
			stack.setDamage(10);
			stack.setDisplayName(new StringTextComponent("Test Pickaxe"));
			startUpgradePointAnimation(stack, 10 * 20);
		}
		
		if(UPGRADE_ANIMATION_TIMER > 0){
			UPGRADE_ANIMATION_TIMER--;
			if(UPGRADE_ANIMATION_TIMER <= 0){
				UPGRADE_OVERLAY_STACK = ItemStack.EMPTY;
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@SubscribeEvent
    @OnlyIn(Dist.CLIENT)
	public static void renderOverlays(RenderGameOverlayEvent event){
		if(event.getType() == RenderGameOverlayEvent.ElementType.ALL){
			renderUpgradePointAnimation(event);
			
			//Elytra Motion
			/*MatrixStack matrixstack = event.getMatrixStack();
			Minecraft mc = Minecraft.getInstance();
			if(mc.currentScreen == null){
				PlayerEntity player = mc.player;
				if(player.isElytraFlying()){
					double speed = Math.sqrt(LivingEntity.horizontalMag(player.getMotion()));
					String speedStr = "" + speed;
					MainWindow window = event.getWindow();
					int overlayX = window.getScaledWidth() - 5 - mc.fontRenderer.getStringWidth(speedStr);
					int overlayY = 20;
					matrixstack.push();
					matrixstack.translate(overlayX, overlayY, 0);
					
					mc.fontRenderer.drawString(matrixstack, speedStr, 0, 0, 0xffffff);
					matrixstack.pop();
				}
			}*/
			Minecraft mc = Minecraft.getInstance();
			MatrixStack matrixstack = event.getMatrixStack();
			if(mc.currentScreen == null){
				MainWindow window = event.getWindow();
				PlayerEntity player = mc.player;
				ItemStack hand = player.getHeldItemMainhand();
				if(!hand.isEmpty()){
					if(UpgradePointManager.isFeatureEnabled()){
						if(UpgradePointManager.hasUpgrades(hand)){
							int overlayWidth = 100;
							int overlayHeight = 8;
							int overlayX = 0;
							int overlayY = 0;
							
							//0 = BR, 1 = BL, 2 = TL, 3 = TR
							int overlayMode = 0;
							if(overlayMode == 0){
								overlayX = window.getScaledWidth() - overlayWidth - 5;
								overlayY = window.getScaledHeight() - 10;
							}
							if(overlayMode == 1){
								overlayX = 15;
								overlayY = window.getScaledHeight() - 10;
							}
							if(overlayMode == 2){
								overlayX = 15;
								overlayY = 10;
							}
							if(overlayMode == 3){
								overlayX = window.getScaledWidth() - overlayWidth - 5;
								overlayY = 10;
							}
							
							RenderSystem.pushMatrix();
							RenderSystem.enableRescaleNormal();
					        RenderSystem.enableBlend();
					        RenderSystem.defaultBlendFunc();
							
							matrixstack.push();
							mc.getTextureManager().bindTexture(GuiManager.TEXTURE_WIDGETS);
							
							matrixstack.translate(overlayX, overlayY, 0);
							//RenderUtil.innerBlit(matrixstack, 0, 0, 0, overlayWidth, 10, 0, 43, 182, 5, 256, 256);
							Screen.blit(matrixstack, 0, 0, overlayWidth, overlayHeight, 0, 55, 182, 10, 256, 256);
							
							double xpValue = UpgradePointManager.getToolXP(hand);
							double maxXP = UpgradePointManager.getMaxXP(hand);
							//int barValue = (int)((overlayWidth - 1) * (xpValue / maxXP));
							int barValue = Math.max(1, (int)((xpValue / maxXP) * (overlayWidth)));
							
							if(xpValue > 0)Screen.fill(matrixstack, 1, 1, barValue, overlayHeight - 1, Color.GREEN.getRGB());
							matrixstack.pop();
							
							RenderSystem.disableRescaleNormal();
					        RenderSystem.disableBlend();
					        RenderSystem.popMatrix();
					        
					        String xpStr = (int)xpValue + " / " + (int)maxXP;
					        float scale = 0.7f;
					        int strX = (int) (overlayX + (overlayWidth / 2) - ((mc.fontRenderer.getStringWidth(xpStr) * scale) / 2));
					        
					        matrixstack.push();
					        matrixstack.translate(strX, overlayY - (10 * scale), 0);
					        matrixstack.scale(scale, scale, 1.0f);
					        mc.fontRenderer.drawStringWithShadow(matrixstack, xpStr, 0, 0, Color.GREEN.getRGB());
					        matrixstack.pop();
					        
					        int points = UpgradePointManager.getToolPoints(hand);
					        if(points > 0){
					        	String pointStr = "+" + points;
					        	int pointX = (int) (overlayX - 6 - ((mc.fontRenderer.getStringWidth(pointStr) * scale) / 2));
					        	matrixstack.push();
						        matrixstack.translate(pointX, overlayY + (2 * scale), 0);
						        matrixstack.scale(scale, scale, 1.0f);
						        mc.fontRenderer.drawStringWithShadow(matrixstack, pointStr, 0, 0, Color.YELLOW.darker().getRGB());
						        matrixstack.pop();
					        }
						}
					}
				}
			}
		}
	}
	
	//TODO Maybe create buffer
	@SuppressWarnings("deprecation")
	private static void renderUpgradePointAnimation(RenderGameOverlayEvent event) {
		MatrixStack matrixstack = event.getMatrixStack();
		Minecraft mc = Minecraft.getInstance();
		if(mc.currentScreen == null && UPGRADE_ANIMATION_TIMER > 0){
			MainWindow window = event.getWindow();
			int overlayX = window.getScaledWidth() / 2;
			int overlayY = window.getScaledHeight() / 4;
			ItemStack renderStack = UPGRADE_OVERLAY_STACK;
			
			ItemRenderer itemRenderer = mc.getItemRenderer();
			
			int opacity = 0;
	        int timeToFade = 20 * 3;
        	int incrs = 255 / timeToFade;
	        int duration = (UPGRADE_ANIMATION_TIMER_START - UPGRADE_ANIMATION_TIMER);
	        if(duration <= timeToFade){	 
	        	opacity = (duration * incrs);	        	
	        } else if(UPGRADE_ANIMATION_TIMER < timeToFade){	        	
	        	opacity = UPGRADE_ANIMATION_TIMER * incrs;
	        } else {
	        	opacity = 255;
	        }
			
			RenderSystem.pushMatrix();
			RenderSystem.enableRescaleNormal();
	        RenderSystem.enableBlend();
	        RenderSystem.defaultBlendFunc();
	        
	        RenderSystem.pushMatrix();
	        RenderSystem.translated(overlayX, overlayY, 0);
	        double scale = 2.0;
	        RenderSystem.pushMatrix();	        
	        RenderSystem.scaled(scale, scale, 0);
	        RenderSystem.translated(-(8 * (scale / 2)), -(8 * (scale / 2)), 0);
	        
	        RenderSystem.enableAlphaTest();
	        RenderSystem.defaultAlphaFunc();
	        RenderSystem.enableBlend();
	        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
	        RenderUtil.renderItemInGui(renderStack, 0, 0, itemRenderer, opacity / 255.0f);
	        if (renderStack.getItem().showDurabilityBar(renderStack)) {
	        	RenderSystem.disableDepthTest();
	        	RenderSystem.disableTexture();
	        	double health = renderStack.getItem().getDurabilityForDisplay(renderStack);
	        	int i = Math.round(13.0F - (float)health * 13.0F);
	        	int j = renderStack.getItem().getRGBDurabilityForDisplay(renderStack);
	        	int color = ((opacity & 0xff) << 24) | (j & 0x00ffffff);
	        	int colorBlack = ((opacity & 0xff) << 24) | (Color.BLACK.getRGB() & 0x00ffffff);
	        	Screen.fill(matrixstack, 2, 13, 15, 15, colorBlack);
	        	Screen.fill(matrixstack, 2, 13, 2 + i, 14, color);
	        	RenderSystem.enableTexture();
	        	RenderSystem.enableDepthTest();
	        }
	        RenderSystem.disableAlphaTest();
	        
	        RenderSystem.popMatrix();
	        
	        
	        int textYOffset = 8;
	        RenderSystem.translated(0, (textYOffset * (scale)), 0);
	        String text = LangUtil.localize("popup.upgradepoint.unlock");
	        
	        int textColorDisplayName = ((opacity & 0xff) << 24) | (Color.WHITE.getRGB() & 0x00ffffff);
	        int textColor = ((opacity & 0xff) << 24) | (Color.YELLOW.getRGB() & 0x00ffffff);
	        if(opacity > 0){
	        	IFormattableTextComponent displayName = (new StringTextComponent("")).append(renderStack.getDisplayName()).mergeStyle(renderStack.getRarity().color);
	            if (renderStack.hasDisplayName()) {
	            	displayName.mergeStyle(TextFormatting.ITALIC);
	            }
	        	mc.fontRenderer.func_243246_a(matrixstack, displayName, -mc.fontRenderer.getStringPropertyWidth(displayName) / 2, 0, textColorDisplayName);
	        	
	        	mc.fontRenderer.drawStringWithShadow(matrixstack, text, -mc.fontRenderer.getStringWidth(text) / 2, 12, textColor);
	        }
	        
	        
	        RenderSystem.popMatrix();
	        
	        RenderSystem.disableRescaleNormal();
	        RenderSystem.disableBlend();
	        RenderSystem.popMatrix();
		}
	}

	private static void handleUpgradePoints(ItemTooltipEvent event) {
		ItemStack stack = event.getItemStack();
		if(UpgradePointManager.isFeatureEnabled()){
			if(UpgradePointManager.hasUpgrades(stack)){
				double xp = UpgradePointManager.getToolXP(stack);
				double maxXp = UpgradePointManager.getMaxXP(stack);
				
				if(UpgradePointManager.isBasicUpgradeItem(stack)){
					event.getToolTip().add(new StringTextComponent(TextFormatting.GREEN + LangUtil.localize("tooltip.upgradepoints.basicitem")));
				}
				else event.getToolTip().add(new StringTextComponent(TextFormatting.GREEN + LangUtil.localizeFormat("tooltip.upgradepoints.xp", "" + (int)xp, "" + (int)maxXp)));
				
				int points = UpgradePointManager.getToolPoints(stack);
				if(points > 0){
					event.getToolTip().add(new StringTextComponent(TextFormatting.GOLD + LangUtil.localizeFormat("tooltip.upgradepoints.points", "" + points)));
				}
				Map<IBaseUpgrade, Integer> upgrades = UpgradePointManager.getUpgrades(stack);
				if(!upgrades.isEmpty()){
					boolean isShiftDown = Screen.hasShiftDown();
					if(!isShiftDown){
						event.getToolTip().add(new StringTextComponent(TextFormatting.DARK_PURPLE + LangUtil.localize("tooltip.upgrades.shift")));
					} else {
						for(IBaseUpgrade upgrade : upgrades.keySet()){
							int lvl = upgrades.get(upgrade);
							event.getToolTip().addAll(upgrade.getToolTipName(event.getPlayer(), stack, lvl));
						}
					}
				}
			}
		}
	}

	private static void handleEnchantmentChecker(ItemTooltipEvent event) {
		Container container = Minecraft.getInstance().player.openContainer;
		if(container !=null && container instanceof RepairContainer){
			RepairContainer anvil = (RepairContainer)container;
			ItemStack hoverStack = event.getItemStack();
			if(hoverStack.getItem() == Items.ENCHANTED_BOOK){
				Slot itemSlot = anvil.getSlot(0);
				if(itemSlot.getHasStack()){
					ItemStack stack = itemSlot.getStack();

					if(stack.getItem() == Items.ENCHANTED_BOOK)return;
					
					Map<Enchantment, Integer> stackEnchantments = EnchantmentHelper.getEnchantments(stack);
					Map<Enchantment, Integer> bookEnchantments = EnchantmentHelper.getEnchantments(hoverStack);

					Map<Enchantment, Integer> invalidEnchants = Maps.newHashMap();

					for(Enchantment enchantment : bookEnchantments.keySet()) {
						if (enchantment != null) {
							boolean canApply = enchantment.canApply(stack); 

							for(Enchantment otherEnchantment : stackEnchantments.keySet()) {
								if (otherEnchantment != enchantment && !otherEnchantment.isCompatibleWith(enchantment)) {
									canApply = false;
								}
							}

							if(!canApply){
								invalidEnchants.put(enchantment, bookEnchantments.get(enchantment));
							}
						}
					}

					if(!invalidEnchants.isEmpty()){
						if(invalidEnchants.size() == 1){
							event.getToolTip().add(new TranslationTextComponent(EnchantmentUtils.resourceDot("tooltip.enchantment.invalid")).mergeStyle(TextFormatting.RED));
						} else {
							event.getToolTip().add(new TranslationTextComponent(EnchantmentUtils.resourceDot("tooltip.enchantment.invalid.multi")).mergeStyle(TextFormatting.RED));
						}
						for(Enchantment enchantment : invalidEnchants.keySet()){
							event.getToolTip().add(new StringTextComponent(TextFormatting.RED + "- " + enchantment.getDisplayName(invalidEnchants.get(enchantment)).getString()));
						}
					}
				}
			}
		}
	}
	
	private static void handleGrindstone(ItemTooltipEvent event) {
		Container container = Minecraft.getInstance().player.openContainer;
		if(container !=null && container instanceof GrindstoneContainer){
			//GrindstoneContainer grindstone = (GrindstoneContainer)container;
			ItemStack hoverStack = event.getItemStack();
			if(hoverStack.isEnchanted()){
				int fullXP = ItemUtil.getEnchantmentXp(hoverStack);
				int minXP = (int)Math.ceil((double)fullXP / 2.0D);
				event.getToolTip().add(new StringTextComponent(minXP + "-" + fullXP+"xp").mergeStyle(TextFormatting.GREEN));
			}
		}
	}
}
