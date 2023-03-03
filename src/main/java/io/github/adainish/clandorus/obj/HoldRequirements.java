package io.github.adainish.clandorus.obj;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonBuilder;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;

import java.util.ArrayList;
import java.util.List;

public class HoldRequirements
{
    public int min31Ivs = 1;

    public List<Pokemon> bannedPokemon = new ArrayList<>();

    public HoldRequirements()
    {

    }

    public void banLegends()
    {
        List<Species> needToBeBanned = new ArrayList<>();
        for (int i: PixelmonSpecies.getLegendaries()) {
            if (!PixelmonSpecies.fromDex(i).isPresent()) {
                continue;
            }
            Species sp = PixelmonSpecies.fromDex(i).get();
            if (!bannedPokemon.isEmpty()) {

                for (Pokemon p : bannedPokemon) {
                    if (p.getSpecies().equals(sp))
                        continue;
                    needToBeBanned.add(sp);
                }
            } else needToBeBanned.add(sp);
        }
        if (needToBeBanned.isEmpty())
            return;
        for (Species sp:needToBeBanned) {
            Pokemon p = PokemonBuilder.builder().species(sp).build();
            bannedPokemon.add(p);
        }
    }

    public boolean isBanned(Pokemon pokemon)
    {
        if (bannedPokemon.isEmpty())
            return false;
        return bannedPokemon.stream().anyMatch(p -> p.getSpecies().equals(pokemon.getSpecies()) && p.getForm().equals(pokemon.getForm()));
    }

    public boolean isAllowed(Pokemon pokemon)
    {
        if (isBanned(pokemon))
            return false;


        int counter = 0;
        for (BattleStatsType bts: BattleStatsType.EV_IV_STATS) {
            if (pokemon.getIVs().getStat(bts) == 31)
                counter++;
        }

        if (counter < min31Ivs)
            return false;

        return true;
    }
}
