package io.github.aquerr.eaglefactions.storage.sql.h2;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.storage.sql.AbstractPlayerStorage;

public class H2PlayerStorage extends AbstractPlayerStorage
{
    public H2PlayerStorage(final EagleFactions eagleFactions)
    {
        super(eagleFactions, H2Provider.getInstance(eagleFactions));
    }
}
