package alec_wam.enchantutils.common.blocks.mobkiller;

import com.mojang.blaze3d.matrix.MatrixStack;

import alec_wam.enchantutils.EnchantmentUtils;
import alec_wam.enchantutils.client.gui.BaseContainerGui;
import alec_wam.enchantutils.common.network.ModNetwork;
import alec_wam.enchantutils.common.network.PacketTileMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.widget.button.Button;
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
