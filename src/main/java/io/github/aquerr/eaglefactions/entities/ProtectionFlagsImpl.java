package io.github.aquerr.eaglefactions.entities;

import io.github.aquerr.eaglefactions.api.entities.ProtectionFlag;
import io.github.aquerr.eaglefactions.api.entities.ProtectionFlagType;
import io.github.aquerr.eaglefactions.api.entities.ProtectionFlags;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ProtectionFlagsImpl implements ProtectionFlags
{
    private final Map<ProtectionFlagType, Boolean> flags;

    public ProtectionFlagsImpl(Set<ProtectionFlag> flags)
    {
        this.flags = new HashMap<>();
        flags.forEach(protectionFlag -> this.flags.put(protectionFlag.getType(), protectionFlag.getValue()));
    }

    @Override
    public Set<ProtectionFlag> getProtectionFlags()
    {
        return flags.entrySet().stream()
                .map(entry -> new ProtectionFlagImpl(entry.getKey(), entry.getValue()))
                .collect(Collectors.toSet());
    }

    @Override
    public boolean getValueForFlag(ProtectionFlagType type)
    {
        return Optional.ofNullable(flags.get(type))
                .orElse(false);
    }

    @Override
    public void putFlag(ProtectionFlag protectionFlag)
    {
        this.flags.put(protectionFlag.getType(), protectionFlag.getValue());
    }
}
