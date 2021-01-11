package alec_wam.enchantutils.common.feature.upgradepoints;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nonnull;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;

import alec_wam.enchantutils.Config;
import alec_wam.enchantutils.EnchantmentUtils;
import alec_wam.enchantutils.client.ModParticles;
import alec_wam.enchantutils.common.entities.fireproof.UpgradedItemEntity;
import alec_wam.enchantutils.common.feature.upgradepoints.upgrade.ElytraTrailUpgrade;
import alec_wam.enchantutils.common.feature.upgradepoints.upgrade.SoulBoundUpgrade;
import alec_wam.enchantutils.common.loot.UpgradeLootCondition;
import alec_wam.enchantutils.common.util.ItemNBTHelper;
import alec_wam.enchantutils.common.util.ItemUtil;
import alec_wam.enchantutils.common.util.ReflectionHelper;
import alec_wam.enchantutils.common.util.RegistryHelper;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.data.DataGenerator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.item.crafting.FurnaceRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.loot.conditions.BlockStateProperty;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleType;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.items.ItemHandlerHelper;

public class UpgradePointEventHandler {
	
	private static final ResourceLocation CAPABILITY_KEY = new ResourceLocation(EnchantmentUtils.MOD_ID, "entitydamagexp");
	
	@SubscribeEvent
	public void onCapabilityAttach(AttachCapabilitiesEvent<Entity> event) {
		if(event.getObject() instanceof LivingEntity && event.getObject().isAlive()) {
			event.addCapability(CAPABILITY_KEY, new EntityDamageXPHandler());
    	}
	}

