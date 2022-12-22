package io.github.adainish.clandorus.obj.clan;

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
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.api.util.helpers.SpriteItemHelper;
import io.github.adainish.clandorus.Clandorus;
import io.github.adainish.clandorus.api.ClanEvent;
import io.github.adainish.clandorus.conf.LanguageConfig;
import io.github.adainish.clandorus.enumeration.Roles;
import io.github.adainish.clandorus.obj.*;
import io.github.adainish.clandorus.obj.clan.data.*;
import io.github.adainish.clandorus.storage.ClanStorage;
import io.github.adainish.clandorus.storage.PlayerStorage;
import io.github.adainish.clandorus.util.Util;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraftforge.common.MinecraftForge;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static io.github.adainish.clandorus.util.Util.filler;

public class Clan {
    private String clanName;
    private UUID clanIdentifier;
    private UUID leaderUUID;
    private List<UUID> clanMembers = new ArrayList<>();
    private Role adminRole = new Role();
    private Role gruntsRole = new Role();

    private AuditLog auditLog;
    private int elo = 0;

    private ClanPokemonStorage pokemonStorage;
    private ClanItemStorage itemStorage;
    private Bank clanBank;

    private ClanChat clanChat;


    public Clan(Player leader) {
        setLeaderUUID(leader.getUuid());
        setClanIdentifier(UUID.randomUUID());
        getAdminRole().setRole(Roles.Admin);
        getGruntsRole().setRole(Roles.Grunt);
        setItemStorage(new ClanItemStorage());
        setPokemonStorage(new ClanPokemonStorage());
        setClanBank(new Bank());
    }

    public Clan(Player leader, String name) {
        setLeaderUUID(leader.getUuid());
        setClanIdentifier(UUID.randomUUID());
        getAdminRole().setRole(Roles.Admin);
        getGruntsRole().setRole(Roles.Grunt);
        clanMembers.add(leaderUUID);
        setClanName(name);
        setItemStorage(new ClanItemStorage());
        setPokemonStorage(new ClanPokemonStorage());
        setClanBank(new Bank());
    }

    public void disband() {
        for (UUID uuid : clanMembers) {
            Player player = PlayerStorage.getPlayer(uuid);
            if (player == null)
                continue;
            player.setClanID(null);
            player.savePlayer();
        }
        ClanStorage.saveClan(clanIdentifier);
    }

    public List<ServerPlayerEntity> getOnlineMembers() {
        List<ServerPlayerEntity> playerEntities = new ArrayList<>();
        for (UUID uuid : clanMembers) {
            if (Util.isPlayerOnline(uuid))
                playerEntities.add(Util.getPlayer(uuid));
        }
        return playerEntities;
    }

    public void addMemberToClan(Player player) {
        if (getClanMembers().contains(player.getUuid()))
            return;
        getClanMembers().add(player.getUuid());
        getGruntsRole().getMemberList().add(player.getUuid());
        player.setClanID(getClanIdentifier());
        save();
        player.savePlayer();
        doTeamBroadcast(LanguageConfig.getConfig().get().node("Clan", "NewMember").getString().replace("%p%", Util.getPlayerName(player.getUuid())));
    }

    public String getRoleString(UUID uuid)
    {
        Roles role = getRoleFromPlayer(uuid);

        String display = "&7Grunt";
        switch (role) {
            case Leader: {
                display = "&4Leader";
                break;
            }
            case Admin: {
                display = "&bAdmin";
                break;
            }
        }
        return display;
    }

    public Roles getRoleFromPlayer(UUID uuid)
    {
        if (isLeader(uuid))
            return Roles.Leader;
        if (isAdmin(uuid))
            return Roles.Admin;
        return Roles.Grunt;
    }

    public void doTeamBroadcast(String msg) {
        for (UUID uuid : getClanMembers()) {
            if (Util.isPlayerOnline(uuid)) {
                Util.send(Util.getPlayer(uuid), msg);
            }
        }
    }

    public String getClanName() {
        return clanName;
    }

    public void setClanName(String clanName) {
        this.clanName = clanName;
    }

    public List<UUID> getClanMembers() {
        return clanMembers;
    }

    public void setClanMembers(List<UUID> clanMembers) {
        this.clanMembers = clanMembers;
    }

    public UUID getClanIdentifier() {
        return clanIdentifier;
    }

    public void setClanIdentifier(UUID clanIdentifier) {
        this.clanIdentifier = clanIdentifier;
    }

    public UUID getLeaderUUID() {
        return leaderUUID;
    }

    public void setLeaderUUID(UUID leaderUUID) {
        this.leaderUUID = leaderUUID;
    }

    public Role getAdminRole() {
        return adminRole;
    }

