package io.github.aquerr.eaglefactions.integrations.dynmap;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.integrations.Integration;
import io.github.aquerr.eaglefactions.integrations.IntegrationActivationResult;
import io.github.aquerr.eaglefactions.integrations.exception.CouldNotActivateIntegrationException;

import static io.github.aquerr.eaglefactions.integrations.IntegrationActivationResult.failure;
import static io.github.aquerr.eaglefactions.integrations.IntegrationActivationResult.success;
import static java.lang.String.format;

public class DynMapIntegration implements Integration
{
    private final EagleFactions plugin;
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
        IntegrationActivationResult integrationActivationResult = canActivate();
        if (integrationActivationResult.isCanActivate())
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
        else
        {
            throw new CouldNotActivateIntegrationException(integrationActivationResult.getReason());
        }
    }

    @Override
    public IntegrationActivationResult canActivate()
    {
        if (plugin.getConfiguration().getDynmapConfig().isDynmapIntegrationEnabled())
        {
            try
            {
                Class.forName("org.dynmap.DynmapCommonAPI");
                return success();
            }
            catch (final ClassNotFoundException exception)
            {
                return failure("Dynmap could not be found.");
            }
        }
        return failure(format(getName() + " is disabled in the config file."));
    }

    public DynmapService getDynmapService()
    {
        return dynmapService;
    }
}
