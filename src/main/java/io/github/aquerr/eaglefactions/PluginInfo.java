package io.github.aquerr.eaglefactions;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Created by Aquerr on 2017-07-10.
 */

public final class PluginInfo
{
    public static final String ID = "eaglefactions";
    public static final String NAME = "Eagle Factions";
    public static final String VERSION = "%VERSION%";
    public static final String DESCRIPTION = "A factions plugin that will make managing your battle-server easier. :)";
    public static final String PLUGIN_PREFIX_PLAIN = "[EF] ";
    public static final TextComponent PLUGIN_PREFIX = Component.text(PLUGIN_PREFIX_PLAIN, NamedTextColor.AQUA);
    public static final TextComponent ERROR_PREFIX = Component.text(PLUGIN_PREFIX_PLAIN, NamedTextColor.DARK_RED);
    public static final String AUTHOR = "Aquerr";
    public static final String URL = "https://github.com/Aquerr/EagleFactions";

    private PluginInfo()
    {

    }
}