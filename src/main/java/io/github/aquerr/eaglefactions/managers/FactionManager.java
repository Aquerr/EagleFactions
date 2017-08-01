package io.github.aquerr.eaglefactions.managers;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.github.aquerr.eaglefactions.config.ConfigAccess;
import io.github.aquerr.eaglefactions.config.IConfig;
import io.github.aquerr.eaglefactions.config.FactionsConfig;
import ninja.leaping.configurate.ConfigurationNode;


import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Aquerr on 2017-07-12.
 */
public class FactionManager
{
    //TODO:Add other configs
    //private static IConfig mainConfig = MainConfig.getMainConfig();
    private static IConfig factionsConfig = FactionsConfig.getConfig();
    //private static IConfig claimsConfig = ClaimsConfig.getMainConfig();
    //private static IConfig messageConfig = MessageConfig.getMainConfig();

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
            else if(FactionManager.getLeader(faction).equals(playerUUID.toString ()))
            {
                return faction;
            }

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
        ConfigurationNode valueNode = ConfigAccess.getConfig(factionsConfig).getNode((Object[]) ("factions." + factionName + ".leader").split("\\."));

        if (valueNode.getValue() != null)
            return valueNode.getString();
        else
            return "";
    }

    private static ArrayList<String> getMembers(String factionName)
    {
       // ConfigurationNode valueNode = ConfigAccess.getMainConfig(factionsConfig).getNode((Object[]) ("teams." + factionName + ".members").split("\\."));
//
       // if (valueNode.getValue() == null)
       //     return Lists.newArrayList();
//
       // String list = valueNode.getString();
        ArrayList<String> membersList = Lists.newArrayList();
       // boolean finished = false;
//
       // if (finished != true)
       // {
       //     int endIndex = list.indexOf(",");
       //     if (endIndex != -1)
       //     {
       //         String substring = list.substring(0, endIndex);
       //         membersList.add(substring);
//
       //         // If they Have More than 1
       //         while (finished != true)
       //         {
       //             int startIndex = endIndex;
       //             endIndex = list.indexOf(",", startIndex + 1);
       //             if (endIndex != -1)
       //             {
       //                 String substrings = list.substring(startIndex + 1, endIndex);
       //                 membersList.add(substrings);
       //             }
       //             else
       //             {
       //                 finished = true;
       //             }
       //         }
       //     }
       //     else
       //     {
       //         membersList.add(list);
       //         finished = true;
       //     }
       // }
//
        return membersList;
    }

    public static Set<Object> getFactions()
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

    public static boolean createFaction(String factionName, UUID player)
    {
        try
        {
            ConfigAccess.setValueAndSave(factionsConfig,new Object[]{"factions", factionName, "leader"},(player.toString()));
            ConfigAccess.setValueAndSave(factionsConfig,new Object[]{"factions", factionName, "members"},"");
            ConfigAccess.setValueAndSave(factionsConfig,new Object[]{"factions", factionName, "enemies"},"");
        }
        catch (Exception exception)
        {
            return false;
        }

        return true;
    }
}
