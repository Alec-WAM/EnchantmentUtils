package alec_wam.enchantutils.common.feature.upgradepoints;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

//https://github.com/SlimeKnights/TinkersToolLeveling/blob/master/src/main/java/slimeknights/toolleveling/capability/CapabilityDamageXp.java
public final class CapabilityEntityDamageXP {

	@CapabilityInject(IDamageEntityXP.class)
	public static Capability<IDamageEntityXP> CAPABILITY = null;

	public static void register() {
		CapabilityManager.INSTANCE.register(IDamageEntityXP.class, new IStorage<IDamageEntityXP>() {

			@Override
			public INBT writeNBT(Capability<IDamageEntityXP> capability, IDamageEntityXP instance, Direction side) {
				return instance.serializeNBT();
			}

			@Override
			public void readNBT(Capability<IDamageEntityXP> capability, IDamageEntityXP instance, Direction side, INBT nbt) {
				instance.deserializeNBT((ListNBT) nbt);
			}
		}, EntityDamageXPHandler::new);
	}
}
