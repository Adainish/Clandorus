package io.github.adainish.clandorus.listener;

import com.pixelmonmod.pixelmon.api.events.dialogue.DialogueInputEvent;
import com.pixelmonmod.pixelmon.api.util.Scheduling;
import io.github.adainish.clandorus.obj.Player;
import io.github.adainish.clandorus.storage.PlayerStorage;
import net.minecraft.entity.ai.brain.schedule.Schedule;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

public class DialogueScreenListener {
    @SubscribeEvent
    public void onDialogueScreenEvent(DialogueInputEvent.Submitted event)
    {
        Player player = PlayerStorage.getPlayer(event.getPlayer().getUniqueID());
        if (player != null)
        {
            if (player.getRewardBuilder() != null)
            {
                switch (player.getRewardBuilder().getActiveBuilderAction())
                {
                    case commands:
                    {
                        player.getRewardBuilder().addCommand(event.getInput());
                        Scheduling.schedule(2, () -> {
                            player.getRewardBuilder().openRewardBuilder(player);
                        } , false);
                        break;
                    }
                    case title:
                    {
                        player.getRewardBuilder().setGUITitle(event.getInput());
                        Scheduling.schedule(2, () -> {
                            player.getRewardBuilder().openRewardBuilder(player);
                        } , false);
                        break;
                    }
                    case item:
                    {
                        ResourceLocation location = new ResourceLocation(event.getInput());
                        Item item;
                        if (ForgeRegistries.ITEMS.containsKey(location))
                            item = ForgeRegistries.ITEMS.getValue(location).getItem();
                        else item = Items.PAPER;
                        player.getRewardBuilder().setItemStack(new ItemStack(item));
                        Scheduling.schedule(2, () -> {
                            player.getRewardBuilder().openRewardBuilder(player);
                        } , false);
                        break;
                    }
                    case lore:
                    {
                        player.getRewardBuilder().addLoreString(event.getInput());
                        Scheduling.schedule(2, () -> {
                            player.getRewardBuilder().openRewardBuilder(player);
                        } , false);
                        break;
                    }
                    default:
                    {
                        return;
                    }
                }
            }
        }
    }
}
