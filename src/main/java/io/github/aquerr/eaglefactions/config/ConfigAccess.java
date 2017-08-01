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

    public static CommentedConfigurationNode getConfig(IConfig config)
    {
        return config.get();
    }

    public static void saveConfig(IConfig config)
    {
        config.save();
    }

    public static void setValueAndSave(IConfig config, Object[] nodePath, Object value)
    {
        config.get().getNode(nodePath).setValue(value);
        config.save();
    }

    public static void setValue(IConfig config, Object[] nodePath, Object value)
    {
        config.get().getNode(nodePath).setValue(value);
    }

    public static void removeChild(IConfig config, Object[] nodePath, Object child)
    {
        config.get().getNode(nodePath).removeChild(child);
        config.save();
    }

    public static void removeChildren(IConfig config, Object[] nodePath)
    {
        for (Object child : config.get().getNode(nodePath).getChildrenMap().keySet())
        {
            config.get().getNode(nodePath).removeChild(child);
        }

        config.save();
    }
}
