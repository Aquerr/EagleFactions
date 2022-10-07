package io.github.aquerr.eaglefactions.entities;

import io.github.aquerr.eaglefactions.api.entities.ProtectionFlag;
import io.github.aquerr.eaglefactions.api.entities.ProtectionFlagType;

public class ProtectionFlagImpl implements ProtectionFlag
{
    private final ProtectionFlagType type;
    private boolean value;

    public ProtectionFlagImpl(ProtectionFlagType type, boolean value)
    {
        this.type = type;
        this.value = value;
    }

    @Override
    public ProtectionFlagType getType()
    {
        return type;
    }

    @Override
    public boolean getValue()
    {
        return value;
    }

    public void setValue(boolean value)
    {
        this.value = value;
    }
}
