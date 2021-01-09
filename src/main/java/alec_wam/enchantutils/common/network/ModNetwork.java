package alec_wam.enchantutils.common.network;

import alec_wam.enchantutils.EnchantmentUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class ModNetwork {
	public static final ResourceLocation CHANNEL_NAME = new ResourceLocation(EnchantmentUtils.resource("network"));
	public static final String NETWORK_VERSION = EnchantmentUtils.resource("1");
	private static int id = 0;
	
	public static int getID(){
		return id++;
	}
	
	private static SimpleChannel channel;
	
	public static void initChannel(){
		channel = NetworkRegistry.ChannelBuilder.named(CHANNEL_NAME)
		.clientAcceptedVersions(version -> true)
		.serverAcceptedVersions(version -> true)
		.networkProtocolVersion(() -> NETWORK_VERSION)
		.simpleChannel();
	}
	
	public static SimpleChannel getNetworkChannel() {
		channel.messageBuilder(PacketTileMessage.class, getID())
		.decoder(PacketTileMessage::decode)
		.encoder(PacketTileMessage::encode)
		.consumer(PacketTileMessage::handle)
		.add();
		
		channel.messageBuilder(PacketEntityMessage.class, getID())
		.decoder(PacketEntityMessage::decode)
		.encoder(PacketEntityMessage::encode)
		.consumer(PacketEntityMessage::handle)
		.add();
		
		channel.messageBuilder(PacketGuiMessage.class, getID())
		.decoder(PacketGuiMessage::decode)
		.encoder(PacketGuiMessage::encode)
		.consumer(PacketGuiMessage::handle)
		.add();
		
		return channel;
	}

	public static void sendToAll(AbstractPacket packet)
	{
		getNetworkChannel().send(PacketDistributor.ALL.noArg(), packet);
	}
	
	public static void sendTo(ServerPlayerEntity player, AbstractPacket packet)
	{
		getNetworkChannel().send(PacketDistributor.PLAYER.with(() -> player), packet);
	}

	public static void sendToServer(AbstractPacket packet)
	{
		getNetworkChannel().sendToServer(packet);
	}

	public static void sendToChunk(ServerWorld world, BlockPos pos, AbstractPacket packet) {
		IChunk chunk = world.getChunk(pos);
		if (chunk instanceof Chunk) {
			getNetworkChannel().send(PacketDistributor.TRACKING_CHUNK.with(() -> (Chunk)chunk), packet);
		}
	}
	
	public static void sendToAllAround(AbstractPacket packet, TileEntity tile)
	{
		sendToAllAround(packet, new PacketDistributor.TargetPoint(tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ(), 64, tile.getWorld().getDimensionKey()));
	}

	public static void sendToAllAround(AbstractPacket packet, Entity entity)
	{
		sendToAllAround(packet, new PacketDistributor.TargetPoint(entity.getPositionVec().x, entity.getPositionVec().y, entity.getPositionVec().z, 64, entity.getEntityWorld().getDimensionKey()));
	}

	public static void sendToAllAround(AbstractPacket packet, PacketDistributor.TargetPoint point)
	{
		getNetworkChannel().send(PacketDistributor.NEAR.with(() -> point), packet);
	}

	public static void sendToDimension(AbstractPacket packet, RegistryKey<World> type)
	{
		getNetworkChannel().send(PacketDistributor.DIMENSION.with(() -> type), packet);
	}

}
