package io.github.aquerr.eaglefactions.integrations;

import io.github.aquerr.eaglefactions.integrations.exception.CouldNotActivateIntegrationException;

public interface Integration
{
    String getName();

    void activate() throws CouldNotActivateIntegrationException;

    boolean canActivate();
}
