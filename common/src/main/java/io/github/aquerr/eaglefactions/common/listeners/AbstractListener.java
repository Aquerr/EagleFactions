package io.github.aquerr.eaglefactions.common.listeners;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;

public abstract class AbstractListener
{
    private EagleFactionsPlugin plugin;

    public AbstractListener(EagleFactionsPlugin plugin){
        this.plugin = plugin;
    }

    public EagleFactionsPlugin getPlugin()
    {
        return plugin;
    }
}
