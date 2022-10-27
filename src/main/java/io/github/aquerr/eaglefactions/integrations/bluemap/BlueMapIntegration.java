package io.github.aquerr.eaglefactions.integrations.bluemap;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.integrations.Integration;
import io.github.aquerr.eaglefactions.integrations.exception.CouldNotActivateIntegrationException;

public class BlueMapIntegration implements Integration
{
    private EagleFactions plugin;
    private BlueMapService bluemapService;

    public BlueMapIntegration(EagleFactions plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public String getName()
    {
        return "BlueMap Integration";
    }

    @Override
    public void activate() throws CouldNotActivateIntegrationException
    {
        try
        {
            this.bluemapService = new BlueMapService();
        }
        catch (Exception exception)
        {
            throw new CouldNotActivateIntegrationException("Could not activate " + this.getClass().getSimpleName(), exception);
        }
    }

    @Override
    public boolean canActivate()
    {
        if (plugin.getConfiguration().getBluemapConfig().isBluemapIntegrationEnabled())
        {
            try
            {
                Class.forName("de.bluecolored.bluemap.api.BlueMapAPI");
                return true;
            }
            catch (ClassNotFoundException e)
            {
                plugin.printInfo("Bluemap could not be found. " + getName() +" will not be available.");
                return false;
            }
        }
        plugin.printInfo(getName() + " is disabled in the config file.");
        return false;
    }
}
