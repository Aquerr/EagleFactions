package io.github.aquerr.eaglefactions.common.storage.sql.h2;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.common.storage.sql.AbstractPlayerStorage;

public class H2PlayerStorage extends AbstractPlayerStorage
{
    public H2PlayerStorage(final EagleFactions eagleFactions)
    {
        super(eagleFactions, H2Provider.getInstance(eagleFactions));
    }
}
