package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;

public abstract class AbstractListener
{
    private EagleFactions plugin;

    public AbstractListener(EagleFactions plugin){
        this.plugin = plugin;
    }

    public EagleFactions getPlugin()
    {
        return plugin;
    }
}
