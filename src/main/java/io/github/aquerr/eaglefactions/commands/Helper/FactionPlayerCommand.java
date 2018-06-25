package io.github.aquerr.eaglefactions.commands.Helper;

import com.google.inject.Inject;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.FactionMemberType;
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

/**
 * Gets rid of all of the annoying repetitive checks so a command can contain what it really needs.
 */
@RequiredRank
public abstract class FactionPlayerCommand implements CommandExecutor
{
    @Inject
    protected FactionsCache cache;

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if (source instanceof Player)
        {
            Optional<Faction> faction = cache.getFactionByPlayer(((Player) source).getUniqueId());
            if (faction.isPresent())
            {
                FactionMemberType memberType = PlayerManager.getFactionMemberType((Player) source, faction.get());
                if (memberType.compareTo(((RequiredRank) this).minimumRank()) <= 0 || EagleFactions.AdminList.contains(((Player) source).getUniqueId()))
                {
                    if(executeCommand((Player) source, faction.get(), context)){
                        return CommandResult.success();
                    }else{
                        throw new CommandException(Text.of(TextColors.RED, "Something went wrong in ", getClass().getCanonicalName()));
                    }
                }else{
                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, "You need to be a ", TextColors.WHITE,
                            memberType.toString().toLowerCase(), TextColors.RED, " in your faction to use this command!"));
                }
            } else
            {
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));
            }
        } else
        {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));
        }
        return CommandResult.success();
    }

    protected abstract boolean executeCommand(Player player, Faction faction, CommandContext context);
}
