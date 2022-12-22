package io.github.adainish.clandorus.obj.clan.data;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;

import java.util.ArrayList;
import java.util.List;

public class ClanPokemonStorage {
    public List<Pokemon> pokemonList = new ArrayList<>();
    public int maxPokemon = 60;


    public ClanPokemonStorage()
    {

    }

    public boolean isStorageFull()
    {
        return this.pokemonList.size() >= this.maxPokemon;
    }

    public void setMaxStorage(int amount)
    {
        this.maxPokemon = amount;
    }

    public void increaseMaxStorage(int amount)
    {
        this.maxPokemon += amount;
    }

    public void decreaseMaxStorage(int amount)
    {
        this.maxPokemon -= amount;
    }

    public void addToStorage(Pokemon storageItem)
    {
        if (pokemonList.size() >= maxPokemon)
        {
            return;
        }
        pokemonList.add(storageItem);
    }

    public void removeFromStorage(Pokemon storageItem)
    {
        pokemonList.remove(storageItem);
    }
}
