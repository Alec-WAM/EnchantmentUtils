package alec_wam.enchantutils.client.gui;

import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class BaseContainerGui<C extends Container> extends ContainerScreen<C> {
	private final ResourceLocation texture;
	public BaseContainerGui(C inventorySlotsIn, PlayerInventory inv, ITextComponent title, ResourceLocation texture) {
		super(inventorySlotsIn, inv, title);
		this.texture = texture;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		if(texture !=null){
			Minecraft.getInstance().getTextureManager().bindTexture(texture);
			blit(matrixStack, guiLeft, guiTop, 0, 0, xSize, ySize);
		}
	}
	
	public boolean hideItemTooltips(){
		return false;
	}
	
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		this.renderBackground(matrixStack);
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		if(!hideItemTooltips())this.renderHoveredTooltip(matrixStack, mouseX, mouseY);
		for(Widget button : this.buttons){
			if(button instanceof ITooltipProvider){
				if(isPointInRegion(button.x - guiLeft, button.y - guiTop, button.getWidth(), button.getHeightRealms(), mouseX, mouseY)){
					List<ITextComponent> info = ((ITooltipProvider)button).getInfo();
					if(!info.isEmpty()){
						this.renderWrappedToolTip(matrixStack, info, mouseX, mouseY, font);
					}
				}
			}
		}
	}
	
	public static interface ITooltipProvider {
		public List<ITextComponent> getInfo();
	}

}

