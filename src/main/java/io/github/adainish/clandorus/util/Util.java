package io.github.adainish.clandorus.util;

import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import com.pixelmonmod.pixelmon.api.util.helpers.ResourceLocationHelper;
import io.github.adainish.clandorus.enumeration.Roles;
import io.github.adainish.clandorus.obj.clan.Invite;
import io.github.adainish.clandorus.obj.Player;
import io.github.adainish.clandorus.storage.PlayerStorage;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static io.github.adainish.clandorus.Clandorus.getServer;

public class Util {
    public static MinecraftServer server = getServer();

    private static final MinecraftServer SERVER = server;

    public static boolean isRole(String value) {
        for (Roles role: Roles.values()) {
            if (role.name().equalsIgnoreCase(value))
                return true;
        }
        return false;
    }

    public static Roles getRole(String value) {
        for (Roles role: Roles.values()) {
            if (role.name().equalsIgnoreCase(value))
                return role;
        }
        return null;
    }

    public static boolean isPlayerOnline(ServerPlayerEntity player) {
        return isPlayerOnline(player.getUniqueID());
    }

    public static boolean isPlayerOnline(UUID uuid) {
        ServerPlayerEntity player = SERVER.getPlayerList().getPlayerByUUID(uuid);
        // IJ says it's always true ignore
        return player != null;
    }

    public static String getOfflinePlayerName(UUID uuid) {
        Player player = PlayerStorage.getPlayer(uuid);
        if (player == null)
            return "Invalid player data";
        return player.getName();
    }
    public static String getPlayerName(UUID uuid) {
        ServerPlayerEntity playerEntity = getPlayer(uuid);
        if (playerEntity == null)
            return "Offline Player";
        return playerEntity.getName().getUnformattedComponentText();
    }

    public static Optional<ServerPlayerEntity> getPlayerOptional(String name) {
        return Optional.ofNullable(getServer().getPlayerList().getPlayerByUsername(name));
    }


    public static ServerPlayerEntity getPlayer(String playerName) {
        return server.getPlayerList().getPlayerByUsername(playerName);
    }

    public static ServerPlayerEntity getPlayer(UUID uuid) {
        return server.getPlayerList().getPlayerByUUID(uuid);
    }

    public static RegistryKey<World> getDimension(String dimension) {
        return dimension.isEmpty() ? null : getDimension(ResourceLocationHelper.of(dimension));
    }

    public static RegistryKey<World> getDimension(ResourceLocation key) {
        return RegistryKey.getOrCreateKey(Registry.WORLD_KEY, key);
    }

    public static Optional <ServerWorld> getWorld(RegistryKey<World> key) {
        return Optional.ofNullable(ServerLifecycleHooks.getCurrentServer().getWorld(key));
    }

    public static Optional<ServerWorld> getWorld(String key) {
        return getWorld(getDimension(key));
    }

    public static GooeyButton filler = GooeyButton.builder()
            .display(new ItemStack(Blocks.GRAY_STAINED_GLASS_PANE, 1))
            .build();

    public boolean isPokeBall()
    {

        return false;
    }

    public boolean isPokeBall(ItemStack stack)
    {
        return stack.getItem().getRegistryName().equals(PixelmonItems.poke_ball.getRegistryName());
    }

    public static String getItemStackName(CompoundNBT compoundNBT)
    {
        ItemStack stack = ItemStack.read(compoundNBT);

        return getItemStackName(stack);
    }
    // TODO: 21/12/2022 Add capitalisation reformatted for lower cased word translations 

    public static String getItemStackName(ItemStack stack)
    {
        String formattedName = null;

        if (stack.hasDisplayName()) {
            formattedName = stack.getDisplayName().getUnformattedComponentText();
        } else {
           formattedName = stack.getItem().getRegistryName().getPath().replace("_", " ");
        }
        if (formattedName.isEmpty())
            formattedName = "Name Not Stored";
        return formattedName;
    }

