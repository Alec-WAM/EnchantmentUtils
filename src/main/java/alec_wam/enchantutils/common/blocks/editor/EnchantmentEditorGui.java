package alec_wam.enchantutils.common.blocks.editor;

import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;

import alec_wam.enchantutils.EnchantmentUtils;
import alec_wam.enchantutils.client.gui.BaseContainerGui;
import alec_wam.enchantutils.common.network.ModNetwork;
import alec_wam.enchantutils.common.network.PacketEntityMessage;
import alec_wam.enchantutils.common.network.PacketTileMessage;
import alec_wam.enchantutils.common.util.ItemUtil;
import alec_wam.enchantutils.common.util.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;

public class EnchantmentEditorGui extends BaseContainerGui<EnchantmentEditorContainer> {
	public final static ResourceLocation TEXTURE = EnchantmentUtils.resourceL("textures/gui/enchantment_editor.png");
	final PlayerInventory playerInv;
	final EnchantmentEditorTileEntity editor;
	public List<Integer> selections = Lists.newArrayList();
	public Button buttonTransfer;
	public int arrowKeyIndex = -1;
	public int cost = 0;
	public boolean displayTransferButton;
	
	public EnchantmentEditorGui(EnchantmentEditorContainer container, PlayerInventory inventory, ITextComponent component) {
		super(container, inventory, component, TEXTURE);
		playerInv = inventory;
		this.editor = container.editor;
		this.ySize = 190;
	}	
	
	@Override
	public void tick()
    {
        super.tick();
        displayTransferButton = !editor.getStackInSlot(0).isEmpty() && !editor.getStackInSlot(1).isEmpty() && !selections.isEmpty() && cost > 0;
        boolean validSelections = !editor.getStackInSlot(0).isEmpty();        
        
        if(!validSelections && !selections.isEmpty()){
        	selections.clear();
        	cost = 0;
        }
	}
	
	public void calcCost(){
		boolean itemInSlot = !editor.getStackInSlot(0).isEmpty();
		Map<Enchantment, Integer> enchantments = itemInSlot ? EnchantmentHelper.getEnchantments(editor.getStackInSlot(0)) : Maps.newHashMap();
		if(enchantments.isEmpty()){
			cost = 0;
			return;
		}
		
		if(itemInSlot){
			ItemStack stack = editor.getStackInSlot(0);
			if(stack.getItem() == Items.ENCHANTED_BOOK){
				if(enchantments.size() < 2){
					cost = 0;
					return;
				}
			}
		}
		
		int totalCost = 0;
		@SuppressWarnings("unchecked")
		Entry<Enchantment, Integer>[] entries = (Entry<Enchantment, Integer>[]) enchantments.entrySet().toArray(new Entry[0]);
		for(int i : selections){
			Entry<Enchantment, Integer> entry = entries[i];
			Enchantment e = entry.getKey();
			int lvl = entry.getValue();
			totalCost+=e.getMinEnchantability(lvl);
		}
		cost = Math.max(totalCost/2, selections.isEmpty() ? 0 : 1);
	}
	