	@SubscribeEvent
	public void onDeath(LivingDeathEvent event) {
	    if(!event.getEntity().getEntityWorld().isRemote && event.getEntity().getCapability(CapabilityEntityDamageXP.CAPABILITY, null).isPresent()) {
	      event.getEntity().getCapability(CapabilityEntityDamageXP.CAPABILITY, null).ifPresent(cap -> cap.distributeXpToTools(event.getEntityLiving()));
	    }
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onWeaponAttack(LivingDamageEvent event) {
		if(event.isCanceled()){
	    	return;
	    }
		
		DamageSource source = event.getSource();
		Entity attacker = source.getTrueSource();
		if(attacker == null || !(attacker instanceof PlayerEntity)){
			return;
		}
		
		PlayerEntity player = (PlayerEntity)attacker;
		
		if(player.abilities.isCreativeMode)return;
		
		if(player instanceof FakePlayer){
			if(!Config.COMMON.upgrades_fakeplayer_weapon.get()){
				return;
			}
		}
		
		ItemStack tool = player.getHeldItemMainhand();
		
		if(tool.isEmpty() || !UpgradePointManager.isFeatureEnabled() || !UpgradePointManager.hasUpgrades(tool) || !ItemUtil.isSword(tool)){
			return;
		}		
		
		LivingEntity target = event.getEntityLiving();
		
		if(!target.getEntityWorld().isRemote) {
			double newHealth = Math.max(0, target.getHealth() - event.getAmount());
			double damageDealt = target.getHealth() - newHealth;
			// if we killed it the event for distributing xp was already fired and we just do it manually here
			if(!target.isAlive()) {
				UpgradePointManager.earnToolXP(player, tool, Math.round(damageDealt), UpgradePointManager.getMaxXP(tool));
			}
			else if(target.getCapability(CapabilityEntityDamageXP.CAPABILITY, null).isPresent()) {
				target.getCapability(CapabilityEntityDamageXP.CAPABILITY, null).ifPresent(cap -> cap.addDamageFromTool((float)damageDealt, tool, player));
			}
		}
	}
	
	//TODO Add event handling for bow shoot
	
	@SubscribeEvent
	public void onBlockDestroyed(BlockEvent.BreakEvent event){
		PlayerEntity player = event.getPlayer();
		ItemStack stack = player.getHeldItemMainhand();
		if(!event.isCanceled()){
			if(UpgradePointManager.isFeatureEnabled()){
				if(UpgradePointManager.hasUpgrades(stack)){
					if(ItemUtil.isDiggingTool(stack)){
						if(player.abilities.isCreativeMode)return;

						if(player instanceof FakePlayer){
							if(!Config.COMMON.upgrades_fakeplayer_digging.get()){
								return;
							}
						}

						double xpEarned = 0.0D;

						BlockState state = event.getState();

						if(stack.getItem() instanceof ToolItem){
							ToolItem tool = (ToolItem)stack.getItem();
							if (tool.getToolTypes(stack).stream().anyMatch(e -> state.isToolEffective(e))){
								xpEarned = 1.0D;
							}
						} else {
							xpEarned = 1.0D;
						}
						
						//TODO REMOVE DEBUG
						//if(player.isCrouching())UpgradePointManager.setToolXP(stack, 498);						

						if(stack.getItem() instanceof AxeItem){
							xpEarned *= Config.COMMON.upgrades_xp_mulit_dig_axe.get();
						}
						UpgradePointManager.earnToolXP(player, stack, xpEarned, UpgradePointManager.getMaxXP(stack));
					}
				}
			}
		}
	}
	
	private static Map<PlayerEntity, Integer> delayedPoints = Maps.newHashMap();
	
	@SubscribeEvent
	public void onBlockChanged(EntityPlaceEvent event){
		if(event.isCanceled()){
			return;
		}
		
		if(!(event.getEntity() instanceof PlayerEntity)){
			return;
		}
		
		
		PlayerEntity player = (PlayerEntity)event.getEntity();
		
		if(player.abilities.isCreativeMode)return;
		
		if(player instanceof FakePlayer){
			if(!Config.COMMON.upgrades_fakeplayer_digging.get()){
				return;
			}
		}
		
		ItemStack stack = player.getHeldItemMainhand();
		if(UpgradePointManager.isFeatureEnabled()){
			if(UpgradePointManager.hasUpgrades(stack)){
				if(ItemUtil.isDiggingTool(stack)){
					BlockState oldState = event.getBlockSnapshot().getReplacedBlock();
					BlockState newState = event.getPlacedBlock();

					boolean worked = false;
					World world = (World)event.getWorld();
					BlockPos pos = event.getPos();
					
					for(ToolType type : stack.getToolTypes()){
						BlockState transformState = oldState.getBlock().getToolModifiedState(oldState, world, pos, player, stack, type);


						if(transformState == newState){
							worked = true;
							break;
						}
					}
					
					if(worked && !world.isRemote){
						int currentPoints = 0;
						delayedPoints.put(player, currentPoints + 1);
					}
				}
			}
		}		
	}
	
	@SubscribeEvent
	public void getDigSpeed(PlayerEvent.BreakSpeed event){
		final float oldSpeed = event.getOriginalSpeed();
		float newSpeed = oldSpeed;
		ItemStack stack = event.getEntityLiving().getHeldItemMainhand();
		if(UpgradePointManager.isFeatureEnabled()){
			if(UpgradePointManager.hasUpgrades(stack)){
				if (oldSpeed > 1.0F) {
					int i = UpgradePointManager.getUpgradeLevel(stack, UpgradePointManager.UPGRADE_DIGSPEED);
					if (i > 0) {
						//Efficency
						//newSpeed += (float)(i * i + 1);
						//Haste
						newSpeed *= 1.0F + (float)(i + 1) * 0.2F;
					}
				}
			}
		}
		if(newSpeed !=oldSpeed){
			event.setNewSpeed(newSpeed);
		}
	}
	
	//Shield
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onLivingHurt(LivingAttackEvent event) {
	    if(event.isCanceled()){
	    	return;
	    }
		
		DamageSource source = event.getSource();
	    
	    if(event.getSource().isUnblockable() || event.getSource().getTrueSource() == null) {
	      return;
	    }
	    
	    if(Config.COMMON.upgrades_shield_xp_proj.get() && !event.getSource().isProjectile()){
	    	return;
	    }
	    
	    if(!(event.getEntity() instanceof PlayerEntity)) {
	      return;
	    }
	    
	    PlayerEntity player = (PlayerEntity) event.getEntity();
	    
	    if(!player.isActiveItemStackBlocking() || !canBlockDamageSource(player, source)) {
	      return;
	    }
	    
	    ItemStack stack = player.getActiveItemStack();
	    
	    if(!stack.getItem().isShield(stack, player)){
	    	return;
	    }
	    
	    if(!UpgradePointManager.isFeatureEnabled() || !UpgradePointManager.hasUpgrades(stack)) {
	      return;
	    }
	    
	    double damage = event.getAmount();
	    if (damage >= 3.0F) {
		    double multi = event.getSource().isProjectile() ? 2 : 1;
		    int xpEarned = (int)(Math.max(1, Math.round(damage)) * multi);
			UpgradePointManager.earnToolXP(player, stack, xpEarned, UpgradePointManager.getMaxXP(stack));
	    }
	}
	
	private boolean canBlockDamageSource(PlayerEntity player, DamageSource damageSourceIn) {
      Entity entity = damageSourceIn.getImmediateSource();
      boolean flag = false;
      if (entity instanceof AbstractArrowEntity) {
         AbstractArrowEntity abstractarrowentity = (AbstractArrowEntity)entity;
         if (abstractarrowentity.getPierceLevel() > 0) {
            flag = true;
         }
      }

      if (!damageSourceIn.isUnblockable() && player.isActiveItemStackBlocking() && !flag) {
         Vector3d vector3d2 = damageSourceIn.getDamageLocation();
         if (vector3d2 != null) {
            Vector3d vector3d = player.getLook(1.0F);
            Vector3d vector3d1 = vector3d2.subtractReverse(player.getPositionVec()).normalize();
            vector3d1 = new Vector3d(vector3d1.x, 0.0D, vector3d1.z);
            if (vector3d1.dotProduct(vector3d) < 0.0D) {
               return true;
            }
         }
      }

      return false;
	}
	
	
	//Elytra
	private final Map<PlayerEntity, Integer> elytraTimer = Maps.newHashMap();
	@SubscribeEvent
	public void onLivingUpdate(LivingUpdateEvent event){
		if(event.isCanceled())return;
		if(event.getEntityLiving() instanceof PlayerEntity){
			PlayerEntity player = (PlayerEntity)event.getEntityLiving();
			
			if(!player.getEntityWorld().isRemote){
				if(delayedPoints.containsKey(player)){
					if(player.getEntityWorld().getGameTime() % 10 == 0){
						ItemStack held = player.getHeldItemMainhand();
						if(UpgradePointManager.isFeatureEnabled()){
							if(UpgradePointManager.hasUpgrades(held)){
								double points = delayedPoints.get(player);
								if(held.getItem() instanceof AxeItem){
									points *= Config.COMMON.upgrades_xp_mulit_dig_axe.get();
								}
								UpgradePointManager.earnToolXP(player, held, points, UpgradePointManager.getMaxXP(held));
								delayedPoints.remove(player);
							}
						}
					}
				}
			}
			
			if(player.isElytraFlying()){
				ItemStack chestStack = player.getItemStackFromSlot(EquipmentSlotType.CHEST);
				if(chestStack !=null && !chestStack.isEmpty()){
					if(UpgradePointManager.isFeatureEnabled()){
						if(UpgradePointManager.hasUpgrades(chestStack)){
							if(UpgradePointManager.getUpgradeLevel(chestStack, UpgradePointManager.UPGRADE_ELYTRA_TRAIL) > 0){
								ElytraTrailUpgrade.ElytraTrail trail = ElytraTrailUpgrade.getTrailType(chestStack);
								if(trail !=null){
									IParticleData type = null;
									int delayTime = 0;
									if(trail == ElytraTrailUpgrade.ElytraTrail.BUBBLE){
										type = ModParticles.BUBBLE_TRAIL.get();
									}
									if(trail == ElytraTrailUpgrade.ElytraTrail.FLAME){
										type = ModParticles.FLAME_TRAIL.get();
										delayTime = 2;
									}
									if(trail == ElytraTrailUpgrade.ElytraTrail.GLINT){
										type = ModParticles.GLINT_TRAIL.get();
									}
									if(trail == ElytraTrailUpgrade.ElytraTrail.SPARKLE){
										type = ModParticles.SPARKLE_TRAIL.get();
									}
									if(trail == ElytraTrailUpgrade.ElytraTrail.HEART){
										type = ModParticles.HEART_TRAIL.get();
										delayTime = 2;
									}
									if(trail == ElytraTrailUpgrade.ElytraTrail.COLOR){
										int color = ElytraTrailUpgrade.getTrailColor(chestStack);
										float r = ((color >> 16) & 0xff) / 255.0f;
									    float g = ((color >>  8) & 0xff) / 255.0f;
									    float b = ((color      ) & 0xff) / 255.0f;
									    RedstoneParticleData colorPart = new RedstoneParticleData(r, g, b, 1.0F){
											
											@Override
											public ParticleType<RedstoneParticleData> getType() {
												return ModParticles.COLOR_TRAIL.get();
											}
										};
										type = colorPart;
									}
									
									if (player.getEntityWorld().isRemote && type !=null) {
										Random rand = player.getEntityWorld().rand;
										if(delayTime == 0 || player.getEntityWorld().getGameTime() % delayTime == 0){
											player.getEntityWorld().addParticle(type, player.getPosX(), player.getPosY(), player.getPosZ(), rand.nextGaussian() * 0.05D, -player.getMotion().y * 0.5D, rand.nextGaussian() * 0.05D);
										}
									}
								}
							}
							
							
							int secondsNeeded = 3;
							
							double speedNeeded = 0.0d;//0.5D;
							double speed = Math.sqrt(LivingEntity.horizontalMag(player.getMotion()));
							
							if(speed >= speedNeeded){
								int flyTime = elytraTimer.getOrDefault(player, 0);							
								
								elytraTimer.put(player, flyTime + 1);
								
								
								if(!player.getEntityWorld().isRemote && flyTime % (20 * secondsNeeded) == 0){
									//TODO Create XP For Basic tools
									UpgradePointManager.earnToolXP(player, chestStack, 1, UpgradePointManager.getMaxXP(chestStack));
								}			
							}				
						}
					}
				}
			} else {
				if(elytraTimer.containsKey(player)){
					elytraTimer.remove(player);
				}
			}
		}
	}

	@SubscribeEvent
    public static void runData(GatherDataEvent event)
    {
        event.getGenerator().addProvider(new DataProvider(event.getGenerator(), EnchantmentUtils.MOD_ID));
    }

	private static final RegistryObject<SmeltingUpgradeModifier.Serializer> SMELTING = RegistryHelper.GLM.register("smelting_upgrade", SmeltingUpgradeModifier.Serializer::new);
	private static final RegistryObject<BlockUpgradeModifier.Serializer> UPGRADEBLOCK_ENDERCHEST = RegistryHelper.GLM.register("upgrade_block_enderchest", BlockUpgradeModifier.Serializer::new);

	
	private static class DataProvider extends GlobalLootModifierProvider
    {
        public DataProvider(DataGenerator gen, String modid)
        {
            super(gen, modid);
        }

        @Override
        protected void start()
        {
        	add("smelting_upgrade", SMELTING.get(), new SmeltingUpgradeModifier(
                    new ILootCondition[]{
                            new UpgradeLootCondition(UpgradePointManager.UPGRADE_AUTOSMELT.getID(), 1)
                    })
            );
        	add("upgrade_block_enderchest", UPGRADEBLOCK_ENDERCHEST.get(), new BlockUpgradeModifier(
                    new ILootCondition[]{
                            new BlockStateProperty.Builder(Blocks.ENDER_CHEST).build()
                    })
            );
        }
    }
	private static class SmeltingUpgradeModifier extends LootModifier {
        public SmeltingUpgradeModifier(ILootCondition[] conditionsIn) {
            super(conditionsIn);
        }

        @Nonnull
        @Override
        public List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
            ArrayList<ItemStack> ret = new ArrayList<ItemStack>();     
            
            boolean success = false;
            for(ItemStack stack : generatedLoot){
    			ItemStack smelted = smelt(stack, context);
    			if(!stack.equals(smelted, false)){
    				success = true;
    			}
    			ret.add(smelted);
    		}
            
            if(success){
            	ServerWorld world = context.getWorld();
            	Vector3d pos = context.get(LootParameters.field_237457_g_);
            	for(int i = 0; i < 6; i++){
    				double d3 = (double)pos.getX() + (double)world.rand.nextFloat();
    	            double d4 = (double)pos.getY() + (double)world.rand.nextFloat();
    	            double d5 = (double)pos.getZ() + (double)world.rand.nextFloat();
    	            world.spawnParticle(ParticleTypes.FLAME, d3, d4, d5, 1, 0.0D, 0.00D, 0.0D, 0.0D);
    			}
            }
            
            return ret;
        }

        private static ItemStack smelt(ItemStack stack, LootContext context) {
            return context.getWorld().getRecipeManager().getRecipe(IRecipeType.SMELTING, new Inventory(stack), context.getWorld())
                    .map(FurnaceRecipe::getRecipeOutput)
                    .filter(itemStack -> !itemStack.isEmpty())
                    .map(itemStack -> ItemHandlerHelper.copyStackWithSize(itemStack, stack.getCount() * itemStack.getCount()))
                    .orElse(stack);
        }

        private static class Serializer extends GlobalLootModifierSerializer<SmeltingUpgradeModifier> {
            @Override
            public SmeltingUpgradeModifier read(ResourceLocation name, JsonObject json, ILootCondition[] conditionsIn) {
                return new SmeltingUpgradeModifier(conditionsIn);
            }

            @Override
            public JsonObject write(SmeltingUpgradeModifier instance) {
                return makeConditions(instance.conditions);
            }
        }
    }
	
