package io.github.adainish.clandorus.command;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import io.github.adainish.clandorus.Clandorus;
import io.github.adainish.clandorus.api.RewardBuilder;
import io.github.adainish.clandorus.conf.LanguageConfig;
import io.github.adainish.clandorus.obj.Player;
import io.github.adainish.clandorus.obj.clan.Clan;
import io.github.adainish.clandorus.obj.clan.Invite;
import io.github.adainish.clandorus.storage.ClanStorage;
import io.github.adainish.clandorus.storage.PlayerStorage;
import io.github.adainish.clandorus.util.EconomyUtil;
import io.github.adainish.clandorus.util.PermissionUtil;
import io.github.adainish.clandorus.util.Util;
import io.github.adainish.clandorus.wrapper.PermissionWrapper;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;

import java.util.UUID;

public class ClanCommand {

    public static LiteralArgumentBuilder<CommandSource> getCommand() {
        return Commands.literal("clandorus")

                .executes(cc -> {
                    try {
                        Player player = PlayerStorage.getPlayer(cc.getSource().asPlayer().getUniqueID());
                        if (player == null) {
                            Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Player", "FailLoadingData").getString());
                            return 1;
                        }
                        if (player.inClan()) {
                            Clan clan = ClanStorage.getClan(player.getClanID());
                            if (clan == null)
                                throw new Exception("Clan Retrieval Exception");
                            Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Player", "ReturnClanName").getString().replace("%name%", clan.getClanName()));
                        } else {
                            Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Player", "NotInClan").getString());
                        }
                    } catch (Exception e) {
                        Clandorus.log.error(e);
                        Util.send(cc.getSource(), LanguageConfig.getConfig().get().node("Command", "Warning").getString());
                    }
                    return 1;
                })
                .then(Commands.literal("rewardbuilder")
                        .executes(cc ->
                        {
                            try {
                                RewardBuilder builder = new RewardBuilder();
                                builder.openNewRewardBuilder(cc.getSource().asPlayer());
                            } catch (Exception e)
                            {
                                e.printStackTrace();
                            }

                            return 1;
                        })
                )
                .then(Commands.literal("bank")
                        .executes(cc -> {
                            try {
                                Player player = PlayerStorage.getPlayer(cc.getSource().asPlayer().getUniqueID());
                                if (player == null) {
                                    Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Player", "FailLoadingData").getString());
                                    return 1;
                                }
                                if (player.inClan()) {
                                    Clan clan = ClanStorage.getClan(player.getClanID());
                                    if (clan != null)
                                    Util.sendSuccessFullMessage(cc.getSource().asPlayer(), "Your clan has %amount%$".replace("%amount%", String.valueOf(clan.getClanBank().balance)));
                                    else Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Player", "NotInClan").getString());
                                } else {
                                    Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Player", "NotInClan").getString());
                                }
                            } catch (Exception e) {
                                Clandorus.log.error(e);
                                Util.sendFailMessage(cc.getSource().asPlayer(), "Something went wrong executing this command, please try again later");
                            }

                            return 1;
                        })
                        .then(Commands.literal("remove")
                                .executes(cc -> {
                                    try {
                                        Player player = PlayerStorage.getPlayer(cc.getSource().asPlayer().getUniqueID());
                                        if (player == null) {
                                            Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Player", "FailLoadingData").getString());
                                            return 1;
                                        }
                                        if (player.inClan()) {
                                            Util.sendFailMessage(cc.getSource().asPlayer(), "Please add an amount");
                                        } else {
                                            Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Player", "NotInClan").getString());
                                        }
                                    } catch (Exception e) {
                                        Clandorus.log.error(e);
                                        Util.sendFailMessage(cc.getSource().asPlayer(), "Something went wrong executing this command, please try again later");
                                    }
                                    return 1;
                                })
                                .then(Commands.argument("amount", IntegerArgumentType.integer())
                                        .executes(cc -> {
                                            try {
                                                Player player = PlayerStorage.getPlayer(cc.getSource().asPlayer().getUniqueID());
                                                if (player == null) {
                                                    Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Player", "FailLoadingData").getString());
                                                    return 1;
                                                }
                                                if (player.inClan()) {
                                                    Clan clan = ClanStorage.getClan(player.getClanID());
                                                    if (clan == null)
                                                        throw new Exception("Clan Retrieval Exception");
                                                    if (clan.isLeader(player.getUuid())) {
                                                        int amount = IntegerArgumentType.getInteger(cc, "amount");

                                                        if (amount > 0) {
                                                            if (clan.getClanBank().canAfford(amount)) {
                                                                EconomyUtil.giveBalance(player.getUuid(), amount);
                                                                clan.getClanBank().takeFromAccount(amount);
                                                                //do audit logging
                                                                Util.sendSuccessFullMessage(cc.getSource().asPlayer(), "Added %amount% to your account!".replace("%amount%", String.valueOf(amount)));
                                                            } else {
                                                                Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Clan", "CantDonate").getString());
                                                            }
                                                        } else {
                                                            Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Clan", "DonationTooSmall").getString());
                                                        }

                                                    } else Util.sendFailMessage(cc.getSource().asPlayer(), "Only the leader can take money!");
                                                } else {
                                                    Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Player", "NotInClan").getString());
                                                }
                                            } catch (Exception e) {
                                                Clandorus.log.error(e);
                                                Util.sendFailMessage(cc.getSource().asPlayer(), "Something went wrong executing this command, please try again later");
                                            }

                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("add")
                                .executes(cc -> {
                                    try {
                                        Player player = PlayerStorage.getPlayer(cc.getSource().asPlayer().getUniqueID());
                                        if (player == null) {
                                            Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Player", "FailLoadingData").getString());
                                            return 1;
                                        }
                                        if (player.inClan()) {
                                            Util.sendFailMessage(cc.getSource().asPlayer(), "Please add an amount");
                                        } else {
                                            Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Player", "NotInClan").getString());
                                        }
                                    } catch (Exception e) {
                                        Clandorus.log.error(e);
                                        Util.sendFailMessage(cc.getSource().asPlayer(), "Something went wrong executing this command, please try again later");
                                    }
                                    return 1;
                                })
                                .then(Commands.argument("amount", IntegerArgumentType.integer())
                                        .executes(cc -> {
                                            try {
                                                Player player = PlayerStorage.getPlayer(cc.getSource().asPlayer().getUniqueID());
                                                if (player == null) {
                                                    Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Player", "FailLoadingData").getString());
                                                    return 1;
                                                }
                                                if (player.inClan()) {
                                                    Clan clan = ClanStorage.getClan(player.getClanID());
                                                    if (clan == null)
                                                        throw new Exception("Clan Retrieval Exception");

                                                    int amount = IntegerArgumentType.getInteger(cc, "amount");

                                                    if (amount > 0) {
                                                        if (EconomyUtil.canAfford(player.getUuid(), amount)) {
                                                            EconomyUtil.takeBalance(player.getUuid(), amount);
                                                            clan.getClanBank().addToAccount(amount);
                                                            clan.updateCache();
                                                            Util.sendSuccessFullMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Clan", "Donated").getString().replace("%amount%", String.valueOf(amount)));
                                                        } else {
                                                            Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Clan", "CantDonate").getString());
                                                        }
                                                    } else {
                                                        Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Clan", "DonationTooSmall").getString());
                                                    }

                                                } else {
                                                    Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Player", "NotInClan").getString());
                                                }
                                            } catch (Exception e) {
                                                Clandorus.log.error(e);
                                                Util.sendFailMessage(cc.getSource().asPlayer(), "Something went wrong executing this command, please try again later");
                                            }

                                            return 1;
                                        })
                                )
                        )
                )
                .then(Commands.literal("reload")
                        .requires(cs -> PermissionUtil.checkPermAsPlayer(cs, PermissionWrapper.adminPermission))
                        .executes(cc -> {
                            Util.send(cc.getSource(), "&eReloaded the Clans Mod, please check the console for any errors.");
                            Clandorus.getInstance().reload();
                            return 1;
                        })
                )
                .then(Commands.literal("audit")
                        .executes(cc -> {
                            cc.getSource().sendFeedback(new StringTextComponent("This feature is currently being built!"), true);
                            return 1;
                        })
                )
                .then(Commands.literal("members")
                        .executes(cc -> {
                            try {
                                Player player = PlayerStorage.getPlayer(cc.getSource().asPlayer().getUniqueID());
                                if (player == null) {
                                    Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Player", "FailLoadingData").getString());
                                    return 1;
                                }
                                if (player.inClan()) {
                                    Clan clan = ClanStorage.getClan(player.getClanID());
                                    if (clan == null)
                                        throw new Exception("Clan Retrieval Exception");
                                    clan.viewMemberManagement(cc.getSource().asPlayer());
                                } else {
                                    Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Player", "NotInClan").getString());
                                }
                            } catch (Exception e) {
                                Clandorus.log.error(e);
                                Util.send(cc.getSource(), "&cSomething went wrong executing this command, please try again later");
                            }
                            return 1;
                        })
                )
                .then(Commands.literal("disband")
                        .executes(cc -> {
                            try {
                                Player player = PlayerStorage.getPlayer(cc.getSource().asPlayer().getUniqueID());
                                if (player == null) {
                                    Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Player", "FailLoadingData").getString());
                                    return 1;
                                }
                                if (player.inClan()) {
                                    Clan clan = ClanStorage.getClan(player.getClanID());
                                    if (clan == null)
                                        throw new Exception("Clan Retrieval Exception");
                                    if (clan.isLeader(player.getUuid())) {
                                        clan.disband();
                                        Util.send(player.getUuid(), LanguageConfig.getConfig().get().node("Clan", "Disbanded").getString());
                                    } else {
                                        Util.send(player.getUuid(), LanguageConfig.getConfig().get().node("Clan", "LeaderOnlyDisband").getString());
                                    }
                                } else {
                                    Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Player", "NotInClan").getString());
                                }
                            } catch (Exception e) {
                                Clandorus.log.error(e);
                                Util.send(cc.getSource(), "&cSomething went wrong executing this command, please try again later");
                            }
                            return 1;
                        })
                )
                .then(Commands.literal("invite")
                        .then(Commands.argument("player", StringArgumentType.string())
                                .executes(cc -> {

                                    try {
                                        Player player = PlayerStorage.getPlayer(cc.getSource().asPlayer().getUniqueID());
                                        if (player == null) {
                                            Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Player", "FailLoadingData").getString());
                                            return 1;
                                        }
                                        if (!player.inClan()) {
                                            Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Player", "NotInClan").getString());
                                            return 1;
                                        }
                                        Clan clan = ClanStorage.getClan(player.getClanID());
                                        if (clan == null) {
                                            Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Player", "FailLoadingData").getString());
                                            return 1;
                                        }

                                        if (!clan.isLeader(player.getUuid())) {
                                            Util.send(player.getUuid(), LanguageConfig.getConfig().get().node("Invite", "OnlyLeaderInvite").getString());
                                            return 1;
                                        }

                                        String target = StringArgumentType.getString(cc, "player");
                                        ServerPlayerEntity playerEntity = Util.getPlayer(target);
                                        if (playerEntity == null) {
                                            Util.send(player.getUuid(), LanguageConfig.getConfig().get().node("Command", "FailedRetrievingPlayer").getString());
                                            return 1;
                                        }

                                        Player targetPlayer = PlayerStorage.getPlayer(playerEntity.getUniqueID());

                                        if (targetPlayer == null) {
                                            throw new Exception(LanguageConfig.getConfig().get().node("Command", "FailedRetrievingPlayer").getString());
                                        }

                                        if (targetPlayer.getUuid().equals(player.getUuid())) {
                                            Util.send(player.getUuid(), LanguageConfig.getConfig().get().node("Invite", "InvitedSelf").getString());
                                            return 1;
                                        }

                                        if (targetPlayer.getClanID() != null) {
                                            Util.send(player.getUuid(), LanguageConfig.getConfig().get().node("Invite", "InAClan").getString());
                                            return 1;
                                        }
                                        if (clan.getClanMembers().contains(targetPlayer.getUuid())) {
                                            Util.send(player.getUuid(), LanguageConfig.getConfig().get().node("Invite", "InTheClan").getString());
                                            return 1;
                                        }
                                        Invite invite = new Invite(clan, targetPlayer, player.getUuid());
                                        Clandorus.inviteWrapper.inviteList.add(invite);
                                        Util.sendSuccessFullMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Invite", "Invited").getString().replace("%target%", target));
                                        Util.sendInvite(
                                                targetPlayer.getUuid(),
                                                LanguageConfig.getConfig().get().node("Invite", "Invite").getString()
                                                        .replace("%clan%", clan.getClanName())
                                                        .replace("%sender%", Util.getPlayerName(player.getUuid()))
                                                , invite);
                                    } catch (Exception e) {
                                        Clandorus.log.error(e);
                                        Util.send(cc.getSource(), "&cSomething went wrong executing this command, please try again later");
                                    }

                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("acceptinvite")
                        .then(Commands.argument("clanid", StringArgumentType.string())
                                .executes(cc -> {
                                    try {
                                        Player player = PlayerStorage.getPlayer(cc.getSource().asPlayer().getUniqueID());
                                        if (player == null)
                                            throw new Exception(LanguageConfig.getConfig().get().node("Player", "FailLoadingData").getString());
                                        UUID uuid = UUID.fromString(StringArgumentType.getString(cc, "clanid"));
                                        Clan clan = ClanStorage.getClan(uuid);
                                        if (clan == null) {
                                            throw new Exception(LanguageConfig.getConfig().get().node("Invite", "Failed").getString());
                                        }
                                        Invite invite = Clandorus.inviteWrapper.getInvite(clan, player);
                                        if (invite == null)
                                            throw new Exception(LanguageConfig.getConfig().get().node("Invite", "Failed").getString());
                                        if (clan.isMember(player.getUuid())) {
                                            throw new Exception(LanguageConfig.getConfig().get().node("Invite", "AlreadyAMember").getString().replace("%clan%", clan.getClanName()));
                                        }
                                        if (invite.isExpiredInvite())
                                            throw new Exception(LanguageConfig.getConfig().get().node("Invite", "Expired").getString());
                                        invite.setInviteAccepted(true);
                                        clan.addMemberToClan(player);
                                        Util.send(player.getUuid(), LanguageConfig.getConfig().get().node("Invite", "Joined").getString().replace("%name%", clan.getClanName()));
                                    } catch (Exception e) {
                                        Util.send(cc.getSource().asPlayer().getUniqueID(), "&e" + e.getMessage());
                                    }

                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("create")
                        .then(Commands.argument("clanname", StringArgumentType.greedyString())
                                .executes(cc -> {
                                    try {
                                        Player player = PlayerStorage.getPlayer(cc.getSource().asPlayer().getUniqueID());
                                        if (player == null) {
                                            Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Player", "FailLoadingData").getString());
                                            return 1;
                                        }
                                        if (player.inClan()) {
                                            Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Player", "InClan").getString());
                                            return 1;
                                        }
                                        String clanname = StringArgumentType.getString(cc, "clanname");
                                        if (clanname.length() > 8) {
                                            Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Clan", "CharacterLimit").getString());
                                            return 1;
                                        }
                                        Clan clan = new Clan(player, clanname);
                                        ClanStorage.makeClan(clan);
                                        Util.sendSuccessFullMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Clan", "ClanCreated").getString().replace("%name%", clanname));
                                        player.setClanID(clan.getClanIdentifier());
                                        player.updateCache();
                                    } catch (Exception e) {
                                        Clandorus.log.error(e);
                                        Util.sendFailMessage(cc.getSource().asPlayer(), "Something went wrong executing this command, please try again later");
                                    }

                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("pokemonstorage")
                        .executes(cc -> {
                            try {
                                Player player = PlayerStorage.getPlayer(cc.getSource().asPlayer().getUniqueID());
                                if (player == null) {
                                    Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Player", "FailLoadingData").getString());
                                    return 1;
                                }
                                if (player.inClan()) {
                                    Clan clan = ClanStorage.getClan(player.getClanID());
                                    if (clan == null)
                                        throw new Exception("Clan Retrieval Exception");

                                    clan.openPokemonStorage(cc.getSource().asPlayer());
                                } else {
                                    Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Player", "NotInClan").getString());
                                }
                            } catch (Exception e) {
                                Clandorus.log.error(e);
                                Util.sendFailMessage(cc.getSource().asPlayer(), "Something went wrong executing this command, please try again later");
                            }
                            return 1;
                        })
                        .then(Commands.literal("add")
                                .executes(cc ->
                                {
                                    try {
                                        Player player = PlayerStorage.getPlayer(cc.getSource().asPlayer().getUniqueID());
                                        if (player == null) {
                                            Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Player", "FailLoadingData").getString());
                                            return 1;
                                        }
                                        if (player.inClan()) {
                                            Clan clan = ClanStorage.getClan(player.getClanID());
                                            if (clan == null)
                                                throw new Exception("Clan Retrieval Exception");
                                            Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Storage", "ValidSlotPokemon").getString());
                                            return 1;
                                        } else {
                                            Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Player", "NotInClan").getString());
                                        }
                                    } catch (Exception e) {
                                        Clandorus.log.error(e);
                                        Util.sendFailMessage(cc.getSource().asPlayer(), "Something went wrong executing this command, please try again later");
                                    }
                                    return 1;
                                })
                                .then(Commands.argument("slotnumber", IntegerArgumentType.integer(1, 6))
                                        .executes(cc -> {
                                            try {
                                                Player player = PlayerStorage.getPlayer(cc.getSource().asPlayer().getUniqueID());
                                                if (player == null) {
                                                    Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Player", "FailLoadingData").getString());
                                                    return 1;
                                                }
                                                if (player.inClan()) {
                                                    Clan clan = ClanStorage.getClan(player.getClanID());
                                                    if (clan == null)
                                                        throw new Exception("Clan Retrieval Exception");
                                                    int slot = IntegerArgumentType.getInteger(cc, "slotnumber");
                                                    int actualSlot = slot - 1;
                                                    PlayerPartyStorage pps = StorageProxy.getParty(cc.getSource().asPlayer().getUniqueID());
                                                    if (pps.get(actualSlot) != null) {
                                                        Pokemon p = pps.get(actualSlot);
                                                        pps.set(actualSlot, null);
                                                        clan.getPokemonStorage().addToStorage(p);
                                                        clan.updateCache();
                                                        Util.sendSuccessFullMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Storage", "AddedPokemon").getString().replace("%p%", p.getSpecies().getName()));
                                                    } else {
                                                        Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Storage", "ProvideValidPokemon").getString());
                                                        return 1;
                                                    }
                                                    return 1;
                                                } else {
                                                    Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Player", "NotInClan").getString());
                                                }
                                            } catch (Exception e) {
                                                Clandorus.log.error(e);
                                                Util.sendFailMessage(cc.getSource().asPlayer(), "Something went wrong executing this command, please try again later");
                                            }
                                            return 1;
                                        })
                                )
                        )

                )
                .then(Commands.literal("itemstorage")
                        .executes(cc -> {
                            try {
                                Player player = PlayerStorage.getPlayer(cc.getSource().asPlayer().getUniqueID());
                                if (player == null) {
                                    Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Player", "FailLoadingData").getString());
                                    return 1;
                                }
                                if (player.inClan()) {
                                    Clan clan = ClanStorage.getClan(player.getClanID());
                                    if (clan == null)
                                        throw new Exception("Clan Retrieval Exception");
                                    clan.openItemStorage(cc.getSource().asPlayer());
                                } else {
                                    Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Player", "NotInClan").getString());
                                }
                            } catch (Exception e) {
                                Clandorus.log.error(e);
                                Util.sendFailMessage(cc.getSource().asPlayer(), "Something went wrong executing this command, please try again later");
                            }
                            return 1;
                        })
                        .then(Commands.literal("add")
                                .executes(cc ->
                                {
                                    try {
                                        Player player = PlayerStorage.getPlayer(cc.getSource().asPlayer().getUniqueID());
                                        if (player == null) {
                                            Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Player", "FailLoadingData").getString());
                                            return 1;
                                        }
                                        if (player.inClan()) {
                                            Clan clan = ClanStorage.getClan(player.getClanID());
                                            if (clan == null)
                                                throw new Exception("Clan Retrieval Exception");
                                            Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Storage", "ValidSlotItem").getString());
                                            return 1;
                                        } else {
                                            Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Player", "NotInClan").getString());
                                        }
                                    } catch (Exception e) {
                                        Clandorus.log.error(e);
                                        Util.sendFailMessage(cc.getSource().asPlayer(), "Something went wrong executing this command, please try again later");
                                    }
                                    return 1;
                                })
                                .then(Commands.argument("slotnumber", IntegerArgumentType.integer(1, 9))
                                        .executes(cc -> {
                                            try {
                                                Player player = PlayerStorage.getPlayer(cc.getSource().asPlayer().getUniqueID());
                                                if (player == null) {
                                                    Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Player", "FailLoadingData").getString());
                                                    return 1;
                                                }
                                                if (player.inClan()) {
                                                    Clan clan = ClanStorage.getClan(player.getClanID());
                                                    if (clan == null)
                                                        throw new Exception("Clan Retrieval Exception");
                                                    int slot = IntegerArgumentType.getInteger(cc, "slotnumber");
                                                    int actualSlot = slot - 1;
                                                    ServerPlayerEntity playerEntity = Util.getPlayer(player.getUuid());
                                                    if (!playerEntity.inventory.getStackInSlot(actualSlot).isEmpty()) {
                                                        ItemStack stack = playerEntity.inventory.getStackInSlot(actualSlot);
                                                        clan.getItemStorage().addToStorage(stack);
                                                        clan.updateCache();
                                                        playerEntity.inventory.removeStackFromSlot(actualSlot);

                                                        Util.sendSuccessFullMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Storage", "AddedItem")
                                                                .getString().replace("%item%", Util.getItemStackName(stack))
                                                                .replace("%amount%", String.valueOf(stack.getCount()))
                                                        );
                                                    } else {
                                                        Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Storage", "ProvideValidItem").getString());
                                                        return 1;
                                                    }
                                                    return 1;
                                                } else {
                                                    Util.sendFailMessage(cc.getSource().asPlayer(), LanguageConfig.getConfig().get().node("Player", "NotInClan").getString());
                                                }
                                            } catch (Exception e) {
                                                Clandorus.log.error(e);
                                                Util.send(cc.getSource().asPlayer(), "Something went wrong executing this command, please try again later");
                                            }
                                            return 1;
                                        })
                                )
                        )
                )
                ;
    }
}
