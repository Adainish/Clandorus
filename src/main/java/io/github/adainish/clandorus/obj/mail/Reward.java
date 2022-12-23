package io.github.adainish.clandorus.obj.mail;

import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import io.github.adainish.clandorus.Clandorus;
import io.github.adainish.clandorus.obj.Player;
import io.github.adainish.clandorus.storage.PlayerStorage;
import io.github.adainish.clandorus.util.Util;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Reward {
    public String identifier;
    public boolean isConfigBased = false;

    public List<String> commandList = new ArrayList<>();
    public String displayTitle = "";

    public List<String> displayLore = new ArrayList<>();

    public ItemStack displayItem;

    public Reward()
    {

    }

    public Reward(String identifier)
    {
        this.identifier = identifier;
        this.isConfigBased = true;
    }

    public void handOutRewards(ServerPlayerEntity player)
    {
        if (commandList.isEmpty()) {
            Clandorus.log.warn("While handing out rewards Clandorus detected that no commands were added! Please make sure to investigate!");
            return;
        }
        for (String s:commandList) {
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
        if (commandList.isEmpty()) {
            Clandorus.log.warn("While handing out rewards Clandorus detected that no commands were added! Please make sure to investigate!");
            return;
        }
        for (String s:commandList) {
            if (s.isEmpty()) {
                Clandorus.log.warn("Command String empty while handing out reward for %p%.".replace("%p%", player.getName()));
                continue;
            }
            Util.runCommand(s.replace("%player%", player.getName()));
        }
    }

    public void handOutRewards(Player player)
    {
        if (commandList.isEmpty()) {
            Clandorus.log.warn("While handing out rewards Clandorus detected that no commands were added! Please make sure to investigate!");
            return;
        }
        for (String s:commandList) {
            if (s.isEmpty()) {
                Clandorus.log.warn("Command String empty while handing out reward for %p%.".replace("%p%", player.getName()));
                continue;
            }
            Util.runCommand(s.replace("%player%", player.getName()));
        }
    }
}
