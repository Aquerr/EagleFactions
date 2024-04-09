package io.github.aquerr.eaglefactions.config;

import io.github.aquerr.eaglefactions.api.config.Configuration;
import io.github.aquerr.eaglefactions.api.config.LangConfig;

import java.io.IOException;

public class LangConfigImpl implements LangConfig
{
    private final Configuration configuration;

    private String languageTag = "en";

    public LangConfigImpl(Configuration configuration)
    {
        this.configuration = configuration;
    }

    @Override
    public void reload() throws IOException
    {
        this.languageTag = this.configuration.getString("en", "language");
    }

    @Override
    public String getLanguageTag()
    {
        return this.languageTag;
    }
}
