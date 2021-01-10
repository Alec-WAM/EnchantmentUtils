package alec_wam.enchantutils.common.network;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent.Context;

public class PacketEntityMessage extends AbstractPacket {

	public int id;
	private String type;
	private CompoundNBT data;
	
	public PacketEntityMessage(){}
	
	public PacketEntityMessage(Entity entity, String type){
		this(entity.getEntityId(), type, null);
	}
	
	public PacketEntityMessage(Entity entity, String type, @Nullable CompoundNBT data){
		this(entity.getEntityId(), type, data);
	}
	
    public PacketEntityMessage(int id, String type, @Nullable CompoundNBT data){
    	this.id = id;
    	this.type = type;
    	this.data = data;
    }
	
	public static PacketEntityMessage decode(PacketBuffer buffer) {
		int id = buffer.readInt();
		String type = buffer.readString(100);
		CompoundNBT data = null;
		if(buffer.readBoolean()){
			data = buffer.readCompoundTag();
		}
		return new PacketEntityMessage(id, type, data);
	}

	@Override
	public void writeToBuffer(PacketBuffer buffer) {
		buffer.writeInt(id);
		buffer.writeString(type);
		buffer.writeBoolean(data !=null);		
		if(data !=null)buffer.writeCompoundTag(data);
	}
	
	public static final String DEFAULT_ADD_XP = "#AddXP#";
	public static final String DEFAULT_REMOVE_XP_LEVEL = "#RemoveXP#";
	
	@Override
	public void handleClient(PlayerEntity player, Context ctx) {
		Entity entity = player.getEntityWorld().getEntityByID(id);
		handle(entity, ctx, true);
	}

	@Override
	public void handleServer(ServerPlayerEntity player, Context ctx) {
		Entity entity = player.getEntityWorld().getEntityByID(id);
		handle(entity, ctx, false);
	}
	
	public void handle(Entity entity, Context ctx, boolean client){		
		if(entity !=null){
			if(type.equalsIgnoreCase(DEFAULT_ADD_XP)){
				if(entity instanceof PlayerEntity){
					PlayerEntity player = (PlayerEntity)entity;
					ctx.enqueueWork(() -> 
						player.giveExperiencePoints(data.getInt("Amount"))
					);
				}
			}
			else if(type.equalsIgnoreCase(DEFAULT_REMOVE_XP_LEVEL)){
				if(entity instanceof PlayerEntity){
					PlayerEntity player = (PlayerEntity)entity;
					ctx.enqueueWork(() -> 
						player.addExperienceLevel(-data.getInt("Amount"))
					);
				}
			}
			else if(entity instanceof IMessageHandler){
				ctx.enqueueWork(() -> 
					((IMessageHandler)entity).handleMessage(type, data, client)
				);
			}
		}
	}

}
