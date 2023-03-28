package io.github.adainish.clandorus.obj.gyms;

import com.pixelmonmod.api.pokemon.PokemonSpecificationProxy;
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

    public List<String> bannedPokemonSpecs = new ArrayList <>();

    public List<String> bannedAbilities = new ArrayList <>();

    public List<String> bannedMoves = new ArrayList <>();

    public HoldRequirements()
    {

    }

    public void banUBS()
    {
        List<String> needToBeBanned = new ArrayList<>();
        for (int i: PixelmonSpecies.getUltraBeasts()) {
            if (!PixelmonSpecies.fromDex(i).isPresent()) {
                continue;
            }
            Species sp = PixelmonSpecies.fromDex(i).get();
            if (!bannedPokemonSpecs.isEmpty()) {
                for (String s : bannedPokemonSpecs) {
                    Pokemon p = PokemonSpecificationProxy.create(s).create();
                    if (p != null) {
                        if (p.getSpecies().equals(PixelmonSpecies.MISSINGNO.getValueUnsafe()))
                            continue;
                        if (p.getSpecies().equals(sp))
                            continue;
                        if (needToBeBanned.contains(sp.getName()))
                            break;
                        if (needToBeBanned.contains(s))
                            continue;
                        if (bannedPokemonSpecs.contains(sp.getName()))
                            continue;
                        needToBeBanned.add(sp.getName());
                        break;
                    }
                }
            } else needToBeBanned.add(sp.getName());
        }
        if (needToBeBanned.isEmpty())
            return;
        bannedPokemonSpecs.addAll(needToBeBanned);
    }

    public void banLegends()
    {
        List<String> needToBeBanned = new ArrayList<>();
        for (int i: PixelmonSpecies.getLegendaries()) {
            if (!PixelmonSpecies.fromDex(i).isPresent()) {
                continue;
            }
            Species sp = PixelmonSpecies.fromDex(i).get();
            if (!bannedPokemonSpecs.isEmpty()) {
                for (String s : bannedPokemonSpecs) {
                    Pokemon p = PokemonSpecificationProxy.create(s).create();
                    if (p != null) {
                        if (p.getSpecies().equals(PixelmonSpecies.MISSINGNO.getValueUnsafe()))
                            continue;
                        if (p.getSpecies().equals(sp))
                            continue;
                        if (needToBeBanned.contains(sp.getName()))
                            break;
                        if (needToBeBanned.contains(s))
                            continue;
                        if (bannedPokemonSpecs.contains(sp.getName()))
                            continue;
                        needToBeBanned.add(sp.getName());
                        break;
                    }
                }
            } else needToBeBanned.add(sp.getName());
        }
        if (needToBeBanned.isEmpty())
            return;
        bannedPokemonSpecs.addAll(needToBeBanned);
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
        if (bannedPokemonSpecs.isEmpty())
            return false;

        return bannedPokemonSpecs.stream().map(s -> PokemonSpecificationProxy.create(s).create()).anyMatch(p -> p.getSpecies().equals(pokemon.getSpecies()) && p.getForm().getName().equalsIgnoreCase(pokemon.getForm().getName()));
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

        return counter < min31Ivs;
    }
}
