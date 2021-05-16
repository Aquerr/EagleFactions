package io.github.aquerr.eaglefactions.common.commands;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public abstract class AbstractCommand implements CommandExecutor
{
    private EagleFactions plugin;

    public AbstractCommand(final EagleFactions plugin)
    {
        this.plugin = plugin;
    }

    public EagleFactions getPlugin()
    {
        return plugin;
    }

    @Override
    public abstract CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException;

    protected Faction requirePlayerFaction(Player player) throws CommandException
    {
        final Optional<Faction> optionalPlayerFaction = this.plugin.getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        return optionalPlayerFaction.orElseThrow(() -> new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND)));
    }

    protected Player requirePlayerSource(CommandSource commandSource) throws CommandException
    {
        if(!isPlayer(commandSource))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));
        return(Player) commandSource;
    }

    protected boolean isPlayer(CommandSource commandSource)
    {
        return commandSource instanceof Player;
    }

//    @Override
//    public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
//    {
//        if (!(source instanceof Player))
//            return CommandResult.success();
//
//        final Player player = (Player)source;
//        final Optional<Faction> optionalFaction = this.plugin.getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
//        if (!optionalFaction.isPresent())
//            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.DARK_RED, "You don't have access to use this command!"));
//
//        final Faction faction = optionalFaction.get();
//        final FactionMemberType playerMemberType = faction.getPlayerMemberType(player.getUniqueId());
//
//        //Check if player has access for this command.
//        final Class<? extends AbstractCommand> clazz = this.getClass();
//        final RequiredRank annotation = clazz.getAnnotation(RequiredRank.class);
//        if (annotation != null)
//        {
//            if (canUseCommand(annotation.rank(), playerMemberType))
//                return CommandResult.success();
//            else
//            {
//                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.DARK_RED, "You don't have access to use this command!"));
//            }
//        }
//
//        return CommandResult.success();
//    }

//    private boolean canUseCommand(final FactionMemberType requiredRank, final FactionMemberType playerRank)
//    {
//        switch (requiredRank)
//        {
//            case LEADER:
//                return playerRank == FactionMemberType.LEADER;
//            case OFFICER:
//                return playerRank == FactionMemberType.LEADER || playerRank == FactionMemberType.OFFICER;
//            case MEMBER:
//                return playerRank == FactionMemberType.LEADER || playerRank == FactionMemberType.OFFICER || playerRank == FactionMemberType.MEMBER;
//            case RECRUIT:
//                return playerRank == FactionMemberType.LEADER || playerRank == FactionMemberType.OFFICER || playerRank == FactionMemberType.MEMBER || playerRank == FactionMemberType.RECRUIT;
//            default:
//                return false;
//        }
//    }
}
