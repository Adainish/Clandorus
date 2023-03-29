package io.github.adainish.clandorus;

import ca.landonjw.gooeylibs2.implementation.tasks.Task;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.pokemon.ability.Ability;
import com.pixelmonmod.pixelmon.api.pokemon.ability.AbilityRegistry;
import com.pixelmonmod.pixelmon.enums.technicalmoves.*;
import io.github.adainish.clandorus.command.ClanChatCommand;
import io.github.adainish.clandorus.command.ClanCommand;
import io.github.adainish.clandorus.conf.ClanGymConfig;
import io.github.adainish.clandorus.conf.DefaultTeamConfig;
import io.github.adainish.clandorus.conf.LanguageConfig;
import io.github.adainish.clandorus.conf.RewardConfig;
import io.github.adainish.clandorus.listener.*;
import io.github.adainish.clandorus.obj.Player;
import io.github.adainish.clandorus.obj.clan.Clan;
import io.github.adainish.clandorus.obj.gyms.ClanGym;
import io.github.adainish.clandorus.registry.ClanGymRegistry;
import io.github.adainish.clandorus.registry.DefaultTeamRegistry;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Mod("clandorus")
public class Clandorus {
    private static Clandorus instance;

    public static Clandorus getInstance() {
        return instance;
    }

    public static final String MOD_NAME = "Clandorus";
    public static final String VERSION = "1.0.0-Beta";
    public static final String AUTHORS = "Winglet";
    public static final String YEAR = "2023";

    public static final Logger log = LogManager.getLogger(MOD_NAME);
    private static MinecraftServer server;
    public static File configDir;
    public static File playerStorageDir;
    public static File storageDir;
    public static File clanStorageDir;
    public static File clanGymStorageDir;

    public static ClanWrapper clanWrapper;

    public static InviteWrapper inviteWrapper;

    public static PermissionWrapper permissionWrapper;

    public static List<Task> tasks = new ArrayList<>();

    public static RewardRegistry rewardRegistry;

    public static ClanGymRegistry clanGymRegistry;

    public static DefaultTeamRegistry defaultTeamRegistry;

    public static List<ITechnicalMove> iTechnicalMoveList = new ArrayList <>();

