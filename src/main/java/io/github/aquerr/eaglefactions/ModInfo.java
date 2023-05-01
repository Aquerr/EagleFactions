package io.github.aquerr.eaglefactions;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public final class ModInfo
{
    public static final String ID = "eaglefactions";
    public static final String NAME = "Eagle Factions";
    public static final String VERSION = "%VERSION%";
    public static final String DESCRIPTION = "A factions plugin that will make managing your battle-server easier. :)";
    public static final String PLUGIN_PREFIX_PLAIN = "[EF] ";
    public static final Component MOD_PREFIX = Component.literal(PLUGIN_PREFIX_PLAIN).withStyle(ChatFormatting.AQUA);
    public static final Component ERROR_PREFIX = Component.literal(PLUGIN_PREFIX_PLAIN).withStyle(ChatFormatting.DARK_RED);
    public static final String AUTHOR = "Aquerr";
    public static final String URL = "https://github.com/Aquerr/EagleFactions";

    private ModInfo()
    {

    }
}