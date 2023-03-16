package io.github.adainish.clandorus.listener;

import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import io.github.adainish.clandorus.Clandorus;
import io.github.adainish.clandorus.api.GymBuilder;
import io.github.adainish.clandorus.items.GymEditorItem;
import io.github.adainish.clandorus.obj.Player;
import io.github.adainish.clandorus.obj.gyms.ClanGym;
import io.github.adainish.clandorus.registry.ClanGymRegistry;
import io.github.adainish.clandorus.storage.PlayerStorage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ItemInteractListener
{
    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteractSpecific event) {
        if (event.isCanceled())
            return;

        if (event.getTarget() instanceof NPCTrainer) {
            if (GymEditorItem.isEditor(event.getItemStack())) {
                if (event.getPlayer() == null) {
                    event.setCanceled(true);
                    return;
                }
                // check if user has appropriate permission
                if (GymEditorItem.canUse((ServerPlayerEntity) event.getPlayer())) {
                    // else take item from inventory and throw notifier event and warn in console
                    event.setCanceled(true);
                    Player player = PlayerStorage.getPlayer(event.getPlayer().getUniqueID());
                    if (player != null) {
                        GymBuilder gymBuilder = new GymBuilder();
                        // check if target has already been marked as gym npc
                        ClanGym gym = null;
                        String gymID = event.getTarget().getPersistentData().getString("clandorusGymID");
                        if (Clandorus.clanGymRegistry.clanGymCache.containsKey(gymID))
                            gym = Clandorus.clanGymRegistry.clanGymCache.get(gymID);
                        //check if gym exists in storage
                        // and assign gym
                        gymBuilder.openEditorUI(player, (NPCTrainer) event.getTarget(), gym);
                    } else {

                    }
                    //open editor GUI
                    // else open creation UI
                } else {
                    //take item
                    event.getPlayer().inventory.mainInventory.remove(event.getItemStack());
                    Clandorus.log.warn("%player% tried using a gym editor illegally!");
                    //do warning event
                }
            }
        }
    }
}
