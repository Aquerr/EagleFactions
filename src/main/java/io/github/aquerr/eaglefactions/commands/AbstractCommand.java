package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import net.kyori.adventure.audience.Audience;
import org.spongepowered.api.command.CommandExecutor;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Optional;

public abstract class AbstractCommand implements CommandExecutor
{
    private EagleFactions plugin;

    protected AbstractCommand(final EagleFactions plugin)
    {
        this.plugin = plugin;
    }

    public EagleFactions getPlugin()
    {
        return plugin;
    }

    protected Faction requirePlayerFaction(Player player) throws CommandException
    {
        final Optional<Faction> optionalPlayerFaction = this.plugin.getFactionLogic().getFactionByPlayerUUID(player.uniqueId());
        return optionalPlayerFaction.orElseThrow(() -> this.plugin.getMessageService().resolveExceptionWithMessage(EFMessageService.ERROR_YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND_MESSAGE_KEY));
    }

    protected ServerPlayer requirePlayerSource(CommandContext context) throws CommandException
    {
        if(!isServerPlayer(context.cause().audience()))
            throw this.plugin.getMessageService().resolveExceptionWithMessage(EFMessageService.ERROR_ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND);
        return(ServerPlayer) context.cause().audience();
    }

    protected boolean isServerPlayer(Audience audience)
    {
        return audience instanceof ServerPlayer;
    }
}
