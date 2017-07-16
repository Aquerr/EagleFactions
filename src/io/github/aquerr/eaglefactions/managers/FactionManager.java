package io.github.aquerr.eaglefactions.managers;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.config.Configs;
import io.github.aquerr.eaglefactions.config.Configurable;
import io.github.aquerr.eaglefactions.config.FactionsConfig;
import ninja.leaping.configurate.ConfigurationNode;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Aquerr on 2017-07-12.
 */
public class FactionManager
{
    //TODO:Add other configs
    //private static Configurable mainConfig = Config.getConfig();
    private static Configurable factionConfig = FactionsConfig.getConfig();
    //private static Configurable claimsConfig = ClaimsConfig.getConfig();
    //private static Configurable messageConfig = MessageConfig.getConfig();

    public static String getFaction(UUID playerUUID)
    {
        for (Object t : FactionManager.getFactions ())
        {
            String faction = String.valueOf (t);

            if(faction.equals ("WarZone") || faction.equals ("SafeZone"))
            {
                continue;
            }

            //TODO: If even leader and officers are stored in Members group, checking members is enough.
            if(FactionManager.getMembers(faction).contains(playerUUID.toString ()))
            {
                return faction;
            }
           // else if(FactionManager.getLeader(faction).equals(playerUUID.toString ()))
           // {
           //     return faction;
           // }

            //TODO:Add check for officers.
           // else if(TeamManager.getOfficers(faction).contains(playerUUID.toString ()))
           // {
           //     return faction;
           // }
        }
        return null;
    }

    private static String getLeader(String factionName)
    {
        ConfigurationNode valueNode = Configs.getConfig(factionConfig).getNode((Object[]) ("teams." + factionName + ".leader").split("\\."));

        if (valueNode.getValue() != null)
            return valueNode.getString();
        else
            return "";
    }

    private static ArrayList<String> getMembers(String factionName)
    {
        ConfigurationNode valueNode = Configs.getConfig(factionConfig).getNode((Object[]) ("teams." + factionName + ".members").split("\\."));

        if (valueNode.getValue() == null)
            return Lists.newArrayList();

        String list = valueNode.getString();
        ArrayList<String> membersList = Lists.newArrayList();
        boolean finished = false;

        if (finished != true)
        {
            int endIndex = list.indexOf(",");
            if (endIndex != -1)
            {
                String substring = list.substring(0, endIndex);
                membersList.add(substring);

                // If they Have More than 1
                while (finished != true)
                {
                    int startIndex = endIndex;
                    endIndex = list.indexOf(",", startIndex + 1);
                    if (endIndex != -1)
                    {
                        String substrings = list.substring(startIndex + 1, endIndex);
                        membersList.add(substrings);
                    }
                    else
                    {
                        finished = true;
                    }
                }
            }
            else
            {
                membersList.add(list);
                finished = true;
            }
        }

        return membersList;
    }

    public static Set<Object> getFactions()
    {
        if(Configs.getConfig(factionConfig).getNode ("factions","factions").getValue() != null)
        {
            Configs.removeChild(factionConfig, new Object[]{"factions"}, "factions");
        }

        if(Configs.getConfig(factionConfig).getNode("factions").getValue() != null)
        {
            return Configs.getConfig(factionConfig).getNode("factions").getChildrenMap().keySet();
        }

            return Sets.newHashSet ();
    }

    public static void createFaction(String factionName, UUID player)
    {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put("Name",factionName);
        jsonObject.put("Home", "null");
        jsonObject.put("Leader", player.toString());

        JSONArray members = new JSONArray();
        members.add(player);
        jsonObject.put("Members",members);


        JSONArray claims = new JSONArray();
        jsonObject.put("Claims",claims);

        String factionFile = EagleFactions.getEagleFactions().getConfigDir().toString() + factionName + ".json";

        try(FileWriter file = new FileWriter(factionFile))
        {
            file.write(jsonObject.toJSONString());
            file.flush();
        }
        catch(IOException exception)
        {
            exception.printStackTrace();
        }
    }
}
