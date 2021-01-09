package alec_wam.enchantutils.common.network;

import net.minecraft.nbt.CompoundNBT;

public interface IMessageHandler {

	public void handleMessage(String messageId, CompoundNBT messageData, boolean client);
	
}
