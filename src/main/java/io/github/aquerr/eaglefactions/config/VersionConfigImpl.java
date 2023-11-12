package io.github.aquerr.eaglefactions.config;

import io.github.aquerr.eaglefactions.api.config.Configuration;
import io.github.aquerr.eaglefactions.api.config.VersionConfig;

public class VersionConfigImpl implements VersionConfig
{
    private final Configuration configuration;

    private boolean performVersionCheck = true;

    public VersionConfigImpl(Configuration configuration)
    {
        this.configuration = configuration;
    }

    @Override
    public void reload()
    {
        this.performVersionCheck = this.configuration.getBoolean(true, "version-check");
    }

    @Override
    public boolean shouldPerformVersionCheck()
    {
        return this.performVersionCheck;
    }
}
