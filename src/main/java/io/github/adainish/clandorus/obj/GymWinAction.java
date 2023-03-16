package io.github.adainish.clandorus.obj;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import io.github.adainish.clandorus.Clandorus;
import io.github.adainish.clandorus.obj.clan.Clan;
import io.github.adainish.clandorus.obj.mail.Reward;
import io.github.adainish.clandorus.registry.RewardRegistry;
import io.github.adainish.clandorus.util.EconomyUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class GymWinAction
{
    public List <String> rewardIDs = new ArrayList <>();
    public boolean takePokemon = false;
    public List<String> givePokemon = new ArrayList <>();
    public int money = 0;

    public GymWinAction()
    {

    }


    public void executeWinAction(Clan clan, Player player, List<Pokemon> pokemonList, UUID previousHolder)
    {
        PlayerPartyStorage pps = player.getPixelmonPartyStorage();
        List<Reward> rewardList = new ArrayList <>();
        if (!this.rewardIDs.isEmpty())
        {
            for (String s:this.rewardIDs) {
                if (Clandorus.rewardRegistry.rewardCache.containsKey(s)) {
                    Reward r = Clandorus.rewardRegistry.rewardCache.get(s);
                    rewardList.add(r);
                }
            }
        }
        if (this.takePokemon)
        {
            //open take menu and return pokemon that was stolen
        }
        if (!this.givePokemon.isEmpty())
        {
            //create and distribute

        }
        if (!rewardList.isEmpty())
        {
            for (Reward r:rewardList) {
                r.handOutRewards(player);
            }
        }

        if (this.money > 0)
            EconomyUtil.giveBalance(player.getUuid(), money);

        if (!pokemonList.isEmpty())
        {
            for (Pokemon p:pokemonList) {
                if (p != null)
                {
                    clan.getPokemonStorage().addToStorage(p);
                }
            }
            if (clan.getPokemonStorage().isEncumbered())
            {
                clan.doTeamBroadcast("&4&lThe Clans Pokemon Storage is currently encumbered, %encumbered%/%maxstorage%"
                        .replace("%encumbered%", String.valueOf(clan.getPokemonStorage().encumberAmount()))
                        .replace("%maxstorage%", String.valueOf(clan.getPokemonStorage().maxPokemon))
                );
            }
            clan.save();
        }
    }
}
