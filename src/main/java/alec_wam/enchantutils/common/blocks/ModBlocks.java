package alec_wam.enchantutils.common.blocks;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import alec_wam.enchantutils.common.blocks.anvil.ReinforcedAnvilBlock;
import alec_wam.enchantutils.common.blocks.editor.EnchantmentEditorBlock;
import alec_wam.enchantutils.common.blocks.editor.EnchantmentEditorTileEntity;
import alec_wam.enchantutils.common.blocks.editor.EnchantmentEditorTileEntityRenderer;
import alec_wam.enchantutils.common.blocks.mobkiller.MobKillerBlock;
import alec_wam.enchantutils.common.blocks.mobkiller.MobKillerTileEntity;
import alec_wam.enchantutils.common.blocks.upgradebench.UpgradeBenchBlock;
import alec_wam.enchantutils.common.blocks.upgradebench.UpgradeBenchTileEntity;
import alec_wam.enchantutils.common.blocks.upgradebench.UpgradeBenchTileEntityRenderer;
import alec_wam.enchantutils.common.util.RegistryHelper;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ModBlocks {

	public static Block ENCHANTMENT_EDITOR;
	public static TileEntityType<EnchantmentEditorTileEntity> TILE_ENCHANTMENT_EDITOR;
	
	public static Map<String, Block> UPGRADE_BENCH_MAP = Maps.newHashMap();
	public static List<Supplier<Block>> UPGRADE_BENCHES = new LinkedList<>();
	public static TileEntityType<UpgradeBenchTileEntity> TILE_UPGRADE_BENCH;
	
	public static Block REINFORCED_ANVIL;
	
	public static Block MOB_KILLER;
	public static TileEntityType<MobKillerTileEntity> TILE_MOB_KILLER;
	
	private static final ImmutableSet<String> WOODS = ImmutableSet.of(
			"oak", "spruce", "birch", "jungle", "acacia", "dark_oak", "crimson", "warped"
	);
	
	//TODO EnchantmentEditor Recipe
	//TODO Upgrade Module Recipe
	public static void constructBlocks() {
		
		ENCHANTMENT_EDITOR = new EnchantmentEditorBlock();
		TILE_ENCHANTMENT_EDITOR = TileEntityType.Builder.create(EnchantmentEditorTileEntity::new, ENCHANTMENT_EDITOR).build(null);
		RegistryHelper.registerTile(TILE_ENCHANTMENT_EDITOR, "enchantment_editor");
		
		for(String wood : WOODS){
			Block bench = new UpgradeBenchBlock(wood);
			UPGRADE_BENCH_MAP.put(wood, bench);
			UPGRADE_BENCHES.add(() -> bench);
		}
		TILE_UPGRADE_BENCH = registerMultiBlock(UpgradeBenchTileEntity::new, UPGRADE_BENCHES);
		RegistryHelper.registerTile(TILE_UPGRADE_BENCH, "upgrade_bench");
		
		REINFORCED_ANVIL = new ReinforcedAnvilBlock(Block.Properties.create(Material.ANVIL, MaterialColor.OBSIDIAN).hardnessAndResistance(50.0F, 1200.0F).sound(SoundType.ANVIL));	
		
		MOB_KILLER = new MobKillerBlock();
		TILE_MOB_KILLER = TileEntityType.Builder.create(MobKillerTileEntity::new, MOB_KILLER).build(null);
		RegistryHelper.registerTile(TILE_MOB_KILLER, "mob_killer");
	}

	public static <T extends TileEntity> TileEntityType<T> registerMultiBlock(Supplier<? extends T> factory, List<Supplier<Block>> list) {
		List<Block> blockTypes = list.stream().map(Supplier::get).collect(Collectors.toList());
		return TileEntityType.Builder.<T>create(factory, blockTypes.toArray(new Block[blockTypes.size()])).build(null);
	}
	
	@OnlyIn(Dist.CLIENT)
	public static void clientSetup() {
		ClientRegistry.bindTileEntityRenderer(TILE_ENCHANTMENT_EDITOR, (d) -> new EnchantmentEditorTileEntityRenderer(d));
		ClientRegistry.bindTileEntityRenderer(TILE_UPGRADE_BENCH, (d) -> new UpgradeBenchTileEntityRenderer(d));
	}

}
