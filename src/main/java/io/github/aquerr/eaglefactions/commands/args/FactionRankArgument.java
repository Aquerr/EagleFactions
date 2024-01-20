package io.github.aquerr.eaglefactions.commands.args;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.Rank;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
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
import java.util.stream.Collectors;

public class FactionRankArgument
{
    private FactionRankArgument()
    {

    }

    public static class ValueParser implements org.spongepowered.api.command.parameter.managed.ValueParser<Rank>
    {
        private final FactionLogic factionLogic;

        public ValueParser(FactionLogic factionLogic)
        {
            this.factionLogic = factionLogic;
        }

        @Override
        public Optional<? extends Rank> parseValue(Parameter.Key<? super Rank> parameterKey, ArgumentReader.Mutable reader, CommandContext.Builder context) throws ArgumentParseException
        {
            Faction faction = context.one(EagleFactionsCommandParameters.faction())
                    .or(() -> getFactionIfServerPlayer(context))
                    .orElse(null);

            if (faction == null)
                return Optional.empty();

            List<Rank> ranks = faction.getRanks();
            if (reader.canRead())
            {
                String argument = reader.parseUnquotedString();

                for(Rank rank : ranks)
                {
                    if(rank.getName().equals(argument))
                        return Optional.of(rank);
                }

                throw reader.createException(Component.text("Argument is not a valid rank!"));
            }
            else
            {
                throw reader.createException(Component.text("Argument is not a valid rank!"));
            }
        }

        private Optional<? extends Faction> getFactionIfServerPlayer(CommandContext.Builder context)
        {
            return Optional.of(context.cause().audience())
                    .filter(ServerPlayer.class::isInstance)
                    .map(ServerPlayer.class::cast)
                    .map(ServerPlayer::uniqueId)
                    .flatMap(factionLogic::getFactionByPlayerUUID);
        }
    }
    public static class Completer implements ValueCompleter
    {
        private final FactionLogic factionLogic;

        public Completer(FactionLogic factionLogic)
        {
            this.factionLogic = factionLogic;
        }

        @Override
        public List<CommandCompletion> complete(CommandContext context, String currentInput)
        {
            Faction faction = context.one(EagleFactionsCommandParameters.faction())
                    .or(() -> getFactionIfServerPlayer(context))
                    .orElse(null);

            if (faction == null)
                return Collections.emptyList();

            final List<Rank> ranks = faction.getRanks();
            final List<String> resultList = new LinkedList<>();
            for (final Rank rank : ranks)
            {
                final String rankName = rank.getName();
                if (rankName.toLowerCase().startsWith(currentInput.toLowerCase()))
                {
                    resultList.add(rankName);
                }
            }
            return resultList.stream()
                    .map(CommandCompletion::of)
                    .collect(Collectors.toList());
        }

        private Optional<? extends Faction> getFactionIfServerPlayer(CommandContext context)
        {
            return Optional.of(context.cause().audience())
                    .filter(ServerPlayer.class::isInstance)
                    .map(ServerPlayer.class::cast)
                    .map(ServerPlayer::uniqueId)
                    .flatMap(factionLogic::getFactionByPlayerUUID);
        }
    }
}
