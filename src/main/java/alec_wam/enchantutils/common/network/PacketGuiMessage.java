package alec_wam.enchantutils.common.network;

import alec_wam.enchantutils.client.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketGuiMessage extends AbstractPacket {

	private String type;
	private CompoundNBT data;
	
	public PacketGuiMessage(){}
	
	public PacketGuiMessage(String type){
		this(type, new CompoundNBT());
	}
	
    public PacketGuiMessage(String type, CompoundNBT data){
    	this.type = type;
    	this.data = data;
    }
	
	public static PacketGuiMessage decode(PacketBuffer buffer) {
		String type = buffer.readString(100);
		CompoundNBT data = buffer.readCompoundTag();
		return new PacketGuiMessage(type, data);
	}

	@Override
	public void writeToBuffer(PacketBuffer buffer) {
		buffer.writeString(type);
		buffer.writeCompoundTag(data);
	}
	
	public static final String CUSTOM_PACKET_UPGRADEPOINT = "#UpgradePoint#";
	
	@Override
	public void handleClient(PlayerEntity player, Context ctx) {
		Screen currentScreen = Minecraft.getInstance().currentScreen;
		if(type.equalsIgnoreCase(CUSTOM_PACKET_UPGRADEPOINT)){
			ItemStack stack = ItemStack.read(data.getCompound("Item"));
			int time = data.getInt("Time");
			ClientProxy.startUpgradePointAnimation(stack, time);
			return;
		}
		
		if(currentScreen !=null){			
			if(currentScreen instanceof IMessageHandler){
				ctx.enqueueWork(() -> 
					((IMessageHandler)currentScreen).handleMessage(type, data, true)
				);
			}
		}
	}

	@Override
	public void handleServer(ServerPlayerEntity player, Context ctx) {
		if(player.openContainer !=null){
			if(player.openContainer instanceof IMessageHandler){
				ctx.enqueueWork(() -> 
					((IMessageHandler)player.openContainer).handleMessage(type, data, false)
				);
			}
		}
	}

}

