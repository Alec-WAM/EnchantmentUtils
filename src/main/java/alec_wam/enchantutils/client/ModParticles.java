package alec_wam.enchantutils.client;

import com.mojang.serialization.Codec;

import alec_wam.enchantutils.common.util.RegistryHelper;
import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.RegistryObject;

public class ModParticles {
	public static final RegistryObject<BasicParticleType> BUBBLE_TRAIL = RegistryHelper.PARTICLES.register("bubble_trail", () -> new BasicParticleType(false));
	public static final RegistryObject<BasicParticleType> FLAME_TRAIL = RegistryHelper.PARTICLES.register("flame_trail", () -> new BasicParticleType(false));
	public static final RegistryObject<BasicParticleType> GLINT_TRAIL = RegistryHelper.PARTICLES.register("glint_trail", () -> new BasicParticleType(false));
	public static final RegistryObject<BasicParticleType> SPARKLE_TRAIL = RegistryHelper.PARTICLES.register("sparkle_trail", () -> new BasicParticleType(false));
	public static final RegistryObject<BasicParticleType> HEART_TRAIL = RegistryHelper.PARTICLES.register("heart_trail", () -> new BasicParticleType(false));
	public static final RegistryObject<ParticleType<RedstoneParticleData>> COLOR_TRAIL = RegistryHelper.PARTICLES.register("color_trail", () -> new ParticleType<RedstoneParticleData>(false, RedstoneParticleData.DESERIALIZER) {
         public Codec<RedstoneParticleData> func_230522_e_() {
            return RedstoneParticleData.field_239802_b_;
         }
      });

	
	public static void constructParticles(){
		
	}
	
	public static void registerFactories(){
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class TrailParticle extends SpriteTexturedParticle {
		private IAnimatedSprite spriteWithAge;
		private TrailParticle(ClientWorld world, double x, double y, double z, double motionX, double motionY, double motionZ) {
			super(world, x, y, z);
			this.motionX = motionX * (double)0.2F + (Math.random() * 2.0D - 1.0D) * (double)0.02F;
			this.motionY = motionY * (double)0.2F + (Math.random() * 2.0D - 1.0D) * (double)0.02F;
			this.motionZ = motionZ * (double)0.2F + (Math.random() * 2.0D - 1.0D) * (double)0.02F;
			this.maxAge = (int)(8.0D / (Math.random() * 0.8D + 0.2D)) * 2;
		}

		private TrailParticle(ClientWorld world, double x, double y, double z, double motionX, double motionY, double motionZ, RedstoneParticleData particleData, IAnimatedSprite spriteWithAge) {
			super(world, x, y, z, motionX, motionY, motionZ);
			this.spriteWithAge = spriteWithAge;
			this.motionX *= (double)0.1F;
			this.motionY *= (double)0.1F;
			this.motionZ *= (double)0.1F;
			float f = (float)Math.random() * 0.4F + 0.6F;
			this.particleRed = ((float)(Math.random() * (double)0.2F) + 0.8F) * particleData.getRed() * f;
			this.particleGreen = ((float)(Math.random() * (double)0.2F) + 0.8F) * particleData.getGreen() * f;
			this.particleBlue = ((float)(Math.random() * (double)0.2F) + 0.8F) * particleData.getBlue() * f;
			this.particleScale *= 0.75F * particleData.getAlpha();
			this.maxAge = (int)(8.0D / (Math.random() * 0.8D + 0.2D)) * 2;
			this.selectSpriteWithAge(spriteWithAge);
		}

		public float getScale(float scaleFactor) {
			
			if(spriteWithAge !=null){
				return this.particleScale * MathHelper.clamp(((float)this.age + scaleFactor) / (float)this.maxAge * 32.0F, 0.0F, 1.0F);
			}
			
			float f = ((float)this.age + scaleFactor) / (float)this.maxAge;
			return this.particleScale * (1.0F - f * f * 0.5F);
		}
		
		public void tick() {
			this.prevPosX = this.posX;
			this.prevPosY = this.posY;
			this.prevPosZ = this.posZ;
			if (this.maxAge-- <= 0) {
				this.setExpired();
			} else {
				
				this.move(this.motionX, this.motionY, this.motionZ);
				if(this.spriteWithAge !=null){
					//this.selectSpriteWithAge(this.spriteWithAge);
					this.motionX *= (double)0.96F;
					this.motionY *= (double)0.96F;
					this.motionZ *= (double)0.96F;
				} else {
					this.motionX *= (double)0.85F;
					this.motionY *= (double)0.85F;
					this.motionZ *= (double)0.85F;
				}
			}
		}

		public IParticleRenderType getRenderType() {
			return IParticleRenderType.PARTICLE_SHEET_OPAQUE;
		}

		@OnlyIn(Dist.CLIENT)
		public static class Factory implements IParticleFactory<BasicParticleType> {
			private final IAnimatedSprite spriteSet;

			public Factory(IAnimatedSprite spriteSet) {
				this.spriteSet = spriteSet;
			}

			public Particle makeParticle(BasicParticleType typeIn, ClientWorld worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
				TrailParticle bubbleparticle = new TrailParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed);
				bubbleparticle.selectSpriteRandomly(this.spriteSet);
				return bubbleparticle;
			}
		}
		
		@OnlyIn(Dist.CLIENT)
		public static class ColorFactory implements IParticleFactory<RedstoneParticleData> {
			private final IAnimatedSprite spriteSet;

			public ColorFactory(IAnimatedSprite spriteSet) {
				this.spriteSet = spriteSet;
			}

			public Particle makeParticle(RedstoneParticleData typeIn, ClientWorld worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
				return new TrailParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, typeIn, this.spriteSet);
			}
		}
	}
}