    public void setAdminRole(Role adminRole) {
        this.adminRole = adminRole;
    }

    public Role getGruntsRole() {
        return gruntsRole;
    }

    public void setGruntsRole(Role gruntsRole) {
        this.gruntsRole = gruntsRole;
    }

    public void save() {
        ClanStorage.saveClan(this);
    }

    public void updateCache() {
        if (Clandorus.clanWrapper.clanCache.containsKey(this.getClanIdentifier()))
            Clandorus.clanWrapper.clanCache.replace(this.getClanIdentifier(), this);
        else Clandorus.clanWrapper.clanCache.put(this.getClanIdentifier(), this);
    }

    public boolean isLeader(UUID uuid) {
        return getLeaderUUID().equals(uuid);
    }

    public boolean isMember(UUID uuid) {
        for (UUID memberUUID : clanMembers) {
            if (uuid.equals(memberUUID))
                return true;
        }
        return false;
    }

    public AuditLog getAuditLog() {
        return auditLog;
    }

    public void setAuditLog(AuditLog auditLog) {
        this.auditLog = auditLog;
    }

    public int getElo() {
        return elo;
    }

    public void setElo(int elo) {
        this.elo = elo;
    }

    public ClanPokemonStorage getPokemonStorage() {
        return pokemonStorage;
    }

    public void setPokemonStorage(ClanPokemonStorage pokemonStorage) {
        this.pokemonStorage = pokemonStorage;
    }

    public ClanItemStorage getItemStorage() {
        return itemStorage;
    }

    public void setItemStorage(ClanItemStorage itemStorage) {
        this.itemStorage = itemStorage;
    }

    public Bank getClanBank() {
        return clanBank;
    }

    public void setClanBank(Bank clanBank) {
        this.clanBank = clanBank;
    }

    public ClanChat getClanChat() {
        return clanChat;
    }

    public void setClanChat(ClanChat clanChat) {
        this.clanChat = clanChat;
    }

    public boolean isAdmin(UUID uuid) {
        for (UUID memberUUID : adminRole.getMemberList()) {
            if (uuid.equals(memberUUID))
                return true;
        }
        return false;
    }

    public boolean isGrunt(UUID uuid) {
        for (UUID memberUUID : gruntsRole.getMemberList()) {
            if (uuid.equals(memberUUID))
                return true;
        }
        return false;
    }

    public boolean canKick(UUID senderUUID, UUID targetUUID) {
        if (leaderUUID.equals(targetUUID))
            return false;
        if (isLeader(senderUUID))
            return true;
        if (isAdmin(senderUUID)) {
            return !isAdmin(targetUUID);
        }
        return false;
    }

    public List<Player> memberPlayerDaya() {
        List<Player> players = new ArrayList<>();

        for (UUID uuid : clanMembers) {
            Player p = PlayerStorage.getPlayer(uuid);
            players.add(p);
        }

        return players;
    }

    public List<Button> memberButtons() {
        List<Button> buttons = new ArrayList<>();

        for (Player p : memberPlayerDaya()) {
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
                    .title(Util.formattedString("&b" + p.getName() + " " + getRoleString(p.getUuid())))
                    .onClick(b ->
                    {
                        UIManager.openUIForcefully(b.getPlayer(), manageMemberPage(b.getPlayer().getUniqueID(), p));
                    })
                    .display(skullStack)
                    .build();
            buttons.add(button);
        }
        return buttons;
    }

    public boolean canDemote(UUID senderUUID, UUID targetUUID) {
        if (senderUUID.equals(targetUUID))
            return false;
        if (isLeader(senderUUID)) {
            return !isGrunt(targetUUID);
        }
        return false;
    }

    public boolean canPromote(UUID senderUUID, UUID targetUUID) {
        if (senderUUID.equals(targetUUID))
            return false;
        if (isLeader(senderUUID)) {
            return !isAdmin(targetUUID);
        }
        return false;
    }

    public void demote(UUID senderUUID, UUID targetUUID)
    {
        this.adminRole.getMemberList().remove(targetUUID);
        this.gruntsRole.getMemberList().add(targetUUID);
        //do broadcast? idk
        //post event
        updateCache();
    }

    public void promote(UUID senderUUID, UUID targetUUID)
    {
        this.gruntsRole.getMemberList().remove(targetUUID);
        this.adminRole.getMemberList().add(targetUUID);
        //do broadcast? idk
        //post event
        updateCache();
    }

