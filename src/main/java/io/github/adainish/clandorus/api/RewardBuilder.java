package io.github.adainish.clandorus.api;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.pixelmonmod.pixelmon.api.dialogue.DialogueInputScreen;
import com.pixelmonmod.pixelmon.api.util.Scheduling;
import io.github.adainish.clandorus.enumeration.BuilderAction;
import io.github.adainish.clandorus.obj.Player;
import io.github.adainish.clandorus.obj.mail.Reward;
import io.github.adainish.clandorus.storage.PlayerStorage;
import io.github.adainish.clandorus.util.Util;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.github.adainish.clandorus.util.Util.filler;

public class RewardBuilder {

    private BuilderAction activeBuilderAction;

    private Reward reward;

    public RewardBuilder()
    {
        this.reward = new Reward();
    }

    public RewardBuilder(ItemStack stack, List<String> lore, String title, List<String> commands)
    {
        this.reward = new Reward();
        this.setItemStack(stack);
        this.setLore(lore);
        this.setGUITitle(title);

    }

    public void setItemStack(ItemStack stack)
    {
        this.reward.displayItem = stack;
    }

    public void setCommands(List<String> commands)
    {
        this.reward.commandList = commands;
    }

    public void addCommand(String command)
    {
        this.reward.commandList.add(command);
    }

    public void setGUITitle(String title)
    {
        this.reward.displayTitle = title;
    }

    public void addLoreString(String lore)
    {
        this.reward.displayLore.add(lore);
    }

    public void setLore(List<String> lore)
    {
        this.reward.displayLore = lore;
    }

    public Reward build()
    {
        return this.reward;
    }

    public void openRewardBuilder(Player player)
    {
        ServerPlayerEntity playerEntity = Util.getPlayer(player.getUuid());
        UIManager.openUIForcefully(playerEntity, BuilderMenu(player));
    }

    public void openNewRewardBuilder(ServerPlayerEntity player) {
        Player clanPlayer = PlayerStorage.getPlayer(player.getUniqueID());
        if (clanPlayer != null) {
            clanPlayer.setRewardBuilder(this);
            UIManager.openUIForcefully(player, BuilderMenu(clanPlayer));
        } else
        {
            Util.send(player, "&cSomething went wrong opening the reward builder! Does your player data exist for Clandorus?");
        }
    }

    public boolean doneBuilding()
    {
        if (this.reward.commandList == null || this.reward.commandList.isEmpty())
            return false;

        if (this.reward.displayLore == null || this.reward.displayLore.isEmpty())
            return false;

        if (this.reward.displayTitle == null || this.reward.displayTitle.isEmpty())
            return false;

        if (this.reward.isConfigBased && this.reward.identifier == null || this.reward.isConfigBased && this.reward.identifier.isEmpty())
            return false;

        return this.reward.displayItem != null && !this.reward.displayItem.isEmpty();
    }

    public DialogueInputScreen.Builder dialogueInputScreenBuilder(BuilderAction builderAction, Player player)
    {

        DialogueInputScreen.Builder builder = new DialogueInputScreen.Builder();
        builder.setShouldCloseOnEsc(false);
        switch (builderAction)
        {
            case lore:
            {
                builder.setTitle(Util.formattedString("&bLore"));
                builder.setText(Util.formattedString("&7Type the Lore you want to add for the reward!"));
                break;
            }
            case item:
            {
                builder.setTitle(Util.formattedString("&bItem"));
                builder.setText(Util.formattedString("&7Provide the Item ID you want to use for the reward!"));
                break;
            }
            case title:
            {
                builder.setTitle(Util.formattedString("&bTitle"));
                builder.setText(Util.formattedString("&7Provide the GUI Title you want to use for the Reward!"));
                break;
            }
            case identifier:
            {
                builder.setTitle(Util.formattedString("&bIdentifier"));
                builder.setText(Util.formattedString("&7Provide the identifier under which the reward needs to be saved in the reward registry!"));
                break;
            }
            case commands:
            {
                builder.setTitle(Util.formattedString("&bCommand"));
                builder.setText(Util.formattedString("&7Provide a command you want to execute when a player claims a reward!"));
                break;
            }
            case none:
            {
                break;
            }
        }
        activeBuilderAction = builderAction;
        updateRewardBuilderCache(player);
        return builder;
    }