	private static class BlockUpgradeModifier extends LootModifier {
        public BlockUpgradeModifier(ILootCondition[] conditionsIn) {
            super(conditionsIn);
        }

        @Nonnull
        @Override
        public List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
            ArrayList<ItemStack> ret = new ArrayList<ItemStack>();     
            
            for(ItemStack stack : generatedLoot){
            	if(UpgradePointManager.isBasicUpgradeItem(stack)){
            		ItemStack upgrade = handleUpgrades(stack, context);
            		ret.add(upgrade);
            	} else {
            		ret.add(stack);
            	}
    		}
            
            return ret;
        }

        private ItemStack handleUpgrades(ItemStack stack, LootContext context) {
        	TileEntity tile = context.get(LootParameters.BLOCK_ENTITY);
        	if(tile !=null && tile.getTileData() !=null){
        		if(tile.getTileData().contains(UpgradePointManager.NBT_TAG)){
        			UpgradePointManager.setupUpgrades(stack);
        			CompoundNBT nbt = ItemNBTHelper.getCompound(stack);
        			CompoundNBT upgradeNBT = nbt.getCompound(UpgradePointManager.NBT_TAG);
        			upgradeNBT.put(UpgradePointManager.TAG_UPGRADES, tile.getTileData().get(UpgradePointManager.NBT_TAG));
        			stack.setTag(nbt);
        		}
        	}
            return stack;
        }

