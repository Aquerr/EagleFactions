package io.github.aquerr.eaglefactions.storage.sql.h2;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.storage.sql.AbstractFactionStorage;

public class H2FactionStorage extends AbstractFactionStorage
{
    public H2FactionStorage(final EagleFactions plugin)
    {
        super(plugin, H2Provider.getInstance(plugin));
    }
}
