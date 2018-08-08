package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;

public abstract class AbstractCommand
{
    private EagleFactions _eagleFactions;

    protected AbstractCommand(EagleFactions plugin)
    {
        this._eagleFactions = plugin;
    }

    protected EagleFactions getPlugin()
    {
        return _eagleFactions;
    }
}
