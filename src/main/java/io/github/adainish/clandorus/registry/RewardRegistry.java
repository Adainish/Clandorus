package io.github.adainish.clandorus.registry;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.button.PlaceholderButton;
import ca.landonjw.gooeylibs2.api.button.linked.LinkType;
import ca.landonjw.gooeylibs2.api.button.linked.LinkedPageButton;
import ca.landonjw.gooeylibs2.api.helpers.PaginationHelper;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.template.Template;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import com.pixelmonmod.pixelmon.api.util.helpers.RandomHelper;
import info.pixelmon.repack.org.spongepowered.CommentedConfigurationNode;
import info.pixelmon.repack.org.spongepowered.ConfigurateException;
import info.pixelmon.repack.org.spongepowered.serialize.SerializationException;
import io.github.adainish.clandorus.Clandorus;
import io.github.adainish.clandorus.conf.RewardConfig;
import io.github.adainish.clandorus.obj.mail.Reward;
import io.github.adainish.clandorus.util.Util;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.*;

import static io.github.adainish.clandorus.util.Util.filler;

public class RewardRegistry {
    public HashMap<String, Reward> rewardCache = new HashMap<>();

    public RewardRegistry()
    {
        loadRegistry();
    }

    public void loadRegistry() {
        CommentedConfigurationNode node = RewardConfig.getConfig().get().node("Rewards");
        Map<Object, CommentedConfigurationNode> nodeMap = node.childrenMap();
        for (Object obj : nodeMap.keySet()) {
            if (obj == null) {
                Clandorus.log.error("OBJ Null while generating Reward");
                continue;
            }
            String rewardID = obj.toString();

            Reward reward = new Reward(rewardID);
            rewardCache.put(rewardID, reward);

        }
    }

    public List<String> alphabet()
    {
        return new ArrayList<>(Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"));
    }


    public String randomIDGenerator()
    {
        StringBuilder stringBuilder = new StringBuilder("AutoID");
        for (int i = 0; i < 10; i++) {
            stringBuilder.append(RandomHelper.getRandomElementFromCollection(alphabet()));
        }
        return stringBuilder.toString();
    }


    public void saveToConfig(Reward reward)
    {
        RewardConfig rewardConfig = RewardConfig.getConfig();
        CommentedConfigurationNode configurationNode = rewardConfig.get();
        try {
            if (!reward.isConfigBased())
                reward.setIdentifier(randomIDGenerator());
            configurationNode.node("Rewards", reward.getIdentifier(), "Title").set(reward.getDisplayTitle());
            configurationNode.node("Rewards", reward.getIdentifier(), "Lore").set(reward.getDisplayLore());
            configurationNode.node("Rewards", reward.getIdentifier(), "Commands").set(reward.getCommandList());
            configurationNode.node("Rewards", reward.getIdentifier(), "Item").set(Util.getResourceLocationStringFromItemStack(reward.getDisplayItem()));
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
        try {
            rewardConfig.getConfigLoader().save(configurationNode);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
        rewardCache.put(reward.getIdentifier(), reward);
    }

    public void viewRegistry(ServerPlayerEntity player)
    {
        UIManager.openUIForcefully(player, RewardRegistryMenu());
    }

    public List<Button> rewardRegistryButtonList()
    {
        List<Button> buttons = new ArrayList<>();
        for (Reward reward:rewardCache.values()) {
            List<String> lore = new ArrayList<>();
            lore.add("&7This reward has the following lore:");
            lore.addAll(reward.getDisplayLore());
            lore.add("&7This reward has the following commands:");
            lore.addAll(reward.getCommandList());
            GooeyButton button = GooeyButton.builder()
                    .title(Util.formattedString(reward.getDisplayTitle()))
                    .lore(Util.formattedArrayList(lore))
                    .display(reward.getDisplayItem())
                    .onClick(b ->
                    {

                    })
                    .build();
            buttons.add(button);
        }
        return buttons;
    }

    public LinkedPage RewardRegistryMenu()
    {
        PlaceholderButton placeHolderButton = new PlaceholderButton();
        LinkedPageButton previous = LinkedPageButton.builder()
                .display(new ItemStack(PixelmonItems.trade_holder_left))
                .title("Previous Page")
                .linkType(LinkType.Previous)
                .build();

        LinkedPageButton next = LinkedPageButton.builder()
                .display(new ItemStack(PixelmonItems.trade_holder_right))
                .title("Next Page")
                .linkType(LinkType.Next)
                .build();

        Template template;
        if (rewardRegistryButtonList().size() > 8) {
            template = ChestTemplate.builder(6)
                    .border(0, 0, 6, 9, filler)
                    .set(0, 3, previous)
                    .set(0, 5, next)
                    .rectangle(1, 1, 4, 7, placeHolderButton)
                    .build();
        } else {
            template = ChestTemplate.builder(3)
                    .border(0, 0, 3, 9, filler)
                    .row(1, placeHolderButton)
                    .build();
        }

        return PaginationHelper.createPagesFromPlaceholders(template, rewardRegistryButtonList(), LinkedPage.builder().title(Util.formattedString("&bReward Registry")).template(template));
    }


}
