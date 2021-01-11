package alec_wam.enchantutils.common.blocks.upgradebench;

import java.awt.Color;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import alec_wam.enchantutils.EnchantmentUtils;
import alec_wam.enchantutils.client.gui.BaseContainerGui;
import alec_wam.enchantutils.client.gui.GuiManager;
import alec_wam.enchantutils.common.feature.upgradepoints.UpgradePointManager;
import alec_wam.enchantutils.common.feature.upgradepoints.upgrade.ElytraTrailUpgrade;
import alec_wam.enchantutils.common.feature.upgradepoints.upgrade.IBaseUpgrade;
import alec_wam.enchantutils.common.network.ModNetwork;
import alec_wam.enchantutils.common.network.PacketEntityMessage;
import alec_wam.enchantutils.common.network.PacketTileMessage;
import alec_wam.enchantutils.common.util.LangUtil;
import alec_wam.enchantutils.common.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class UpgradeBenchScreen extends BaseContainerGui<UpgradeBenchContainer> {
	public final static ResourceLocation TEXTURE = EnchantmentUtils.resourceL("textures/gui/upgrade_bench.png");
	final PlayerInventory playerInv;
	final UpgradeBenchTileEntity tile;
	public List<Integer> selections = Lists.newArrayList();
	public Button buttonTransfer;
	public int arrowKeyIndex = -1;
	public int scrollIndex = 0;
	public int cost = 0;
	public boolean displayTransferButton;
	
	public boolean elytraTrailSettings;
	private int colorPickerPointX = 0;
	private int colorPickerPointY = 0;
	public int colorPickerTone;
	
	public UpgradeBenchScreen(UpgradeBenchContainer container, PlayerInventory inventory, ITextComponent component) {
		super(container, inventory, component, TEXTURE);
		playerInv = inventory;
		this.tile = container.tile;
		this.xSize = 256;
		this.ySize = 234;
		
		this.titleX = 8;
		this.titleY = 6;
		this.playerInventoryTitleX = 47;
		this.playerInventoryTitleY = this.ySize - 97;
	}	
	
	@Override
	protected void init() {
		super.init();
	}
	
	public void updateScroll(double value){
		double size = (double)(getUpgradeDisplayList().size());
		UpgradeBenchScreen.this.scrollIndex = MathHelper.floor(MathHelper.clampedLerp(0.0D, size, value));
	}
	
	@Override
	public void tick()
    {
        super.tick();
        /*displayTransferButton = !tile.getStackInSlot(0).isEmpty() && !tile.getStackInSlot(1).isEmpty() && !selections.isEmpty() && cost > 0;
        boolean validSelections = !tile.getStackInSlot(0).isEmpty();        
        
        if(!validSelections && !selections.isEmpty()){
        	selections.clear();
        	cost = 0;
        }*/
	}
	
	public static int[] convertToInt(List<Integer> list){
		int[] newArray = new int[list.size()];
		for(int i = 0; i < list.size(); i++){
			newArray[i] = list.get(i);
		}
		return newArray;
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scroll)
	{ 
		if (scroll != 0) {
			List<IBaseUpgrade> upgrades = !tile.getStackInSlot(0).isEmpty() ? getUpgradeDisplayList() : Lists.newArrayList();
			if(scroll < 0){
				if(!upgrades.isEmpty()){
					if(arrowKeyIndex < upgrades.size()-1){
						arrowKeyIndex++;
						return true;
					}
				}
			}
			if(scroll > 0){
				if(!upgrades.isEmpty()){
					if(arrowKeyIndex > 0){
						arrowKeyIndex--;
						return true;
					}
				}
			}
		}
		return super.mouseScrolled(mouseX, mouseY, scroll);
	}
	
	@Override
	public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_)
    {
		if(elytraTrailSettings){
			if (p_keyPressed_1_ == 256){
				elytraTrailSettings = false;
				return true;
			}
		}
		
		List<IBaseUpgrade> upgrades = !tile.getStackInSlot(0).isEmpty() ? getUpgradeDisplayList() : Lists.newArrayList();
		//Keyboard.KEY_RETURN
		if(p_keyPressed_1_ == 257){
			/*if(arrowKeyIndex >= 0){
				int index = selections.indexOf(arrowKeyIndex);
				if(index == -1){
					selections.add(arrowKeyIndex);
				} else {
					selections.remove(index);
				}
				calcCost();
				return true;
			}*/
		}
		//Keyboard.KEY_DOWN
		if(p_keyPressed_1_ == 264){
			if(!upgrades.isEmpty()){
				if(arrowKeyIndex < upgrades.size()-1){
					arrowKeyIndex++;
					return true;
				}
			}
		}
		//Keyboard.KEY_UP
		if(p_keyPressed_1_ == 265){
			if(!upgrades.isEmpty()){
				if(arrowKeyIndex > 0){
					arrowKeyIndex--;
					return true;
				}
			}
		}
		return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
	}
	
	public boolean isSelected(int index){
		return selections.contains(index);
	}
	
	public List<IBaseUpgrade> getUpgradeDisplayList(){
		ItemStack tool = tile.getStackInSlot(0);
		if(!tool.isEmpty() && UpgradePointManager.hasUpgrades(tool)){
			List<IBaseUpgrade> upgrades = Lists.newArrayList();
			List<IBaseUpgrade> installedUpgrades = Lists.newArrayList();
			List<IBaseUpgrade> otherUpgrades = Lists.newArrayList();
			
			for(IBaseUpgrade upgrade : UpgradePointManager.getUpgradesAvailable(tool)){
				if(UpgradePointManager.getUpgradeLevel(tool, upgrade) > 0){
					installedUpgrades.add(upgrade);
				}
				else {
					otherUpgrades.add(upgrade);
				}
			}
			
			Collections.sort(installedUpgrades, new Comparator<IBaseUpgrade>(){
	
				@Override
				public int compare(IBaseUpgrade arg0, IBaseUpgrade arg1) {
					int lvl0 = UpgradePointManager.getUpgradeLevel(tool, arg0);
					int lvl1 = UpgradePointManager.getUpgradeLevel(tool, arg1);
					String displayName1 = arg0.getDisplayName(lvl0);
					String displayName2 = arg1.getDisplayName(lvl1);
					return displayName1.compareToIgnoreCase(displayName2);
				}
				
			});
			Collections.sort(otherUpgrades, new Comparator<IBaseUpgrade>(){
				
				@Override
				public int compare(IBaseUpgrade arg0, IBaseUpgrade arg1) {
					int lvl0 = UpgradePointManager.getUpgradeLevel(tool, arg0);
					int lvl1 = UpgradePointManager.getUpgradeLevel(tool, arg1);
					String displayName1 = arg0.getDisplayName(lvl0);
					String displayName2 = arg1.getDisplayName(lvl1);
					return displayName1.compareToIgnoreCase(displayName2);
				}
				
			});
			
			upgrades.addAll(installedUpgrades);
			upgrades.addAll(otherUpgrades);
			
			return upgrades;
		}
		return Lists.newArrayList();
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
		if(elytraTrailSettings){
			int settingsWidth = 200;
			int settingsHeight = 150;
			int settingsX = (xSize / 2) - (settingsWidth / 2);
			int settingsY = (ySize / 2) - (settingsHeight / 2);
			if(!isPointInRegion(settingsX, settingsY, settingsWidth, settingsHeight, mouseX, mouseY)){
				elytraTrailSettings = false;
				return true;
			} else {
				
				ItemStack tool = tile.getStackInSlot(0);
				if(!tool.isEmpty()){
					if(UpgradePointManager.hasUpgrades(tool)){		
						ElytraTrailUpgrade.ElytraTrail currentTrail = ElytraTrailUpgrade.getTrailType(tool);
						
						//
						int colorSize = 20;
						int colorX = settingsX + (settingsWidth / 2) - (colorSize / 2);
						int colorY = settingsY + 65 - (colorSize / 2);
						if(isPointInRegion(colorX, colorY, colorSize, colorSize, mouseX, mouseY)){
							if(mouseButton == 1){
								String clipboard = Minecraft.getInstance().keyboardListener.getClipboardString();
								try {
									int parsed = Integer.parseInt(clipboard, 16);
									setFromColor(parsed);
								} catch (NumberFormatException e) {
									//TODO Send warning
								}
								
								return true;
							}
						}
						
						int colorBarWidth = 100;
						int colorBarHeight = 70;
						int colorScaleHeight = colorBarHeight - 20;
						int colorBarX = settingsX + (settingsWidth / 2) - (colorBarWidth / 2);
						int colorBarY = settingsY + 80;
						if(isPointInRegion(colorBarX, colorBarY, colorBarWidth, colorScaleHeight, mouseX, mouseY)){
							this.colorPickerPointX = (int)(mouseX - guiLeft - colorBarX);
							this.colorPickerPointY = (int)(mouseY - guiTop - colorBarY);
							return true;
						}
						
						int toneX = colorBarX;
						int toneWidth = colorBarWidth;
						int toneY = colorBarY + colorScaleHeight;
						int toneHeight = 10;
						
						if(isPointInRegion(toneX, toneY, toneWidth, toneHeight, mouseX, mouseY)){
							int newPos = (int)(mouseX - guiLeft - toneX);
							colorPickerTone = (int)((newPos / 100.0f) * 256.0f);
						}
						
						int currentColor = getColorPickerColor();
						if(currentColor != ElytraTrailUpgrade.getTrailColor(tool)){
							String strButton = "Apply";
							int buttonX = colorBarX + colorBarWidth + 5;
							int buttonY = colorBarY + colorScaleHeight - 2;
							int stringWidth = font.getStringWidth(strButton);
							int buttonWidth = stringWidth + 10;
							
							if(isPointInRegion(buttonX, buttonY, buttonWidth, 12, mouseX, mouseY)){
								CompoundNBT nbt = new CompoundNBT();
								nbt.putInt("Color", currentColor);
								ModNetwork.sendToServer(new PacketTileMessage(tile.getPos(), "ElytraTrail", nbt));
								Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
								return true;
							}
						}
						
						int trailCount = ElytraTrailUpgrade.ElytraTrail.values().length;
						int slotStartX = settingsX + ((settingsWidth / 2) - ((28 * trailCount) / 2));
						for(int s = 0; s < trailCount; s++){
							ElytraTrailUpgrade.ElytraTrail trail = ElytraTrailUpgrade.ElytraTrail.values()[s];
							if(trail == currentTrail)continue;
							int slotX = slotStartX + (28 * s);
							int slotY = settingsY + 20;
							
							if(isPointInRegion(slotX, slotY, 28, 28, mouseX, mouseY)){						
								CompoundNBT nbt = new CompoundNBT();
								nbt.putString("Type", trail.name().toLowerCase());
								ModNetwork.sendToServer(new PacketTileMessage(tile.getPos(), "ElytraTrail", nbt));
								Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
								
								return true;
							}
						}
					}
				}				
				return true;
			}
		}
		
		
		int boxX = 70;
		int boxY = 18;		
		//Is not inside list, not inside inventory slots, and is not middle click
		if(!isPointInRegion(boxX, boxY, 178, 113, mouseX, mouseY) && isPointInRegion(0, 0, xSize, ySize, mouseX, mouseY) && mouseButton != 2){
			arrowKeyIndex = -1;
		}
		
		
		ItemStack tool = tile.getStackInSlot(0);
		if(!tool.isEmpty()){
			if(UpgradePointManager.hasUpgrades(tool)){		

				if(isPointInRegion(boxX, boxY, 178, 113, mouseX, mouseY)){
					int upgradeNodeOffsetX = 1;
					int upgradeNodeOffsetY = 1;

					List<IBaseUpgrade> upgrades = getUpgradeDisplayList();
					int start = Math.max(arrowKeyIndex - 5, 0);
					for(int u = start; u < start + Math.min(upgrades.size() - start, 5); u++){

						IBaseUpgrade upgrade = upgrades.get(u);
						int nodeX = boxX + upgradeNodeOffsetX;
						int nodeY = boxY + upgradeNodeOffsetY + (21 * (u - start));
						if(isPointInRegion(nodeX, nodeY, 176, 20, mouseX, mouseY)){
							//In Node
							if(!tool.isEmpty() && UpgradePointManager.hasUpgrades(tool)){
								int currentLevel = UpgradePointManager.getUpgradeLevel(tool, upgrade);
								
								if(currentLevel > 0 && upgrade == UpgradePointManager.UPGRADE_ELYTRA_TRAIL){
									boolean hoveredPlus = isPointInRegion(nodeX + 160, nodeY + 2, 20, 20, mouseX, mouseY);
									if(hoveredPlus){
										int currentColor = ElytraTrailUpgrade.getTrailColor(tool);
										setFromColor(currentColor);
										
										elytraTrailSettings = true;
										return true;
									}
								}	
								
								if(currentLevel < upgrade.getMaxLevel()){
									if(UpgradePointManager.isBasicUpgradeItem(tool)){
										PlayerEntity player = Minecraft.getInstance().player;
										int cost = currentLevel == 0 ? upgrade.getPointCost(tool) : 1;
										boolean isCreative = player.abilities.isCreativeMode;
										int playerXP = player.experienceLevel;
										cost *= UpgradePointManager.getXPLevelsPerPoint(tool);
										boolean canAfford = isCreative || playerXP >= cost;
										if(canAfford){
											CompoundNBT nbt = new CompoundNBT();
											nbt.putString("id", upgrade.getID().toString());
											nbt.putInt("level", currentLevel + 1);
											nbt.putInt("cost", 0);
											nbt.putBoolean("xp", true);
											ModNetwork.sendToServer(new PacketTileMessage(tile.getPos(), "AddUpgrade", nbt));
											
											if(!isCreative){
												player.addExperienceLevel(-cost);
												nbt = new CompoundNBT();
												nbt.putInt("Amount", cost);
												ModNetwork.sendToServer(new PacketEntityMessage(player, PacketEntityMessage.DEFAULT_REMOVE_XP_LEVEL, nbt));
											}
											
											Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
											return true;
										}
									}
									else {
										int cost = currentLevel == 0 ? upgrade.getPointCost(tool) : 1;
										boolean isCreative = Minecraft.getInstance().player.abilities.isCreativeMode;
										boolean canAfford = isCreative || UpgradePointManager.getToolPoints(tool) >= cost;
										if(canAfford){
											CompoundNBT nbt = new CompoundNBT();
											nbt.putString("id", upgrade.getID().toString());
											nbt.putInt("level", currentLevel + 1);
											nbt.putInt("cost", isCreative ? 0 : cost);
											ModNetwork.sendToServer(new PacketTileMessage(tile.getPos(), "AddUpgrade", nbt));
											
											Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
											return true;
										}
									}
								}
							}
						}
					}
				}
			}else {
				PlayerEntity player = Minecraft.getInstance().player;
				boolean isCreative = player.abilities.isCreativeMode;
				if(UpgradePointManager.isUpgradeable(tool) && (!tile.getStackInSlot(1).isEmpty() || isCreative)){
					String text = LangUtil.localize("gui.upgradebench.install");				
					int barWidth = font.getStringWidth(text) + 6;
					int barX = 37 - (barWidth / 2);
					int barY = 80;
					int barHeight = 14;
					if(isPointInRegion(barX, barY, barWidth, barHeight, mouseX, mouseY)){
						CompoundNBT nbt = new CompoundNBT();
						
						if(isCreative){
							nbt.putBoolean("Creative", true);
						}
						ModNetwork.sendToServer(new PacketTileMessage(tile.getPos(), "InstallModule", nbt));
						//Minecraft.getInstance().getSoundHandler().play(SimpleSound.master(SoundEvents.UI_BUTTON_CLICK, 1.0F));
						//TODO Create Upgrade Sound
						Minecraft.getInstance().world.playSound(tile.getPos(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 0.6f, 0.8f, false);						
					}
				}
			}
		}

		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	public int getColorPickerColor() {
		float h = colorPickerPointX * 0.01f;
		float b = 1.0f - (colorPickerPointY * 0.02f);
		float s = 1.0f - (colorPickerTone / 255f);
		return Color.HSBtoRGB(h, s, b);
	}

	public void setFromColor(int col) {
		float[] hsb = new float[3];
		Color.RGBtoHSB((col & 0xFF0000) >> 16, (col & 0x00FF00) >> 8, col & 0x0000FF, hsb);
		colorPickerPointX = (int)(hsb[0] * 100);
		colorPickerPointY = (int)((1.0f - hsb[2]) * 50);
		colorPickerTone = (255 - (int)(hsb[1] * 255));
	}
	
	@Override
	public boolean hideItemTooltips(){
		return elytraTrailSettings;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack matrixstack, int mouseX, int mouseY)
    {
		ItemStack tool = tile.getStackInSlot(0);
		boolean showXPTooltip = false;
		List<ITextComponent> upgradeDesc = null;
		if(!tool.isEmpty()){
			if(UpgradePointManager.hasUpgrades(tool)){
				int boxX = 70;
				int boxY = 18;
				
				int upgradeNodeOffsetX = 1;
				int upgradeNodeOffsetY = 1;
				
				List<IBaseUpgrade> upgrades = getUpgradeDisplayList();
				int start = Math.max(arrowKeyIndex - 5, 0);
				for(int u = start; u < start + Math.min(upgrades.size() - start, 5); u++){
					Minecraft.getInstance().getTextureManager().bindTexture(GuiManager.TEXTURE_WIDGETS);
					
					IBaseUpgrade upgrade = upgrades.get(u);
					int nodeX = boxX + upgradeNodeOffsetX;
					int nodeY = boxY + upgradeNodeOffsetY + (21 * (u - start));
					
					int lvl = UpgradePointManager.getUpgradeLevel(tool, upgrade); 
					boolean mouseOver = isPointInRegion(nodeX, nodeY, 176, 20, mouseX, mouseY);
					boolean hovered = (arrowKeyIndex > -1 ? arrowKeyIndex == u : mouseOver) && !elytraTrailSettings;
					boolean installed = lvl > 0;
					if(installed)RenderSystem.color3f(0.95F, 0.0f, 1.0f);
					blit(matrixstack, nodeX, nodeY, 0, hovered ? 21 : 0, 176, 20);
					if(installed)RenderSystem.color3f(1.0F, 1.0f, 1.0f);
					
					boolean hasIcon = true;
					ResourceLocation icon = upgrade.getIcon();
					
					if(icon == null){
						hasIcon = false;
					}
					else if(this.minecraft.getTextureManager().getTexture(icon) !=null){
						if(this.minecraft.getTextureManager().getTexture(icon) == MissingTextureSprite.getDynamicTexture()){
							hasIcon = false;
						}
					}
					
					if(hasIcon){
						this.minecraft.getTextureManager().bindTexture(icon);
						blit(matrixstack, nodeX + 3, nodeY + 3, 14, 14, 0, 0, 18, 18, 18, 18);
					}
					
					String name = upgrade.getDisplayName(Math.max(lvl, 1));				
					font.drawString(matrixstack, name, nodeX + (hasIcon ? 20 : 5), nodeY + 6, 0x000000);
					
					if(lvl < upgrade.getMaxLevel()){
						int cost = lvl == 0 ? upgrade.getPointCost(tool) : 1;
						boolean canAfford = Minecraft.getInstance().player.abilities.isCreativeMode || UpgradePointManager.getToolPoints(tool) >= cost;
						String price = (canAfford ? TextFormatting.YELLOW : TextFormatting.RED) + "" + cost;
						
						if(UpgradePointManager.isBasicUpgradeItem(tool)){
							PlayerEntity player = Minecraft.getInstance().player;
							int playerXP = player.experienceLevel;
							cost *= UpgradePointManager.getXPLevelsPerPoint(tool);
							canAfford = player.abilities.isCreativeMode || playerXP >= cost;
							price = (canAfford ? TextFormatting.GREEN : TextFormatting.RED) + "" + cost+"L";
						}
						
						font.drawString(matrixstack, price, nodeX + 165 - (font.getStringWidth(price) / 2), nodeY + 6, 0x000000);
					}
					
					if(mouseOver){
						upgradeDesc = upgrade.getDescription(tool, Math.max(lvl, 1));	
					}
					
					if(installed && upgrade == UpgradePointManager.UPGRADE_ELYTRA_TRAIL){
						boolean hoveredPlus = isPointInRegion(nodeX + 160, nodeY + 2, 20, 20, mouseX, mouseY) && !elytraTrailSettings;
						font.drawString(matrixstack, "+", nodeX + 165, nodeY + 6, hoveredPlus ? Color.YELLOW.getRGB() : 0x000000);
					}					
				}
				
				//TODO Draw arrows incase of scroll
				if(start > 0 && arrowKeyIndex > 4){
					
				}
				
				
				if(!UpgradePointManager.isBasicUpgradeItem(tool)){
					double xp = UpgradePointManager.getToolXP(tool);
					double max = UpgradePointManager.getMaxXP(tool);
					int barX = 7;
					int barWidth = 60;
					int barY = 80;
					int barHeight = 8;
					fill(matrixstack, barX, barY, barX + barWidth, barY + barHeight, Color.GRAY.darker().getRGB());
					fill(matrixstack, barX + 1, barY + 1, barX + barWidth - 1, barY + barHeight - 1, Color.GRAY.getRGB());
					
					int xpBarX = barX + 1;
					int xpBarbarWidth = barWidth - 1;
					if(xp > 0){
						int widthXP = (int)((xp / max) * xpBarbarWidth);
						fill(matrixstack, xpBarX, barY + 1, barX + widthXP, barY + barHeight - 1, Color.GREEN.darker().getRGB());
						
						showXPTooltip = isPointInRegion(barX, barY, barWidth, barHeight, mouseX, mouseY);					
					}
					
					String points = LangUtil.localizeFormat("gui.upgradebench.points", "" + UpgradePointManager.getToolPoints(tool));				
					int pointX = barX + (barWidth / 2) - (font.getStringWidth(points) / 2);
					int pointY = barY + barHeight + 8;
					font.drawStringWithShadow(matrixstack, points, pointX, pointY, Color.YELLOW.getRGB());
				}
			} else {
				if(UpgradePointManager.isUpgradeable(tool)){
					PlayerEntity player = Minecraft.getInstance().player;
					boolean isCreative = player.abilities.isCreativeMode;
					
					if(!tile.getStackInSlot(1).isEmpty() || isCreative){
						String text = LangUtil.localize("gui.upgradebench.install");				
						int barWidth = font.getStringWidth(text) + 6;
						int barX = 37 - (barWidth / 2);
						int barY = 80;
						int barHeight = 14;
						fill(matrixstack, barX, barY, barX + barWidth, barY + barHeight, Color.GRAY.darker().getRGB());
						boolean hovered = isPointInRegion(barX, barY, barWidth, barHeight, mouseX, mouseY);
						int color = hovered ? Color.GRAY.brighter().getRGB() : Color.GRAY.getRGB();
						fill(matrixstack, barX + 1, barY + 1, barX + barWidth - 1, barY + barHeight - 1, color);
						
						int pointX = barX + (barWidth / 2) - (font.getStringWidth(text) / 2);
						int pointY = barY + 3;
						font.drawStringWithShadow(matrixstack, text, pointX, pointY, Color.WHITE.getRGB());
					}
				}
			}
		}
		super.drawGuiContainerForegroundLayer(matrixstack, mouseX, mouseY);
		if(!elytraTrailSettings){
			if(showXPTooltip){
				double xp = UpgradePointManager.getToolXP(tool);
				double max = UpgradePointManager.getMaxXP(tool);
				String xpStr = xp + " / " + max;
				matrixstack.push();
				matrixstack.translate((float)-guiLeft, (float)-guiTop, 0.0F);
				renderTooltip(matrixstack, new StringTextComponent(xpStr), mouseX, mouseY);
				matrixstack.pop();	
			}		
	
			if(upgradeDesc !=null && !upgradeDesc.isEmpty()){
				matrixstack.push();
				matrixstack.translate((float)-guiLeft, (float)-guiTop, 0.0F);
				renderWrappedToolTip(matrixstack, upgradeDesc, mouseX, mouseY, font);
				matrixstack.pop();							
			}
		} else {
			if(tool.isEmpty() || !UpgradePointManager.hasUpgrades(tool))return;
			
			String hoverString = null;
			this.fillGradient(matrixstack, -guiLeft, -guiTop, this.width, this.height, -1072689136, -804253680);
			
			minecraft.getTextureManager().bindTexture(new ResourceLocation("minecraft:textures/gui/demo_background.png"));
			int settingsWidth = 200;
			int settingsHeight = 150;
			int settingsX = (xSize / 2) - (settingsWidth / 2);
			int settingsY = (ySize / 2) - (settingsHeight / 2);
			int offsetZ = 0;
			RenderUtil.innerBlit(matrixstack, settingsX, settingsX + settingsWidth, settingsY, settingsY + settingsHeight, offsetZ, 248, 166, 0, 0, 256, 256);
			
			String settingsTitle = UpgradePointManager.UPGRADE_ELYTRA_TRAIL.getDisplayName(1);	
			this.font.drawString(matrixstack, settingsTitle, (float)settingsX + 6, (float)settingsY + 6, 4210752);
			
			int trailCount = ElytraTrailUpgrade.ElytraTrail.values().length;
			int slotStartX = settingsX + ((settingsWidth / 2) - ((28 * trailCount) / 2));
			for(int s = 0; s < trailCount; s++){
				ElytraTrailUpgrade.ElytraTrail trail = ElytraTrailUpgrade.ElytraTrail.values()[s];
				minecraft.getTextureManager().bindTexture(TEXTURE);
				int slotX = slotStartX + (28 * s);
				int slotY = settingsY + 20;
				blit(matrixstack, slotX, slotY, 23, 29, 26, 26);
				
				int middleOffsetX = 4;
				int middleOffsetY = 4;
				ResourceLocation icon = null;
				if(trail == ElytraTrailUpgrade.ElytraTrail.BUBBLE){
					icon = new ResourceLocation("minecraft:textures/particle/bubble.png");
				}
				if(trail == ElytraTrailUpgrade.ElytraTrail.FLAME){
					middleOffsetX = middleOffsetY = 5;
					icon = new ResourceLocation("minecraft:textures/particle/flame.png");
				}
				if(trail == ElytraTrailUpgrade.ElytraTrail.GLINT){
					middleOffsetX = middleOffsetY = 6;
					icon = new ResourceLocation("minecraft:textures/particle/glint.png");
				}
				if(trail == ElytraTrailUpgrade.ElytraTrail.SPARKLE){
					icon = new ResourceLocation("minecraft:textures/particle/spark_4.png");
				}
				if(trail == ElytraTrailUpgrade.ElytraTrail.HEART){
					middleOffsetX = 5;
					icon = new ResourceLocation("minecraft:textures/particle/heart.png");
				}
				if(trail == ElytraTrailUpgrade.ElytraTrail.COLOR){
					icon = new ResourceLocation("minecraft:textures/particle/generic_6.png");
				}
				if(icon !=null){
					minecraft.getTextureManager().bindTexture(icon);
					int renderWidth = 16;
					
					if(trail == ElytraTrailUpgrade.ElytraTrail.COLOR){
						int ticks = this.minecraft.player.ticksExisted;
			            int i = ticks / 25;
			            int j = DyeColor.values().length;
			            int k = i % j;
			            int l = (i + 1) % j;
			            float f3 = ((float)(ticks % 25) + this.minecraft.getRenderPartialTicks()) / 25.0F;
			            float[] afloat1 = SheepEntity.getDyeRgb(DyeColor.byId(k));
			            float[] afloat2 = SheepEntity.getDyeRgb(DyeColor.byId(l));
			            float r = afloat1[0] * (1.0F - f3) + afloat2[0] * f3;
			            float g = afloat1[1] * (1.0F - f3) + afloat2[1] * f3;
			            float b = afloat1[2] * (1.0F - f3) + afloat2[2] * f3;
			            
			            RenderSystem.color3f(r, g, b);
						RenderUtil.innerBlit(matrixstack, slotX + middleOffsetX, slotX + middleOffsetX + renderWidth, slotY + middleOffsetY, slotY + middleOffsetY + renderWidth, 0, 8, 8, 0, 0, 8, 8);
						RenderSystem.color3f(1.0f, 1.0f, 1.0f);					
			            
					} else {					
						RenderUtil.innerBlit(matrixstack, slotX + middleOffsetX, slotX + middleOffsetX + renderWidth, slotY + middleOffsetY, slotY + middleOffsetY + renderWidth, 0, 8, 8, 0, 0, 8, 8);
					}
				}
				
				if(isPointInRegion(slotX, slotY, 26, 26, mouseX, mouseY)){
					hoverString = trail.getDisplayName();
				}
			}
			
			
			ElytraTrailUpgrade.ElytraTrail currentTrail = ElytraTrailUpgrade.getTrailType(tool);
			int selectedTrail = currentTrail == null ? -1 : currentTrail.ordinal();
			if(selectedTrail > -1){
				int slotX = slotStartX + (28 * selectedTrail) - 4;
				int slotY = settingsY + 20 - 4;
				int renderWidth = 34;
				minecraft.getTextureManager().bindTexture(Widget.WIDGETS_LOCATION);
				RenderUtil.innerBlit(matrixstack, slotX, slotX + renderWidth, slotY, slotY + renderWidth, 0, 24, 24, 0, 22, 256, 256);
				
				
				if(currentTrail == ElytraTrailUpgrade.ElytraTrail.COLOR){
					int currentColor = getColorPickerColor();
					int colorSize = 20;
					int colorX = settingsX + (settingsWidth / 2) - (colorSize / 2);
					int colorY = settingsY + 65 - (colorSize / 2);
					fill(matrixstack, colorX, colorY, colorX + colorSize, colorY + colorSize, currentColor);				
					
					if(isPointInRegion(colorX, colorY, colorSize, colorSize, mouseX, mouseY)){
						hoverString = "R-Click to paste color from clipboard";
					}
					
					String colorValue = String.format("%06X", currentColor);
					this.font.drawString(matrixstack, colorValue, colorX  + colorSize + 5, colorY + 10, currentColor);
					
					int colorBarWidth = 100;
					int colorBarHeight = 70;
					int colorScaleHeight = colorBarHeight - 20;
					int colorBarX = settingsX + (settingsWidth / 2) - (colorBarWidth / 2);
					int colorBarY = settingsY + 80;
					
					int toneX = colorBarX;
					int toneWidth = colorBarWidth;
					int toneY = colorBarY + colorScaleHeight;
					int toneHeight = 10;
					
					fill(matrixstack, toneX, toneY, toneX + toneWidth, toneY + toneHeight, Color.GRAY.getRGB());
					
					float offsetTone = toneWidth / 256.0f;
					int sliderX = (int)(toneX + (offsetTone * colorPickerTone));
					fill(matrixstack, sliderX - 1, toneY, sliderX + 1, toneY + toneHeight, Color.BLACK.getRGB());

					Minecraft.getInstance().getTextureManager().bindTexture(GuiManager.TEXTURE_WIDGETS);					
					blit(matrixstack, colorBarX, colorBarY, 0, 206, colorBarWidth, colorScaleHeight);
					fill(matrixstack, colorBarX, colorBarY, colorBarX + colorBarWidth, colorBarY + colorScaleHeight, (colorPickerTone << 24) | 0xFFFFFF);
					
					
					RenderSystem.disableTexture();
					RenderSystem.enableBlend();
					RenderSystem.disableAlphaTest();
					RenderSystem.defaultBlendFunc();
					RenderSystem.shadeModel(7425);
					Tessellator tessellator = Tessellator.getInstance();
					BufferBuilder bufferbuilder = tessellator.getBuffer();
					bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
					
					float x1 = colorBarX;
					float x2 = colorBarX + colorBarWidth;
					float y1 = colorBarY;
					float y2 = colorBarY + colorScaleHeight;
					float z = 0;
					int colorA = Color.BLACK.getRGB();
					int colorB = Color.BLACK.getRGB();
					float f = 0.0f;					
					float f1 = (float)(colorA >> 16 & 255) / 255.0F;
					float f2 = (float)(colorA >> 8 & 255) / 255.0F;
					float f3 = (float)(colorA & 255) / 255.0F;
					
					float f4 = 1.0f;
					float f5 = (float)(colorB >> 16 & 255) / 255.0F;
					float f6 = (float)(colorB >> 8 & 255) / 255.0F;
					float f7 = (float)(colorB & 255) / 255.0F;
					bufferbuilder.pos(matrixstack.getLast().getMatrix(), (float)x2, (float)y1, (float)z).color(f1, f2, f3, f).endVertex();
					bufferbuilder.pos(matrixstack.getLast().getMatrix(), (float)x1, (float)y1, (float)z).color(f1, f2, f3, f).endVertex();
					bufferbuilder.pos(matrixstack.getLast().getMatrix(), (float)x1, (float)y2, (float)z).color(f5, f6, f7, f4).endVertex();
					bufferbuilder.pos(matrixstack.getLast().getMatrix(), (float)x2, (float)y2, (float)z).color(f5, f6, f7, f4).endVertex();
					
					tessellator.draw();
					RenderSystem.shadeModel(7424);
					RenderSystem.disableBlend();
					RenderSystem.enableAlphaTest();
					RenderSystem.enableTexture();
					
					fill(matrixstack, colorBarX + colorPickerPointX - 1,
							colorBarY + colorPickerPointY - 1,
							colorBarX + colorPickerPointX + 1,
							colorBarY + colorPickerPointY + 1, 0xCCCC0000);
					
					if(currentColor != ElytraTrailUpgrade.getTrailColor(tool)){
						String strButton = "Apply";
						int buttonX = colorBarX + colorBarWidth + 5;
						int buttonY = colorBarY + colorScaleHeight - 2;
						int stringWidth = font.getStringWidth(strButton);
						int buttonWidth = stringWidth + 10;
						fill(matrixstack, buttonX, buttonY, buttonX + buttonWidth, buttonY + 12, Color.GRAY.getRGB());	
						boolean hovered = isPointInRegion(buttonX, buttonY, buttonWidth, 12, mouseX, mouseY);
						font.drawString(matrixstack, strButton, buttonX + (buttonWidth / 2) - (stringWidth / 2), buttonY + 2, hovered ? Color.YELLOW.getRGB() : Color.WHITE.getRGB());
					}
				}
			}
			
			if(hoverString !=null){
				matrixstack.push();
				matrixstack.translate((float)-guiLeft, (float)-guiTop, 0.0F);
				renderTooltip(matrixstack, new StringTextComponent(hoverString), mouseX, mouseY);
				matrixstack.pop();	
			}
		}
    }
	
}


