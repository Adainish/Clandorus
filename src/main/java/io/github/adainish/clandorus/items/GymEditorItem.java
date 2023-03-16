package io.github.adainish.clandorus.items;

import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import io.github.adainish.clandorus.util.ItemBuilder;
import io.github.adainish.clandorus.util.PermissionUtil;
import io.github.adainish.clandorus.wrapper.PermissionWrapper;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GymEditorItem
{

    public String display = "";
    public List <String> lore = new ArrayList <>();

    public GymEditorItem()
    {

    }

    public static boolean isEditor(ItemStack stack)
    {
        return stack.hasTag() && stack.getTag().getBoolean("isGymEditor");
    }

    public CompoundNBT editorNBT()
    {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putBoolean("isGymEditor", true);
        return nbt;
    }

    public static boolean isGymNPC(NPCTrainer trainer)
    {
        return trainer.getPersistentData().getBoolean("clandorusGym");
    }

    public Item editorItem()
    {
        return Items.BLAZE_ROD;
    }

    public ItemStack getEditorStack()
    {

        ItemStack stack = new ItemStack(editorItem());
        stack.setTag(editorNBT());
        ItemBuilder itemBuilder = new ItemBuilder(stack);
        itemBuilder.setName("&b&lClan Gym Editor");
        itemBuilder.setLore(Arrays.asList("&7Right click an NPC to edit its clan gym info or create a new Clan Gym"));
        return itemBuilder.build();
    }

    public static boolean canUse(ServerPlayerEntity playerEntity)
    {
        if (playerEntity == null)
            return false;
        return PermissionUtil.checkPerm(playerEntity, PermissionWrapper.editorPermission);
    }
}
