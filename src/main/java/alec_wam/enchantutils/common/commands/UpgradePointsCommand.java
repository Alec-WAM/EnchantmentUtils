package alec_wam.enchantutils.common.commands;

public class UpgradePointsCommand {

	/*private static final DynamicCommandExceptionType NONLIVING_ENTITY_EXCEPTION = new DynamicCommandExceptionType((p_208839_0_) -> {
		return new TranslationTextComponent(EnchantmentUtils.resourceDot("commands.upgradepoints.failed.entity"), p_208839_0_);
	});
	private static final DynamicCommandExceptionType ITEMLESS_EXCEPTION = new DynamicCommandExceptionType((p_208835_0_) -> {
		return new TranslationTextComponent(EnchantmentUtils.resourceDot("commands.upgradepoints.failed.itemless"), p_208835_0_);
	});
	private static final DynamicCommandExceptionType INCOMPATIBLE_EXCEPTION = new DynamicCommandExceptionType((p_208837_0_) -> {
		return new TranslationTextComponent(EnchantmentUtils.resourceDot("commands.upgradepoints.failed.imcompatible"), p_208837_0_);
	});
	private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslationTextComponent(EnchantmentUtils.resourceDot("commands.upgradepoints.failed")));


	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(Commands.literal("enchant").requires((p_203630_0_) -> {
			return p_203630_0_.hasPermissionLevel(2);
		}).then(Commands.argument("targets", EntityArgument.entities()).then(Commands.argument("enchantment", EnchantmentArgument.enchantment()).executes((p_202648_0_) -> {
			return addXP(p_202648_0_.getSource(), EntityArgument.getEntities(p_202648_0_, "targets"), EnchantmentArgument.getEnchantment(p_202648_0_, "enchantment"), 1);
		})

		.then(Commands.argument("points", IntegerArgumentType.integer(0)).executes((p_202650_0_) -> {
			return addPoints(p_202650_0_.getSource(), EntityArgument.getEntities(p_202650_0_, "targets"), IntegerArgumentType.getInteger(p_202650_0_, "amount"));
		})))));
	}

	private static int addXP(CommandSource source, Collection<? extends Entity> targets, double amount) throws CommandSyntaxException {
		if(!UpgradePointManager.isFeatureEnabled()){
			throw FAILED_EXCEPTION.create();
		}
		int i = 0;

		for(Entity entity : targets) {
			if (entity instanceof LivingEntity) {
				LivingEntity livingentity = (LivingEntity)entity;
				ItemStack itemstack = livingentity.getHeldItemMainhand();
				if (!itemstack.isEmpty()) {
					if (UpgradePointManager.isUpgradeable(itemstack)) {
						UpgradePointManager.addToolPoints(itemstack, amount);
						++i;
					} else if (targets.size() == 1) {
						throw INCOMPATIBLE_EXCEPTION.create(itemstack.getItem().getDisplayName(itemstack).getString());
					}
				} else if (targets.size() == 1) {
					throw ITEMLESS_EXCEPTION.create(livingentity.getName().getString());
				}
			} else if (targets.size() == 1) {
				throw NONLIVING_ENTITY_EXCEPTION.create(entity.getName().getString());
			}
		}

		if (i == 0) {
			throw FAILED_EXCEPTION.create();
		} else {
			if (targets.size() == 1) {
				source.sendFeedback(new TranslationTextComponent(EnchantmentUtils.resourceDot("commands.upgradepoints.success.single"), targets.iterator().next().getDisplayName()), true);
			} else {
				source.sendFeedback(new TranslationTextComponent(EnchantmentUtils.resourceDot("commands.upgradepoints.success.multiple"), targets.size()), true);
			}

			return i;
		}
	}

	private static int addPoints(CommandSource source, Collection<? extends Entity> targets, int amount) throws CommandSyntaxException {
		if(!UpgradePointManager.isFeatureEnabled()){
			throw FAILED_EXCEPTION.create();
		}
		int i = 0;

		for(Entity entity : targets) {
			if (entity instanceof LivingEntity) {
				LivingEntity livingentity = (LivingEntity)entity;
				ItemStack itemstack = livingentity.getHeldItemMainhand();
				if (!itemstack.isEmpty()) {
					if (UpgradePointManager.isUpgradeable(itemstack)) {
						UpgradePointManager.addToolPoints(itemstack, amount);
						++i;
					} else if (targets.size() == 1) {
						throw INCOMPATIBLE_EXCEPTION.create(itemstack.getItem().getDisplayName(itemstack).getString());
					}
				} else if (targets.size() == 1) {
					throw ITEMLESS_EXCEPTION.create(livingentity.getName().getString());
				}
			} else if (targets.size() == 1) {
				throw NONLIVING_ENTITY_EXCEPTION.create(entity.getName().getString());
			}
		}

		if (i == 0) {
			throw FAILED_EXCEPTION.create();
		} else {
			if (targets.size() == 1) {
				source.sendFeedback(new TranslationTextComponent(EnchantmentUtils.resourceDot("commands.upgradepoints.success.single"), targets.iterator().next().getDisplayName()), true);
			} else {
				source.sendFeedback(new TranslationTextComponent(EnchantmentUtils.resourceDot("commands.upgradepoints.success.multiple"), targets.size()), true);
			}

			return i;
		}
	}*/

}
