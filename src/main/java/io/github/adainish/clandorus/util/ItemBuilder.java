package io.github.adainish.clandorus.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class ItemBuilder
{
    private ItemStack stack;
    public ItemBuilder(ItemStack stack)
    {
        this.stack = stack;
    }
    public ItemBuilder(String identifier, int amount) throws Exception {
        ResourceLocation location = new ResourceLocation(identifier);
        Item item = ForgeRegistries.ITEMS.getValue(location);
        if (item == null)
            throw new Exception("Item returned null, please check your config for any type errors. The provided string was: " + identifier);
        this.stack = new ItemStack(item, amount);
    }

    public ItemBuilder(String identifier) throws Exception {
        ResourceLocation location = new ResourceLocation(identifier);
        Item item = ForgeRegistries.ITEMS.getValue(location);
        if (item == null)
            throw new Exception("Item returned null, please check your config for any type errors. The provided string was: " + identifier);
        this.stack = new ItemStack(item, 1);
    }


    public ItemBuilder setName(String name) {
        this.stack.setDisplayName(new StringTextComponent(Util.formattedString(name)));
        return this;
    }

    public ItemBuilder setLore(List <String> tag) {
        ItemStack newStack = stack;
        ListNBT nbtLore = new ListNBT();
        for (String line : tag) {
            if (line != null) {
                nbtLore.add(StringNBT.valueOf(ITextComponent.Serializer.toJson(TextUtil.parseHexCodes(Util.formattedString(line), false))));
            }
        }
        stack.getOrCreateChildTag("display").put("Lore", nbtLore);

        this.stack = newStack;
        return this;
    }

    public ItemStack build() {
        return this.stack;
    }
}
