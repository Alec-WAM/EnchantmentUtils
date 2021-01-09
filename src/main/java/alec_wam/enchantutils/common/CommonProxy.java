package alec_wam.enchantutils.common;

import alec_wam.enchantutils.common.feature.upgradepoints.CapabilityEntityDamageXP;
import alec_wam.enchantutils.common.feature.upgradepoints.UpgradePointEventHandler;
import alec_wam.enchantutils.common.feature.upgradepoints.UpgradePointManager;
import alec_wam.enchantutils.common.loot.ModLootModifiers;
import alec_wam.enchantutils.common.network.ModNetwork;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class CommonProxy {

	public void start() {
		ModNetwork.initChannel();
		ContentLoader.INSTANCE.start();
		
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		registerListeners(bus);
		bus.addListener(this::preInit);
		
		UpgradePointManager.registerUpgrades();
		ModLootModifiers.setupLoot();
	}
	
	public void preInit(FMLCommonSetupEvent evt){
		CapabilityEntityDamageXP.register();		
	}
	
	public void registerListeners(IEventBus bus) {
		//bus.addListener(this::setup);
		MinecraftForge.EVENT_BUS.register(new UpgradePointEventHandler());
	}
	
}
