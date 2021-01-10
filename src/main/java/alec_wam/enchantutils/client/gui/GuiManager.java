package alec_wam.enchantutils.client.gui;

import alec_wam.enchantutils.EnchantmentUtils;
import alec_wam.enchantutils.common.blocks.editor.EnchantmentEditorContainer;
import alec_wam.enchantutils.common.blocks.editor.EnchantmentEditorGui;
import alec_wam.enchantutils.common.blocks.mobkiller.MobKillerContainer;
import alec_wam.enchantutils.common.blocks.mobkiller.MobKillerScreen;
import alec_wam.enchantutils.common.blocks.upgradebench.UpgradeBenchContainer;
import alec_wam.enchantutils.common.blocks.upgradebench.UpgradeBenchScreen;
import alec_wam.enchantutils.common.util.RegistryHelper;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeContainerType;

public class GuiManager {

	public final static ResourceLocation TEXTURE_WIDGETS = EnchantmentUtils.resourceL("textures/gui/widgets.png");

	public static ContainerType<EnchantmentEditorContainer> ENCHANTMENT_EDITOR;
	public static ContainerType<UpgradeBenchContainer> UPGRADE_BENCH;
	public static ContainerType<MobKillerContainer> MOB_KILLER;
	
	public static void constructScreens(){
		ENCHANTMENT_EDITOR = IForgeContainerType.create(EnchantmentEditorContainer::fromNetwork);
		RegistryHelper.CONTAINERS.register("enchantment_editor", () -> ENCHANTMENT_EDITOR);
		
		UPGRADE_BENCH = IForgeContainerType.create(UpgradeBenchContainer::fromNetwork);
		RegistryHelper.CONTAINERS.register("upgrade_bench", () -> UPGRADE_BENCH);
		
		MOB_KILLER = IForgeContainerType.create(MobKillerContainer::fromNetwork);
		RegistryHelper.CONTAINERS.register("mob_killer", () -> MOB_KILLER);
	}
	
	@OnlyIn(Dist.CLIENT)
	public static void clientSetup() {
		ScreenManager.registerFactory(ENCHANTMENT_EDITOR, EnchantmentEditorGui::new);
		ScreenManager.registerFactory(UPGRADE_BENCH, UpgradeBenchScreen::new);
		ScreenManager.registerFactory(MOB_KILLER, MobKillerScreen::new);
	}
	
}
