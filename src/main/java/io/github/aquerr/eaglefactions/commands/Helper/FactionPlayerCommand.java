package io.github.aquerr.eaglefactions.commands.Helper;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.commands.annotations.AllowedGroups;
import io.github.aquerr.eaglefactions.commands.annotations.RequiredRank;
import io.github.aquerr.eaglefactions.commands.annotations.RequiresFaction;
import io.github.aquerr.eaglefactions.config.Settings;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import io.github.aquerr.eaglefactions.managers.PlayerManager;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * Gets rid of all of the annoying repetitive checks so a command can contain what it really needs.
 */
@RequiredRank
@RequiresFaction
@AllowedGroups
public abstract class FactionPlayerCommand extends FactionCommand
{

    public FactionPlayerCommand(FactionsCache cache, Settings settings, FactionLogic factionLogic, Logger logger)
    {
        super(cache, settings, factionLogic, logger);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if(verifyConstraints(source)){
            if(!executeCommand((Player) source, cache.getFactionByPlayer(((Player) source).getUniqueId()).get(), context)){
                throw new CommandException(Text.of(TextColors.RED, "Something went wrong in ", getClass().getCanonicalName()));
            }
        }
        return CommandResult.success();
    }

    @Override
    protected boolean executeCommand(CommandSource source, CommandContext context)
    {
        return true;
    }

    protected abstract boolean executeCommand(Player player, Faction faction, CommandContext context);
}
