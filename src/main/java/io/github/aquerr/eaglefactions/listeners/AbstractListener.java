package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.api.EagleFactions;

public abstract class AbstractListener
{
    private final EagleFactions plugin;

    protected AbstractListener(EagleFactions plugin){
        this.plugin = plugin;
    }

    public EagleFactions getPlugin()
    {
        return plugin;
    }
}
