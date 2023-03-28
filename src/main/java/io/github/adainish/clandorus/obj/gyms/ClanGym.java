package io.github.adainish.clandorus.obj.gyms;

import com.google.gson.Gson;
import com.pixelmonmod.pixelmon.api.battles.BattleType;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.battles.api.rules.BattleProperty;
import com.pixelmonmod.pixelmon.battles.api.rules.BattleRuleRegistry;
import com.pixelmonmod.pixelmon.battles.api.rules.BattleRules;
import com.pixelmonmod.pixelmon.battles.api.rules.clauses.BattleClause;
import com.pixelmonmod.pixelmon.battles.api.rules.clauses.BattleClauseRegistry;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import info.pixelmon.repack.org.spongepowered.serialize.SerializationException;
import io.github.adainish.clandorus.Clandorus;
import io.github.adainish.clandorus.conf.ClanGymConfig;
import io.github.adainish.clandorus.enumeration.GymWinActions;
import io.github.adainish.clandorus.obj.*;
import io.github.adainish.clandorus.storage.PlayerStorage;
import io.github.adainish.clandorus.util.Adapters;
import io.leangen.geantyref.TypeToken;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ClanGym {
    private String identifier;
    private UUID activeHoldingPlayer;

    private transient BattleRules battleRules;

    private transient List<BattleClause> battleClauses = new ArrayList<>();

    private transient List<String> battleProperties = new ArrayList<>();

    private long lastChallenged;

    private transient HoldRequirements holdRequirements;

    private OccupyingHolder occupyingHolder;

    private Location location;

    private List<Pokemon> activePokemon = new ArrayList<>();

    private List<GymWinActions> selectedWinActions = new ArrayList<>();

    private int gymEntityID = -1;

    private String defaultTeamID = "";

    private GymWinAction winAction;


    public ClanGym()
    {
        setOccupyingHolder(null);
        setHoldRequirements(new HoldRequirements());
        setWinAction(new GymWinAction());
        setLocation(new Location());
    }

    public ClanGym(String identifier)
    {
        this.setIdentifier(identifier);
        setOccupyingHolder(null);
        setHoldRequirements(new HoldRequirements());
        setWinAction(new GymWinAction());
        setLocation(new Location());
        syncConfig();
    }

    public void syncConfig() {
        this.location.setWorldID(ClanGymConfig.getConfig().get().node("Gyms", this.identifier, "Location", "WorldID").getString());
        this.location.setPosX(ClanGymConfig.getConfig().get().node("Gyms", this.identifier, "Location", "X").getDouble());
        this.location.setPosY(ClanGymConfig.getConfig().get().node("Gyms", this.identifier, "Location", "Y").getDouble());
        this.location.setPosZ(ClanGymConfig.getConfig().get().node("Gyms", this.identifier, "Location", "Z").getDouble());


        try {
            List <String> bannedSpecs = ClanGymConfig.getConfig().get().node("Gyms", this.identifier, "HoldRequirements", "BannedPokemonSpecs").getList(TypeToken.get(String.class));
            List <String> bannedAbilities = ClanGymConfig.getConfig().get().node("Gyms", this.identifier, "HoldRequirements", "BannedAbilities").getList(TypeToken.get(String.class));
            List <String> bannedAttacks = ClanGymConfig.getConfig().get().node("Gyms", this.identifier, "HoldRequirements", "BannedAttacks").getList(TypeToken.get(String.class));

            List <String> rewardIDS = ClanGymConfig.getConfig().get().node("Gyms", this.identifier, "WinAction", "RewardIDs").getList(TypeToken.get(String.class));
            List <String> pokemonSpecs = ClanGymConfig.getConfig().get().node("Gyms", this.identifier, "WinAction", "PokemonSpecs").getList(TypeToken.get(String.class));


            List <String> battleClausesList = ClanGymConfig.getConfig().get().node("Gyms", this.identifier, "BattleRules").getList(TypeToken.get(String.class));

            if (bannedSpecs != null)
                this.holdRequirements.bannedPokemonSpecs.addAll(bannedSpecs);
            if (bannedAbilities != null)
                this.holdRequirements.bannedAbilities.addAll(bannedAbilities);
            if (bannedAttacks != null)
                this.holdRequirements.bannedMoves.addAll(bannedAttacks);
            if (rewardIDS != null)
                this.winAction.rewardIDs.addAll(rewardIDS);
            if (pokemonSpecs != null)
                this.winAction.pokemonSpecList.addAll(pokemonSpecs);
            if (battleClausesList != null)
                battleClausesList.stream().map(BattleClauseRegistry::getClause).filter(Objects::nonNull).forEach(clause -> battleClauses.add(clause));

        } catch (SerializationException e) {
            Clandorus.log.error(e);
        }

        this.holdRequirements.min31Ivs = ClanGymConfig.getConfig().get().node("Gyms", this.identifier, "HoldRequirements", "MinIVS").getInt();
        this.winAction.takePokemon = ClanGymConfig.getConfig().get().node("Gyms", this.identifier, "WinAction", "TakePokemon").getBoolean();
        this.winAction.money = ClanGymConfig.getConfig().get().node("Gyms", this.identifier, "WinAction", "Money").getInt();
        this.defaultTeamID =  ClanGymConfig.getConfig().get().node("Gyms", this.identifier, "DefaultTeam").getString();
    }



    public void generateClanGym()
    {


    }

    public String getHoldingPlayerName()
    {
        Player holding = getHoldingPlayer();
        if (holding == null)
            return "No holder found";
        return holding.getName();
    }

    @Nullable
    public Player getHoldingPlayer()
    {
        if (getActiveHoldingPlayer() == null)
            return null;

        return PlayerStorage.getPlayer(getActiveHoldingPlayer());
    }

    public boolean isClanGymNPC(Entity entity)
    {
        return entity.getPersistentData().getBoolean("clandorusGym");
    }


    public void initiateBattle()
    {

    }

    public void loadGymNPC()
    {
        NPCTrainer trainer = getClanGymNPC();
        if (trainer == null) {
            Clandorus.log.warn("Clan Gym NPC for %clangym% did not exist, creating new one".replace("%clangym%", getIdentifier()));
            createNewClanGymNPC();
        }
    }

    public void save()
    {
        if (!Clandorus.clanGymRegistry.fileInStorage(identifier + ".json"))
        {
            Clandorus.clanGymRegistry.makeClanGym(this);
        }
        Clandorus.clanGymRegistry.save(this);
    }


    public void deleteNPCGym()
    {
        NPCTrainer trainer = getClanGymNPC();
        if (trainer != null) {
            trainer.getEntity().remove();
        }
    }

    public void loadBattleRules() {
        setBattleRules(new BattleRules(BattleType.SINGLE));
        getBattleRules().setNewClauses(getBattleClauses());
        initialiseBattleProperties();
    }

    public void initialiseBattleProperties()
    {
        for (String btp: getBattleProperties()) {
            BattleProperty battleProperty = BattleRuleRegistry.getProperty(btp);
            getBattleRules().set(battleProperty, "");
        }
    }

    public Location getLocation()
    {
        return location;
    }

    public World getWorld()
    {
        return getLocation().getWorld();
    }

    public NPCTrainer createNewClanGymNPC()
    {
        NPCTrainer npcTrainer = new NPCTrainer(getWorld());
        npcTrainer.getPersistentData().putBoolean("clandorusGym", true);
        npcTrainer.getPersistentData().putString("clandorusGymID", getIdentifier());
        for (int i = 0; i < 6; i++) {
            npcTrainer.getPokemonStorage().set(i, null);
        }
        if (getActivePokemon().isEmpty())
        {
            for (int i = 0; i < 6; i++) {
                Pokemon newPokemon = PokemonFactory.create(PixelmonSpecies.getRandomSpecies());
                npcTrainer.getPokemonStorage().set(i, newPokemon);
            }
        } else
        {
            for (int i = 0; i < getActivePokemon().size(); i++) {
                if (i >= 6)
                    break;
                Pokemon p = getActivePokemon().get(i);
                if (p == null)
                    continue;
                npcTrainer.getPokemonStorage().set(i, p);
            }
        }
        return npcTrainer;
    }

    public NPCTrainer getClanGymNPC()
    {
        World world = getWorld();
        if (world.isAreaLoaded(getLocation().returnBlockPos(), 40))
        {
            Location storedLocation = getLocation();
            AxisAlignedBB isWithinAABB = new AxisAlignedBB(getLocation().returnBlockPos());
            List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, isWithinAABB);
            for (Entity e:entities) {
                if (isClanGymNPC(e)) {
                    if (e.getPersistentData().getString("clandorusGymID").equals(getIdentifier())) {
                        Clandorus.log.log(Level.WARN, "Detected existing clandorus gym npc " + getIdentifier() + " loading to cache, no new one has to be created");
                        NPCTrainer trainer = (NPCTrainer) e;
                        if (trainer.getPosX() != storedLocation.getPosX() || trainer.getPosY() != storedLocation.getPosY() || trainer.getPosZ() != storedLocation.getPosZ())
                        {
                            trainer.setPositionAndUpdate(getLocation().getPosX(), getLocation().getPosY(), getLocation().getPosZ());
                        }
                        return trainer;
                    }
                }
            }
        }
        return null;
    }

    public NPCTrainer getOrCreateClanGymNPC()
    {
        World world = getWorld();
        if (world.isAreaLoaded(getLocation().returnBlockPos(), 40))
        {
            Location storedLocation = getLocation();
            AxisAlignedBB isWithinAABB = new AxisAlignedBB(getLocation().returnBlockPos());
            List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, isWithinAABB);
            for (Entity e:entities) {
                if (isClanGymNPC(e)) {
                    if (e.getPersistentData().getString("clandorusGymID").equals(getIdentifier())) {
                        Clandorus.log.log(Level.WARN, "Detected existing clandorys gym npc " + getIdentifier() + " loading to cache, no new one has to be created");
                        NPCTrainer trainer = (NPCTrainer) e;
                        if (trainer.getPosX() != storedLocation.getPosX() || trainer.getPosY() != storedLocation.getPosY() || trainer.getPosZ() != storedLocation.getPosZ())
                        {
                            trainer.setPositionAndUpdate(getLocation().getPosX(), getLocation().getPosY(), getLocation().getPosZ());
                        }
                        return trainer;
                    }
                }
            }
        } else
        {
            NPCTrainer npcTrainer = new NPCTrainer(getWorld());
            npcTrainer.getPersistentData().putBoolean("clandorusGym", true);
            npcTrainer.getPersistentData().putString("clandorusGymID", getIdentifier());
            for (int i = 0; i < 6; i++) {
                npcTrainer.getPokemonStorage().set(i, null);
            }
            if (getActivePokemon().isEmpty())
            {
                for (int i = 0; i < 6; i++) {
                    Pokemon newPokemon = PokemonFactory.create(PixelmonSpecies.getRandomSpecies());
                    npcTrainer.getPokemonStorage().set(i, newPokemon);
                }
            } else
            {
                for (int i = 0; i < getActivePokemon().size(); i++) {
                    if (i >= 6)
                        break;
                    Pokemon p = getActivePokemon().get(i);
                    if (p == null)
                        continue;
                    npcTrainer.getPokemonStorage().set(i, p);
                }
            }
            return npcTrainer;
        }
      return null;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public UUID getActiveHoldingPlayer() {
        return activeHoldingPlayer;
    }

    public void setActiveHoldingPlayer(UUID activeHoldingPlayer) {
        this.activeHoldingPlayer = activeHoldingPlayer;
    }

    public BattleRules getBattleRules() {
        return battleRules;
    }

    public void setBattleRules(BattleRules battleRules) {
        this.battleRules = battleRules;
    }

    public List <BattleClause> getBattleClauses() {
        return battleClauses;
    }

    public void setBattleClauses(List <BattleClause> battleClauses) {
        this.battleClauses = battleClauses;
    }

    public List <String> getBattleProperties() {
        return battleProperties;
    }

    public void setBattleProperties(List <String> battleProperties) {
        this.battleProperties = battleProperties;
    }

    public long getLastChallenged() {
        return lastChallenged;
    }

    public void setLastChallenged(long lastChallenged) {
        this.lastChallenged = lastChallenged;
    }

    public HoldRequirements getHoldRequirements() {
        return holdRequirements;
    }

    public void setHoldRequirements(HoldRequirements holdRequirements) {
        this.holdRequirements = holdRequirements;
    }

    public OccupyingHolder getOccupyingHolder() {
        return occupyingHolder;
    }

    public void setOccupyingHolder(OccupyingHolder occupyingHolder) {
        this.occupyingHolder = occupyingHolder;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public List <Pokemon> getActivePokemon() {
        return activePokemon;
    }

    public void setActivePokemon(List <Pokemon> activePokemon) {
        this.activePokemon = activePokemon;
    }

    public List <GymWinActions> getSelectedWinActions() {
        return selectedWinActions;
    }

    public void setSelectedWinActions(List <GymWinActions> selectedWinActions) {
        this.selectedWinActions = selectedWinActions;
    }

    public int getGymEntityID() {
        return gymEntityID;
    }

    public void setGymEntityID(int gymEntityID) {
        this.gymEntityID = gymEntityID;
    }

    public String getDefaultTeamID() {
        if (defaultTeamID.isEmpty())
            return "Undefined";
        return defaultTeamID;
    }

    public void setDefaultTeamID(String defaultTeamID) {
        this.defaultTeamID = defaultTeamID;
    }

    public GymWinAction getWinAction() {
        return winAction;
    }

    public void setWinAction(GymWinAction winAction) {
        this.winAction = winAction;
    }


    // TODO: 28/12/2022 Create in-game editor for npcGyms

}
