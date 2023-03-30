package io.github.adainish.clandorus.obj.gyms;

import com.pixelmonmod.api.pokemon.PokemonSpecification;
import com.pixelmonmod.api.pokemon.PokemonSpecificationProxy;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import info.pixelmon.repack.org.spongepowered.serialize.SerializationException;
import io.github.adainish.clandorus.Clandorus;
import io.github.adainish.clandorus.conf.DefaultTeamConfig;
import io.leangen.geantyref.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class DefaultTeam
{
    public String identifier;
    public List<String> pokemonSpecs = new ArrayList<>();

    public DefaultTeam(String identifier)
    {
        this.identifier = identifier;
        try {
            List<String> stringList = DefaultTeamConfig.getConfig().get().node("DefaultTeams", identifier, "PokemonSpecs").getList(TypeToken.get(String.class));
            if (stringList != null) {
                pokemonSpecs.addAll(stringList);
            } else {
                Clandorus.log.warn("Failed to load Pokemon Spec List for Default Team %identifier%".replace("%identifier%", identifier));
            }
        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
    }

    public DefaultTeam()
    {

    }

    public List<PokemonSpecification> pokemonSpecifications()
    {
        List<PokemonSpecification> specifications = new ArrayList<>();

        for (String s:pokemonSpecs) {
            PokemonSpecification spec = PokemonSpecificationProxy.create(s);
            specifications.add(spec);
        }

        return specifications;
    }

    public List<Pokemon> getPokemonTeam()
    {
        List<Pokemon> pokemons = new ArrayList<>();
        for (PokemonSpecification spec:pokemonSpecifications()) {
            Pokemon pokemon = spec.create();
            pokemons.add(pokemon);
        }
        return pokemons;
    }
}
