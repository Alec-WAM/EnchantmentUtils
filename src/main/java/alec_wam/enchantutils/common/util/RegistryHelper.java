package alec_wam.enchantutils.common.util;

import static alec_wam.enchantutils.EnchantmentUtils.MOD_ID;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Pair;

import alec_wam.enchantutils.EnchantmentUtils;
import alec_wam.enchantutils.common.util.registry.IBlockColorProvider;
import alec_wam.enchantutils.common.util.registry.IBlockItemProvider;
import alec_wam.enchantutils.common.util.registry.IItemColorProvider;
import alec_wam.enchantutils.common.util.registry.IItemPropertiesFiller;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class RegistryHelper {

	private static Queue<Pair<Item, IItemColorProvider>> itemColors = new ArrayDeque<>();
	private static Queue<Pair<Block, IBlockColorProvider>> blockColors = new ArrayDeque<>();	
	
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
    public static final DeferredRegister<TileEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MOD_ID);
    public static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, MOD_ID);
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, MOD_ID);
    public static final DeferredRegister<GlobalLootModifierSerializer<?>> GLM = DeferredRegister.create(ForgeRegistries.LOOT_MODIFIER_SERIALIZERS, MOD_ID);
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, MOD_ID);
    
	public static void setupRegistries(){
		BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        TILES.register(FMLJavaModLoadingContext.get().getModEventBus());
        CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());
        SOUNDS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        GLM.register(FMLJavaModLoadingContext.get().getModEventBus());
        PARTICLES.register(FMLJavaModLoadingContext.get().getModEventBus());
	}
	
	public static void registerBlock(Block block, String resloc) {
		registerBlock(block, resloc, true, null);
	}
	
	public static void registerBlock(Block block, String resloc, @Nullable ItemGroup group) {
		registerBlock(block, resloc, true, group);
	}

	public static void registerBlock(Block block, String resloc, boolean hasBlockItem, @Nullable ItemGroup group) {
		BLOCKS.register(resloc, () -> block);

		if(hasBlockItem) {
			ITEMS.register(resloc, () -> createItemBlock(block, group));
		}

		if(block instanceof IBlockColorProvider)
			blockColors.add(Pair.of(block, (IBlockColorProvider) block));
	}

	public static void registerItem(Item item, String resloc) {
		ITEMS.register(resloc, () -> item);

		if(item instanceof IItemColorProvider)
			itemColors.add(Pair.of(item, (IItemColorProvider) item));
	}
	
	public static void registerTile(TileEntityType<?> tile, String resloc) {
		TILES.register(resloc, () -> tile);
	}
	
	public static ItemGroup createTab(String name, Supplier<ItemStack> stack) {
        return new ItemGroup(name) {
            @Override
            public ItemStack createIcon() {
                return stack.get();
            }
        };
    }
	
	
	public static Item createItemBlock(Block block, @Nullable ItemGroup group) {
		Item.Properties props = new Item.Properties();
		if(group != null)
			props = props.group(group);

		if(block instanceof IItemPropertiesFiller)
			((IItemPropertiesFiller) block).fillItemProperties(props);

		BlockItem blockitem;
		if(block instanceof IBlockItemProvider)
			blockitem = ((IBlockItemProvider) block).provideItemBlock(block, props);
		else blockitem = new BlockItem(block, props);

		if(block instanceof IItemColorProvider)
			itemColors.add(Pair.of(blockitem, (IItemColorProvider) block));

		return blockitem;
	}

	@SuppressWarnings("deprecation")
	public static void loadComplete(FMLLoadCompleteEvent event) {
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> loadCompleteClient(event));

		itemColors.clear();
		blockColors.clear();
	}

	@OnlyIn(Dist.CLIENT)
	private static boolean loadCompleteClient(FMLLoadCompleteEvent event) {
		Minecraft mc = Minecraft.getInstance();
		BlockColors bcolors = mc.getBlockColors();
		ItemColors icolors = mc.getItemColors();

		while(!blockColors.isEmpty()) {
			Pair<Block, IBlockColorProvider> pair = blockColors.poll();
			IBlockColor color = pair.getSecond().getBlockColor();

			bcolors.register(color, pair.getFirst());
		}

		while(!itemColors.isEmpty()) {
			Pair<Item, IItemColorProvider> pair = itemColors.poll();
			IItemColor color = pair.getSecond().getItemColor();

			icolors.register(color, pair.getFirst());
		}

		return true;
	}
	

}