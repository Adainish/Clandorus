package io.github.adainish.clandorus;

import ca.landonjw.gooeylibs2.implementation.tasks.Task;
import com.pixelmonmod.pixelmon.Pixelmon;
import io.github.adainish.clandorus.command.ClanChatCommand;
import io.github.adainish.clandorus.command.ClanCommand;
import io.github.adainish.clandorus.conf.LanguageConfig;
import io.github.adainish.clandorus.conf.RewardConfig;
import io.github.adainish.clandorus.listener.MailBuilderDialogueInputListener;
import io.github.adainish.clandorus.listener.RewardBuilderDialogueInputListener;
import io.github.adainish.clandorus.listener.PlayerListener;
import io.github.adainish.clandorus.obj.Player;
import io.github.adainish.clandorus.obj.clan.Clan;
import io.github.adainish.clandorus.registry.RewardRegistry;
import io.github.adainish.clandorus.tasks.UpdateClanDataTask;
import io.github.adainish.clandorus.tasks.UpdateInvitesTask;
import io.github.adainish.clandorus.wrapper.ClanWrapper;
import io.github.adainish.clandorus.wrapper.InviteWrapper;
import io.github.adainish.clandorus.wrapper.PermissionWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartedEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Mod("clandorus")
public class Clandorus {
    private static Clandorus instance;

    public static Clandorus getInstance() {
        return instance;
    }

    public static final String MOD_NAME = "Clandorus";
    public static final String VERSION = "1.0.0-Beta";
    public static final String AUTHORS = "Winglet";
    public static final String YEAR = "2022";

    public static final Logger log = LogManager.getLogger(MOD_NAME);
    private static MinecraftServer server;
    public static File configDir;
    public static File playerStorageDir;
    public static File storageDir;
    public static File clanStorageDir;

    public static ClanWrapper clanWrapper;

    public static InviteWrapper inviteWrapper;

    public static PermissionWrapper permissionWrapper;

    public static List<Task> tasks = new ArrayList<>();

    public static RewardRegistry rewardRegistry;

    public Clandorus() {
        instance = this;
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static File getConfigDir() {
        return configDir;
    }

    public static void setConfigDir(File configDir) {
        Clandorus.configDir = configDir;
    }

    public static File getPlayerStorageDir() {
        return playerStorageDir;
    }

    public static void setPlayerStorageDir(File playerStorageDir) {
        Clandorus.playerStorageDir = playerStorageDir;
    }

    public static MinecraftServer getServer() {
        return server;
    }

    public static void setServer(MinecraftServer server) {
        Clandorus.server = server;
    }

    private void setup(final FMLCommonSetupEvent event) {
        log.info("Booting up %n by %authors %v %y"
                .replace("%n", MOD_NAME)
                .replace("%authors", AUTHORS)
                .replace("%v", VERSION)
                .replace("%y", YEAR)
        );
        initDirs();
    }

    @SubscribeEvent
    public void onCommandRegistry(RegisterCommandsEvent event) {
        log.warn("Registering permission nodes");
        permissionWrapper = new PermissionWrapper();
        log.warn("Registering Commands");
        event.getDispatcher().register(ClanCommand.getCommand());
        event.getDispatcher().register(ClanChatCommand.getCommand());
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        initConfig();
    }

    @SubscribeEvent
    public void onServerStarted(FMLServerStartedEvent event) {
        setServer(ServerLifecycleHooks.getCurrentServer());
        loadClanWrapper();
        initListeners();
        initTasks();
        loadRewardRegistry();
    }


    @SubscribeEvent
    public void onServerShutDown(FMLServerStoppingEvent event)
    {
        shutdownTasks();
        log.warn("Saving Player Data");
        clanWrapper.playerCache.values().forEach(Player::savePlayer);
        log.warn("Saving Clan Data");
        clanWrapper.clanCache.values().forEach(Clan::save);
    }

    public void initDirs() {
        log.log(Level.WARN, "Setting up Storage Paths and Directories for Clandorus");
        setConfigDir((new File(FMLPaths.GAMEDIR.get().resolve(FMLConfig.defaultConfigPath()).toString())));
        getConfigDir().mkdir();
        storageDir = new File(configDir + "/Clandorus/Storage/");
        storageDir.mkdirs();
        playerStorageDir = new File(storageDir + "/PlayerData/");
        playerStorageDir.mkdirs();
        clanStorageDir = new File(storageDir + "/TeamData/");
        clanStorageDir.mkdirs();
    }

    public void setupConfigs() {
        log.log(Level.WARN, "Setting up config data to be read by Clandorus");
        LanguageConfig.getConfig().setup();
        RewardConfig.getConfig().setup();
    }

    public void loadConfigs() {
        log.log(Level.WARN, "Loading and Reading Config Data for Clandorus");
        LanguageConfig.getConfig().load();
        RewardConfig.getConfig().load();
    }

    public void initConfig() {
        setupConfigs();
        loadConfigs();
    }

    public void loadClanWrapper() {
        log.log(Level.WARN, "Loading Clan Data from Config and Storage");
        clanWrapper = new ClanWrapper();
        inviteWrapper = new InviteWrapper();
    }

    public void loadRewardRegistry()
    {
        rewardRegistry = new RewardRegistry();
    }


    public void reload()
    {
        log.warn("Reload requested, this isn't advised! If anything goes wrong we recommend rebooting your server over reloading!");
        shutdownTasks();
        setupConfigs();
        loadConfigs();
        loadRewardRegistry();
    }

    public void initTasks()
    {
        log.warn("Loading up Tasks for Clandorus");
        tasks.add(Task.builder().infinite().execute(new UpdateInvitesTask()).interval(20).build());
        tasks.add(Task.builder().infinite().execute(new UpdateClanDataTask()).interval(20).build());
    }

    public void shutdownTasks() {
        log.info("Shutting down Tasks");
        for (Task t:tasks) {
            t.setExpired();
        }
        log.info("Shut down all Tasks");
    }

    public void initListeners() {
        MinecraftForge.EVENT_BUS.register(new PlayerListener());
        Pixelmon.EVENT_BUS.register(new RewardBuilderDialogueInputListener());
        Pixelmon.EVENT_BUS.register(new MailBuilderDialogueInputListener());
//        Pixelmon.EVENT_BUS.register(new BattleListener());
    }



}
