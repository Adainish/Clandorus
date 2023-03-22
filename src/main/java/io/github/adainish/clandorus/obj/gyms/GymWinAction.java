package io.github.adainish.clandorus.obj.gyms;

import com.pixelmonmod.api.pokemon.PokemonSpecification;
import com.pixelmonmod.api.pokemon.PokemonSpecificationProxy;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import io.github.adainish.clandorus.Clandorus;
import io.github.adainish.clandorus.obj.Player;
import io.github.adainish.clandorus.obj.clan.Clan;
import io.github.adainish.clandorus.obj.mail.Reward;
import io.github.adainish.clandorus.storage.PlayerStorage;
import io.github.adainish.clandorus.util.EconomyUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GymWinAction
{
    public List <String> rewardIDs = new ArrayList <>();
    public boolean takePokemon = false;
    public List<String> pokemonSpecList = new ArrayList <>();
    public int money = 0;

    public GymWinAction()
    {

    }


    public List<Reward> returnRewardsFromIDs()
    {
        List<Reward> rewardList = new ArrayList<>();

        for (String s:rewardIDs) {
            if (Clandorus.rewardRegistry.rewardCache.containsKey(s)) {
                Reward r = Clandorus.rewardRegistry.rewardCache.get(s);
                rewardList.add(r);
            }
        }

        return rewardList;
    }


    public Pokemon pokemonFromString(String s)
    {
        PokemonSpecification spec = PokemonSpecificationProxy.create(s);
        return PokemonFactory.create(spec);
    }


    public void executeWinAction(Clan clan, Player player, List<Pokemon> pokemonList, UUID previousHolder) {
        Player oldHolder = PlayerStorage.getPlayer(previousHolder);
        PlayerPartyStorage pps = player.getPixelmonPartyStorage();
        Clan oldHolderClan = null;
        if (oldHolder != null) {
            if (oldHolder.getClanOptional().isPresent())
                oldHolderClan = oldHolder.getClanOptional().get();
        }
        //old clan storage?
        List<Reward> rewardList = new ArrayList<>(returnRewardsFromIDs());
        if (this.takePokemon) {
            //open take menu and return pokemon that was stolen
        }
        if (!this.pokemonSpecList.isEmpty()) {
            handOutRewardPokemon(pps);
        }
        if (!rewardList.isEmpty()) {
            for (Reward r : rewardList) {
                r.handOutRewards(player);
            }
        }

        if (this.money > 0)
            EconomyUtil.giveBalance(player.getUuid(), money);

        if (!pokemonList.isEmpty()) {
            for (Pokemon p : pokemonList) {
                if (oldHolderClan != null) {
                    if (p != null) {
                        oldHolderClan.getPokemonStorage().addToStorage(p);
                    }

                    if (oldHolderClan.getPokemonStorage().isEncumbered()) {
                        oldHolderClan.doTeamBroadcast("&4&lThe Clans Pokemon Storage is currently encumbered, %encumbered%/%maxstorage%"
                                .replace("%encumbered%", String.valueOf(oldHolderClan.getPokemonStorage().encumberAmount()))
                                .replace("%maxstorage%", String.valueOf(oldHolderClan.getPokemonStorage().maxPokemon))
                        );
                    }
                    oldHolderClan.save();
                }
            }
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
        if (takePokemon)
            takePokemon = false;
        else takePokemon = true;
    }
}
