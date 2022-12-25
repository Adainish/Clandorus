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
import com.mojang.authlib.GameProfile;
import com.pixelmonmod.pixelmon.api.dialogue.DialogueInputScreen;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import com.pixelmonmod.pixelmon.api.util.Scheduling;
import io.github.adainish.clandorus.Clandorus;
import io.github.adainish.clandorus.enumeration.MailAction;
import io.github.adainish.clandorus.enumeration.MailSender;
import io.github.adainish.clandorus.obj.Player;
import io.github.adainish.clandorus.obj.clan.Clan;
import io.github.adainish.clandorus.obj.mail.Mail;
import io.github.adainish.clandorus.obj.mail.Reward;
import io.github.adainish.clandorus.registry.RewardRegistry;
import io.github.adainish.clandorus.storage.ClanStorage;
import io.github.adainish.clandorus.storage.PlayerStorage;
import io.github.adainish.clandorus.util.PermissionUtil;
import io.github.adainish.clandorus.util.Util;
import io.github.adainish.clandorus.wrapper.PermissionWrapper;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static io.github.adainish.clandorus.util.Util.filler;

public class MailBuilder {

    private Mail mail;

    private MailAction mailAction;

    private boolean toClan;

    private List<UUID> clanList = new ArrayList<>();

    private List<UUID> playerList = new ArrayList<>();

    public MailBuilder() {
        this.mail = new Mail();
    }

    public boolean canAddRewards() {
        switch (mail.getSender()) {
            case Server:
                return true;
            case Player: {
                Player p = PlayerStorage.getPlayer(mail.getSenderUUID());
                return PermissionUtil.checkPerm(p.getServerEntity(), PermissionWrapper.addRewardToMailPermission);
            }
            case Clan:
                return false;
        }
        return false;
    }

    public void openNewMailBuilder(ServerPlayerEntity player) {
        Player clanPlayer = PlayerStorage.getPlayer(player.getUniqueID());
        if (clanPlayer != null) {
            mail.setSender(MailSender.Player);
            mail.setSenderUUID(clanPlayer.getUuid());
            clanPlayer.setMailBuilder(this);
            UIManager.openUIForcefully(player, MailBuilderMenu(clanPlayer));
        } else {
            Util.send(player, "&cSomething went wrong opening the mail builder! Does your player data exist for Clandorus?");
        }
    }

    public void openMailBuilder(Player player) {
        UIManager.openUIForcefully(player.getServerEntity(), MailBuilderMenu(player));
    }

    public boolean finishedBuilding() {
        if (mail.getMessage() == null || mail.getMessage().isEmpty())
            return false;

        return !playerList.isEmpty() || !clanList.isEmpty();

    }

    public void sendMail() {
        List<UUID> markedUUIDS = new ArrayList<>();
        if (!clanList.isEmpty()) {
            for (UUID uuid : clanList) {
                Clan clan = ClanStorage.getClan(uuid);
                if (clan != null) {
                    for (Player p : clan.memberPlayerData()) {
                        p.initialiseNull();
                        p.getMailBox().mailList.add(0, mail);
                        p.savePlayer();
                        markedUUIDS.add(p.getUuid());
                    }
                }
            }
        }
        if (!playerList.isEmpty()) {
            for (UUID uuid : playerList) {
                if (markedUUIDS.contains(uuid))
                    continue;
                Player p = PlayerStorage.getPlayer(uuid);
                if (p != null) {
                    p.initialiseNull();
                    p.getMailBox().mailList.add(0, mail);
                    p.savePlayer();
                }
            }
        }
    }

