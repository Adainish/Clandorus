package io.github.adainish.clandorus.obj.mail;

import info.pixelmon.repack.org.spongepowered.serialize.SerializationException;
import io.github.adainish.clandorus.Clandorus;
import io.github.adainish.clandorus.conf.RewardConfig;
import io.github.adainish.clandorus.obj.Player;
import io.github.adainish.clandorus.storage.PlayerStorage;
import io.github.adainish.clandorus.util.Util;
import io.leangen.geantyref.TypeToken;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Reward {
    private String identifier;
    private boolean isConfigBased = false;

    private List<String> commandList = new ArrayList<>();
    private String displayTitle = "";

    private List<String> displayLore = new ArrayList<>();

    private ItemStack displayItem;

    public Reward()
    {

    }

    public void saveItemStack()
    {
        String itemLocationString = RewardConfig.getConfig().get().node("Rewards", identifier, "Item").getString();
        ResourceLocation location = new ResourceLocation(itemLocationString);
        Item item;
        if (ForgeRegistries.ITEMS.containsKey(location))
            item = ForgeRegistries.ITEMS.getValue(location).getItem();
        else item = Items.PAPER;
        this.setDisplayItem(new ItemStack(item));
    }


    public Reward(String identifier)
    {
        this.setIdentifier(identifier);
        this.setConfigBased(true);
        this.setDisplayTitle(RewardConfig.getConfig().get().node("Rewards", identifier, "Title").getString());
        try {
            this.setDisplayLore(RewardConfig.getConfig().get().node("Rewards", identifier, "Lore").getList(TypeToken.get(String.class)));
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
        try {
            this.setCommandList(RewardConfig.getConfig().get().node("Rewards", identifier, "Commands").getList(TypeToken.get(String.class)));
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
        saveItemStack();
    }

    public void handOutRewards(ServerPlayerEntity player)
    {
        if (getCommandList().isEmpty()) {
            Clandorus.log.warn("While handing out rewards Clandorus detected that no commands were added! Please make sure to investigate!");
            return;
        }
        for (String s: getCommandList()) {
            if (s.isEmpty()) {
                Clandorus.log.warn("Command String empty while handing out reward for %p%.".replace("%p%", player.getName().getUnformattedComponentText()));
                continue;
            }
            Util.runCommand(s.replace("%player%", player.getName().getUnformattedComponentText()));
        }
    }

    public void handOutRewards(UUID uuid)
    {
        Player player = PlayerStorage.getPlayer(uuid);
        if (player == null)
        {
            Clandorus.log.warn("Player data could not be retrieved, rewards could not be handed out!");
            return;
        }
        if (getCommandList().isEmpty()) {
            Clandorus.log.warn("While handing out rewards Clandorus detected that no commands were added! Please make sure to investigate!");
            return;
        }
        for (String s: getCommandList()) {
            if (s.isEmpty()) {
                Clandorus.log.warn("Command String empty while handing out reward for %p%.".replace("%p%", player.getName()));
                continue;
            }
            Util.runCommand(s.replace("%player%", player.getName()));
        }
    }

    public void handOutRewards(Player player)
    {
        if (getCommandList().isEmpty()) {
            Clandorus.log.warn("While handing out rewards Clandorus detected that no commands were added! Please make sure to investigate!");
            return;
        }
        for (String s: getCommandList()) {
            if (s.isEmpty()) {
                Clandorus.log.warn("Command String empty while handing out reward for %p%.".replace("%p%", player.getName()));
                continue;
            }
            Util.runCommand(s.replace("%player%", player.getName()));
        }
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public boolean isConfigBased() {
        return isConfigBased;
    }

    public void setConfigBased(boolean configBased) {
        isConfigBased = configBased;
    }

    public List<String> getCommandList() {
        return commandList;
    }

    public void setCommandList(List<String> commandList) {
        this.commandList = commandList;
    }

    public String getDisplayTitle() {
        return displayTitle;
    }

    public void setDisplayTitle(String displayTitle) {
        this.displayTitle = displayTitle;
    }

    public List<String> getDisplayLore() {
        return displayLore;
    }

    public void setDisplayLore(List<String> displayLore) {
        this.displayLore = displayLore;
    }

    public ItemStack getDisplayItem() {
        return displayItem;
    }

    public void setDisplayItem(ItemStack displayItem) {
        this.displayItem = displayItem;
    }
}
