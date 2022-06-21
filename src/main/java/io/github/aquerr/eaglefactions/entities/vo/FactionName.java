package io.github.aquerr.eaglefactions.entities.vo;

import java.util.Objects;

public class FactionName
{
    private final String name;

    public static FactionName of(String name)
    {
        //TODO: Add faction's name validation here?
        return new FactionName(name);
    }

    private FactionName(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FactionName that = (FactionName) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name);
    }
}