    public List<Button> selectedRewards(Player player) {
        List<Button> buttons = new ArrayList<>();

        for (Reward r : player.getMailBuilder().getMail().getRewardList()) {
            List<String> lore = new ArrayList<>();
            lore.add("&7Click to remove this reward");
            lore.addAll(r.getDisplayLore());
            GooeyButton button = GooeyButton.builder()
                    .title(Util.formattedString(r.getDisplayTitle()))
                    .lore(Util.formattedArrayList(lore))
                    .display(r.getDisplayItem())
                    .onClick(b ->
                    {
                        player.getMailBuilder().getMail().getRewardList().remove(r);
                        player.getMailBuilder().openMailBuilder(player);
                    })
                    .build();
            buttons.add(button);
        }

        return buttons;
    }


    public List<Button> selectableRewards(Player player)
    {
        List<Button> buttons = new ArrayList<>();

        for (Reward r : Clandorus.rewardRegistry.rewardCache.values()) {
            GooeyButton button = GooeyButton.builder()
                    .title(Util.formattedString(r.getDisplayTitle()))
                    .lore(Util.formattedArrayList(r.getDisplayLore()))
                    .display(r.getDisplayItem())
                    .onClick(b ->
                    {
                        player.getMailBuilder().getMail().getRewardList().add(r);
                        player.getMailBuilder().openMailBuilder(player);
                    })
                    .build();
            buttons.add(button);
        }

        return buttons;
    }

    public List<Button> viewableRewards(Player player)
    {
        List<Button> buttons = new ArrayList<>();

        for (Reward r : player.getMailBuilder().getMail().getRewardList()) {
            GooeyButton button = GooeyButton.builder()
                    .title(Util.formattedString(r.getDisplayTitle()))
                    .lore(Util.formattedArrayList(r.getDisplayLore()))
                    .display(r.getDisplayItem())
                    .onClick(b ->
                    {
                        player.getMailBuilder().openMailBuilder(player);
                    })
                    .build();
            buttons.add(button);
        }

        return buttons;
    }

    public List<Button> selectedClanButtons(Player player) {
        List<Button> buttons = new ArrayList<>();
        for (UUID uuid : clanList) {
            Clan c = ClanStorage.getClan(uuid);
            if (c != null) {
                GooeyButton button = GooeyButton.builder()
                        .title(Util.formattedString(c.getClanName()))
                        .onClick(b ->
                        {
                            player.getMailBuilder().clanList.remove(c.getClanIdentifier());
                            player.getMailBuilder().openMailBuilder(player);
                        })
                        .lore(Util.formattedArrayList(Arrays.asList("&7Remove this clan from the selected recipient list")))
                        .display(new ItemStack(Items.BLACK_BANNER))
                        .build();
                buttons.add(button);
            }
        }
        return buttons;
    }

    public List<Button> selectedPlayerButtons(Player player) {
        List<Button> buttons = new ArrayList<>();

        for (UUID uuid : playerList) {
            Player p = PlayerStorage.getPlayer(uuid);
            if (p != null) {
                Item playerSkull = Items.PLAYER_HEAD;
                CompoundNBT nbt = new CompoundNBT();
                ItemStack skullStack;
                GameProfile gameprofile = Clandorus.getServer().getPlayerProfileCache().getProfileByUUID(p.getUuid());
                if (gameprofile != null) {
                    nbt.put("SkullOwner", NBTUtil.writeGameProfile(new CompoundNBT(), gameprofile));
                    skullStack = new ItemStack(playerSkull);
                    skullStack.setTag(nbt);
                } else skullStack = new ItemStack(Items.SKELETON_SKULL);

                GooeyButton button = GooeyButton.builder()
                        .display(skullStack)
                        .lore(Util.formattedArrayList(Arrays.asList("&7Click to remove this player from the selected list of recipients")))
                        .onClick(b ->
                        {
                            player.getMailBuilder().playerList.remove(p.getUuid());
                            player.getMailBuilder().openMailBuilder(player);
                        })
                        .title(Util.formattedString("&6" + p.getName()))
                        .build();
                buttons.add(button);
            }
        }

        return buttons;
    }

