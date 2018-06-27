package io.github.aquerr.eaglefactions.commands;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.commands.assembly.FactionCommand;
import io.github.aquerr.eaglefactions.commands.annotations.Subcommand;
import io.github.aquerr.eaglefactions.config.Settings;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.logging.Logger;

@Singleton
@Subcommand(aliases = {"version", "v"}, description = "Shows plugin version.", permission = PluginPermissions.VersionCommand)
public class VersionCommand extends FactionCommand
{
    @Inject
    public VersionCommand(FactionsCache cache, Settings settings, FactionLogic factionLogic, @Named("factions") Logger logger)
    {
        super(cache, settings, factionLogic, logger);
    }

    @Override
    protected boolean executeCommand(CommandSource source, CommandContext context)
    {
        source.sendMessage(Text.of(TextColors.AQUA, PluginInfo.Name, TextColors.WHITE, " - " + PluginMessages.VERSION + " ", PluginInfo.Version));
        return true;
    }
}
