package io.github.aquerr.eaglefactions.commands.args;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueCompleter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.*;

public class OwnFactionPlayerArgument
{
    private OwnFactionPlayerArgument()
    {

    }

    public static class ValueParser implements org.spongepowered.api.command.parameter.managed.ValueParser<FactionPlayer>
    {
        private final FactionLogic factionLogic;
        private final PlayerManager playerManager;
        private final MessageService messageService;

        public ValueParser(MessageService messageService, FactionLogic factionLogic, PlayerManager playerManager)
        {
            this.factionLogic = factionLogic;
            this.playerManager = playerManager;
            this.messageService = messageService;
        }

        @Override
        public Optional<? extends FactionPlayer> parseValue(Parameter.Key<? super FactionPlayer> parameterKey, ArgumentReader.Mutable reader, CommandContext.Builder context) throws ArgumentParseException
        {
            ServerPlayer serverPlayer = context.cause().first(ServerPlayer.class).orElse(null);
            if (serverPlayer != null)
            {
                UUID playerUUID = serverPlayer.uniqueId();
                final Faction faction = this.factionLogic.getFactionByPlayerUUID(playerUUID)
                        .orElseThrow(() -> reader.createException(Component.text(messageService.resolveMessage(EFMessageService.ERROR_YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND_MESSAGE_KEY))));

                final List<FactionPlayer> factionPlayers = new LinkedList<>();
                for (final UUID uuid : faction.getPlayers())
                {
                    this.playerManager.getFactionPlayer(uuid)
                            .ifPresent(factionPlayers::add);
                }

                if (reader.canRead())
                {
                    String argument = reader.parseUnquotedString();
                    for (FactionPlayer player : factionPlayers)
                    {
                        if (player.getName().equals(argument))
                            return Optional.of(player);
                    }
                }
                throw reader.createException(Component.text("Argument is not a valid player!"));
            }
            else
            {
                final String argument = reader.parseUnquotedString();
                final Set<FactionPlayer> players = this.playerManager.getServerPlayers();
                for (final FactionPlayer factionPlayer : players)
                {
                    if (argument.equals(factionPlayer.getName()))
                        return Optional.of(factionPlayer);
                }
                throw reader.createException(Component.text("Argument is not a valid player!"));
            }
        }
    }

    public static class Completer implements ValueCompleter
    {
        private final FactionLogic factionLogic;
        private final PlayerManager playerManager;

        public Completer(FactionLogic factionLogic, PlayerManager playerManager)
        {
            this.factionLogic = factionLogic;
            this.playerManager = playerManager;
        }

        @Override
        public List<CommandCompletion> complete(CommandContext context, String currentInput)
        {
            final ServerPlayer serverPlayer = context.cause().first(ServerPlayer.class).orElse(null);
            if (serverPlayer != null)
            {
                final UUID playerUUID = serverPlayer.uniqueId();
                final Optional<Faction> optionalFaction = factionLogic.getFactionByPlayerUUID(playerUUID);
                if (!optionalFaction.isPresent())
                    return Collections.emptyList();
                final Faction faction = optionalFaction.get();
                final List<FactionPlayer> factionPlayers = new LinkedList<>();
                for (final UUID uuid : faction.getPlayers())
                {
                    this.playerManager.getFactionPlayer(uuid)
                            .ifPresent(factionPlayers::add);
                }

                final List<CommandCompletion> resultList = new LinkedList<>();
                for (final FactionPlayer factionPlayer : factionPlayers)
                {
                    final String factionPlayerName = factionPlayer.getName();
                    if (factionPlayerName.toLowerCase().startsWith(currentInput.toLowerCase()))
                    {
                        resultList.add(CommandCompletion.of(factionPlayerName));
                    }
                }
                return resultList;
            }
            else
            {
                final Set<FactionPlayer> factionPlayers = this.playerManager.getServerPlayers();
                final List<CommandCompletion> resultList = new LinkedList<>();
                for (final FactionPlayer factionPlayer : factionPlayers)
                {
                    if (factionPlayer.getName().toLowerCase().startsWith(currentInput.toLowerCase()))
                        resultList.add(CommandCompletion.of(factionPlayer.getName()));
                }
                return resultList;
            }
        }
    }
}
