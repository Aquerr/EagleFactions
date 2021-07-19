package io.github.aquerr.eaglefactions.commands.args;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.messaging.Messages;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueCompleter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class OwnFactionPlayerArgument
{
    private OwnFactionPlayerArgument()
    {

    }
//    @Override
//    public List<String> complete(final CommandSource src, final CommandArgs args, final CommandContext context)
//    {
//        if (src instanceof Player)
//        {
//            final UUID playerUUID = ((Player) src).getUniqueId();
//            final Optional<Faction> optionalFaction = plugin.getFactionLogic().getFactionByPlayerUUID(playerUUID);
//            if (!optionalFaction.isPresent())
//                return Collections.EMPTY_LIST;
//            final Faction faction = optionalFaction.get();
//            final List<FactionPlayer> factionPlayers = new LinkedList<>();
//            for (final UUID uuid : faction.getPlayers())
//            {
//                this.plugin.getPlayerManager().getFactionPlayer(uuid)
//                        .ifPresent(factionPlayers::add);
//            }
//
//            if (args.hasNext())
//            {
//                String charSequence = args.nextIfPresent().get();
//                final List<String> resultList = new ArrayList<>();
//                for (int i = 0; i < factionPlayers.size(); i++)
//                {
//                    final FactionPlayer factionPlayer = factionPlayers.get(i);
//                    final String factionPlayerName = factionPlayer.getName();
//                    if (factionPlayerName.toLowerCase().startsWith(charSequence.toLowerCase()))
//                    {
//                        resultList.add(factionPlayerName);
//                    }
//                }
//                return resultList;
//            }
//            return factionPlayers.stream().map(FactionPlayer::getName).collect(Collectors.toList());
//        }
//        else
//        {
//            final Set<FactionPlayer> factionPlayers = this.plugin.getPlayerManager().getServerPlayers();
//            if (args.hasNext())
//            {
//                String argument = args.nextIfPresent().get();
//                final List<String> resultList = new ArrayList<>();
//                for (final FactionPlayer factionPlayer : factionPlayers)
//                {
//                    if (factionPlayer.getName().toLowerCase().startsWith(argument.toLowerCase()))
//                        resultList.add(factionPlayer.getName());
//                }
//                return resultList;
//            }
//            else
//            {
//                return factionPlayers.stream().map(FactionPlayer::getName).collect(Collectors.toList());
//            }
//        }
//    }

    public static class ValueParser implements org.spongepowered.api.command.parameter.managed.ValueParser<FactionPlayer>
    {
        private final FactionLogic factionLogic;
        private final PlayerManager playerManager;

        public ValueParser(FactionLogic factionLogic, PlayerManager playerManager)
        {
            this.factionLogic = factionLogic;
            this.playerManager = playerManager;
        }

        @Override
        public Optional<? extends FactionPlayer> parseValue(Parameter.Key<? super FactionPlayer> parameterKey, ArgumentReader.Mutable reader, CommandContext.Builder context) throws ArgumentParseException
        {
            ServerPlayer serverPlayer = context.cause().first(ServerPlayer.class).orElse(null);
            if (serverPlayer != null)
            {
                UUID playerUUID = serverPlayer.uniqueId();
                final Optional<Faction> optionalFaction = this.factionLogic.getFactionByPlayerUUID(playerUUID);
                if (!optionalFaction.isPresent())
                    throw reader.createException(Component.text(Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

                final Faction faction = optionalFaction.get();
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
