package io.github.aquerr.eaglefactions.integrations;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.integrations.bluemap.BlueMapIntegration;
import io.github.aquerr.eaglefactions.integrations.dynmap.DynMapIntegration;
import io.github.aquerr.eaglefactions.integrations.exception.CouldNotActivateIntegrationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class IntegrationManager
{
    private final EagleFactions plugin;
    private final DynMapIntegration dynMapIntegration;
    private final BlueMapIntegration blueMapIntegration;

    private final List<Integration> integrations = new ArrayList<>();

    public IntegrationManager(EagleFactions plugin)
    {
        this.plugin = plugin;
        this.dynMapIntegration = new DynMapIntegration(plugin);
        this.blueMapIntegration = new BlueMapIntegration(plugin);
        this.integrations.addAll(Arrays.asList(
                this.dynMapIntegration,
                this.blueMapIntegration
        ));
    }

    public BlueMapIntegration getBlueMapIntegration()
    {
        return blueMapIntegration;
    }

    public DynMapIntegration getDynMapIntegration()
    {
        return dynMapIntegration;
    }

    public void activateIntegrations()
    {
        for (final Integration integration : integrations)
        {
            if (integration.canActivate())
            {
                try
                {
                    this.plugin.printInfo("Enabling " + integration.getName());
                    integration.activate();
                }
                catch (CouldNotActivateIntegrationException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
