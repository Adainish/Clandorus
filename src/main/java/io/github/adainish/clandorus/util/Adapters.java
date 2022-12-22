package io.github.adainish.clandorus.util;

import com.google.gson.*;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pixelmonmod.api.SpecificationTypeAdapter;
import com.pixelmonmod.api.pokemon.PokemonSpecification;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;

import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

public class Adapters {
    public static Gson PRETTY_MAIN_GSON = new GsonBuilder()
            .setPrettyPrinting()
            .excludeFieldsWithModifiers(Modifier.TRANSIENT)
            .registerTypeAdapter(Pokemon.class, new PokemonAdapter())
            .registerTypeAdapter(CompoundNBT.class, new NBTAdapter())
            .registerTypeAdapter(PokemonSpecification.class, new SpecificationTypeAdapter(PokemonSpecification.class))
            .registerTypeAdapter(ItemStack.class, new ItemStackAdapter())
            .create();


    public static class NBTAdapter implements JsonSerializer<CompoundNBT>, JsonDeserializer<CompoundNBT>
    {
        @Override
        public CompoundNBT deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            try
            {
                return JsonToNBT.getTagFromJson(json.getAsString());
            }
            catch (CommandSyntaxException e)
            {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public JsonElement serialize(CompoundNBT src, Type typeOfSrc, JsonSerializationContext context)
        {
            return context.serialize(src.toString(), String.class);
        }
    }


    public static class PokemonAdapter implements JsonSerializer<Pokemon>, JsonDeserializer<Pokemon>
    {
        @Override
        public Pokemon deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            try
            {
                return PokemonFactory.create(JsonToNBT.getTagFromJson(json.getAsString()));
            }
            catch (CommandSyntaxException e)
            {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public JsonElement serialize(Pokemon src, Type typeOfSrc, JsonSerializationContext context)
        {
            return context.serialize(src.writeToNBT(new CompoundNBT()).toString(), String.class);
        }
    }

    public static class ItemStackAdapter implements JsonSerializer <ItemStack>, JsonDeserializer <ItemStack>
    {
        @Override
        public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException
        {
            try
            {
                String nbtString = json.getAsString();
                if (nbtString == null || nbtString.isEmpty()) {
                    return null;
                }

                ItemStack item = ItemStack.read(JsonToNBT.getTagFromJson(nbtString));
                return item.isEmpty() ? ItemStack.EMPTY : item;
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public JsonElement serialize(ItemStack src, Type typeOfSrc, JsonSerializationContext context)
        {
            if (src.isEmpty())
                return context.serialize("", String.class);
            else
                return context.serialize(src.write(new CompoundNBT()).toString(), String.class);
        }
    }
}
