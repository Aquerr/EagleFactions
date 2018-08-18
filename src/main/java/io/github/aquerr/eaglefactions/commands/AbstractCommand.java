package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;

public abstract class AbstractCommand
{
    private EagleFactions _eagleFactions;

    public AbstractCommand(EagleFactions plugin)
    {
        this._eagleFactions = plugin;
    }

    public EagleFactions getPlugin()
    {
        return _eagleFactions;
    }
}
