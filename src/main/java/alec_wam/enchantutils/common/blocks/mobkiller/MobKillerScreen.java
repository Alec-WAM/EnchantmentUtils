package alec_wam.enchantutils.common.blocks.mobkiller;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import alec_wam.enchantutils.EnchantmentUtils;
import alec_wam.enchantutils.client.gui.BaseContainerGui;
import alec_wam.enchantutils.common.network.ModNetwork;
import alec_wam.enchantutils.common.network.PacketTileMessage;
import alec_wam.enchantutils.common.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.button.CheckboxButton;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class MobKillerScreen extends BaseContainerGui<MobKillerContainer> {
   private static final ResourceLocation TEXTURE = EnchantmentUtils.resourceL("textures/gui/mob_killer.png");

   public PlayerInventory playerInv;
   public MobKillerScreen(MobKillerContainer container, PlayerInventory inventory, ITextComponent component) {
      super(container, inventory, component, TEXTURE);
      playerInv = inventory;
      xSize = 192;
      ySize = 186;
      this.playerInventoryTitleX = 16;
      this.playerInventoryTitleY = this.ySize - 93;
   }

   @Override
   protected void init() {
      super.init();
      this.titleX = (this.xSize - this.font.getStringPropertyWidth(this.title)) / 2;
      
      int buttonWidth = 20 + 20 + 24 + 24 + 6;
      int buttonY = guiTop + 70;
      int button1X = guiLeft + (xSize / 2) - (buttonWidth / 2);
      
      MobKillerTileEntity tile = container.tile;
      
      this.addButton(new Button(button1X, buttonY, 20, 20, new StringTextComponent("-1"), new Button.IPressable(){

		@Override
		public void onPress(Button p_onPress_1_) {
			CompoundNBT nbt = new CompoundNBT();
			nbt.putInt("Levels", 1);
			nbt.putUniqueId("PlayerUUID", Minecraft.getInstance().player.getUniqueID());
			ModNetwork.sendToServer(new PacketTileMessage(tile.getPos(), "TransferXP", nbt));
		}
    	  
      }));
      int button2X = button1X + 22;
      this.addButton(new Button(button2X, buttonY, 20, 20, new StringTextComponent("-5"), new Button.IPressable(){

  		@Override
  		public void onPress(Button p_onPress_1_) {
  			CompoundNBT nbt = new CompoundNBT();
			nbt.putInt("Levels", 5);
			nbt.putUniqueId("PlayerUUID", Minecraft.getInstance().player.getUniqueID());
			ModNetwork.sendToServer(new PacketTileMessage(tile.getPos(), "TransferXP", nbt));
  		}
      	  
      }));
      int button3X = button2X + 22;
      this.addButton(new Button(button3X, buttonY, 24, 20, new StringTextComponent("-10"), new Button.IPressable(){

  		@Override
  		public void onPress(Button p_onPress_1_) {
  			CompoundNBT nbt = new CompoundNBT();
			nbt.putInt("Levels", 10);
			nbt.putUniqueId("PlayerUUID", Minecraft.getInstance().player.getUniqueID());
			ModNetwork.sendToServer(new PacketTileMessage(tile.getPos(), "TransferXP", nbt));
  		}
      	  
      }));
      
      int button4X = button3X + 26;
      this.addButton(new Button(button4X, buttonY, 26, 20, new StringTextComponent("-All"), new Button.IPressable(){

  		@Override
  		public void onPress(Button p_onPress_1_) {
  			CompoundNBT nbt = new CompoundNBT();
			nbt.putInt("Levels", tile.getExperienceLevel() + 1);
			nbt.putUniqueId("PlayerUUID", Minecraft.getInstance().player.getUniqueID());
			ModNetwork.sendToServer(new PacketTileMessage(tile.getPos(), "TransferXP", nbt));
  		}
      	  
      }));
      
      this.addButton(new CheckboxButton(guiLeft + 173, guiTop + 8, 10, 10, new StringTextComponent("Show Killbox"), tile.isKillBoxVisible){

    		@Override
    		public void onPress() {
    			super.onPress();
    			tile.isKillBoxVisible = isChecked();
    			CompoundNBT nbt = new CompoundNBT();
    			nbt.putBoolean("Show", isChecked());
    			ModNetwork.sendToServer(new PacketTileMessage(tile.getPos(), "ShowKillbox", nbt));
    		}
    		
    		@SuppressWarnings("deprecation")
			@Override
    		public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
    			Minecraft minecraft = Minecraft.getInstance();
    			minecraft.getTextureManager().bindTexture(new ResourceLocation("textures/gui/checkbox.png"));
    			RenderSystem.enableDepthTest();
    			RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
    			RenderSystem.enableBlend();
    			RenderSystem.defaultBlendFunc();
    			RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    			RenderUtil.innerBlit(matrixStack, this.x, this.x + this.width, this.y, this.y + this.height, 0, 20, 20, this.isFocused() ? 20.0F : 0.0F, isChecked() ? 20.0F : 0.0F, 64, 64);
    			this.renderBg(matrixStack, minecraft, mouseX, mouseY);
    			if (this.isHovered()) {
    				MobKillerScreen.this.renderTooltip(matrixStack, getMessage(), mouseX, mouseY);
    			}
    		}
        	  
        });
   }

   @Override
   protected void drawGuiContainerForegroundLayer(MatrixStack matrixstack, int mouseX, int mouseY)
   {
	   super.drawGuiContainerForegroundLayer(matrixstack, mouseX, mouseY);
	   MobKillerTileEntity tile = this.container.tile;
	   if(tile == null)return;
	   
	   Minecraft mc = Minecraft.getInstance();
	   mc.getTextureManager().bindTexture(AbstractGui.GUI_ICONS_LOCATION);
	   int i = tile.getXpBarCapacity();
	   if (i > 0) {		   

		   float scale = 1.0f;
		   int xpWidth = 182;
		   int xpX = (xSize / 2) - ((int)(xpWidth * scale) / 2);
		   int xpY = 60;
		   matrixstack.push();
		   matrixstack.translate(xpX, xpY, 0);
		   matrixstack.scale(scale, scale, 1.0f);
		   int k = (int)(tile.getExperience() * 183.0F);
		   blit(matrixstack, 0, 0, 0, 64, xpWidth, 5);
		   if (k > 0) {
			   blit(matrixstack, 0, 0, 0, 69, k, 5);
		   }
		   matrixstack.pop();
		   
		   if (tile.getExperienceLevel() > 0) {
			   String s = "" + tile.getExperienceLevel();
			   int levelX = (xSize / 2) - ((int)(this.font.getStringWidth(s) * scale) / 2);
			   int levelY = 52;
			   matrixstack.push();
			   matrixstack.translate(levelX, levelY, 0);
			   matrixstack.scale(scale, scale, 1.0f);
			   this.font.drawString(matrixstack, s, (float)(1), (float)0, 0);
			   this.font.drawString(matrixstack, s, (float)(-1), (float)0, 0);
			   this.font.drawString(matrixstack, s, (float)0, (float)(1), 0);
			   this.font.drawString(matrixstack, s, (float)0, (float)(-1), 0);
			   this.font.drawString(matrixstack, s, (float)0, (float)0, 8453920);
			   matrixstack.pop();
		   }
	   }
	   
   }
}
