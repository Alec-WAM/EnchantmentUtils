package alec_wam.enchantutils.common.entities.fireproof;

import javax.annotation.Nonnull;

import alec_wam.enchantutils.common.entities.ModEntities;
import alec_wam.enchantutils.common.feature.upgradepoints.UpgradePointManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class UpgradedItemEntity extends ItemEntity {

	public UpgradedItemEntity(EntityType<? extends ItemEntity> p_i50217_1_, World p_i50217_2_) {
		super(p_i50217_1_, p_i50217_2_);
	}

	public UpgradedItemEntity(World worldIn, double x, double y, double z) {
		super(ModEntities.UPGRADED_ITEM, worldIn);
		this.setPosition(x, y, z);
		this.rotationYaw = this.rand.nextFloat() * 360.0F;
		this.setMotion(this.rand.nextDouble() * 0.2D - 0.1D, 0.2D, this.rand.nextDouble() * 0.2D - 0.1D);
	}

	public UpgradedItemEntity(World worldIn, double x, double y, double z, ItemStack stack) {
		this(worldIn, x, y, z);
		this.setItem(stack);
		this.lifespan = (stack.getItem() == null ? 6000 : stack.getEntityLifespan(worldIn));
	}

	public boolean hasFireproofUpgrade(){
		if(!getItem().isEmpty()){
			if(UpgradePointManager.isFeatureEnabled()){
				if(UpgradePointManager.hasUpgrades(getItem())){
					if(UpgradePointManager.getUpgradeLevel(getItem(), UpgradePointManager.UPGRADE_FIREPROOF) > 0){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public boolean hasExplosionProofUpgrade(){
		if(!getItem().isEmpty()){
			if(UpgradePointManager.isFeatureEnabled()){
				if(UpgradePointManager.hasUpgrades(getItem())){
					if(UpgradePointManager.getUpgradeLevel(getItem(), UpgradePointManager.UPGRADE_EXPLOSION_PROOF) > 0){
						return true;
					}
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean isInvulnerableTo(DamageSource source) {
		if(source.isFireDamage() && hasFireproofUpgrade()){
			return true;
		}
		if (source.isExplosion() && hasExplosionProofUpgrade()) {
			return true;
		}
		return super.isInvulnerableTo(source);
	}
	
	@Override
	public boolean isImmuneToFire() {
		return hasFireproofUpgrade();
	}
	
	@Nonnull
	@Override
	public IPacket<?> createSpawnPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

}