    public GooeyPage manageMemberPage(UUID senderUUID, Player targetPlayer)
    {
        ChestTemplate.Builder template = ChestTemplate.builder(3);

        template.fill(filler);

        GooeyButton kickButton;
        String kickTitle = "&4Not Allowed";
        ItemStack kickDisplay = new ItemStack(Items.BARRIER);

        if (canKick(senderUUID, targetPlayer.getUuid()))
        {
            kickTitle = "&aKick Member";
            kickDisplay = new ItemStack(Items.CHAINMAIL_BOOTS);
        }
        kickButton = GooeyButton.builder()
                .title(Util.formattedString(kickTitle))
                .display(kickDisplay)
                .onClick(b ->
                {
                    if (canKick(senderUUID, targetPlayer.getUuid()))
                    {
                        this.kickMember(senderUUID, targetPlayer.getUuid());
                        UIManager.openUIForcefully(b.getPlayer(), memberPage());
                    } else
                    {

                        UIManager.closeUI(b.getPlayer());
                    }
                })
                .build();

        GooeyButton promoteButton;
        String promoteTitle = "&4Not Allowed";
        ItemStack promoteDisplay = new ItemStack(Items.BARRIER);
        if (canPromote(senderUUID, targetPlayer.getUuid()))
        {
            promoteTitle = "&aPromote Member";
            promoteDisplay = new ItemStack(Items.KNOWLEDGE_BOOK);
        }
        promoteButton = GooeyButton.builder()
                .title(Util.formattedString(promoteTitle))
                .display(promoteDisplay)
                .onClick(b ->
                {
                    if (canPromote(senderUUID, targetPlayer.getUuid()))
                    {
                        promote(senderUUID, targetPlayer.getUuid());
                        UIManager.openUIForcefully(b.getPlayer(), memberPage());
                    }
                    UIManager.closeUI(b.getPlayer());
                })
                .build();

        GooeyButton demoteButton;
        String demoteTitle = "&4Not Allowed";
        ItemStack demoteDisplay = new ItemStack(Items.BARRIER);
        if (canDemote(senderUUID, targetPlayer.getUuid()))
        {
            demoteTitle = "&aDemote Member";
            demoteDisplay = new ItemStack(Items.PAPER);
        }
        demoteButton = GooeyButton.builder()
                .title(Util.formattedString(demoteTitle))
                .display(demoteDisplay)
                .onClick(b ->
                {
                    if (canDemote(senderUUID, targetPlayer.getUuid()))
                    {
                        demote(senderUUID, targetPlayer.getUuid());
                        UIManager.openUIForcefully(b.getPlayer(), memberPage());
                    }
                    UIManager.closeUI(b.getPlayer());
                })
                .build();

        template.set(1, 3, promoteButton);

        template.set(1, 5, demoteButton);

        template.set(1, 7, kickButton);


        return GooeyPage.builder().template(template.build()).title(Util.formattedString("Manage %name%".replace("%name%", targetPlayer.getName()))).build();
    }

    public LinkedPage memberPage() {
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
        if (memberButtons().size() > 8) {
            template = ChestTemplate.builder(5)
                    .border(0, 0, 5, 9, filler)
                    .set(0, 3, previous)
                    .set(0, 5, next)
                    .rectangle(1, 1, 3, 7, placeHolderButton)
                    .build();
        } else {
            template = ChestTemplate.builder(3)
                    .border(0, 0, 3, 9, filler)
                    .row(1, placeHolderButton)
                    .build();
        }

        return PaginationHelper.createPagesFromPlaceholders(template, memberButtons(), LinkedPage.builder().title(Util.formattedString("&bClan Members")).template(template));

    }

    public void viewMemberManagement(ServerPlayerEntity player)
    {
        UIManager.openUIForcefully(player, memberPage());
    }

    public void updateKickedData(UUID uuid)
    {
        Player player = PlayerStorage.getPlayer(uuid);
        if (player == null)
            return;
        player.setClanID(null);
        player.savePlayer();
    }
    public void kickMember(UUID sender, UUID target)
    {
        Player senderPlayer = PlayerStorage.getPlayer(sender);
        Player targetPlayer = PlayerStorage.getPlayer(target);

        MinecraftForge.EVENT_BUS.post(new ClanEvent.ClanKickEvent(this, senderPlayer, targetPlayer));

        clanMembers.remove(target);
        if (isGrunt(target))
            gruntsRole.getMemberList().remove(target);
        if (isAdmin(target))
            gruntsRole.getMemberList().remove(target);
        updateKickedData(target);
        updateCache();

        senderPlayer.sendMessage("&cYou kicked %name% from the clan".replace("%name%", targetPlayer.getName()));

//        String senderName = Util.getPlayerName(sender);
//        String targetName = Util.getOfflinePlayerName(target);
//
//        Log log = new Log(sender, AuditType.Kick, "%sender% kicked %target% from %clanname%"
//                .replace("%sender%", senderName)
//                .replace("%target%", targetName)
//                .replace("%clanname%", clanName)
//        );

    }

