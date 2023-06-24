package io.github.aquerr.eaglefactions.commands;

import com.mojang.brigadier.Command;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.exception.CommandException;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public abstract class AbstractCommand implements Command<CommandSourceStack>
{
    private final EagleFactions eagleFactions;

    protected AbstractCommand(final EagleFactions eagleFactions)
    {
        this.eagleFactions = eagleFactions;
    }

    public EagleFactions getPlugin()
    {
        return eagleFactions;
    }

    protected Faction requirePlayerFaction(ServerPlayer player) throws CommandException
    {
        final Optional<Faction> optionalPlayerFaction = this.eagleFactions.getFactionManager().getFactionByPlayerUUID(player.getUUID());
        return optionalPlayerFaction.orElseThrow(() -> this.eagleFactions.getMessageService().resolveExceptionWithMessage(EFMessageService.ERROR_YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND_MESSAGE_KEY));
    }

    protected ServerPlayer requirePlayerSource(CommandSourceStack commandSourceStack) throws CommandException
    {
        if(!commandSourceStack.isPlayer())
            throw this.eagleFactions.getMessageService().resolveExceptionWithMessage(EFMessageService.ERROR_ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND);
        return commandSourceStack.getPlayer();
    }

    protected boolean isServerPlayer(CommandSourceStack commandSourceStack)
    {
        return commandSourceStack.isPlayer();
    }
}
