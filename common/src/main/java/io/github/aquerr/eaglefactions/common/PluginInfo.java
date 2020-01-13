package io.github.aquerr.eaglefactions.common;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

/**
 * Created by Aquerr on 2017-07-10.
 */

public final class PluginInfo
{
    public static final String ID = "eaglefactions";
    public static final String NAME = "Eagle Factions";
    public static final String VERSION = "0.13.2";
    public static final String DESCRIPTION = "A factions plugin that will make managing your battle-server easier. :)";
    public static final Text PLUGIN_PREFIX = Text.of(TextColors.AQUA, "[EF] ");
    public static final Text ERROR_PREFIX = Text.of(TextColors.DARK_RED, "[EF] ");
    public static final String AUTHOR = "Aquerr";

    private PluginInfo()
    {

    }
}