package io.github.aquerr.eaglefactions.integrations.bluemap;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.integrations.Integration;
import io.github.aquerr.eaglefactions.integrations.IntegrationActivationResult;
import io.github.aquerr.eaglefactions.integrations.exception.CouldNotActivateIntegrationException;

import static io.github.aquerr.eaglefactions.integrations.IntegrationActivationResult.failure;
import static io.github.aquerr.eaglefactions.integrations.IntegrationActivationResult.success;
import static java.lang.String.format;

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
        IntegrationActivationResult integrationActivationResult = canActivate();
        if (integrationActivationResult.isCanActivate())
        {
            try
            {
                this.bluemapService = new BlueMapService(plugin);
                this.bluemapService.activate();
                plugin.printInfo(getName() + " is active!");
            }
            catch (Exception exception)
            {
                throw new CouldNotActivateIntegrationException("Could not activate " + getName(), exception);
            }
        }
        else
        {
            throw new CouldNotActivateIntegrationException(integrationActivationResult.getReason());
        }
    }

    @Override
    public IntegrationActivationResult canActivate()
    {
        if (plugin.getConfiguration().getBluemapConfig().isBluemapIntegrationEnabled())
        {
            try
            {
                Class.forName("de.bluecolored.bluemap.api.BlueMapAPI");
                return success();
            }
            catch (ClassNotFoundException e)
            {
                return failure(format("Bluemap could not be found. %s will not be available.", getName()));
            }
        }
        return failure(format(getName() + " is disabled in the config file."));
    }
}
