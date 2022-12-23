package io.github.adainish.clandorus.listener;

import com.pixelmonmod.pixelmon.api.events.dialogue.DialogueInputEvent;
import io.github.adainish.clandorus.obj.Player;
import io.github.adainish.clandorus.storage.PlayerStorage;
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
                        player.getRewardBuilder().openRewardBuilder(player);
                        break;
                    }
                    case title:
                    {
                        player.getRewardBuilder().setGUITitle(event.getInput());
                        player.getRewardBuilder().openRewardBuilder(player);
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
                        player.getRewardBuilder().openRewardBuilder(player);
                        break;
                    }
                    case lore:
                    {
                        player.getRewardBuilder().addLoreString(event.getInput());
                        player.getRewardBuilder().openRewardBuilder(player);
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
