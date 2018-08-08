package io.github.aquerr.eaglefactions.listeners;

import io.github.aquerr.eaglefactions.EagleFactions;

public abstract class AbstractListener
{
    protected EagleFactions plugin;

    protected AbstractListener(EagleFactions plugin){
        this.plugin = plugin;
    }
}
