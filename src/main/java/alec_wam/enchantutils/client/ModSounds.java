package alec_wam.enchantutils.client;

import alec_wam.enchantutils.EnchantmentUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

public class ModSounds {
    public static SoundEvent UPGRADEPOINT_UNLOCK;

    public static SoundEvent register(String name) {
        ResourceLocation loc = EnchantmentUtils.resourceL(name);
        SoundEvent event = new SoundEvent(loc);
        //event.setRegistryName(loc);
        return event;
    }

	public static void constructSounds() {
		UPGRADEPOINT_UNLOCK = register("ui.upgradepoint.unlock");// RegistryHelper.SOUNDS.register("ui.upgradepoint.unlock", () -> register("ui.upgradepoint.unlock")).get();
	}
}
