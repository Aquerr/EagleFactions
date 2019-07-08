package io.github.aquerr.eaglefactions.common.storage.sql.h2;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.storage.sql.AbstractFactionStorage;

public class H2FactionStorage extends AbstractFactionStorage
{
    public H2FactionStorage(final EagleFactionsPlugin plugin)
    {
        super(plugin, H2Provider.getInstance(plugin));
    }
}
