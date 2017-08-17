package io.github.aquerr.eaglefactions.logic;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.google.common.collect.Sets;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.config.ConfigAccess;
import io.github.aquerr.eaglefactions.config.IConfig;
import io.github.aquerr.eaglefactions.config.FactionsConfig;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.services.PowerService;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.world.Chunk;


import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

/**
 * Created by Aquerr on 2017-07-12.
 */
public class FactionLogic
{
    //TODO:Add other configs
    //private static IConfig mainConfig = MainConfig.getMainConfig();
    private static IConfig factionsConfig = FactionsConfig.getConfig();
    //private static IConfig claimsConfig = ClaimsConfig.getMainConfig();
    //private static IConfig messageConfig = MessageConfig.getMainConfig();

    public static String getFactionName(UUID playerUUID)
    {
        for (Object t : FactionLogic.getFactionsNames())
        {
            String faction = String.valueOf (t);

            if(faction.equals ("WarZone") || faction.equals ("SafeZone"))
            {
                continue;
            }

            //TODO: If even leader and officers are stored in Members group, checking members is enough.
            if(FactionLogic.getMembers(faction).contains(playerUUID.toString ()))
            {
                return faction;
            }
            else if(FactionLogic.getLeader(faction).equals(playerUUID.toString ()))
            {
                return faction;
            }

            //TODO:Add check for officers.
            else if(FactionLogic.getOfficers(faction).contains(playerUUID.toString ()))
            {
                return faction;
            }
        }
        return null;
    }

    public static String getFactionNameByChunk(Vector3i chunk)
    {
        for(Object object: getFactionsNames())
        {
            String factionName = String.valueOf(object);

            if(getClaims(factionName).contains(chunk.toString()))
            {
                return factionName;
            }
        }

        return null;
    }

    public static Faction getFaction(String factionName)
    {
        EagleFactions.getEagleFactions().getLogger().info("Getting a faction: " + factionName);
        ConfigurationNode leaderNode = ConfigAccess.getConfig(factionsConfig).getNode("factions", factionName, "leader");

        String leaderUUID = "";
        if(leaderNode.getValue() != null) leaderUUID = leaderNode.getString();

        ConfigurationNode tagNode = ConfigAccess.getConfig(factionsConfig).getNode("factions", factionName, "tag");

        String factionTag = "";
        if(tagNode.getValue() != null) factionTag = tagNode.getString();

        Faction faction = new Faction(factionName, factionTag, leaderUUID);

        faction.Members = getMembers(factionName);
        faction.Officers = getOfficers(factionName);
        faction.Enemies = getEnemies(factionName);
        faction.Alliances = getAlliances(factionName);
        faction.Claims = getClaims(factionName);
        faction.Power = PowerService.getFactionPower(faction);

        EagleFactions.getEagleFactions().getLogger().info("Returning a faction...");
        return faction;
    }

    public static String getLeader(String factionName)
    {
        ConfigurationNode valueNode = ConfigAccess.getConfig(factionsConfig).getNode((Object[]) ("factions." + factionName + ".leader").split("\\."));

        if (valueNode.getValue() != null)
            return valueNode.getString();
        else
            return "";
    }

    public static List<String> getOfficers(String factionName)
    {
        ConfigurationNode officersNode = ConfigAccess.getConfig(factionsConfig).getNode("factions", factionName,"officers");

        if (officersNode.getValue() != null)
        {
            List<String> officersList = officersNode.getList(objectToStringTransformer);

            List<String> helpList = new ArrayList<>(officersList);

            return helpList;
        }
        else return new ArrayList<String>();
    }

    public static List<String> getMembers(String factionName)
    {
        ConfigurationNode membersNode = ConfigAccess.getConfig(factionsConfig).getNode("factions", factionName,"members");

        if (membersNode.getValue() != null)
        {
            List<String> membersList = membersNode.getList(objectToStringTransformer);

            List<String> helpList = new ArrayList<>(membersList);

            return helpList;
        }
        else return new ArrayList<String>();
    }

    public static Set<Object> getFactionsNames()
    {
        if(ConfigAccess.getConfig(factionsConfig).getNode ("factions","factions").getValue() != null)
        {
            ConfigAccess.removeChild(factionsConfig, new Object[]{"factions"}, "factions");
        }

        if(ConfigAccess.getConfig(factionsConfig).getNode("factions").getValue() != null)
        {
            return ConfigAccess.getConfig(factionsConfig).getNode("factions").getChildrenMap().keySet();
        }

            return Sets.newHashSet ();
    }

    public static List<Faction> getFactions()
    {
        List<Faction> factionsList = new ArrayList<>();

        for (Object object: getFactionsNames())
        {
            String factionName = String.valueOf(object);

            factionsList.add(getFaction(factionName));
        }

        return factionsList;
    }

