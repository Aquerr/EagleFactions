package io.github.aquerr.eaglefactions.entities;

import io.github.aquerr.eaglefactions.api.entities.ProtectionFlag;
import io.github.aquerr.eaglefactions.api.entities.ProtectionFlagType;
import io.github.aquerr.eaglefactions.api.entities.ProtectionFlags;

import java.util.Collections;
import java.util.Set;

public class ProtectionFlagsImpl implements ProtectionFlags
{
    private final Set<ProtectionFlag> flags;

    public ProtectionFlagsImpl(Set<ProtectionFlag> flags)
    {
        this.flags = flags;
    }

    @Override
    public Set<ProtectionFlag> getProtectionFlags()
    {
        return Collections.unmodifiableSet(flags);
    }

    @Override
    public boolean getValueForFlag(ProtectionFlagType type)
    {
        return flags.stream()
                .filter(protectionFlag -> protectionFlag.getType() == type)
                .findFirst()
                .map(ProtectionFlag::getValue)
                .orElse(false);
    }

    @Override
    public void add(ProtectionFlag flag)
    {
        this.flags.add(flag);
    }
}
