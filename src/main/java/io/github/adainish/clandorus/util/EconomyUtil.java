package io.github.adainish.clandorus.util;

import com.pixelmonmod.pixelmon.api.economy.BankAccountProxy;

import java.util.UUID;

public class EconomyUtil {
    public static int getPlayerBalance(UUID uuid) {
        return BankAccountProxy.getBankAccount(uuid).orElse(null).getBalance().intValue();
    }

    public static void setBalance(UUID uuid, int newBalance) {
        BankAccountProxy.getBankAccount(uuid).orElse(null).setBalance(newBalance);
    }

    public static void giveBalance(UUID uuid, int amount) {
        BankAccountProxy.getBankAccount(uuid).orElse(null).add(amount);
    }

    public static void takeBalance(UUID uuid, int amount) {
        setBalance(uuid, (getPlayerBalance(uuid) - amount));
    }

    public static void resetBalance(UUID uuid) {
        BankAccountProxy.getBankAccount(uuid).orElse(null).setBalance(0);
    }

    public static boolean canAfford(UUID uuid, int amount) {
        return (getPlayerBalance(uuid) - amount) >= 0;
    }
}
