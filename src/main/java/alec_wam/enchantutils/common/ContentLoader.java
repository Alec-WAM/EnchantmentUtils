package alec_wam.enchantutils.common;

import alec_wam.enchantutils.client.ModParticles;
import alec_wam.enchantutils.client.ModSounds;
import alec_wam.enchantutils.client.gui.GuiManager;
import alec_wam.enchantutils.common.blocks.ModBlocks;
import alec_wam.enchantutils.common.entities.ModEntities;
import alec_wam.enchantutils.common.items.ModItems;

public class ContentLoader {

	public static final ContentLoader INSTANCE = new ContentLoader(); 
	
	public void start(){
		ModBlocks.constructBlocks();
		ModItems.constructItems();
		ModEntities.constructEntites();
		GuiManager.constructScreens();
		ModSounds.constructSounds();

		ModParticles.constructParticles();
	}

	public void clientSetup() {
		ModBlocks.clientSetup();
		ModEntities.clientSetup();
	}
	
}