        private static class Serializer extends GlobalLootModifierSerializer<BlockUpgradeModifier> {
            @Override
            public BlockUpgradeModifier read(ResourceLocation name, JsonObject json, ILootCondition[] conditionsIn) {
                return new BlockUpgradeModifier(conditionsIn);
            }

            @Override
            public JsonObject write(BlockUpgradeModifier instance) {
                return makeConditions(instance.conditions);
            }
        }
    }
	
	public boolean createSpecialItem(ItemStack stack){
		if(UpgradePointManager.hasUpgrades(stack)){
			if(UpgradePointManager.getUpgradeLevel(stack, UpgradePointManager.UPGRADE_FIREPROOF) > 0){
				return true;
			}
			if(UpgradePointManager.getUpgradeLevel(stack, UpgradePointManager.UPGRADE_EXPLOSION_PROOF) > 0){
				return true;
			}
		}
		return false;
	}
	
	@SubscribeEvent
	public void swapSpecialItem(EntityJoinWorldEvent event){
		if(!UpgradePointManager.isFeatureEnabled())return;
		Entity entity = event.getEntity();
		if(entity !=null && entity instanceof ItemEntity){
			if(!(entity instanceof UpgradedItemEntity)){
				ItemStack stack = ((ItemEntity)entity).getItem();
				if(createSpecialItem(stack)){
					event.setCanceled(true);
					CompoundNBT oldNBT = entity.writeWithoutTypeId(new CompoundNBT());
					oldNBT.remove("UUID");
					Vector3d pos = entity.getPositionVec();
					UpgradedItemEntity newEntity = new UpgradedItemEntity(event.getWorld(), pos.x, pos.y, pos.z);
					newEntity.read(oldNBT);
					event.getWorld().addEntity(newEntity);
				}
			}
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	private void addEntityImpl(ClientWorld world, int p_217424_1_, Entity p_217424_2_) {
		world.removeEntityFromWorld(p_217424_1_);
		
		Int2ObjectMap<Entity> entitiesById = ReflectionHelper.getPrivateValue(ClientWorld.class, world, 1);
		entitiesById.put(p_217424_1_, p_217424_2_);
		world.getChunkProvider().getChunk(MathHelper.floor(p_217424_2_.getPosX() / 16.0D), MathHelper.floor(p_217424_2_.getPosZ() / 16.0D), ChunkStatus.FULL, true).addEntity(p_217424_2_);
		p_217424_2_.onAddedToWorld();
	}
	
	//SOUL BOUND
	@SubscribeEvent(priority = EventPriority.HIGH)
	public void onPlayerDeath(LivingDropsEvent event)
	{
		if (event.getEntity() instanceof PlayerEntity) {
			SoulBoundUpgrade.saveDrops((PlayerEntity)event.getEntityLiving(), event.getDrops());
		}
	}
	
	@SubscribeEvent
	public void itemTransferEvent(PlayerEvent.Clone event)
	{
		if (event.isWasDeath()) {
			PlayerEntity oldPlayer = event.getOriginal();
			if (SoulBoundUpgrade.hasSavedDrops(oldPlayer)) {
				SoulBoundUpgrade.loadDrops(oldPlayer, event.getPlayer());
			} else if (SoulBoundUpgrade.hasSavedDrops(event.getPlayer())) {
				SoulBoundUpgrade.loadDrops(event.getPlayer(), event.getPlayer());
			}
		}
	}
	
	//Placed Upgrade Items
	
	private Map<PlayerEntity, ItemStack> lastUsedItem = Maps.newHashMap();
	
	@SubscribeEvent
	public void onRightClick(PlayerInteractEvent.RightClickBlock event){
		if(event.getItemStack() !=null){
			ItemStack stack = event.getItemStack();
			if(UpgradePointManager.isBasicUpgradeItem(stack)){
				lastUsedItem.put(event.getPlayer(), stack);
			}
		}
	}
	
	@SubscribeEvent
	public void onItemUse(PlayerInteractEvent.RightClickItem event){
		if(event.getItemStack() !=null){
			ItemStack stack = event.getItemStack();
			if(UpgradePointManager.isBasicUpgradeItem(stack)){
				lastUsedItem.put(event.getPlayer(), stack);
			}
		}
	}
	
	@SubscribeEvent
	public void onBlockPlaced(BlockEvent.EntityPlaceEvent event){
		if(event.getPlacedBlock().getBlock() == Blocks.ENDER_CHEST || event.getPlacedBlock().getBlock() instanceof ShulkerBoxBlock){
			BlockPos pos = event.getPos();
			TileEntity tile = event.getWorld().getTileEntity(pos);
			if(event.getEntity() instanceof PlayerEntity){
				PlayerEntity player = (PlayerEntity)event.getEntity();
				ItemStack stack = lastUsedItem.get(player);
				if(stack !=null){
					if(UpgradePointManager.isBasicUpgradeItem(stack)){
						CompoundNBT nbt = tile.getTileData();
						if(stack.getTag().contains(UpgradePointManager.NBT_TAG)){
							CompoundNBT upgradeNBT = ItemNBTHelper.getCompound(stack).getCompound(UpgradePointManager.NBT_TAG);
							ListNBT list = upgradeNBT.getList(UpgradePointManager.TAG_UPGRADES, Constants.NBT.TAG_COMPOUND);
							nbt.put(UpgradePointManager.NBT_TAG, list);
						}
					}
				}
			}
		}
	}
	
}
