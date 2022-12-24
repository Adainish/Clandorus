package io.github.adainish.clandorus.api;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.button.PlaceholderButton;
import ca.landonjw.gooeylibs2.api.button.linked.LinkType;
import ca.landonjw.gooeylibs2.api.button.linked.LinkedPageButton;
import ca.landonjw.gooeylibs2.api.helpers.PaginationHelper;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.template.Template;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.pixelmonmod.pixelmon.api.dialogue.DialogueInputScreen;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import com.pixelmonmod.pixelmon.api.util.Scheduling;
import io.github.adainish.clandorus.Clandorus;
import io.github.adainish.clandorus.enumeration.BuilderAction;
import io.github.adainish.clandorus.obj.Player;
import io.github.adainish.clandorus.obj.mail.Reward;
import io.github.adainish.clandorus.storage.PlayerStorage;
import io.github.adainish.clandorus.util.Util;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

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
        this.setCommands(commands);
    }

    public void setItemStack(ItemStack stack)
    {
        this.reward.setDisplayItem(stack);
    }

    public void setCommands(List<String> commands)
    {
        this.reward.setCommandList(commands);
    }

    public void addCommand(String command)
    {
        this.reward.getCommandList().add(command);
    }

    public void setGUITitle(String title)
    {
        this.reward.setDisplayTitle(title);
    }

    public void addLoreString(String lore)
    {
        this.reward.getDisplayLore().add(lore);
    }

    public void setLore(List<String> lore)
    {
        this.reward.setDisplayLore(lore);
    }

    public Reward buildReward()
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
        if (this.reward.getCommandList() == null || this.reward.getCommandList().isEmpty())
            return false;

        if (this.reward.getDisplayLore() == null || this.reward.getDisplayLore().isEmpty())
            return false;

        if (this.reward.getDisplayTitle() == null || this.reward.getDisplayTitle().isEmpty())
            return false;

        if (this.reward.isConfigBased() && this.reward.getIdentifier() == null || this.reward.isConfigBased() && this.reward.getIdentifier().isEmpty())
            return false;

        return this.reward.getDisplayItem() != null && !this.reward.getDisplayItem().isEmpty();
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

    public List<Button> commandButtonList(Player player)
    {
        List<Button> buttons = new ArrayList<>();
        for (String s:player.getRewardBuilder().reward.getCommandList()) {
            GooeyButton button = GooeyButton.builder()
                    .title(Util.formattedString("&aClick to remove the following option:"))
                    .display(new ItemStack(Items.COMMAND_BLOCK))
                    .lore(Util.formattedArrayList(Arrays.asList(s)))
                    .onClick(b ->
                    {
                        player.getRewardBuilder().reward.getCommandList().remove(s);
                        player.getRewardBuilder().openRewardBuilder(player);
                    })
                    .build();
            buttons.add(button);
        }
        return buttons;
    }

    public LinkedPage RemoveCommandMenu(Player player)
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

        GooeyButton goBack = GooeyButton.builder()
                .title(Util.formattedString("&cGo Back"))
                .lore(Util.formattedArrayList(Arrays.asList("&7Click to go back to the previous menu!")))
                .display(new ItemStack(Items.ARROW))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), CommandEditingMenu(player));
                })
                .build();

        Template template;
        if (commandButtonList(player).size() > 8) {
            template = ChestTemplate.builder(6)
                    .border(0, 0, 6, 9, filler)
                    .set(0, 1, goBack)
                    .set(0, 3, previous)
                    .set(0, 5, next)
                    .rectangle(1, 1, 4, 7, placeHolderButton)
                    .build();
        } else {
            template = ChestTemplate.builder(3)
                    .border(0, 0, 3, 9, filler)
                    .row(1, placeHolderButton)
                    .set(0, 1, goBack)
                    .build();
        }

        return PaginationHelper.createPagesFromPlaceholders(template, commandButtonList(player), LinkedPage.builder().title(Util.formattedString("&bEdit Commands")).template(template));
    }

    public GooeyPage CommandEditingMenu(Player player)
    {
        ChestTemplate.Builder builder = ChestTemplate.builder(4);
        builder.fill(filler);

        GooeyButton goBack = GooeyButton.builder()
                .title(Util.formattedString("&cGo Back"))
                .lore(Util.formattedArrayList(Arrays.asList("&7Click to go back to the previous menu!")))
                .display(new ItemStack(Items.ARROW))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), BuilderMenu(player));
                })
                .build();

        GooeyButton clearCommands = GooeyButton.builder()
                .title(Util.formattedString("&aClear Commands"))
                .display(new ItemStack(Items.BOOK))
                .onClick(b -> {
                    player.getRewardBuilder().setCommands(new ArrayList<>());
                    player.getRewardBuilder().openRewardBuilder(player);
                })
                .build();

        GooeyButton addCommand = GooeyButton.builder()
                .title(Util.formattedString("&aAdd Command"))
                .display(new ItemStack(Items.WRITABLE_BOOK))
                .onClick(b ->
                {
                    UIManager.closeUI(b.getPlayer());
                    Scheduling.schedule(2, scheduledTask -> dialogueInputScreenBuilder(BuilderAction.commands, player).sendTo(b.getPlayer()), false);
                })
                .build();

        GooeyButton removeCommand = GooeyButton.builder()
                .title(Util.formattedString("&aRemove Command"))
                .display(new ItemStack(Items.WRITTEN_BOOK))
                .onClick(b ->
                {
                    UIManager.openUIForcefully(b.getPlayer(), RemoveCommandMenu(player));
                })
                .lore(Util.formattedArrayList(Arrays.asList("&7Click to remove a specific command option")))
                .build();

        builder.set(1, 1, goBack);
        builder.set(1, 3, clearCommands);
        builder.set(1, 5, addCommand);
        builder.set(1, 7, removeCommand);

        return GooeyPage.builder().template(builder.build()).title(Util.formattedString("&bEdit Commands")).build();
    }

    public List<Button> loreButtonList(Player player)
    {
        List<Button> buttons = new ArrayList<>();
        for (String s:player.getRewardBuilder().reward.getDisplayLore()) {
            GooeyButton button = GooeyButton.builder()
                    .title(Util.formattedString("&aClick to remove the following option:"))
                    .lore(Util.formattedArrayList(Arrays.asList(s)))
                    .display(new ItemStack(Items.PAPER))
                    .onClick(b ->
                    {
                        player.getRewardBuilder().reward.getDisplayLore().remove(s);
                        player.getRewardBuilder().openRewardBuilder(player);
                    })
                    .build();
            buttons.add(button);
        }
        return buttons;
    }

    public LinkedPage RemoveLoreMenu(Player player)
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

        GooeyButton goBack = GooeyButton.builder()
                .title(Util.formattedString("&cGo Back"))
                .lore(Util.formattedArrayList(Arrays.asList("&7Click to go back to the previous menu!")))
                .display(new ItemStack(Items.ARROW))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), LoreEditingMenu(player));
                })
                .build();

        Template template;
        if (loreButtonList(player).size() > 8) {
            template = ChestTemplate.builder(6)
                    .border(0, 0, 6, 9, filler)
                    .set(0, 1, goBack)
                    .set(0, 3, previous)
                    .set(0, 5, next)
                    .rectangle(1, 1, 4, 7, placeHolderButton)
                    .build();
        } else {
            template = ChestTemplate.builder(3)
                    .border(0, 0, 3, 9, filler)
                    .row(1, placeHolderButton)
                    .set(0, 1, goBack)
                    .build();
        }

        return PaginationHelper.createPagesFromPlaceholders(template, loreButtonList(player), LinkedPage.builder().title(Util.formattedString("&bEdit Lore")).template(template));
    }

    public GooeyPage LoreEditingMenu(Player player)
    {
        ChestTemplate.Builder builder = ChestTemplate.builder(4);
        builder.fill(filler);

        GooeyButton goBack = GooeyButton.builder()
                .title(Util.formattedString("&cGo Back"))
                .lore(Util.formattedArrayList(Arrays.asList("&7Click to go back to the previous menu!")))
                .display(new ItemStack(Items.ARROW))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), BuilderMenu(player));
                })
                .build();

        GooeyButton clearLore = GooeyButton.builder()
                .title(Util.formattedString("&aClear Lore"))
                .display(new ItemStack(Items.BOOK))
                .onClick(b -> {
                    player.getRewardBuilder().setLore(new ArrayList<>());
                    player.getRewardBuilder().openRewardBuilder(player);
                })
                .build();

        GooeyButton addLore = GooeyButton.builder()
                .title(Util.formattedString("&aAdd Lore"))
                .display(new ItemStack(Items.WRITABLE_BOOK))
                .onClick(b ->
                {
                    UIManager.closeUI(b.getPlayer());
                    Scheduling.schedule(2, scheduledTask -> dialogueInputScreenBuilder(BuilderAction.lore, player).sendTo(b.getPlayer()), false);
                })
                .build();

        GooeyButton removeLore = GooeyButton.builder()
                .title(Util.formattedString("&aRemove Lore"))
                .display(new ItemStack(Items.WRITTEN_BOOK))
                .onClick(b ->
                {
                    UIManager.openUIForcefully(b.getPlayer(), RemoveLoreMenu(player));
                })
                .lore(Util.formattedArrayList(Arrays.asList("&7Click to remove a specific lore option")))
                .build();

        builder.set(1, 1, goBack);
        builder.set(1, 3, clearLore);
        builder.set(1, 5, addLore);
        builder.set(1, 7, removeLore);

        return GooeyPage.builder().template(builder.build()).title(Util.formattedString("&bEdit Lore")).build();
    }

    public GooeyPage BuilderFinishMenu(Player player) {
        ChestTemplate.Builder builder = ChestTemplate.builder(4);
        builder.fill(filler);

        GooeyButton goBack = GooeyButton.builder()
                .title(Util.formattedString("&cGo Back"))
                .lore(Util.formattedArrayList(Arrays.asList("&7Click to go back to the previous menu!")))
                .display(new ItemStack(Items.ARROW))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), BuilderMenu(player));
                })
                .build();

        GooeyButton finishNoSave = GooeyButton.builder()
                .title(Util.formattedString("&aFinish"))
                .lore(Util.formattedArrayList(Arrays.asList("&7Finish building")))
                .display(new ItemStack(Items.MAP))
                .onClick(b ->
                {
                    UIManager.closeUI(b.getPlayer());
                    //send to wherever they need to be
                })
                .build();

        GooeyButton saveToConfig = GooeyButton.builder()
                .title(Util.formattedString("&aSave To Config"))
                .onClick(b -> {
                    Clandorus.rewardRegistry.saveToConfig(buildReward());
                })
                .display(new ItemStack(Items.LIME_WOOL))
                .build();

        builder.set(1, 2, goBack);
        builder.set(1, 4, finishNoSave);
        builder.set(1, 6, saveToConfig);

        return GooeyPage.builder().template(builder.build()).title(Util.formattedString("&bFinish Actions")).build();
    }

    public List<Button> itemButtonList(Player player)
    {
        List<Button> buttons = new ArrayList<>();
        for (Item i: ForgeRegistries.ITEMS.getValues()) {
            ItemStack stack = new ItemStack(i);
            List<String> lore = new ArrayList<>();
            lore.add("&7Click to select %itemname%".replace("%itemname%", Util.getItemStackName(stack)));
            GooeyButton button = GooeyButton.builder()
                    .title(Util.formattedString(Util.getItemStackName(stack)))
                    .lore(Util.formattedArrayList(lore))
                    .display(stack)
                    .onClick(b ->
                    {
                        player.getRewardBuilder().setItemStack(stack);
                        player.getRewardBuilder().openRewardBuilder(player);
                    })
                    .build();
            buttons.add(button);
        }
        return buttons;
    }

    public LinkedPage ItemSelectionMenu(Player player)
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
        if (itemButtonList(player).size() > 8) {
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

        return PaginationHelper.createPagesFromPlaceholders(template, itemButtonList(player), LinkedPage.builder().title(Util.formattedString("&bItem Selection")).template(template));
    }

    public GooeyPage ItemDisplayAction(Player player)
    {
        ChestTemplate.Builder builder = ChestTemplate.builder(4);
        builder.fill(filler);


        GooeyButton menu = GooeyButton.builder()
                .title(Util.formattedString("&aFrom Menu"))
                .lore(Util.formattedArrayList(Arrays.asList("&aBuild the reward based on a navigable menu with different items!")))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), ItemSelectionMenu(player));
                })
                .display(new ItemStack(Items.LIME_WOOL))
                .build();

        GooeyButton fromHand = GooeyButton.builder()
                .title(Util.formattedString("&aFrom Hand"))
                .lore(Util.formattedArrayList(Arrays.asList("&7Build the reward based on the current item in your main hand!")))
                .onClick(b -> {
                    player.getRewardBuilder().setItemStack(b.getPlayer().inventory.getCurrentItem());
                    UIManager.openUIForcefully(b.getPlayer(), BuilderMenu(player));
                })
                .display(new ItemStack(Items.LIME_WOOL))
                .build();

        GooeyButton stringInput = GooeyButton.builder()
                .title(Util.formattedString("&aText Input"))
                .lore(Util.formattedArrayList(Arrays.asList("&7Build the reward based on text input")))
                .onClick(b -> {
                    UIManager.closeUI(b.getPlayer());
                    Scheduling.schedule(2, scheduledTask -> dialogueInputScreenBuilder(BuilderAction.item, player).sendTo(b.getPlayer()), false);
                })
                .display(new ItemStack(Items.LIME_WOOL))
                .build();

        builder.set(1, 2, menu);
        builder.set(1, 4, fromHand);
        builder.set(1, 6, stringInput);

        return GooeyPage.builder().template(builder.build()).title(Util.formattedString("&bItem Display")).build();
    }

    public GooeyPage BuilderMenu(Player player)
    {
        this.reward = player.getRewardBuilder().reward;
        ChestTemplate.Builder builder = ChestTemplate.builder(4);
        builder.fill(filler);

        GooeyButton displayItemStack;

        if (this.reward.getDisplayItem() == null || this.reward.getDisplayItem().isEmpty())
            displayItemStack = GooeyButton.builder()
                    .display(new ItemStack(Items.BARRIER))
                    .title(Util.formattedString("&cSelect an ItemStack"))
                    .onClick(b ->
                    {
                        UIManager.openUIForcefully(b.getPlayer(), ItemDisplayAction(player));
                    })
                    .build();
        else displayItemStack = GooeyButton.builder()
                .display(this.reward.getDisplayItem())
                .title(Util.formattedString("&aItem Selected"))
                .lore(Util.formattedArrayList(Arrays.asList(
                        "&7You've selected %item% to be your Reward Display Item"
                                .replace("%item%", Util.getItemStackName(this.reward.getDisplayItem())),
                        "&aYou can select another item by clicking this button!"
                )))
                .onClick(b ->
                {
                    UIManager.openUIForcefully(b.getPlayer(), ItemDisplayAction(player));
                })
                .build();

        GooeyButton lore;

        if (this.reward.getDisplayLore() == null || this.reward.getDisplayLore().isEmpty())
        {
            lore = GooeyButton.builder()
                    .title(Util.formattedString("&cSet up Lore"))
                    .display(new ItemStack(Items.BARRIER))
                    .onClick(b -> {
                        UIManager.openUIForcefully(b.getPlayer(), LoreEditingMenu(player));
                    })
                    .build();
        } else {
            List<String> buttonLore = new ArrayList<>();
            buttonLore.add("&aYou've set up the following display lore for the Reward:");
            buttonLore.addAll(this.reward.getDisplayLore());
            lore = GooeyButton.builder()
                    .title(Util.formattedString("&aLore Set Up"))
                    .display(new ItemStack(Items.WRITTEN_BOOK))
                    .onClick(b -> {
                        UIManager.openUIForcefully(b.getPlayer(), LoreEditingMenu(player));
                    })
                    .lore(Util.formattedArrayList(buttonLore))
                    .build();
        }

        GooeyButton title;

        if (this.reward.getDisplayTitle() == null || this.reward.getDisplayTitle().isEmpty())
        {
            title = GooeyButton.builder()
                    .title(Util.formattedString("&cSet up Title"))
                    .display(new ItemStack(Items.BARRIER))
                    .onClick(b ->
                    {
                        UIManager.closeUI(b.getPlayer());
                        Scheduling.schedule(2, scheduledTask -> dialogueInputScreenBuilder(BuilderAction.title, player).sendTo(b.getPlayer()), false);
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
                        Scheduling.schedule(2, scheduledTask -> dialogueInputScreenBuilder(BuilderAction.title, player).sendTo(b.getPlayer()), false);
                    })
                    .lore(Util.formattedArrayList(Arrays.asList("&aYou've set up the following title for the display: %title%"
                            .replace("%title%", this.reward.getDisplayTitle()))))
                    .build();
        }

        GooeyButton rewards;
        if (this.reward.getCommandList() == null || this.reward.getCommandList().isEmpty())
        {
            rewards = GooeyButton.builder()
                    .title(Util.formattedString("&cCommands have not been set up!"))
                    .onClick(b ->
                    {
                        UIManager.openUIForcefully(b.getPlayer(), CommandEditingMenu(player));
                    })
                    .display(new ItemStack(Items.BARRIER))
                    .build();
        } else {
            List<String> loreList = new ArrayList<>();
            loreList.add("&aYou've set up the following commands:");
            loreList.addAll(this.reward.getCommandList());

            rewards = GooeyButton.builder()
                    .title(Util.formattedString("&aConfigured Commands!"))
                    .display(new ItemStack(Items.COMMAND_BLOCK))
                    .lore(Util.formattedArrayList(loreList))
                    .onClick(b ->
                    {
                        UIManager.openUIForcefully(b.getPlayer(), CommandEditingMenu(player));
                    })
                    .build();
        }

        GooeyButton identifier;

        if (this.reward.isConfigBased())
        {
            if (this.reward.getIdentifier() == null || this.reward.getIdentifier().isEmpty())
            {
                identifier = GooeyButton.builder()
                        .title(Util.formattedString("&cIdentifier has not been set up!"))
                        .onClick(b ->
                        {
                            UIManager.closeUI(b.getPlayer());
                            Scheduling.schedule(2, scheduledTask -> dialogueInputScreenBuilder(BuilderAction.identifier, player).sendTo(b.getPlayer()), false);
                        })
                        .display(new ItemStack(Items.BARRIER))
                        .build();
            } else {
                List<String> idArray = new ArrayList<>();
                idArray.add("&7Your reward will be stored under the following id:");
                idArray.add("&b%identifier%".replace("%identifier%", this.reward.getIdentifier()));
                identifier = GooeyButton.builder()
                        .title(Util.formattedString("&aIdentifier has been set up!"))
                        .onClick(b ->
                        {
                            UIManager.closeUI(b.getPlayer());
                            Scheduling.schedule(2, scheduledTask -> dialogueInputScreenBuilder(BuilderAction.identifier, player).sendTo(b.getPlayer()), false);
                        })
                        .lore(Util.formattedArrayList(idArray))
                        .display(new ItemStack(Items.MAP))
                        .build();
            }
        } else identifier = filler;

        GooeyButton finishBuilder = GooeyButton.builder()
                .title(Util.formattedString("&aFinish Building"))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), BuilderFinishMenu(player));
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

        if (this.reward.isConfigBased())
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