    public static List<Ability> abilityList = new ArrayList <>();

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
        loadClanGymRegistry();
        loadITechnicalMoves();
        loadAbilities();
    }


    @SubscribeEvent
    public void onServerShutDown(FMLServerStoppingEvent event)
    {
        shutdownTasks();
        log.warn("Saving Player Data");
        clanWrapper.playerCache.values().forEach(Player::savePlayer);
        log.warn("Saving Clan Data");
        clanWrapper.clanCache.values().forEach(Clan::save);
        if (rewardRegistry != null) {
            rewardRegistry.saveAll();
        }
    }

    public void loadITechnicalMoves()
    {
        iTechnicalMoveList.addAll(Arrays.asList(Gen1TechnicalMachines.values()));
        iTechnicalMoveList.addAll(Arrays.asList(Gen2TechnicalMachines.values()));
        iTechnicalMoveList.addAll(Arrays.asList(Gen3TechnicalMachines.values()));
        iTechnicalMoveList.addAll(Arrays.asList(Gen4TechnicalMachines.values()));
        iTechnicalMoveList.addAll(Arrays.asList(Gen5TechnicalMachines.values()));
        iTechnicalMoveList.addAll(Arrays.asList(Gen6TechnicalMachines.values()));
        iTechnicalMoveList.addAll(Arrays.asList(Gen7TechnicalMachines.values()));
        iTechnicalMoveList.addAll(Arrays.asList(Gen8TechnicalMachines.values()));
        iTechnicalMoveList.addAll(Arrays.asList(Gen8TechnicalRecords.values()));
    }

    public void loadAbilities()
    {
        for (List <Optional <Ability>> optionals : Arrays.asList(Arrays.asList(AbilityRegistry.ADAPTABILITY, AbilityRegistry.AERILATE, AbilityRegistry.AFTERMATH, AbilityRegistry.AIR_LOCK, AbilityRegistry.ANALYTIC, AbilityRegistry.ANGER_POINT, AbilityRegistry.ANTICIPATION, AbilityRegistry.ARENA_TRAP, AbilityRegistry.AROMA_VEIL, AbilityRegistry.AS_ONE, AbilityRegistry.AURA_BREAK, AbilityRegistry.BAD_DREAMS, AbilityRegistry.BALL_FETCH, AbilityRegistry.BATTERY, AbilityRegistry.BATTLE_ARMOUR, AbilityRegistry.BATTLE_BOND, AbilityRegistry.BEAST_BOOST, AbilityRegistry.BERSERK, AbilityRegistry.BIG_PECKS, AbilityRegistry.BLAZE, AbilityRegistry.BULLETPROOF, AbilityRegistry.CHEEK_POUCH, AbilityRegistry.CHILLING_NEIGH, AbilityRegistry.CHLOROPHYLL, AbilityRegistry.CLEAR_BODY, AbilityRegistry.CLOUD_NINE, AbilityRegistry.COLOR_CHANGE, AbilityRegistry.COMATOSE, AbilityRegistry.COMING_SOON, AbilityRegistry.COMPETITIVE, AbilityRegistry.COMPOUND_EYES, AbilityRegistry.CONTRARY, AbilityRegistry.CORROSION, AbilityRegistry.COTTON_DOWN, AbilityRegistry.CURIOUS_MEDICINE, AbilityRegistry.CURSED_BODY, AbilityRegistry.CUTE_CHARM, AbilityRegistry.DAMP, AbilityRegistry.DANCER, AbilityRegistry.DARK_AURA, AbilityRegistry.DAUNTLESS_SHIELD, AbilityRegistry.DAZZLING, AbilityRegistry.DEFEATIST, AbilityRegistry.DEFIANT, AbilityRegistry.DELTA_STREAM, AbilityRegistry.DESOLATE_LAND), Arrays.asList(AbilityRegistry.DISGUISE, AbilityRegistry.DOWNLOAD, AbilityRegistry.DRAGONS_MAW, AbilityRegistry.DRIZZLE, AbilityRegistry.DROUGHT, AbilityRegistry.DRY_SKIN, AbilityRegistry.EARLY_BIRD, AbilityRegistry.EFFECT_SPORE, AbilityRegistry.ELECTRIC_SURGE, AbilityRegistry.EMERGENCY_EXIT, AbilityRegistry.ERROR, AbilityRegistry.FAIRY_AURA, AbilityRegistry.FILTER, AbilityRegistry.FLAME_BODY, AbilityRegistry.FLARE_BOOST, AbilityRegistry.FLASH_FIRE, AbilityRegistry.FLOWER_GIFT, AbilityRegistry.FLOWER_VEIL, AbilityRegistry.FLUFFY, AbilityRegistry.FORECAST, AbilityRegistry.FOREWARN, AbilityRegistry.FRIEND_GUARD, AbilityRegistry.FRISK, AbilityRegistry.FULL_METAL_BODY, AbilityRegistry.FUR_COAT, AbilityRegistry.GALE_WINGS, AbilityRegistry.GALVANIZE, AbilityRegistry.GLUTTONY, AbilityRegistry.GOOEY, AbilityRegistry.GORILLA_TACTICS, AbilityRegistry.GRASS_PELT, AbilityRegistry.GRASSY_SURGE, AbilityRegistry.GRIM_NEIGH, AbilityRegistry.GULP_MISSILE, AbilityRegistry.GUTS, AbilityRegistry.HARVEST, AbilityRegistry.HEALER, AbilityRegistry.HEATPROOF, AbilityRegistry.HEAVY_METAL, AbilityRegistry.HONEY_GATHER, AbilityRegistry.HUGE_POWER, AbilityRegistry.HUNGER_SWITCH, AbilityRegistry.HUSTLE, AbilityRegistry.HYDRATION, AbilityRegistry.HYPER_CUTTER, AbilityRegistry.ICE_BODY, AbilityRegistry.ICE_FACE, AbilityRegistry.ICE_SCALES, AbilityRegistry.ILLUMINATE, AbilityRegistry.ILLUSION, AbilityRegistry.IMMUNITY, AbilityRegistry.IMPOSTER, AbilityRegistry.INFILTRATOR, AbilityRegistry.INNARDS_OUT, AbilityRegistry.INNER_FOCUS, AbilityRegistry.INSOMNIA, AbilityRegistry.INTIMIDATE, AbilityRegistry.INTREPID_SWORD), Arrays.asList(AbilityRegistry.IRON_BARBS, AbilityRegistry.IRON_FIST, AbilityRegistry.JUSTIFIED, AbilityRegistry.KEEN_EYE, AbilityRegistry.KLUTZ, AbilityRegistry.LEAF_GUARD, AbilityRegistry.LEVITATE, AbilityRegistry.LIBERO, AbilityRegistry.LIGHT_METAL, AbilityRegistry.LIGHTNING_ROD, AbilityRegistry.LIMBER, AbilityRegistry.LIQUID_OOZE, AbilityRegistry.LIQUID_VOICE, AbilityRegistry.LONG_REACH, AbilityRegistry.MAGIC_BOUNCE, AbilityRegistry.MAGIC_GUARD, AbilityRegistry.MAGICIAN, AbilityRegistry.MAGMA_ARMOR, AbilityRegistry.MAGNET_PULL, AbilityRegistry.MARVEL_SCALE, AbilityRegistry.MEGA_LAUNCHER, AbilityRegistry.MERCILESS, AbilityRegistry.MIMICRY, AbilityRegistry.MINUS, AbilityRegistry.MIRROR_ARMOR, AbilityRegistry.MISTY_SURGE, AbilityRegistry.MOLD_BREAKER, AbilityRegistry.MOODY, AbilityRegistry.MOTOR_DRIVE, AbilityRegistry.MOXIE, AbilityRegistry.MULTISCALE, AbilityRegistry.MULTITYPE, AbilityRegistry.MUMMY, AbilityRegistry.NATURAL_CURE, AbilityRegistry.NEUROFORCE, AbilityRegistry.NEUTRALIZING_GAS), Arrays.asList(AbilityRegistry.NO_GUARD, AbilityRegistry.NORMALIZE, AbilityRegistry.OBLIVIOUS, AbilityRegistry.OVERCOAT, AbilityRegistry.OVERGROW, AbilityRegistry.OWN_TEMPO, AbilityRegistry.PARENTAL_BOND, AbilityRegistry.PASTEL_VEIL, AbilityRegistry.PERISH_BODY, AbilityRegistry.PICKPOCKET, AbilityRegistry.PICKUP, AbilityRegistry.PIXILATE, AbilityRegistry.PLUS, AbilityRegistry.POISON_HEAL, AbilityRegistry.POISON_POINT, AbilityRegistry.POWER_CONSTRUCT, AbilityRegistry.POWER_OF_ALCHEMY, AbilityRegistry.POWER_SPOT, AbilityRegistry.PRANKSTER, AbilityRegistry.PRESSURE, AbilityRegistry.PRIMORDIAL_SEA, AbilityRegistry.PRISM_ARMOR, AbilityRegistry.PROPELLER_TAIL, AbilityRegistry.PROTEAN, AbilityRegistry.PSYCHIC_SURGE, AbilityRegistry.PUNK_ROCK, AbilityRegistry.PURE_POWER), Arrays.asList(AbilityRegistry.QUEENLY_MAJESTY, AbilityRegistry.QUICK_DRAW, AbilityRegistry.QUICK_FEET, AbilityRegistry.RAIN_DISH, AbilityRegistry.RATTLED, AbilityRegistry.RECEIVER, AbilityRegistry.RECKLESS, AbilityRegistry.REFRIGERATE, AbilityRegistry.REGENERATOR, AbilityRegistry.RIPEN, AbilityRegistry.RIVALRY, AbilityRegistry.R_K_S_SYSTEM, AbilityRegistry.ROCK_HEAD, AbilityRegistry.ROUGH_SKIN, AbilityRegistry.RUN_AWAY, AbilityRegistry.SAND_FORCE, AbilityRegistry.SAND_RUSH, AbilityRegistry.SAND_SPIT, AbilityRegistry.SAND_STREAM, AbilityRegistry.SAND_VEIL, AbilityRegistry.SAP_SIPPER, AbilityRegistry.SCHOOLING, AbilityRegistry.SCRAPPY, AbilityRegistry.SCREEN_CLEANER, AbilityRegistry.SERENE_GRACE, AbilityRegistry.SHADOW_TAG, AbilityRegistry.SHED_SKIN, AbilityRegistry.SHEER_FORCE, AbilityRegistry.SHELL_ARMOUR, AbilityRegistry.SHIELD_DUST, AbilityRegistry.SHIELDS_DOWN, AbilityRegistry.SIMPLE, AbilityRegistry.SKILL_LINK, AbilityRegistry.SLOW_START, AbilityRegistry.SLUSH_RUSH, AbilityRegistry.SNIPER, AbilityRegistry.SNOW_CLOAK, AbilityRegistry.SNOW_WARNING, AbilityRegistry.SOLAR_POWER, AbilityRegistry.SOLID_ROCK, AbilityRegistry.SOUL_HEART, AbilityRegistry.SOUNDPROOF, AbilityRegistry.SPEED_BOOST, AbilityRegistry.STAKEOUT, AbilityRegistry.STALL, AbilityRegistry.STALWART, AbilityRegistry.STAMINA, AbilityRegistry.STANCE_CHANGE, AbilityRegistry.STATIC, AbilityRegistry.STEADFAST, AbilityRegistry.STEAM_ENGINE, AbilityRegistry.STEELWORKER, AbilityRegistry.STEELY_SPIRIT, AbilityRegistry.STENCH, AbilityRegistry.STICKY_HOLD, AbilityRegistry.STORM_DRAIN, AbilityRegistry.STRONG_JAW, AbilityRegistry.STURDY, AbilityRegistry.SUCTION_CUPS, AbilityRegistry.SUPER_LUCK, AbilityRegistry.SURGE_SURFER, AbilityRegistry.SWARM, AbilityRegistry.SWEET_VEIL, AbilityRegistry.SWIFT_SWIM, AbilityRegistry.SYMBIOSIS, AbilityRegistry.SYNCHRONIZE, AbilityRegistry.TANGLED_FEET, AbilityRegistry.TANGLING_HAIR, AbilityRegistry.TECHNICIAN, AbilityRegistry.TELEPATHY, AbilityRegistry.TERAVOLT, AbilityRegistry.THICK_FAT, AbilityRegistry.TINTED_LENS, AbilityRegistry.TORRENT, AbilityRegistry.TOUGH_CLAWS, AbilityRegistry.TOXIC_BOOST, AbilityRegistry.TRACE, AbilityRegistry.TRANSISTOR, AbilityRegistry.TRIAGE, AbilityRegistry.TRUANT, AbilityRegistry.TURBOBLAZE, AbilityRegistry.UNAWARE, AbilityRegistry.UNBURDEN, AbilityRegistry.UNNERVE, AbilityRegistry.UNSEEN_FIST, AbilityRegistry.VICTORY_STAR, AbilityRegistry.VITAL_SPIRIT, AbilityRegistry.VOLT_ABSORB, AbilityRegistry.WANDERING_SPIRIT, AbilityRegistry.WATER_ABSORB, AbilityRegistry.WATER_BUBBLE, AbilityRegistry.WATER_COMPACTION, AbilityRegistry.WATER_VEIL, AbilityRegistry.WEAK_ARMOR, AbilityRegistry.WHITE_SMOKE, AbilityRegistry.WIMP_OUT, AbilityRegistry.WONDER_GUARD, AbilityRegistry.WONDER_SKIN, AbilityRegistry.ZEN_MODE, AbilityRegistry.REVENANT))) {
            optionals.forEach(ability -> {
                ability.ifPresent(value -> abilityList.add(value));
            });
        }
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
        clanGymStorageDir = new File(storageDir + "/GymData/");
        clanGymStorageDir.mkdirs();
    }

    public void setupConfigs() {
        log.log(Level.WARN, "Setting up config data to be read by Clandorus");
        LanguageConfig.getConfig().setup();
        RewardConfig.getConfig().setup();
        ClanGymConfig.getConfig().setup();
        DefaultTeamConfig.getConfig().load();
    }

    public void loadConfigs() {
        log.log(Level.WARN, "Loading and Reading Config Data for Clandorus");
        LanguageConfig.getConfig().load();
        RewardConfig.getConfig().load();
        ClanGymConfig.getConfig().load();
        DefaultTeamConfig.getConfig().load();
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

    public void loadDefaultTeamRegistry()
    {
        if (defaultTeamRegistry == null)
            defaultTeamRegistry = new DefaultTeamRegistry();
        //load
    }

    public void loadClanGymRegistry()
    {
        if (clanGymRegistry == null)
        {
            clanGymRegistry = new ClanGymRegistry();
        }
        clanGymRegistry.load();
    }

    public void reload()
    {
        log.warn("Reload requested, this isn't advised! If anything goes wrong we recommend rebooting your server over reloading!");
        shutdownTasks();
        if (rewardRegistry != null) {
            rewardRegistry.saveAll();
        }
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
        MinecraftForge.EVENT_BUS.register(new ItemInteractListener());
        Pixelmon.EVENT_BUS.register(new RewardBuilderDialogueInputListener());
        Pixelmon.EVENT_BUS.register(new MailBuilderDialogueInputListener());
        Pixelmon.EVENT_BUS.register(new GymBuilderDialogueInputListener());
        Pixelmon.EVENT_BUS.register(new DefaultTeamBuilderDialogueInputListener());
        Pixelmon.EVENT_BUS.register(new BattleListener());
    }



}
