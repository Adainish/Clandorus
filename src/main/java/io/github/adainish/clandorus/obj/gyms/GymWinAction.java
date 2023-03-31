package io.github.adainish.clandorus.obj.gyms;

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
import com.pixelmonmod.api.pokemon.PokemonSpecification;
import com.pixelmonmod.api.pokemon.PokemonSpecificationProxy;
import com.pixelmonmod.pixelmon.api.dialogue.Choice;
import com.pixelmonmod.pixelmon.api.dialogue.Dialogue;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.api.util.Scheduling;
import com.pixelmonmod.pixelmon.api.util.helpers.SpriteItemHelper;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import io.github.adainish.clandorus.Clandorus;
import io.github.adainish.clandorus.api.MailBuilder;
import io.github.adainish.clandorus.enumeration.MailSender;
import io.github.adainish.clandorus.enumeration.OccupiedType;
import io.github.adainish.clandorus.obj.Player;
import io.github.adainish.clandorus.obj.clan.Clan;
import io.github.adainish.clandorus.obj.mail.Reward;
import io.github.adainish.clandorus.storage.PlayerStorage;
import io.github.adainish.clandorus.util.EconomyUtil;
import io.github.adainish.clandorus.util.Util;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static io.github.adainish.clandorus.util.Util.filler;

public class GymWinAction {
    public List <String> rewardIDs = new ArrayList <>();
    public boolean takePokemon = false;
    public List <String> pokemonSpecList = new ArrayList <>();
    public int money = 0;
    public transient List <Pokemon> npcStoredPokemon = new ArrayList <>();
    public transient List <Pokemon> newPokemonList = new ArrayList <>();
    public transient NPCTrainer npcTrainer;
    public transient Clan newClan;
    public transient ClanGym gym;
    public transient Clan oldHolderClan;
    public transient Player player;
    public transient Pokemon stolenPokemon;
    public transient boolean completedAction = false;
    public GymWinAction() {

    }


    public List <Reward> returnRewardsFromIDs() {
        List <Reward> rewardList = new ArrayList <>();

        for (String s : rewardIDs) {
            if (Clandorus.rewardRegistry.rewardCache.containsKey(s)) {
                Reward r = Clandorus.rewardRegistry.rewardCache.get(s);
                rewardList.add(r);
            }
        }

        return rewardList;
    }


    public Pokemon pokemonFromString(String s) {
        PokemonSpecification spec = PokemonSpecificationProxy.create(s);
        return PokemonFactory.create(spec);
    }


    public Dialogue stealDialogue() {
        Dialogue.DialogueBuilder builder = new Dialogue.DialogueBuilder();
        builder.setName("name");
        builder.setText(Util.formattedString("&7Select a Pokemon to steal"));
        for (Pokemon p : this.npcStoredPokemon) {
            Choice.ChoiceBuilder choiceBuilder = new Choice.ChoiceBuilder();
            choiceBuilder.setText(p.getSpecies().getName());
            choiceBuilder.setHandle(c -> {
                this.npcStoredPokemon.remove(p);
                this.stolenPokemon = p;
                Scheduling.schedule(2, action -> {
                    UIManager.openUIForcefully(player.getServerEntity(), PokemonSelectionMenu());
                }, false);
            });
            builder.addChoice(choiceBuilder.build());
        }
        return builder.build();
    }

    public boolean isDefaultTeam(ClanGym gym) {
        if (this.npcStoredPokemon.isEmpty()) {
            //loop through each pokemon and check if matches with assigned default team id
            if (gym.getDefaultTeam() != null) {
                int counter = 0;
                int required = this.npcStoredPokemon.size();
                for (Pokemon p : this.npcStoredPokemon) {
                    DefaultTeam defaultTeam = gym.getDefaultTeam();
                    for (PokemonSpecification spec : defaultTeam.pokemonSpecifications()) {
                        if (spec.matches(p)) {
                            counter++;
                            break;
                        }
                    }
                }
                return counter >= required;
            }
        }

        return false;
    }

    public void addNewPokemonToNPC() {
        for (Pokemon p : this.newPokemonList) {
            if (p != null) {
                //temp
                if (p.isEgg())
                    continue;
                this.npcTrainer.getPokemonStorage().add(p);

            }
        }
    }

