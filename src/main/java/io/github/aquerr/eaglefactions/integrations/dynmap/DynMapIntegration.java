package io.github.aquerr.eaglefactions.integrations.dynmap;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.integrations.Integration;
import io.github.aquerr.eaglefactions.integrations.exception.CouldNotActivateIntegrationException;

public class DynMapIntegration implements Integration
{
    private EagleFactions plugin;
    private DynmapService dynmapService;

    public DynMapIntegration(EagleFactions plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public String getName()
    {
        return "Dynmap Integration";
    }

    @Override
    public void activate() throws CouldNotActivateIntegrationException
    {
        try
        {
            this.dynmapService = new DynmapService(plugin);
            this.dynmapService.activate();
            plugin.printInfo(getName() + " is active!");
        }
        catch (Exception exception)
        {
            throw new CouldNotActivateIntegrationException("Could not activate " + this.getClass().getSimpleName(), exception);
        }
    }

    @Override
    public boolean canActivate()
    {
        if (plugin.getConfiguration().getDynmapConfig().isDynmapIntegrationEnabled())
        {
            try
            {
                Class.forName("org.dynmap.DynmapCommonAPI");
                return true;
            }
            catch (final ClassNotFoundException error)
            {
                plugin.printInfo("Dynmap could not be found. " + getName() + " will not be available.");
                return false;
            }
        }
        plugin.printInfo(getName() + " is disabled in the config file.");
        return false;
    }

    public DynmapService getDynmapService()
    {
        return dynmapService;
    }
}
