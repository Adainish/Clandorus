package io.github.adainish.clandorus.obj.gyms;

import com.pixelmonmod.pixelmon.api.battles.BattleType;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.api.rules.BattleProperty;
import com.pixelmonmod.pixelmon.battles.api.rules.BattleRuleRegistry;
import com.pixelmonmod.pixelmon.battles.api.rules.BattleRules;
import com.pixelmonmod.pixelmon.battles.api.rules.clauses.BattleClause;
import com.pixelmonmod.pixelmon.battles.api.rules.property.BattleTypeProperty;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import io.github.adainish.clandorus.enumeration.GymWinActions;
import io.github.adainish.clandorus.util.WorldUtil;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClanGym {
    public String identifier;
    public UUID activeHoldingPlayer;

    public BattleRules battleRules;

    public List<BattleClause> battleClauses = new ArrayList<>();

    public List<String> battleProperties = new ArrayList<>();

    public String worldRegistryName;

    public double posX;

    public double posY;

    public double posZ;

    public long lastChallenged;

    public List<Pokemon> activePokemon = new ArrayList<>();

    public List<GymWinActions> selectedWinActions = new ArrayList<>();

    public ClanGym()
    {

    }

    public ClanGym(String identifier)
    {
        this.identifier = identifier;
    }

    public void generateClanGym()
    {

    }

    public void initiateBattle()
    {

    }

    public boolean loadGymNPC()
    {

        return false;
    }


    public boolean deleteNPCGym()
    {

        return false;
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

    public World world()
    {
        if (WorldUtil.getWorld(worldRegistryName).isPresent())
        return WorldUtil.getWorld(worldRegistryName).get();
        else return WorldUtil.getBasicWorld();
    }


    public NPCTrainer npcTrainer()
    {
        NPCTrainer npcTrainer = new NPCTrainer(WorldUtil.getBasicWorld());





        return npcTrainer;
    }


    // TODO: 28/12/2022 Create in-game editor for npcGyms

}
