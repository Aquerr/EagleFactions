package io.github.aquerr.eaglefactions.commands.management;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionHome;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.Optional;

public class SetHomeCommand extends AbstractCommand
{
    private final FactionsConfig factionsConfig;
    private final MessageService messageService;

    public SetHomeCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
        this.messageService = plugin.getMessageService();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final ServerPlayer player = requirePlayerSource(context);
        final Faction playerFaction = requirePlayerFaction(player);
        final ServerWorld world = player.world();
        final FactionHome newHome = new FactionHome(world.uniqueId(), player.serverLocation().blockPosition());

        if(super.getPlugin().getPlayerManager().hasAdminMode(player.user()))
        {
            super.getPlugin().getFactionLogic().setHome(playerFaction, newHome);
            player.sendMessage(messageService.resolveMessageWithPrefix("command.set-home.success"));
            return CommandResult.success();
        }

        if(playerFaction.getLeader().equals(player.uniqueId()) || playerFaction.getOfficers().contains(player.uniqueId()))
        {
            final Optional<Faction> chunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(world.uniqueId(), player.serverLocation().chunkPosition());
            if (!chunkFaction.isPresent() && this.factionsConfig.canPlaceHomeOutsideFactionClaim())
            {
                super.getPlugin().getFactionLogic().setHome(playerFaction, newHome);
                player.sendMessage(messageService.resolveMessageWithPrefix("command.set-home.success"));
            }
            else if (!chunkFaction.isPresent() && !this.factionsConfig.canPlaceHomeOutsideFactionClaim())
            {
                throw messageService.resolveExceptionWithMessage("error.command.set-home.faction-home-must-be-placed-inside-faction-territory");
            }
            else if(chunkFaction.isPresent() && chunkFaction.get().getName().equals(playerFaction.getName()))
            {
                super.getPlugin().getFactionLogic().setHome(playerFaction, newHome);
                player.sendMessage(messageService.resolveMessageWithPrefix("command.set-home.success"));
            }
            else
            {
                throw messageService.resolveExceptionWithMessage("error.claim.place-belongs-to-someone-else");
            }
        }
        else
        {
            throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS);
        }

        return CommandResult.success();
    }
}
