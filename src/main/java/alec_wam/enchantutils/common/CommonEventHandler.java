package alec_wam.enchantutils.common;

import java.util.Collection;
import java.util.Iterator;

import alec_wam.enchantutils.EnchantmentUtils;
import alec_wam.enchantutils.common.blocks.mobkiller.MobKillerFakePlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.ZombieEvent.SummonAidEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.Event.Result;

public class CommonEventHandler {

	@SubscribeEvent
	public static void onSummonAid(SummonAidEvent event) {
		if (event.getAttacker() instanceof MobKillerFakePlayer) {
			event.setResult(Result.DENY);
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void onMobDeath(LivingDropsEvent event)
	{
		Entity attacker = event.getSource().getTrueSource();
		if(attacker !=null){
			if(attacker instanceof MobKillerFakePlayer){
				MobKillerFakePlayer fakePlayer = (MobKillerFakePlayer)attacker;
				if(fakePlayer.mobKillerTile !=null){
					Collection<ItemEntity> drops = event.getDrops();
					Iterator<ItemEntity> it = drops.iterator();				
					
					while(it.hasNext()){
						ItemEntity dropItem = it.next();
						ItemStack dropStack = dropItem.getItem();
						ItemStack insertStack = fakePlayer.mobKillerTile.suckInStack(dropStack);
						if(insertStack.isEmpty()){
							it.remove();
						} else if(dropStack.getCount() != insertStack.getCount()){
							dropItem.setItem(insertStack);
						}
					}
				}
			}
		}
	}
	
}