    public List<Button> clanDataButtons(Player player) {
        List<Button> buttons = new ArrayList<>();
        for (Clan c : ClanStorage.getAllClans()) {

            GooeyButton button = GooeyButton.builder()
                    .display(new ItemStack(Items.BLACK_BANNER))
                    .onClick(b ->
                    {
                        player.getMailBuilder().clanList.add(c.getClanIdentifier());
                        player.getMailBuilder().openMailBuilder(player);
                    })
                    .title(Util.formattedString("&6" + c.getClanName()))
                    .build();
            buttons.add(button);
        }
        return buttons;
    }

    public List<Button> playerDataButtons(Player player) {
        List<Button> buttons = new ArrayList<>();

        for (Player p : PlayerStorage.getAllPlayers()) {
            Item playerSkull = Items.PLAYER_HEAD;
            CompoundNBT nbt = new CompoundNBT();
            ItemStack skullStack;
            GameProfile gameprofile = Clandorus.getServer().getPlayerProfileCache().getProfileByUUID(p.getUuid());

            if (gameprofile != null) {
                nbt.put("SkullOwner", NBTUtil.writeGameProfile(new CompoundNBT(), gameprofile));
                skullStack = new ItemStack(playerSkull);
                skullStack.setTag(nbt);
            } else skullStack = new ItemStack(Items.SKELETON_SKULL);

            GooeyButton button = GooeyButton.builder()
                    .display(skullStack)
                    .onClick(b ->
                    {
                        player.getMailBuilder().playerList.add(p.getUuid());
                        player.getMailBuilder().openMailBuilder(player);
                    })
                    .title(Util.formattedString("&6" + p.getName()))
                    .build();

            buttons.add(button);
        }

        return buttons;
    }