    public static boolean createFaction(String factionName,String factionTag, UUID playerUUID)
    {
        try
        {
            ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "tag"}, factionTag);
            ConfigAccess.setValueAndSave(factionsConfig,new Object[]{"factions", factionName, "leader"},(playerUUID.toString()));
            ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "officers"},new ArrayList<String>());
            ConfigAccess.setValueAndSave(factionsConfig,new Object[]{"factions", factionName, "home"},null);
            ConfigAccess.setValueAndSave(factionsConfig,new Object[]{"factions", factionName, "members"},new ArrayList<String>());
            ConfigAccess.setValueAndSave(factionsConfig,new Object[]{"factions", factionName, "enemies"},new ArrayList<String>());
            ConfigAccess.setValueAndSave(factionsConfig,new Object[]{"factions", factionName, "alliances"}, new ArrayList<String>());
            ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "friendlyfire"}, false);
            ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "claims"}, new ArrayList<String>());
        }
        catch (Exception exception)
        {
            return false;
        }

        return true;
    }

    public static void disbandFaction(String factionName)
    {
        ConfigAccess.removeChild(factionsConfig, new Object[]{"factions"},factionName);
    }

    public static void joinFaction(UUID playerUUID, String factionName)
    {
        List<String> memberList = new ArrayList<>(getMembers(factionName));
        memberList.add(playerUUID.toString());

        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "members"}, memberList);

    }

    public static void leaveFaction(UUID playerUUID, String factionName)
    {
        List<String> memberList = new ArrayList<>(getMembers(factionName));

        memberList.remove(playerUUID.toString());

        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "members"}, memberList);
    }

    public static void addAllay(String playerFactionName, String invitedFactionName)
    {
        List<String> playerFactionAllianceList = new ArrayList<>(getAlliances(playerFactionName));
        List<String> invitedFactionAllianceList = new ArrayList<>(getAlliances(invitedFactionName));

        playerFactionAllianceList.add(invitedFactionName);
        invitedFactionAllianceList.add(playerFactionName);

        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", playerFactionName, "alliances"}, playerFactionAllianceList);
        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", invitedFactionName, "alliances"}, invitedFactionAllianceList);
    }

    public static List<String> getAlliances(String factionName)
    {
        ConfigurationNode allianceNode = ConfigAccess.getConfig(factionsConfig).getNode("factions", factionName, "alliances");

        if (allianceNode.getValue() != null)
        {
            List<String> alliancesList = allianceNode.getList(objectToStringTransformer);

            List<String> helpList = new ArrayList<>(alliancesList);

            return helpList;
        }
        else return new ArrayList<String>();
    }

    public static void removeAlliance(String playerFactionName, String removedFaction)
    {
        List<String> playerFactionAllianceList = new ArrayList<>(getAlliances(playerFactionName));
        List<String> removedFactionAllianceList = new ArrayList<>(getAlliances(removedFaction));

        playerFactionAllianceList.remove(removedFaction);
        removedFactionAllianceList.remove(playerFactionName);

        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", playerFactionName, "alliances"}, playerFactionAllianceList);
        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", removedFaction, "alliances"}, removedFactionAllianceList);
    }

    public static List<String> getEnemies(String factionName)
    {
        ConfigurationNode enemiesNode = ConfigAccess.getConfig(factionsConfig).getNode("factions", factionName, "enemies");

        if (enemiesNode.getValue() != null)
        {
            List<String> enemiesList = enemiesNode.getList(objectToStringTransformer);

            List<String> helpList = new ArrayList<>(enemiesList);

            return helpList;
        }
        else return new ArrayList<String>();
    }

    public static void addEnemy(String playerFactionName, String enemyFactionName)
    {
        List<String> playerFactionEnemiesList = new ArrayList<>(getEnemies(playerFactionName));
        List<String> enemyFactionEnemiesList = new ArrayList<>(getEnemies(enemyFactionName));

        playerFactionEnemiesList.add(enemyFactionName);
        enemyFactionEnemiesList.add(playerFactionName);

        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", playerFactionName, "enemies"}, playerFactionEnemiesList);
        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", enemyFactionName, "enemies"}, enemyFactionEnemiesList);
    }

    public static void removeEnemy(String playerFactionName, String enemyFactionName)
    {
        List<String> playerFactionEnemiesList = new ArrayList<>(getEnemies(playerFactionName));
        List<String> enemyFactionEnemiesList = new ArrayList<>(getEnemies(enemyFactionName));

        playerFactionEnemiesList.remove(enemyFactionName);
        enemyFactionEnemiesList.remove(playerFactionName);

        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", playerFactionName, "enemies"}, playerFactionEnemiesList);
        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", enemyFactionName, "enemies"}, enemyFactionEnemiesList);
    }

    public static void addOfficer(String newOfficerName, String factionName)
    {
        List<String> officersList = new ArrayList<>(getOfficers(factionName));
        List<String> membersList = new ArrayList<>(getMembers(factionName));

        officersList.add(newOfficerName);
        membersList.remove(newOfficerName);

        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "officers"}, officersList);
        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "members"}, membersList);
    }

    public static void removeOfficer(String officerName, String factionName)
    {
        List<String> officersList = new ArrayList<>(getOfficers(factionName));
        List<String> membersList = new ArrayList<>(getMembers(factionName));

        officersList.remove(officerName);
        membersList.add(officerName);

        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "officers"}, officersList);
        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "members"}, membersList);

    }

    public static boolean getFactionFriendlyFire(String factionName)
    {
        ConfigurationNode friendlyFireNode = ConfigAccess.getConfig(factionsConfig).getNode("factions", factionName, "friendlyfire");

        Boolean friendlyFire = friendlyFireNode.getBoolean();

        return friendlyFire;
    }

    public static void setFactionFriendlyFire(String factionName, boolean turnOn)
    {
        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "friendlyfire"}, turnOn);
    }

    public static List<String> getClaims(String factionName)
    {
        ConfigurationNode claimsNode = ConfigAccess.getConfig(factionsConfig).getNode("factions", factionName, "claims");

        List<String> calimsList = claimsNode.getList(objectToStringTransformer);

        return calimsList;
    }

    public static void addClaim(String factionName, Vector3i claimedChunk)
    {
        List<String> claimsList = new ArrayList<>(getClaims(factionName));

        claimsList.add(claimedChunk.toString());

        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "claims"}, claimsList);
    }

    public static void removeClaim(String factionName, Vector3i claimedChunk)
    {
        List<String> claimsList = new ArrayList<>(getClaims(factionName));

        claimsList.remove(claimedChunk.toString());

        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "claims"}, claimsList);
    }

    public static boolean isClaimed(Vector3i chunk)
    {
        for (Object object: getFactionsNames())
        {
            String factionName = String.valueOf(object);

            List<String> factionClaims = getClaims(factionName);

            if(!factionClaims.isEmpty() && factionClaims != null)
            {
                for (String claim: factionClaims)
                {
                    if(claim.equalsIgnoreCase(chunk.toString()))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }



    private static Function<Object,Chunk> objectToChunkTransformer = input ->
    {
        if (input instanceof Chunk)
        {
            return (Chunk) input;
        }
        else
        {
            return null;
        }
    };

    private static Function<Object,String> objectToStringTransformer = input ->
    {
        if (input instanceof String)
        {
            return (String) input;
        }
        else
        {
            return null;
        }
    };


    public static boolean isClaimConnected(String factionName, Vector3i chunk)
    {
        List<String> claimsList = getClaims(factionName);

        for (String object: claimsList)
        {
            String vectors[] = object.replace("(", "").replace(")", "").replace(" ", "").split(",");

            int x = Integer.valueOf(vectors[0]);
            int y = Integer.valueOf(vectors[1]);
            int z = Integer.valueOf(vectors[2]);

            Vector3i claim = Vector3i.from(x, y, z);

            if((claim.getX() == chunk.getX()) && ((claim.getZ() + 1 == chunk.getZ()) || (claim.getZ() - 1 == chunk.getZ())))
            {
                return true;
            }
            else if((claim.getZ() == chunk.getZ()) && ((claim.getX() + 1 == chunk.getX()) || (claim.getX() - 1 == chunk.getX())))
            {
                return true;
            }
        }
        return false;
    }

    public static void setHome(String factionName, @Nullable Vector3d home)
    {
        if(home == null) ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions",factionName, "home"}, home);
        else ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "home"}, home.toString());
    }

    public static Vector3d getHome(String factionName)
    {
        ConfigurationNode homeNode = ConfigAccess.getConfig(factionsConfig).getNode("factions", factionName, "home");

        if(homeNode.getValue() != null)
        {
            String homeString = homeNode.getString();

            String vectors[] = homeString.replace("(", "").replace(")", "").replace(" ", "").split(",");

             double x = Double.valueOf(vectors[0]);
             double y = Double.valueOf(vectors[1]);
             double z = Double.valueOf(vectors[2]);

             Vector3d home = Vector3d.from(x, y, z);

             return home;
        }
        else
        {
            return null;
        }
    }

    public static List<String> getFactionsTags()
    {
        List<Faction> factionsList = getFactions();
        List<String> factionsTags = new ArrayList<>();

        for (Faction faction: factionsList)
        {
            factionsTags.add(faction.Tag);
        }

        return factionsTags;
    }

    public static String getFactionTag(String factionName)
    {
        ConfigurationNode tagNode = ConfigAccess.getConfig(factionsConfig).getNode("factions", factionName, "tag");

        return tagNode.getString();
    }
}
