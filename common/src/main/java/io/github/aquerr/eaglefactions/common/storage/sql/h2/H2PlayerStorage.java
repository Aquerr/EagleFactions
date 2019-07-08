package io.github.aquerr.eaglefactions.common.storage.sql.h2;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.storage.sql.AbstractPlayerStorage;

public class H2PlayerStorage extends AbstractPlayerStorage
{
    public H2PlayerStorage(final EagleFactionsPlugin eagleFactions)
    {
        super(eagleFactions, H2Provider.getInstance(eagleFactions));
    }
}
