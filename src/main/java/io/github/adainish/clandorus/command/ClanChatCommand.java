package io.github.adainish.clandorus.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.adainish.clandorus.obj.Player;
import io.github.adainish.clandorus.obj.clan.Clan;
import io.github.adainish.clandorus.obj.clan.ClanChat;
import io.github.adainish.clandorus.storage.ClanStorage;
import io.github.adainish.clandorus.storage.PlayerStorage;
import io.github.adainish.clandorus.util.PermissionUtil;
import io.github.adainish.clandorus.util.Util;
import io.github.adainish.clandorus.wrapper.PermissionWrapper;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.Arrays;
import java.util.List;

public class ClanChatCommand {
    public static LiteralArgumentBuilder<CommandSource> getCommand() {
        return Commands.literal("clanchat")
                .requires(cs -> PermissionUtil.checkPermAsPlayer(cs, PermissionWrapper.clanChatPermission))
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(cc -> {
                            List<String> args = Arrays.asList(StringArgumentType.getString(cc, "message").split(" "));
                            StringBuilder msgBuilder = new StringBuilder();
                            for (String s : args) {
                                msgBuilder.append(" ").append(s);
                            }
                            ServerPlayerEntity entity = cc.getSource().asPlayer();
                            Player player = PlayerStorage.getPlayer(entity.getUniqueID());
                            if (player == null) {
                                Util.send(cc.getSource(), "&cYou need to be in a clan to use the clan chat");
                                return 1;
                            }
                            if (player.inClan()) {
                                Clan clan = ClanStorage.getClan(player.getClanID());
                                if (clan != null)
                                clan.getClanChat().sendClanMessage(clan, player, msgBuilder.toString());
                                else Util.sendFailMessage(entity, "Could not retrieve clan data");
                            }
                            return 1;
                        })
                );
    }
}
