package io.github.aquerr.eaglefactions.common.messaging;

public class Placeholder
{
    private final String placeholder;

    Placeholder(final String placeholder)
    {
        this.placeholder = placeholder;
    }

    public String getPlaceholder()
    {
        return this.placeholder;
    }

    @Override
    public String toString()
    {
        return this.placeholder;
    }

    @Override
    public int hashCode()
    {
        return this.placeholder.hashCode();
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Placeholder that = (Placeholder)o;
        return this.placeholder.equals(that.placeholder);
    }
}