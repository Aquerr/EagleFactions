package io.github.aquerr.eaglefactions.config;

import ninja.leaping.configurate.commented.CommentedConfigurationNode;

/**
 * Created by Aquerr on 2017-07-12.
 */
public final class ConfigAccess
{
    private ConfigAccess()
    {
        ;
    }

    public static CommentedConfigurationNode getConfig(MainConfig config)
    {
        return config.get();
    }

    public static void saveConfig(MainConfig config)
    {
        config.save();
    }

    public static void setValueAndSave(MainConfig config, Object[] nodePath, Object value)
    {
        config.get().getNode(nodePath).setValue(value);
        config.save();
    }

    public static void setValue(MainConfig config, Object[] nodePath, Object value)
    {
        config.get().getNode(nodePath).setValue(value);
    }

    public static void removeChild(MainConfig config, Object[] nodePath, Object child)
    {
        config.get().getNode(nodePath).removeChild(child);
        config.save();
    }

    public static void removeChildren(MainConfig config, Object[] nodePath)
    {
        for (Object child : config.get().getNode(nodePath).getChildrenMap().keySet())
        {
            config.get().getNode(nodePath).removeChild(child);
        }

        config.save();
    }
}
