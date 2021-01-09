package alec_wam.enchantutils;

import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import alec_wam.enchantutils.client.ClientProxy;
import alec_wam.enchantutils.common.CommonProxy;
import alec_wam.enchantutils.common.util.RegistryHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(EnchantmentUtils.MOD_ID)
public class EnchantmentUtils {
	public static final String MOD_ID = "enchantutils";
	
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
	
	public static EnchantmentUtils INSTANCE;
	public static CommonProxy proxy;
	
    public EnchantmentUtils() {
    	INSTANCE = this;
    	
    	ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC, "enchantutils-common.toml");
    	
    	RegistryHelper.setupRegistries();
    	
    	proxy = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
		proxy.start();
    }
    
    public static String resource(String res) {
		return String.format("%s:%s", MOD_ID.toLowerCase(Locale.US), res);
	}
	
	public static ResourceLocation resourceL(String res){
		return new ResourceLocation(resource(res));
	}
	
	public static String resourceDot(String res) {
		return String.format("%s.%s", MOD_ID.toLowerCase(Locale.US), res);
	}
}
