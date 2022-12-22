package io.github.adainish.clandorus.obj.clan.data;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ClanItemStorage {
    public List<ItemStack> storage = new ArrayList<>();

    public int maxStorage = 60;

    public ClanItemStorage()
    {}

    public boolean isStorageFull()
    {
        return this.storage.size() >= this.maxStorage;
    }

    public void setMaxStorage(int amount)
    {
        this.maxStorage = amount;
    }

    public void increaseMaxStorage(int amount)
    {
        this.maxStorage += amount;
    }

    public void decreaseMaxStorage(int amount)
    {
        this.maxStorage -= amount;
    }

    public void addToStorage(ItemStack storageItem)
    {
        if (storage.size() >= maxStorage)
        {
            return;
        }
        storage.add(storageItem);
    }

    public void removeFromStorage(ItemStack storageItem)
    {
        storage.remove(storageItem);
    }
}
