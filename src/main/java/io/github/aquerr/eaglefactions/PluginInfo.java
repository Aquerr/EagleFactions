package io.github.aquerr.eaglefactions;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

/**
 * Created by Aquerr on 2017-07-10.
 */

public abstract class PluginInfo
{
    public static final String Id = "eaglefactions";
    public static final String Name = "Eagle Factions";
    public static final String Version = "0.9.7";
    public static final String Description = "A factions plugin that will make managing your battle-server easier. :)";
    public static final Text PluginPrefix = Text.of(TextColors.AQUA, "[" + Name + "] ");
    public static final Text ErrorPrefix = Text.of(TextColors.DARK_RED, "[ERROR] ");
    public static final String Author = "Aquerr";
}