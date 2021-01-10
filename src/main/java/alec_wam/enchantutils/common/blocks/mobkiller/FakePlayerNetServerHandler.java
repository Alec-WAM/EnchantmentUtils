package alec_wam.enchantutils.common.blocks.mobkiller;

import java.util.Set;

import javax.annotation.Nonnull;

import alec_wam.enchantutils.EnchantmentUtils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketDirection;
import net.minecraft.network.play.ServerPlayNetHandler;
import net.minecraft.network.play.client.CAnimateHandPacket;
import net.minecraft.network.play.client.CChatMessagePacket;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CClientSettingsPacket;
import net.minecraft.network.play.client.CClientStatusPacket;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.network.play.client.CConfirmTeleportPacket;
import net.minecraft.network.play.client.CConfirmTransactionPacket;
import net.minecraft.network.play.client.CCreativeInventoryActionPacket;
import net.minecraft.network.play.client.CCustomPayloadPacket;
import net.minecraft.network.play.client.CEnchantItemPacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.network.play.client.CInputPacket;
import net.minecraft.network.play.client.CKeepAlivePacket;
import net.minecraft.network.play.client.CMoveVehiclePacket;
import net.minecraft.network.play.client.CPlayerAbilitiesPacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.client.CResourcePackStatusPacket;
import net.minecraft.network.play.client.CSpectatePacket;
import net.minecraft.network.play.client.CSteerBoatPacket;
import net.minecraft.network.play.client.CTabCompletePacket;
import net.minecraft.network.play.client.CUpdateSignPacket;
import net.minecraft.network.play.client.CUseEntityPacket;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;

public class FakePlayerNetServerHandler extends ServerPlayNetHandler {

  public FakePlayerNetServerHandler(ServerPlayerEntity p_i1530_3_) {
    super(LogicalSidedProvider.INSTANCE.get(LogicalSide.SERVER), new NetworkManager(PacketDirection.CLIENTBOUND), p_i1530_3_);
  }

  private int warnCount = 0;

  @Override
  public @Nonnull NetworkManager getNetworkManager() {
    if (warnCount++ < 10) {
      EnchantmentUtils.LOGGER.warn("Someone is trying to send network packets to a fake player. This may crash and that is NOT Enchantment Utils fault.");
    }
    return super.netManager;
  }

  @Override
  public void processInput(@Nonnull CInputPacket p_147358_1_) {
  }

  @Override
  public void processPlayer(@Nonnull CPlayerPacket p_147347_1_) {
  }

  @Override
  public void setPlayerLocation(double p_147364_1_, double p_147364_3_, double p_147364_5_, float p_147364_7_, float p_147364_8_) {
  }

  @Override
  public void processPlayerDigging(@Nonnull CPlayerDiggingPacket p_147345_1_) {
  }

  @Override
  public void onDisconnect(@Nonnull ITextComponent p_147231_1_) {
  }

  @Override
  public void sendPacket(@Nonnull IPacket<?> p_147359_1_) {
  }

  @Override
  public void processHeldItemChange(@Nonnull CHeldItemChangePacket p_147355_1_) {
  }

  @Override
  public void processChatMessage(@Nonnull CChatMessagePacket p_147354_1_) {
  }

  @Override
  public void handleAnimation(@Nonnull CAnimateHandPacket packetIn) {
  }

  @Override
  public void processEntityAction(@Nonnull CEntityActionPacket p_147357_1_) {
  }

  @Override
  public void processUseEntity(@Nonnull CUseEntityPacket p_147340_1_) {
  }

  @Override
  public void processClientStatus(@Nonnull CClientStatusPacket p_147342_1_) {
  }

  @Override
  public void processCloseWindow(@Nonnull CCloseWindowPacket p_147356_1_) {
  }

  @Override
  public void processClickWindow(@Nonnull CClickWindowPacket p_147351_1_) {
  }

  @Override
  public void processEnchantItem(@Nonnull CEnchantItemPacket p_147338_1_) {
  }

  @Override
  public void processCreativeInventoryAction(@Nonnull CCreativeInventoryActionPacket p_147344_1_) {
  }

  @Override
  public void processConfirmTransaction(@Nonnull CConfirmTransactionPacket p_147339_1_) {
  }

  @Override
  public void processUpdateSign(@Nonnull CUpdateSignPacket p_147343_1_) {
  }

  @Override
  public void processKeepAlive(@Nonnull CKeepAlivePacket p_147353_1_) {
  }

  @Override
  public void processPlayerAbilities(@Nonnull CPlayerAbilitiesPacket p_147348_1_) {
  }

  @Override
  public void processTabComplete(@Nonnull CTabCompletePacket p_147341_1_) {
  }

  @Override
  public void processClientSettings(@Nonnull CClientSettingsPacket p_147352_1_) {
  }

  @Override
  public void handleSpectate(@Nonnull CSpectatePacket packetIn) {
  }

  @Override
  public void handleResourcePackStatus(@Nonnull CResourcePackStatusPacket packetIn) {
  }

  @Override
  public void tick() {
  }

  @Override
  public void disconnect(@Nonnull ITextComponent textComponent) {
  }

  @Override
  public void processVehicleMove(@Nonnull CMoveVehiclePacket packetIn) {
  }

  @Override
  public void processConfirmTeleport(@Nonnull CConfirmTeleportPacket packetIn) {
  }

  @Override
  public void setPlayerLocation(double x, double y, double z, float yaw, float pitch, Set<SPlayerPositionLookPacket.Flags> relativeSet) {
  }

  @Override
  public void processTryUseItemOnBlock(@Nonnull CPlayerTryUseItemOnBlockPacket packetIn) {
  }

  @Override
  public void processTryUseItem(@Nonnull CPlayerTryUseItemPacket packetIn) {
  }

  @Override
  public void processSteerBoat(@Nonnull CSteerBoatPacket packetIn) {
  }

  @Override
  public void processCustomPayload(@Nonnull CCustomPayloadPacket packetIn) {
  }

}