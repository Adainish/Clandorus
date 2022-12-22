package io.github.adainish.clandorus.api;

import io.github.adainish.clandorus.obj.Player;
import io.github.adainish.clandorus.obj.clan.Clan;
import net.minecraftforge.eventbus.api.Event;

public class ClanEvent extends Event {
    private Clan clan;

    public ClanEvent(Clan clan) {
        this.clan = clan;
    }

    public Clan getClan() {
        return clan;
    }

    public static class ClanChatEvent extends ClanEvent {
        Player sender;
        String message;
        public ClanChatEvent(Clan clan, Player sender, String message) {
            super(clan);
            this.sender = sender;
            this.message = message;
        }
    }

    public static class ClanKickEvent extends ClanEvent {
        Player sender;
        Player kicked;
        public ClanKickEvent(Clan clan, Player sender, Player kicked)
        {
            super(clan);
            this.sender = sender;
            this.kicked = kicked;
        }
    }

    public static class ClanBankTakeEvent extends ClanEvent
    {
        Player donator;
        int amount;
        public ClanBankTakeEvent(Clan clan, Player donator, int amount)
        {
            super(clan);
            this.donator = donator;
            this.amount = amount;
        }
    }

    public static class ClanBankDonateEvent extends ClanEvent
    {
        Player donator;
        int amount;
        public ClanBankDonateEvent(Clan clan, Player donator, int amount)
        {
            super(clan);
            this.donator = donator;
            this.amount = amount;
        }
    }

    public static class ClanCreateEvent extends ClanEvent {

        public ClanCreateEvent(Clan clan) {
            super(clan);
        }
    }

    public static class ClanDeleteEvent extends ClanEvent {

        public ClanDeleteEvent(Clan clan) {
            super(clan);
        }
    }
}
