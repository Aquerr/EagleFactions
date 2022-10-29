package io.github.aquerr.eaglefactions.storage.sql.mysql;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.storage.sql.AbstractFactionStorage;

public class MySQLFactionStorage extends AbstractFactionStorage
{
    public MySQLFactionStorage(final EagleFactions plugin)
    {
        super(plugin, MySQLProvider.getInstance(plugin), new FactionProtectionFlagsMySqlStorageImpl());
    }
}
