package io.github.aquerr.eaglefactions.messaging;

import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Wrapper around {@link NamedTextColor}
 */
public enum TextColor
{
    RED(NamedTextColor.RED),
    DARK_RED(NamedTextColor.DARK_RED),
    GREEN(NamedTextColor.GREEN);

    private NamedTextColor namedTextColor;

    TextColor(NamedTextColor namedTextColor)
    {
        this.namedTextColor = namedTextColor;
    }

    public NamedTextColor getNamedTextColor()
    {
        return namedTextColor;
    }
}
