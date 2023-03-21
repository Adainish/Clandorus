package io.github.adainish.clandorus.obj.gyms;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonBuilder;
import com.pixelmonmod.pixelmon.api.pokemon.ability.Ability;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType;
import com.pixelmonmod.pixelmon.api.pokemon.stats.Moveset;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;

import java.util.ArrayList;
import java.util.List;

public class HoldRequirements
{
    public int min31Ivs = 0;

    public List<Pokemon> bannedPokemon = new ArrayList<>();

    public List<String> bannedAbilities = new ArrayList <>();

    public List<String> bannedMoves = new ArrayList <>();

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

    public boolean isAbilityBanned(Ability ability)
    {
        return bannedAbilities.contains(ability.getName());
    }

    public boolean isAttackBanned(Attack attack)
    {
        return bannedMoves.contains(attack.getMove().getAttackName());
    }

    public boolean isMoveSetBanned(Moveset moveset)
    {
        for (Attack atk:moveset.attacks) {
            if (isAttackBanned(atk))
                return true;
        }
        return false;
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

        if (isAbilityBanned(pokemon.getAbility()))
            return false;

        if (isMoveSetBanned(pokemon.getMoveset()))
            return false;

        if (counter < min31Ivs)
            return false;

        return true;
    }
}