    public List<Button> storedPokemonButtons()
    {
        List<Button> buttons = new ArrayList <>();

        ClanPokemonStorage storage = this.pokemonStorage;

        for (Pokemon p:storage.pokemonList) {
            if (p == null)
                continue;
            if (p.isEgg())
                continue;

            Button pokemonButton = GooeyButton.builder()
                    .display(SpriteItemHelper.getPhoto(p))
                    .title(Util.formattedString("&e" + p.getSpecies().getName()))
                    .onClick(b ->
                    {
                        UIManager.closeUI(b.getPlayer());
                        if (isOutDatedPokemonStorageCache(p))
                        {
                            Util.sendFailMessage(b.getPlayer(), "&cIt seems another clan member is using the storage, please re-open the storage!");
                            return;
                        }
                        PlayerPartyStorage pps = StorageProxy.getParty(b.getPlayer().getUniqueID());
                        pps.add(p);
                        pokemonStorage.removeFromStorage(p);
                        updateCache();
                    })
                    .lore(Util.formattedArrayList(Arrays.asList("")))
                    .build();
            buttons.add(pokemonButton);
        }

        return buttons;
    }

    public LinkedPage PokemonStorage()
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
        if (storedPokemonButtons().size() > 8) {
            template = ChestTemplate.builder(5)
                    .border(0, 0, 5, 9, filler)
                    .set(0, 3, previous)
                    .set(0, 5, next)
                    .rectangle(1, 1, 3, 7, placeHolderButton)
                    .build();
        } else {
            template = ChestTemplate.builder(3)
                    .border(0, 0, 3, 9, filler)
                    .row(1, placeHolderButton)
                    .build();
        }

        return PaginationHelper.createPagesFromPlaceholders(template, storedPokemonButtons(), LinkedPage.builder().title(Util.formattedString("&bPokemon Storage")).template(template));
    }

    public void openPokemonStorage(ServerPlayerEntity playerEntity)
    {
        UIManager.openUIForcefully(playerEntity, PokemonStorage());
    }

    public boolean isOutDatedPokemonStorageCache(Pokemon pokemon)
    {
        if (pokemonStorage.pokemonList.size() != Clandorus.clanWrapper.clanCache.get(clanIdentifier).pokemonStorage.pokemonList.size()) {
            return true;
        }

        if (!Clandorus.clanWrapper.clanCache.get(clanIdentifier).pokemonStorage.pokemonList.contains(pokemon)) {
            return true;
        }
        return !pokemonStorage.pokemonList.equals(Clandorus.clanWrapper.clanCache.get(clanIdentifier).pokemonStorage.pokemonList);
    }

    public boolean isOutDatedItemStorageCache(ItemStack stack)
    {
        if (itemStorage.storage.size() != Clandorus.clanWrapper.clanCache.get(clanIdentifier).itemStorage.storage.size()) {
            return true;
        }

        if (!Clandorus.clanWrapper.clanCache.get(clanIdentifier).itemStorage.storage.contains(stack)) {
            return true;
        }

        return !itemStorage.storage.equals(Clandorus.clanWrapper.clanCache.get(clanIdentifier).itemStorage.storage);
    }

    public List<Button> storedItemButtons()
    {
        List<Button> buttons = new ArrayList <>();

        List<ItemStack> itemStacks = itemStorage.storage;

        for (ItemStack stack:itemStacks) {

            if (stack == null)
                continue;
            if (stack.isEmpty())
                continue;
            ItemStack clonedStack = stack.copy();
            Button itemButton = GooeyButton.builder()
                    .display(clonedStack)
                    .onClick(b ->
                    {
                        UIManager.closeUI(b.getPlayer());
                        if (isOutDatedItemStorageCache(stack))
                        {
                            Util.sendFailMessage(b.getPlayer(), "&cIt seems another clan member is using the storage, please re-open the storage!");
                            return;
                        }
                        if (b.getPlayer().inventory.getFirstEmptyStack() == -1) {
                            Util.send(b.getPlayer(), LanguageConfig.getConfig().get().node("Storage", "InventoryFull").getString());
                            return;
                        }
                        b.getPlayer().inventory.addItemStackToInventory(clonedStack);
                        itemStorage.removeFromStorage(stack);
                        updateCache();
                    })
                    .build();
            buttons.add(itemButton);
        }

        return buttons;
    }

    public LinkedPage ItemStorage()
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
        if (storedItemButtons().size() > 8) {
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

        return PaginationHelper.createPagesFromPlaceholders(template, storedItemButtons(), LinkedPage.builder().title(Util.formattedString("&bItem Storage")).template(template));
    }

    public void openItemStorage(ServerPlayerEntity playerEntity)
    {
        UIManager.openUIForcefully(playerEntity, ItemStorage());
    }
}
