package io.github.adainish.clandorus.conf;

import info.pixelmon.repack.org.spongepowered.serialize.SerializationException;
import io.github.adainish.clandorus.Clandorus;

public class LanguageConfig extends Configurable
{
    private static LanguageConfig config;

    public static LanguageConfig getConfig() {
        if (config == null) {
            config = new LanguageConfig();
        }
        return config;
    }

    public void setup() {
        super.setup();
    }

    public void load() {
        super.load();
    }

    public void populate() {
        try {
            this.get().node("Command", "Warning").set("&cSomething went wrong executing this command, please try again later");
            this.get().node("Command", "PlayerNotOnline").set("&ePlease provide an online player's name");
            this.get().node("Command", "FailedRetrievingPlayer").set("Player could not be retrieved");

            this.get().node("Player", "FailLoadingData").set("&cSomething went wrong loading your data");
            this.get().node("Player", "ReturnClanName").set("&cYou're currently in the Clan %name%");
            this.get().node("Player", "NotInClan").set("&cYou're not currently in a Clan, please either make a Clan or join one");
            this.get().node("Player", "InClan").set("You're currently in a Clan!");

            this.get().node("Clan", "Donated").set("You donated %amount% to your clan");
            this.get().node("Clan", "CantDonate").set("You can't afford to donate this amount to your clan!");
            this.get().node("Clan", "DonationTooSmall").set("You can't donate an amount of 0 to your clan!");
            this.get().node("Clan", "Upgrade").set("&aYour Clan has upgraded %name%!");
            this.get().node("Clan", "NewMember").set("&a%p% &7has joined the Clan");
            this.get().node("Clan", "NotEligibleUpgrade").set("Your clan is currently not eligible for an upgrade.");
            this.get().node("Clan", "EligibleUpgrade").set("&aThe Clan is now eligible for a Rank Upgrade!");
            this.get().node("Clan", "Disbanded").set("&cSuccessfully disbanded the clan");
            this.get().node("Clan", "LeaderOnlyDisband").set("&cOnly the leader may disband the clan");

            this.get().node("Clan", "CharacterLimit").set("The character limit for clan names is 8");
            this.get().node("Clan", "ClanCreated").set("You just made a Clan called : %name%");
            this.get().node("Clan", "IsLeader").set("&4Only the leader can invite players");
            this.get().node("Clan", "KickedPlayer").set("&4You've kicked %name% from the clan");

            this.get().node("Storage", "InventoryFull").set("&cYou need space in your inventory before you retrieve an item!");
            this.get().node("Storage", "AddedItem").set("Added %item% to the clan storage");
            this.get().node("Storage", "AddedPokemon").set("Added %p% to the clan storage");
            this.get().node("Storage", "ValidSlotPokemon").set("Please provide a valid slot number between 1-6");
            this.get().node("Storage", "ValidSlotItem").set("Please provide a valid slot number between 1-9");
            this.get().node("Storage", "ProvideValidPokemon").set("Please provide a valid Pokemon");
            this.get().node("Storage", "ProvideValidItem").set("Please provide a valid Item");


            this.get().node("Invite", "ExpiredInvited").set("&b(&a!&b) &eYour invite to %clan% has expired");
            this.get().node("Invite", "ExpiredInvitee").set("&b(&e!&b) &eYour Clan invite to %clan% has expired");
            this.get().node("Invite", "InvitedSelf").set("&eYou can't invite yourself silly!");
            this.get().node("Invite", "InAClan").set("&cThis player is already in a Clan");
            this.get().node("Invite", "InTheClan").set("&cThis player is already in the Clan");
            this.get().node("Invite", "Invited").set("You invited %target% to join your Clan");
            this.get().node("Invite", "Invite").set("&eYou've been invited to join the Clan %clan% by %sender%. Click this message to accept the invite");

            this.get().node("Invite", "OnlyLeaderInvite").set("&4Only the leader can invite players");
            this.get().node("Invite", "Failed").set("Something went wrong accepting this invite");
            this.get().node("Invite", "AlreadyAMember").set("&aYou're already a member of %clan%");
            this.get().node("Invite", "Expired").set("&eThis invite has expired, please contact the clans leader for a new one!");
            this.get().node("Invite", "Joined").set("&eYou joined %name%");


        } catch (SerializationException e) {
            Clandorus.log.error(e);
        }


    }

    public String getConfigName() {
        return "language.hocon";
    }

    public LanguageConfig() {
    }
}