	public void performTransfer(){
		PlayerEntity player = Minecraft.getInstance().player;
		int playerXP = player.experienceLevel;
		boolean canAfford = player.abilities.isCreativeMode || playerXP >= cost;
		if(canAfford){
			if(!player.abilities.isCreativeMode){
				player.addExperienceLevel(-cost);
				CompoundNBT nbt = new CompoundNBT();
				nbt.putInt("Amount", cost);
				ModNetwork.sendToServer(new PacketEntityMessage(player, PacketEntityMessage.DEFAULT_REMOVE_XP_LEVEL, nbt));
			}
		    //playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_LEVELUP, this.getSoundCategory(), f * 0.75F, 1.0F);
		    Minecraft.getInstance().world.playSound(editor.getPos(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 0.6f, 0.8f, false);
			int[] currentSel = convertToInt(selections);
			selections.clear();
			cost = 0;
			CompoundNBT nbt = new CompoundNBT();
			nbt.putIntArray("Selections", currentSel);
			ModNetwork.sendToServer(new PacketTileMessage(editor.getPos(), "Transfer", nbt));
		}
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
			Map<Enchantment, Integer> enchantments = !editor.getStackInSlot(0).isEmpty() ? EnchantmentHelper.getEnchantments(editor.getStackInSlot(0)) : Maps.newHashMap();
			if(scroll < 0){
				if(!enchantments.isEmpty()){
					if(arrowKeyIndex < enchantments.size()-1){
						arrowKeyIndex++;
						return true;
					}
				}
			}
			if(scroll > 0){
				if(!enchantments.isEmpty()){
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
		Map<Enchantment, Integer> enchantments = !editor.getStackInSlot(0).isEmpty() ? EnchantmentHelper.getEnchantments(editor.getStackInSlot(0)) : Maps.newHashMap();
		if(p_keyPressed_1_ == 257/*Keyboard.KEY_RETURN*/){
			if(arrowKeyIndex >= 0){
				int index = selections.indexOf(arrowKeyIndex);
				if(index == -1){
					selections.add(arrowKeyIndex);
				} else {
					selections.remove(index);
				}
				calcCost();
				return true;
			}
		}
		if(p_keyPressed_1_ == 264/*Keyboard.KEY_DOWN*/){
			if(!enchantments.isEmpty()){
				if(arrowKeyIndex < enchantments.size()-1){
					arrowKeyIndex++;
					return true;
				}
			}
		}
		if(p_keyPressed_1_ == 265/*Keyboard.KEY_UP*/){
			if(!enchantments.isEmpty()){
				if(arrowKeyIndex > 0){
					arrowKeyIndex--;
					return true;
				}
			}
		}
		return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
	}

	private int colorEnchantmentListBack = new Color(232, 230, 93).getRGB();
	private int colorEnchantmentListBack_hover = new Color(163, 162, 75).getRGB();
	private int colorEnchantmentListBorder = Color.BLACK.brighter().getRGB();
	private int colorEnchantmentListBorder_selected = Color.MAGENTA.darker().getRGB();
	
	public boolean isSelected(int index){
		return selections.contains(index);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
		if(isPointInRegion(38, 64, 20, 14, mouseX, mouseY) && displayTransferButton){
			performTransfer();
			return true;
		}
		int x = 88;
		int y = 15;
		//Is not inside list, not inside inventory slots, and is not middle click
		if(!isPointInRegion(x, y, 80, 80, mouseX, mouseY) && isPointInRegion(5, 4, 167, 101, mouseX, mouseY) && mouseButton != 2){
			arrowKeyIndex = -1;
		}
		Map<Enchantment, Integer> enchantments = !editor.getStackInSlot(0).isEmpty() ? EnchantmentHelper.getEnchantments(editor.getStackInSlot(0)) : Maps.newHashMap();
		//Check if normal click or is middle click if we are scrolling
		if(!enchantments.isEmpty() && (mouseButton == 0 || (mouseButton == 2 && arrowKeyIndex >=0))){
			int start = arrowKeyIndex > 7 ? arrowKeyIndex-7 : 0;
			for(int i = start; i < start + (Math.min(enchantments.size() - start, 8)); i++){
				int offset = y + ((i - start) * 10);
				//Is inside list element or is a middle click during scroll
				if(isPointInRegion(x, offset, 80, 10, mouseX, mouseY) || (mouseButton == 2 && i == arrowKeyIndex)){
					int index = selections.indexOf(i);
					if(index == -1){
						selections.add(i);
					} else {
						selections.remove(index);
					}
					calcCost();
					return true;
				} 
			}
		}
		return super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack matrixstack, int mouseX, int mouseY)
    {
		int x = 88;
		int y = 15;
		int colorText = Color.BLACK.getRGB();
		
		Map<Enchantment, Integer> enchantments = !editor.getStackInSlot(0).isEmpty() ? EnchantmentHelper.getEnchantments(editor.getStackInSlot(0)) : Maps.newHashMap();
		if(!enchantments.isEmpty()){
			int start = arrowKeyIndex > 7 ? arrowKeyIndex-7 : 0;
			for(int i = start; i < start + (Math.min(enchantments.size() - start, 8)); i++){
				@SuppressWarnings("unchecked")
				Entry<Enchantment, Integer> entry = (Entry<Enchantment, Integer>) enchantments.entrySet().toArray(new Entry[0])[i];
				int offset = y + ((i - start) * 10);
				Enchantment e = entry.getKey();
				boolean selected = isSelected(i);
				fill(matrixstack, x, offset, x+80, offset+10, selected ? colorEnchantmentListBorder_selected : colorEnchantmentListBorder);
				
				boolean hover = arrowKeyIndex > -1 ? arrowKeyIndex == i : isPointInRegion(x, offset, 80, 10, mouseX, mouseY);
				fill(matrixstack, x+1, offset+1, x+80-1, offset+10-1, hover ? colorEnchantmentListBack_hover : colorEnchantmentListBack);
				
				if(e !=null){
					String name = ItemUtil.getEnchantmentWithLevel(e, entry.getValue());
					RenderUtil.renderScaledText(matrixstack, name, font, x+3, offset+2, 13.0F, 0.8F, colorText);
				} else {
					font.drawString(matrixstack, "ERROR", x+3, offset+1, colorText);
				}
			}
			
			if(start > 0){
				int selectionsAbove = 0;
				if(!selections.isEmpty()){
					for(int i : selections){
						if(i < start){
							selectionsAbove++;
						}
					}
				}
				String remaining = "+" + (start);
				font.drawString(matrixstack, remaining, 88, 5, colorText);
				String remainingSelected = "+" + (selectionsAbove);
				if(selectionsAbove > 0)font.drawString(matrixstack, remainingSelected, 102, 5, colorEnchantmentListBorder_selected);
			}
			
			if(enchantments.size() > start+8){
				int selectionsBelow = 0;
				if(!selections.isEmpty()){
					for(int i : selections){
						if(i > start+7){
							selectionsBelow++;
						}
					}
				}
				String remaining = "+" + (enchantments.size()-(start+8));
				font.drawString(matrixstack, remaining, 88, 97, colorText);
				String remainingSelected = "+" + (selectionsBelow);
				if(selectionsBelow > 0)font.drawString(matrixstack, remainingSelected, 102, 97, colorEnchantmentListBorder_selected);
			}
		}
		
		if(displayTransferButton && cost > 0){
			PlayerEntity player = Minecraft.getInstance().player;
			
			boolean hover = isPointInRegion(38, 64, 20, 14, mouseX, mouseY);
			
			int colorBack = hover ? Color.GRAY.getRGB() : Color.GRAY.darker().getRGB();
			int color = hover ? Color.GRAY.brighter().getRGB() : Color.GRAY.getRGB();
			
			fill(matrixstack, 38, 64, 58, 78, colorBack);
			fill(matrixstack, 39, 65, 57, 77, color);
			int playerXP = player.experienceLevel;
			boolean canAfford = player.abilities.isCreativeMode || playerXP >= cost;
			String cost = ""+this.cost;
			font.drawStringWithShadow(matrixstack, cost, 48 - (this.font.getStringWidth(cost) / 2), 67, canAfford ? 8453920 : 16736352);
		}
		
		super.drawGuiContainerForegroundLayer(matrixstack, mouseX, mouseY);
    }
	
}