    public LinkedPage RemoveClanSelectPage(Player player) {
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

        if (selectedClanButtons(player).size() > 8) {
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

        return PaginationHelper.createPagesFromPlaceholders(template, selectedClanButtons(player), LinkedPage.builder().title(Util.formattedString("&cRemove Recipients")).template(template));
    }

    public LinkedPage RemovePlayerSelectPage(Player player) {
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

        if (selectedPlayerButtons(player).size() > 8) {
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

        return PaginationHelper.createPagesFromPlaceholders(template, selectedPlayerButtons(player), LinkedPage.builder().title(Util.formattedString("&cRemove Recipients")).template(template));
    }

    public GooeyPage ManageClansPage(Player player) {
        ChestTemplate.Builder builder = ChestTemplate.builder(4);
        builder.fill(filler);

        GooeyButton fromAllClans = GooeyButton.builder()
                .title(Util.formattedString("&6From All Clans"))
                .lore(Util.formattedArrayList(Arrays.asList("&7Select a clan from all clans")))
                .display(new ItemStack(Items.SKELETON_SKULL))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), LinkedClanSelectPage(player));
                })
                .build();

        GooeyButton allClans = GooeyButton.builder()
                .title(Util.formattedString("&6Select Every Clan"))
                .display(new ItemStack(PixelmonItems.trainer_editor))
                .onClick(b ->
                {
                    player.getMailBuilder().clanList.clear();
                    player.getMailBuilder().clanList.addAll(ClanStorage.getAllClanUUIDS());
                    player.getMailBuilder().openMailBuilder(player);
                })
                .build();

        GooeyButton clearClans = GooeyButton.builder()
                .title(Util.formattedString("&cDelete Selected Clans"))
                .onClick(b -> {
                    player.getMailBuilder().clanList.clear();
                    player.getMailBuilder().openMailBuilder(player);
                })
                .display(new ItemStack(Items.BOOK))
                .build();

        GooeyButton removeSpecificClan = GooeyButton.builder()
                .title(Util.formattedString("&cRemove Specific Clan"))
                .lore(Util.formattedArrayList(Arrays.asList("&7Remove a specific Clan from the receiver list")))
                .display(new ItemStack(Items.NAME_TAG))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), RemoveClanSelectPage(player));
                })
                .build();

        builder.set(1, 1, fromAllClans);
        builder.set(1, 3, allClans);
        builder.set(1, 5, clearClans);
        builder.set(1, 7, removeSpecificClan);
        return GooeyPage.builder().template(builder.build()).title(Util.formattedString("&bManage Mail Receivers")).build();
    }

    public GooeyPage ManagePlayersPage(Player player) {
        ChestTemplate.Builder builder = ChestTemplate.builder(4);
        builder.fill(filler);

        GooeyButton fromAllPlayers = GooeyButton.builder()
                .title(Util.formattedString("&6From All Players"))
                .lore(Util.formattedArrayList(Arrays.asList("&7Select a player from all players")))
                .display(new ItemStack(Items.SKELETON_SKULL))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), LinkedPlayerSelectPage(player));
                })
                .build();

        GooeyButton allPlayers = GooeyButton.builder()
                .title(Util.formattedString("&6Select Every Player"))
                .display(new ItemStack(PixelmonItems.trainer_editor))
                .onClick(b ->
                {
                    player.getMailBuilder().playerList.clear();
                    player.getMailBuilder().playerList.addAll(PlayerStorage.getAllPlayerUUIDS());
                    player.getMailBuilder().openMailBuilder(player);
                })
                .build();

        GooeyButton clearPlayers = GooeyButton.builder()
                .title(Util.formattedString("&cDelete Selected Players"))
                .onClick(b -> {
                    player.getMailBuilder().playerList.clear();
                    player.getMailBuilder().openMailBuilder(player);
                })
                .display(new ItemStack(Items.BOOK))
                .build();

        GooeyButton removeSpecificPlayer = GooeyButton.builder()
                .title(Util.formattedString("&cRemove Specific Player"))
                .lore(Util.formattedArrayList(Arrays.asList("&7Remove a specific player from the receiver list")))
                .display(new ItemStack(Items.NAME_TAG))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), RemovePlayerSelectPage(player));
                })
                .build();

        builder.set(1, 1, fromAllPlayers);
        builder.set(1, 3, allPlayers);
        builder.set(1, 5, clearPlayers);
        builder.set(1, 7, removeSpecificPlayer);
        return GooeyPage.builder().template(builder.build()).title(Util.formattedString("&bManage Mail Receivers")).build();
    }

    public void updateMailBuilderCache(Player player) {
        player.setMailBuilder(this);
        player.updateCache();
    }

    public DialogueInputScreen.Builder dialogueInputScreenBuilder(MailAction action, Player player) {

        DialogueInputScreen.Builder builder = new DialogueInputScreen.Builder();
        builder.setShouldCloseOnEsc(false);
        switch (action) {
            case setting_mail_text: {
                builder.setTitle(Util.formattedString("&bMessage"));
                builder.setText(Util.formattedString("&7Type the Message you want to send!"));
                break;
            }
            case none: {
                break;
            }
        }
        setMailAction(action);
        updateMailBuilderCache(player);
        return builder;
    }

    public LinkedPage LinkedPlayerSelectPage(Player player) {
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

        if (playerDataButtons(player).size() > 8) {
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

        return PaginationHelper.createPagesFromPlaceholders(template, playerDataButtons(player), LinkedPage.builder().title(Util.formattedString("&6Players")).template(template));
    }

    public LinkedPage LinkedClanSelectPage(Player player) {
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

        if (clanDataButtons(player).size() > 8) {
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

        return PaginationHelper.createPagesFromPlaceholders(
                template,
                clanDataButtons(player),
                LinkedPage
                        .builder()
                        .title(Util.formattedString("&6Clans"))
                        .template(template));
    }

    public GooeyPage MaiLReceiverManageMenu(Player player) {
        ChestTemplate.Builder builder = ChestTemplate.builder(3);
        builder.fill(filler);

        GooeyButton clanSelection = GooeyButton.builder()
                .title(Util.formattedString("&6Manage Receiving Clans"))
                .display(new ItemStack(PixelmonItems.red_flute))
                .onClick(b ->
                {
                    UIManager.openUIForcefully(b.getPlayer(), ManageClansPage(player));
                })
                .build();

        GooeyButton playerSelection = GooeyButton.builder()
                .title(Util.formattedString("&6Manage Receiving Players"))
                .display(new ItemStack(Items.PLAYER_HEAD))
                .onClick(b ->
                {
                    UIManager.openUIForcefully(b.getPlayer(), ManagePlayersPage(player));
                })
                .build();


        builder.set(1, 3, clanSelection);
        builder.set(1, 5, playerSelection);

        return GooeyPage.builder().template(builder.build()).title(Util.formattedString("&bManage Mail Receivers")).build();
    }

    public LinkedPage RemoveSelectedRewardsMenu(Player player)
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

        if (selectedRewards(player).size() > 8) {
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

        return PaginationHelper.createPagesFromPlaceholders(
                template,
                selectedRewards(player),
                LinkedPage
                        .builder()
                        .title(Util.formattedString("&6Remove Rewards"))
                        .template(template));
    }

    public LinkedPage SelectableRewardsMenu(Player player)
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

        if (selectableRewards(player).size() > 8) {
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

        return PaginationHelper.createPagesFromPlaceholders(
                template,
                selectableRewards(player),
                LinkedPage
                        .builder()
                        .title(Util.formattedString("&6Selectable Rewards"))
                        .template(template));
    }

    public LinkedPage ViewableRewardsMenu(Player player)
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

        if (viewableRewards(player).size() > 8) {
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

        return PaginationHelper.createPagesFromPlaceholders(
                template,
                viewableRewards(player),
                LinkedPage
                        .builder()
                        .title(Util.formattedString("&6Viewable Rewards"))
                        .template(template));
    }

    public GooeyPage RewardManageMenu(Player player)
    {
        ChestTemplate.Builder builder = ChestTemplate.builder(3);
        builder.fill(filler);

        GooeyButton clearAll = GooeyButton.builder()
                .display(new ItemStack(Items.LAVA_BUCKET))
                .onClick(b ->
                {
                    player.getMailBuilder().getMail().getRewardList().clear();
                    player.getMailBuilder().openMailBuilder(player);
                })
                .lore(Util.formattedArrayList(Arrays.asList("&7Click to remove &4&lall &7Rewards from the mail")))
                .title(Util.formattedString("&6Remove All"))
                .build();

        GooeyButton selectRewards = GooeyButton.builder()
                .display(new ItemStack(Items.BOOK))
                .title(Util.formattedString("&6Select Reward"))
                .lore(Util.formattedArrayList(Arrays.asList("&7Click to select a Reward from the Reward Registry")))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), SelectableRewardsMenu(player));
                })
                .build();


        GooeyButton viewRewards = GooeyButton.builder()
                .display(new ItemStack(Items.BOOK))
                .title(Util.formattedString("&6View Rewards"))
                .lore(Util.formattedArrayList(Arrays.asList("&7Click to view all rewards you've selected for this mail")))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), ViewableRewardsMenu(player));
                })
                .build();


        GooeyButton removeReward = GooeyButton.builder()
                .display(new ItemStack(Items.BOOK))
                .title(Util.formattedString("&6Remove Reward"))
                .lore(Util.formattedArrayList(Arrays.asList("&7Click to remove a Reward you've selected for this mail")))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), RemoveSelectedRewardsMenu(player));
                })
                .build();

        GooeyButton makeReward = GooeyButton.builder()
                .display(new ItemStack(Items.BOOK))
                .title(Util.formattedString("&6Build a Reward"))
                .lore(Util.formattedArrayList(Arrays.asList("&7Click to build a new reward and add it to the Mail")))
                .onClick(b -> {
                    player.setRewardBuilder(new RewardBuilder());
                    player.getRewardBuilder().openNewRewardBuilder(b.getPlayer());
                })
                .build();


        builder.set(1, 0, viewRewards);
        builder.set(1, 2, makeReward);
        builder.set(1, 4, selectRewards);
        builder.set(1, 6, removeReward);
        builder.set(1, 8, clearAll);

        return GooeyPage.builder().template(builder.build()).title(Util.formattedString("&6Manage Rewards")).build();
    }

    public GooeyPage MailBuilderMenu(Player player)
    {
        ChestTemplate.Builder builder = ChestTemplate.builder(4);
        builder.fill(filler);

        this.mail = player.getMailBuilder().getMail();

        GooeyButton send = GooeyButton.builder()

                .title(Util.formattedString("&aSend Mail"))
                .display(new ItemStack(Items.LIME_WOOL))
                .onClick(b ->
                {
                    sendMail();
                    UIManager.closeUI(b.getPlayer());
                })
                .build();

        if (!finishedBuilding())
            send = GooeyButton.builder()
                    .title(Util.formattedString("&cNot done"))
                    .display(new ItemStack(Items.RED_WOOL))
                    .lore(Util.formattedArrayList(Arrays.asList("&cThere are a few things you need to do before you can send this mail")))
                    .build();

        GooeyButton manageReceivers = GooeyButton.builder()
                .title(Util.formattedString("&5Receivers"))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), MaiLReceiverManageMenu(player));
                })
                .display(new ItemStack(Items.NAME_TAG))
                .build();

        GooeyButton manageRewards = GooeyButton.builder()
                .title(Util.formattedString("&bManage Rewards"))
                .display(new ItemStack(PixelmonItems.gift_box))
                .lore(Util.formattedArrayList(Arrays.asList("&7Click to manage the rewards!")))
                .onClick(b ->
                {
                    UIManager.openUIForcefully(b.getPlayer(), RewardManageMenu(player));
                    //options include build new rewards, view selected rewards, Delete all Selected Rewards, delete optional rewards, select rewards from menu
                })
                .build();

        String activeMessage = "";
        if (mail.getMessage() != null && !mail.getMessage().isEmpty())
            activeMessage = mail.getMessage();
        GooeyButton messageButton = GooeyButton.builder()
                .title(Util.formattedString("&6Message"))
                .lore(Util.formattedArrayList(Arrays.asList("&7Set the message for this mail:", "&7The current message is: ", activeMessage)))
                .onClick(b ->
                {
                    UIManager.closeUI(b.getPlayer());
                    Scheduling.schedule(2, scheduledTask -> dialogueInputScreenBuilder(MailAction.setting_mail_text, player).sendTo(b.getPlayer()), false);
                })
                .display(new ItemStack(Items.WRITABLE_BOOK))
                .build();

        if (canAddRewards())
            builder.set(1, 1, manageRewards);

        builder.set(1, 3, messageButton);
        builder.set(1, 5, manageReceivers);
        builder.set(1, 7, send);

        return GooeyPage.builder().template(builder.build()).title(Util.formattedString("&bMail Builder")).build();
    }


    public Mail getMail() {
        return mail;
    }

    public void setMail(Mail mail) {
        this.mail = mail;
    }

    public MailAction getMailAction() {
        return mailAction;
    }

    public void setMailAction(MailAction mailAction) {
        this.mailAction = mailAction;
    }

    public boolean isToClan() {
        return toClan;
    }

    public void setToClan(boolean toClan) {
        this.toClan = toClan;
    }

    public List<UUID> getClanList() {
        return clanList;
    }

    public void setClanList(List<UUID> clanList) {
        this.clanList = clanList;
    }

    public List<UUID> getPlayerList() {
        return playerList;
    }

    public void setPlayerList(List<UUID> playerList) {
        this.playerList = playerList;
    }
}
