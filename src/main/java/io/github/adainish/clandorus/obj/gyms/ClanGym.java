package io.github.adainish.clandorus.obj.gyms;

import com.pixelmonmod.pixelmon.api.battles.BattleType;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.battles.api.rules.BattleProperty;
import com.pixelmonmod.pixelmon.battles.api.rules.BattleRuleRegistry;
import com.pixelmonmod.pixelmon.battles.api.rules.BattleRules;
import com.pixelmonmod.pixelmon.battles.api.rules.clauses.BattleClause;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import io.github.adainish.clandorus.Clandorus;
import io.github.adainish.clandorus.enumeration.GymWinActions;
import io.github.adainish.clandorus.obj.HoldRequirements;
import io.github.adainish.clandorus.obj.Location;
import io.github.adainish.clandorus.obj.OccupyingHolder;
import io.github.adainish.clandorus.obj.Player;
import io.github.adainish.clandorus.storage.PlayerStorage;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClanGym {
    public String identifier;
    public UUID activeHoldingPlayer;

    public BattleRules battleRules;

    public List<BattleClause> battleClauses = new ArrayList<>();

    public List<String> battleProperties = new ArrayList<>();

    public long lastChallenged;

    public HoldRequirements holdRequirements;

    public OccupyingHolder occupyingHolder;

    public Location location;

    public List<Pokemon> activePokemon = new ArrayList<>();

    public List<GymWinActions> selectedWinActions = new ArrayList<>();

    public ClanGym()
    {

        location = new Location();
    }

    public ClanGym(String identifier)
    {
        this.identifier = identifier;
        location = new Location();
    }



    public void generateClanGym()
    {


    }

    @Nullable
    public Player getHoldingPlayer()
    {
        if (activeHoldingPlayer == null)
            return null;

        return PlayerStorage.getPlayer(activeHoldingPlayer);
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
            Clandorus.log.warn("Clan Gym NPC for %clangym% did not exist, creating new one".replace("%clangym%", identifier));
            createNewClanGymNPC();
        }
    }


    public void deleteNPCGym()
    {
        NPCTrainer trainer = getClanGymNPC();
        if (trainer != null) {
            trainer.getEntity().remove();
        }
    }

    public void loadBattleRules() {
        battleRules = new BattleRules(BattleType.SINGLE);
        battleRules.setNewClauses(battleClauses);
        initialiseBattleProperties();
    }

    public void initialiseBattleProperties()
    {
        for (String btp:battleProperties) {
            BattleProperty battleProperty = BattleRuleRegistry.getProperty(btp);
            battleRules.set(battleProperty, "");
        }
    }

    public Location getLocation()
    {
        return location;
    }

    public World getWorld()
    {
        return location.getWorld();
    }

    public NPCTrainer createNewClanGymNPC()
    {
        NPCTrainer npcTrainer = new NPCTrainer(getWorld());
        npcTrainer.getPersistentData().putBoolean("clandorusGym", true);
        npcTrainer.getPersistentData().putString("clandorusGymID", identifier);
        for (int i = 0; i < 6; i++) {
            npcTrainer.getPokemonStorage().set(i, null);
        }
        if (activePokemon.isEmpty())
        {
            for (int i = 0; i < 6; i++) {
                Pokemon newPokemon = PokemonFactory.create(PixelmonSpecies.getRandomSpecies());
                npcTrainer.getPokemonStorage().set(i, newPokemon);
            }
        } else
        {
            for (int i = 0; i < activePokemon.size(); i++) {
                if (i >= 6)
                    break;
                Pokemon p = activePokemon.get(i);
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
        if (world.isAreaLoaded(getLocation().returnBlockpos(), 40))
        {
            Location storedLocation = getLocation();
            AxisAlignedBB isWithinAABB = new AxisAlignedBB(getLocation().returnBlockpos());
            List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, isWithinAABB);
            for (Entity e:entities) {
                if (isClanGymNPC(e)) {
                    if (e.getPersistentData().getString("clandorusGymID").equals(identifier)) {
                        Clandorus.log.log(Level.WARN, "Detected existing clandorus gym npc " + identifier + " loading to cache, no new one has to be created");
                        NPCTrainer trainer = (NPCTrainer) e;
                        if (trainer.getPosX() != storedLocation.getPosX() || trainer.getPosY() != storedLocation.getPosY() || trainer.getPosZ() != storedLocation.getPosZ())
                        {
                            trainer.setPositionAndUpdate(location.getPosX(), location.getPosY(), location.getPosZ());
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
        if (world.isAreaLoaded(getLocation().returnBlockpos(), 40))
        {
            Location storedLocation = getLocation();
            AxisAlignedBB isWithinAABB = new AxisAlignedBB(getLocation().returnBlockpos());
            List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, isWithinAABB);
            for (Entity e:entities) {
                if (isClanGymNPC(e)) {
                    if (e.getPersistentData().getString("clandorusGymID").equals(identifier)) {
                        Clandorus.log.log(Level.WARN, "Detected existing clandorys gym npc " + identifier + " loading to cache, no new one has to be created");
                        NPCTrainer trainer = (NPCTrainer) e;
                        if (trainer.getPosX() != storedLocation.getPosX() || trainer.getPosY() != storedLocation.getPosY() || trainer.getPosZ() != storedLocation.getPosZ())
                        {
                            trainer.setPositionAndUpdate(location.getPosX(), location.getPosY(), location.getPosZ());
                        }
                        return trainer;
                    }
                }
            }
        } else
        {
            NPCTrainer npcTrainer = new NPCTrainer(getWorld());
            npcTrainer.getPersistentData().putBoolean("clandorusGym", true);
            npcTrainer.getPersistentData().putString("clandorusGymID", identifier);
            for (int i = 0; i < 6; i++) {
                npcTrainer.getPokemonStorage().set(i, null);
            }
            if (activePokemon.isEmpty())
            {
                for (int i = 0; i < 6; i++) {
                    Pokemon newPokemon = PokemonFactory.create(PixelmonSpecies.getRandomSpecies());
                    npcTrainer.getPokemonStorage().set(i, newPokemon);
                }
            } else
            {
                for (int i = 0; i < activePokemon.size(); i++) {
                    if (i >= 6)
                        break;
                    Pokemon p = activePokemon.get(i);
                    if (p == null)
                        continue;
                    npcTrainer.getPokemonStorage().set(i, p);
                }
            }
            return npcTrainer;
        }
      return null;
    }


    // TODO: 28/12/2022 Create in-game editor for npcGyms

}
