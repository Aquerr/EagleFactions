package io.github.aquerr.eaglefactions.integrations;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.integrations.bluemap.BlueMapIntegration;
import io.github.aquerr.eaglefactions.integrations.dynmap.DynMapIntegration;
import io.github.aquerr.eaglefactions.integrations.exception.CouldNotActivateIntegrationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class IntegrationManager
{
    private final EagleFactions plugin;
    private final List<Integration> integrations = new ArrayList<>();

    public IntegrationManager(EagleFactions plugin)
    {
        this.plugin = plugin;
        this.integrations.addAll(Arrays.asList(
                new DynMapIntegration(plugin),
                new BlueMapIntegration(plugin)
        ));
    }

    @SuppressWarnings("unchecked")
    public <T extends Integration> Optional<T> getIntegration(Class<T> integration)
    {
        return Optional.ofNullable((T)this.integrations.stream()
                .filter(integration1 -> integration1.getClass().equals(integration))
                .findFirst()
                .orElse(null));
    }

    public void activateIntegrations()
    {
        for (final Integration integration : integrations)
        {
            IntegrationActivationResult integrationActivationResult = integration.canActivate();
            if (integrationActivationResult.isCanActivate())
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
            else
            {
                this.plugin.printInfo(String.format("Cannot activate %s. Reason: %s", integration.getName(), integrationActivationResult.getReason()));
            }
        }
    }

    public void reloadIntegrations()
    {
        this.integrations.forEach(Integration::reload);
    }
}