    public void sendOldPokemon() {
        if (this.oldHolderClan != null) {
            for (Pokemon p : this.npcStoredPokemon) {
                if (p != null) {
                    this.oldHolderClan.getPokemonStorage().addToStorage(p);
                    this.npcStoredPokemon.remove(p);
                }
            }
            if (this.oldHolderClan.getPokemonStorage().isEncumbered()) {
                this.oldHolderClan.doTeamBroadcast("&4&lThe Clans Pokemon Storage is currently encumbered, %encumbered%/%maxstorage%"
                        .replace("%encumbered%", String.valueOf(this.oldHolderClan.getPokemonStorage().encumberAmount()))
                        .replace("%maxstorage%", String.valueOf(this.oldHolderClan.getPokemonStorage().maxPokemon))
                );
            }
            this.oldHolderClan.save();
        }
    }

    public List <Button> clanPokemonStorageButtonList() {
        List <Button> buttons = new ArrayList <>();
        for (Pokemon p : this.newClan.getPokemonStorage().pokemonList) {
            ItemStack buttonStack = SpriteItemHelper.getPhoto(p);
            List<String> lore = new ArrayList <>();
            if (!gym.getHoldRequirements().isAllowed(p)) {
                buttonStack = new ItemStack(Items.BARRIER);
                lore.add("&4&lThis pokemon is not allowed to be deposited to the clan gym");
            } else {
                lore.add("&a&lThis pokemon is legal for this gym!");
            }
            GooeyButton button = GooeyButton.builder()
                    .title(Util.formattedString("&b" + p.getSpecies().getName()))
                    .display(buttonStack)
                    .lore(Util.formattedArrayList(lore))
                    .onClick(b -> {
                        if (gym.getHoldRequirements().isAllowed(p)) {
                            if (!this.newPokemonList.contains(p)) {
                                if (this.newPokemonList.size() < 6) {
                                    this.newPokemonList.add(p);
                                    this.newClan.getPokemonStorage().pokemonList.remove(p);
                                } else return;
                            } else {
                                this.newClan.getPokemonStorage().pokemonList.add(p);
                                this.newPokemonList.remove(p);
                            }
//                            UIManager.openUIForcefully(b.getPlayer(), PokemonSelectionMenu());
                        }
                    })
                    .build();
            buttons.add(button);
        }
        return buttons;
    }

    public void updateWinAction(UUID uuid)
    {
        PlayerPartyStorage pps = StorageProxy.getParty(uuid);
        if (!this.newPokemonList.isEmpty()) {

            //old clan storage?
            List<Reward> rewardList = new ArrayList<>(returnRewardsFromIDs());

            if (!this.pokemonSpecList.isEmpty()) {
                handOutRewardPokemon(pps);
            }

            if (!rewardList.isEmpty()) {
                MailBuilder mailBuilder = new MailBuilder();
                mailBuilder.getMail().getRewardList().addAll(rewardList);
                mailBuilder.getMail().setMessage("&7Rewards for conquering the clan gym.");
                mailBuilder.getMail().setSender(MailSender.Server);
                mailBuilder.getMail().setTargetUUID(player.getUuid());
                mailBuilder.getPlayerList().add(player.getUuid());
                mailBuilder.sendMail();
            }

            if (this.money > 0)
                EconomyUtil.giveBalance(player.getUuid(), money);


            if (gym.getOccupyingHolder() != null)
            {
                gym.getOccupyingHolder().handoutRewards();
            } else {
                gym.setOccupyingHolder(new OccupyingHolder(player.getUuid()));
                gym.getOccupyingHolder().occupiedType = OccupiedType.gym;
            }
            gym.setActiveHoldingPlayer(player.getUuid());
            //update npc skin
            gym.setNPCSteve(this.npcTrainer, player.getServerEntity().getName().getUnformattedComponentText());
            sendOldPokemon();
            addNewPokemonToNPC();
            if (this.stolenPokemon != null)
            pps.add(stolenPokemon);
        } else {
            if (gym.getDefaultTeam() != null) {
                DefaultTeam defaultTeam = gym.getDefaultTeam();
                if (!defaultTeam.pokemonSpecs.isEmpty()) {
                    for (Pokemon p : defaultTeam.getPokemonTeam()) {
                        this.npcTrainer.getPokemonStorage().add(p);
                    }
                } else {
                    this.npcTrainer.getPokemonStorage().add(PokemonSpecificationProxy.create("rattata").create());
                }
            } else {
                this.npcTrainer.getPokemonStorage().add(PokemonSpecificationProxy.create("rattata").create());
            }
            gym.setActiveHoldingPlayer(null);
            gym.setOccupyingHolder(null);
        }
        this.newClan = null;
        this.npcTrainer = null;
        this.player = null;
        this.stolenPokemon = null;
        this.completedAction = true;
        gym.save();




    }