    public static void sendSuccessFullMessage(ServerPlayerEntity playerEntity, String message)
    {
        StringTextComponent textComponent = new StringTextComponent(formattedString(TextUtil.getMessagePrefix().getString() + message));
        Style componentStyle = Style.EMPTY;
        componentStyle = componentStyle.applyFormatting(TextFormatting.GREEN);
        textComponent.setStyle(componentStyle);
        playerEntity.sendMessage(textComponent, playerEntity.getUniqueID());
    }

    public static void sendFailMessage(ServerPlayerEntity playerEntity, String message)
    {
        StringTextComponent textComponent = new StringTextComponent(formattedString(TextUtil.getMessagePrefix().getString() + message));
        Style componentStyle = Style.EMPTY;
        componentStyle = componentStyle.applyFormatting(TextFormatting.RED);
        textComponent.setStyle(componentStyle);
        playerEntity.sendMessage(textComponent, playerEntity.getUniqueID());
    }

    public static MinecraftServer getInstance() {
        return SERVER;
    }

    public static TextComponent commandTextComponent(String message, String command) {
        TextComponent component = new StringTextComponent(Util.formattedString(message));
        Style componentStyle = Style.EMPTY;
        componentStyle = componentStyle.applyFormatting(TextFormatting.YELLOW);
        componentStyle = componentStyle.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + command));
        component.setStyle(componentStyle);
        return component;
    }

    public static void sendInvite(UUID uuid, String message, Invite invite) {
        TextComponent textComponent = new StringTextComponent(Util.formattedString(TextUtil.getMessagePrefix().getString()+message));
        Style componentStyle = Style.EMPTY;
        componentStyle = componentStyle.applyFormatting(TextFormatting.YELLOW);
        componentStyle = componentStyle.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clandorus acceptinvite " + invite.getClan().getClanIdentifier()));
        textComponent.setStyle(componentStyle);
        getPlayer(uuid).sendMessage(textComponent, uuid);
    }

    public static void sendArray(UUID uuid, List<String> message) {
        for (String s:message) {
            getPlayer(uuid).sendMessage(new StringTextComponent(((TextUtil.getMessagePrefix()).getString() + s).replaceAll("&([0-9a-fk-or])", "\u00a7$1")), uuid);
        }
    }

    public static void sendArrayUnformatted(UUID uuid, List<String> message) {
        for (String s:message) {
            getPlayer(uuid).sendMessage(new StringTextComponent((s).replaceAll("&([0-9a-fk-or])", "\u00a7$1")), uuid);
        }
    }

    public static void send(UUID uuid, String message) {
        getPlayer(uuid).sendMessage(new StringTextComponent(((TextUtil.getMessagePrefix()).getString() + message).replaceAll("&([0-9a-fk-or])", "\u00a7$1")), uuid);
    }

    public static void send(ServerPlayerEntity player, String message) {
        if (player == null)
            return;
        player.sendMessage(new StringTextComponent(((TextUtil.getMessagePrefix()).getString() + message).replaceAll("&([0-9a-fk-or])", "\u00a7$1")), player.getUniqueID());
    }

    public static void sendNoFormat(UUID uuid, String message) {
        getPlayer(uuid).sendMessage(new StringTextComponent((message).replaceAll("&([0-9a-fk-or])", "\u00a7$1")), uuid);
    }

    public static void sendNoFormat(ServerPlayerEntity player, String message) {
        if (player == null)
            return;
        player.sendMessage(new StringTextComponent((message).replaceAll("&([0-9a-fk-or])", "\u00a7$1")), player.getUniqueID());
    }


    public static void send(CommandSource sender, String message) {
        sender.sendFeedback(new StringTextComponent(((TextUtil.getMessagePrefix()).getString() + message).replaceAll("&([0-9a-fk-or])", "\u00a7$1")), false);
    }


    public static void sendNoFormat(CommandSource sender, String message) {
        sender.sendFeedback(new StringTextComponent((message).replaceAll("&([0-9a-fk-or])", "\u00a7$1")), false);
    }


    public static String formattedString(String s) {
        return s.replaceAll("&", "§");
    }

    public static List <String> formattedArrayList(List<String> list) {

        List<String> formattedList = new ArrayList<>();
        for (String s:list) {
            formattedList.add(formattedString(s));
        }

        return formattedList;
    }
}
