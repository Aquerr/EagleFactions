package io.github.aquerr.eaglefactions.integrations;

import io.github.aquerr.eaglefactions.integrations.exception.CouldNotActivateIntegrationException;

public interface Integration
{
    String getName();

    void activate() throws CouldNotActivateIntegrationException;

    IntegrationActivationResult canActivate();

    /**
     * Executed on server reload.
     * Integrations can provide clean-up and/or reload operations in this method.
     */
    void reload();
}