    public LinkedPage PokemonSelectionMenu()
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

        ChestTemplate.Builder template;

        //make selection confirm option
        GooeyButton confirm = GooeyButton.builder()
                .title(Util.formattedString("&aConfirm Selection"))
                .display(new ItemStack(Items.LIME_DYE))
                .onClick(b -> {
                    updateWinAction(b.getPlayer().getUniqueID());
                    UIManager.closeUI(b.getPlayer());
                })
                .build();
        if (clanPokemonStorageButtonList().size() > 8) {
            template = ChestTemplate.builder(6)
                    .border(0, 0, 6, 9, filler)
                    .set(0, 3, previous)
                    .set(0, 5, next)
                    .set(0, 0, confirm)
                    .rectangle(1, 1, 4, 7, placeHolderButton);
        } else {
            template = ChestTemplate.builder(3)
                    .border(0, 0, 3, 9, filler)
                    .row(1, placeHolderButton)
                    .set(0, 0, confirm);
        }

        //loop through currently selected pokemon

        Template finalised = template.build();

        return PaginationHelper.createPagesFromPlaceholders(finalised, clanPokemonStorageButtonList(), LinkedPage.builder().onClose(b -> {
            if (!completedAction)
            updateWinAction(b.getPlayer().getUniqueID());
        }).title(Util.formattedString("&aSelect Pokemon")).template(finalised));
    }


    public void executeWinAction(ClanGym gym, Clan clan, Player player, UUID previousHolder, NPCTrainer trainer) {
        this.newClan = clan;
        this.gym = gym;
        this.npcTrainer = trainer;
        this.player = player;

        Player oldHolder = PlayerStorage.getPlayer(previousHolder);
        if (oldHolder != null) {
            if (oldHolder.getClanOptional().isPresent())
                this.oldHolderClan = oldHolder.getClanOptional().get();
        }
        for (int i = 0; i < 6; i++) {
            if (this.npcTrainer.getPokemonStorage().get(i) != null)
            {
                this.npcStoredPokemon.add(this.npcTrainer.getPokemonStorage().get(i));
                this.npcTrainer.getPokemonStorage().set(i, null);
            }
        }

        if (this.takePokemon) {
            //open take menu if team isn't a default team and return pokemon that weren't stolen to be sent to storage return
            if (!this.npcStoredPokemon.isEmpty()) {
                if (!isDefaultTeam(this.gym))
                    stealDialogue().open(player.getServerEntity());
            }
        }


        //open selection menu
        if (!this.takePokemon)
        {
            UIManager.openUIForcefully(player.getServerEntity(), PokemonSelectionMenu());
        }
    }

    public List<Pokemon> specParsedPokemonList()
    {
        List<Pokemon> pokemonList = new ArrayList <>();
        for (String s: pokemonSpecList) {
            PokemonSpecification pokemonSpecification = PokemonSpecificationProxy.create(s);
            Pokemon p = PokemonFactory.create(pokemonSpecification);
            if (p != null)
                pokemonList.add(p);
        }
        return pokemonList;
    }

    public void handOutRewardPokemon(PlayerPartyStorage pps) {
        for (Pokemon p:specParsedPokemonList()) {
            if (p != null)
                pps.add(p);
        }
    }

    public void updateTakeStatus()
    {
        if (this.takePokemon)
            this.takePokemon = false;
        else this.takePokemon = true;
    }
}
