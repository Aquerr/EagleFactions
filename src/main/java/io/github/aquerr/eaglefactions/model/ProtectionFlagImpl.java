package io.github.aquerr.eaglefactions.model;

import io.github.aquerr.eaglefactions.api.entities.ProtectionFlag;
import io.github.aquerr.eaglefactions.api.entities.ProtectionFlagType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ProtectionFlagImpl implements ProtectionFlag, Comparable<ProtectionFlag>
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

    @Override
    public int compareTo(@NotNull ProtectionFlag o)
    {
        return this.type.getName().compareTo(o.getType().getName());
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProtectionFlagImpl that = (ProtectionFlagImpl) o;
        return type == that.type;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(type);
    }

    @Override
    public String toString()
    {
        return "ProtectionFlagImpl{" +
                "type=" + type +
                ", value=" + value +
                '}';
    }
}