    public void updateRewardBuilderCache(Player player)
    {
        player.setRewardBuilder(this);
        player.updateCache();
    }

    public GooeyPage BuilderMenu(Player player)
    {
        this.reward = player.getRewardBuilder().reward;
        ChestTemplate.Builder builder = ChestTemplate.builder(4);
        builder.fill(filler);

        GooeyButton displayItemStack;

        if (this.reward.displayItem == null || this.reward.displayItem.isEmpty())
            displayItemStack = GooeyButton.builder()
                    .display(new ItemStack(Items.BARRIER))
                    .title(Util.formattedString("&cSelect an ItemStack"))
                    .onClick(b ->
                    {
                        UIManager.closeUI(b.getPlayer());
                        Scheduling.schedule(2, scheduledTask -> {
                            dialogueInputScreenBuilder(BuilderAction.item, player).sendTo(b.getPlayer());
                        }, false);
                    })
                    .build();
        else displayItemStack = GooeyButton.builder()
                .display(this.reward.displayItem)
                .title(Util.formattedString("&aItem Selected"))
                .lore(Util.formattedArrayList(Arrays.asList(
                        "&7You've selected %item% to be your Reward Display Item"
                                .replace("%item%", Util.getItemStackName(this.reward.displayItem)),
                        "&aYou can select another item by clicking this button!"
                )))
                .onClick(b ->
                {
                    UIManager.closeUI(b.getPlayer());
                    Scheduling.schedule(2, scheduledTask -> {
                        dialogueInputScreenBuilder(BuilderAction.item, player).sendTo(b.getPlayer());
                    }, false);
                })
                .build();

        GooeyButton lore;

        if (this.reward.displayLore == null || this.reward.displayLore.isEmpty())
        {
            lore = GooeyButton.builder()
                    .title(Util.formattedString("&cSet up Lore"))
                    .display(new ItemStack(Items.BARRIER))
                    .onClick(b -> {
                        UIManager.closeUI(b.getPlayer());
                        Scheduling.schedule(2, scheduledTask -> {
                            dialogueInputScreenBuilder(BuilderAction.lore, player).sendTo(b.getPlayer());
                        }, false);
                    })
                    .build();
        } else {
            List<String> buttonLore = new ArrayList<>();
            buttonLore.add("&aYou've set up the following display lore for the Reward:");
            buttonLore.addAll(this.reward.displayLore);
            lore = GooeyButton.builder()
                    .title(Util.formattedString("&aLore Set Up"))
                    .display(new ItemStack(Items.WRITTEN_BOOK))
                    .onClick(b -> {
                        UIManager.closeUI(b.getPlayer());
                        Scheduling.schedule(2, scheduledTask -> {
                            dialogueInputScreenBuilder(BuilderAction.lore, player).sendTo(b.getPlayer());
                        }, false);
                    })
                    .lore(Util.formattedArrayList(buttonLore))
                    .build();
        }

        GooeyButton title;

        if (this.reward.displayTitle == null || this.reward.displayTitle.isEmpty())
        {
            title = GooeyButton.builder()
                    .title(Util.formattedString("&cSet up Title"))
                    .display(new ItemStack(Items.BARRIER))
                    .onClick(b ->
                    {
                        UIManager.closeUI(b.getPlayer());
                        Scheduling.schedule(2, scheduledTask -> {
                            dialogueInputScreenBuilder(BuilderAction.title, player).sendTo(b.getPlayer());
                        }, false);
                    })
                    .lore(Util.formattedArrayList(Arrays.asList("&cYou still need to set up the reward title!")))
                    .build();
        } else {
            title = GooeyButton.builder()
                    .title(Util.formattedString("&aTitle Set Up!"))
                    .display(new ItemStack(Items.NAME_TAG))
                    .onClick(b ->
                    {
                        UIManager.closeUI(b.getPlayer());
                        Scheduling.schedule(2, scheduledTask -> {
                            dialogueInputScreenBuilder(BuilderAction.title, player).sendTo(b.getPlayer());
                        }, false);
                    })
                    .lore(Util.formattedArrayList(Arrays.asList("&aYou've set up the following title for the display: %title%"
                            .replace("%title%", this.reward.displayTitle))))
                    .build();
        }

        GooeyButton rewards;
        if (this.reward.commandList == null || this.reward.commandList.isEmpty())
        {
            rewards = GooeyButton.builder()
                    .title(Util.formattedString("&cCommands have not been set up!"))
                    .onClick(b ->
                    {
                        UIManager.closeUI(b.getPlayer());
                        Scheduling.schedule(2, scheduledTask -> {
                            dialogueInputScreenBuilder(BuilderAction.commands, player).sendTo(b.getPlayer());
                        }, false);
                    })
                    .display(new ItemStack(Items.BARRIER))
                    .build();
        } else {
            List<String> loreList = new ArrayList<>();
            loreList.add("&aYou've set up the following commands:");
            loreList.addAll(this.reward.commandList);

            rewards = GooeyButton.builder()
                    .title(Util.formattedString("&aConfigured Commands!"))
                    .display(new ItemStack(Items.COMMAND_BLOCK))
                    .lore(Util.formattedArrayList(loreList))
                    .onClick(b ->
                    {
                        UIManager.closeUI(b.getPlayer());
                        Scheduling.schedule(2, scheduledTask -> {
                            dialogueInputScreenBuilder(BuilderAction.commands, player).sendTo(b.getPlayer());
                        }, false);
                    })
                    .build();
        }

        GooeyButton identifier;

        if (this.reward.isConfigBased)
        {
            if (this.reward.identifier == null || this.reward.identifier.isEmpty())
            {
                identifier = GooeyButton.builder()
                        .title(Util.formattedString("&cIdentifier has not been set up!"))
                        .onClick(b ->
                        {
                            UIManager.closeUI(b.getPlayer());
                            Scheduling.schedule(2, scheduledTask -> {
                                dialogueInputScreenBuilder(BuilderAction.identifier, player).sendTo(b.getPlayer());
                            }, false);
                        })
                        .display(new ItemStack(Items.BARRIER))
                        .build();
            } else {
                List<String> idArray = new ArrayList<>();
                idArray.add("&7Your reward will be stored under the following id:");
                idArray.add("&b%identifier%".replace("%identifier%", this.reward.identifier));
                identifier = GooeyButton.builder()
                        .title(Util.formattedString("&aIdentifier has been set up!"))
                        .onClick(b ->
                        {
                            UIManager.closeUI(b.getPlayer());
                            Scheduling.schedule(2, scheduledTask -> {
                                dialogueInputScreenBuilder(BuilderAction.identifier, player).sendTo(b.getPlayer());
                            }, false);
                        })
                        .lore(Util.formattedArrayList(idArray))
                        .display(new ItemStack(Items.MAP))
                        .build();
            }
        } else identifier = filler;

        GooeyButton finishBuilder = GooeyButton.builder()
                .title(Util.formattedString("&aFinish Building"))
                .onClick(b -> {

                })
                .display(new ItemStack(Items.LIME_WOOL))
                .build();
        if (!doneBuilding())
        {
            finishBuilder = GooeyButton.builder()
                    .title(Util.formattedString("&cNot done building"))
                    .lore(Util.formattedArrayList(Arrays.asList("&cYou still need to set up a few options before you can finish building this reward!")))
                    .display(new ItemStack(Items.RED_WOOL))
                    .build();
        }

        builder.set(1, 1, displayItemStack);
        builder.set(1, 3, lore);
        builder.set(1, 5, title);
        builder.set(1, 7, rewards);

        if (this.reward.isConfigBased)
        {
            builder.set(2, 2, identifier);
        }

        builder.set(2, 4, finishBuilder);

        return GooeyPage.builder().template(builder.build()).title(Util.formattedString("&bReward Builder")).build();
    }


    public BuilderAction getActiveBuilderAction() {
        return activeBuilderAction;
    }

    public void setActiveBuilderAction(BuilderAction activeBuilderAction) {
        this.activeBuilderAction = activeBuilderAction;
    }
}
