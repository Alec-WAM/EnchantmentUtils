package alec_wam.enchantutils.common.loot;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import alec_wam.enchantutils.common.feature.upgradepoints.UpgradePointManager;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ILootSerializer;
import net.minecraft.loot.LootConditionType;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameter;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.ResourceLocation;

public class UpgradeLootCondition implements ILootCondition {

	private final ResourceLocation id;
	private final int min;

    public UpgradeLootCondition(ResourceLocation id, int required) {
    	this.id = id;
        this.min = required;
    }

    @Override
    public LootConditionType func_230419_b_() {
        return ModLootModifiers.UPGRADE;
    }

    @Override
    public Set<LootParameter<?>> getRequiredParameters() {
        return ImmutableSet.of(LootParameters.TOOL);
    }

    @Override
    public boolean test(LootContext ctx) {
        int level = 0;
        ItemStack tool = ctx.get(LootParameters.TOOL);
        if (tool !=null && UpgradePointManager.isUpgradeable(tool)) {
            level = UpgradePointManager.getUpgradeLevel(tool, UpgradePointManager.UPGRADE_REGISTRY.get(id));
        }
        return level >= this.min;
    }

    public static ILootCondition.IBuilder get(ResourceLocation id, int val) {
        return () -> new UpgradeLootCondition(id, val);
    }

    public static class Serializer implements ILootSerializer<UpgradeLootCondition> {

        @Override
        public void serialize(JsonObject object, UpgradeLootCondition condition, JsonSerializationContext context) {
        	object.addProperty("upgrade", condition.id.toString());
            object.addProperty("min_level", condition.min);
        }

        @Override
        public UpgradeLootCondition deserialize(JsonObject obj, JsonDeserializationContext context) {
        	System.out.print(obj.toString());
        	String str = obj.get("upgrade").getAsString();
        	ResourceLocation id = new ResourceLocation(str);
        	int level = obj.get("min_level").getAsInt();
            return new UpgradeLootCondition(id, level);
        }
    }
}
