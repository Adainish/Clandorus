package io.github.adainish.clandorus.wrapper;

import io.github.adainish.clandorus.Clandorus;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import org.apache.logging.log4j.Level;

public class PermissionWrapper {
    public static String clanChatPermission = "clandorus.clanchat.base";
    public static String adminPermission = "clandorus.admin";
    public PermissionWrapper() {
        registerPermissions();
    }
    public void registerPermissions() {
        registerCommandPermission(clanChatPermission, "The clan chat base permission players need to use it");
        registerCommandPermission(adminPermission, "The clan admin permission");
    }
    public static void registerCommandPermission(String s) {
        if (s == null || s.isEmpty()) {
            Clandorus.log.log(Level.FATAL, "Trying to register a permission node failed, please check any configs for null/empty Configs");
            return;
        }
        PermissionAPI.registerNode(s, DefaultPermissionLevel.NONE, s);
    }

    public static void registerCommandPermission(String s, String description) {
        if (s == null || s.isEmpty()) {
            Clandorus.log.log(Level.FATAL, "Trying to register a permission node failed, please check any configs for null/empty Configs");
            return;
        }
        PermissionAPI.registerNode(s, DefaultPermissionLevel.NONE, description);
    }
}
