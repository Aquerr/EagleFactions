package io.github.aquerr.eaglefactions.commands.args;

import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueCompleter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class FactionPlayerArgument
{
    private FactionPlayerArgument()
    {

    }

    public static class ValueParser implements org.spongepowered.api.command.parameter.managed.ValueParser<FactionPlayer>
    {
        @Override
        public Optional<? extends FactionPlayer> parseValue(Parameter.Key<? super FactionPlayer> parameterKey, ArgumentReader.Mutable reader, CommandContext.Builder context) throws ArgumentParseException
        {
            //Just in case someone new entered the server after start.
            Set<FactionPlayer> serverPlayers = new HashSet<>(FactionsCache.getPlayersMap().values());

            if (reader.canRead())
            {
                String argument = reader.parseUnquotedString();

                for(FactionPlayer player : serverPlayers)
                {
                    if(player.getName().equals(argument))
                        return Optional.of(player);
                }

                throw reader.createException(Component.text("Argument is not a valid player!"));
            }
            else
            {
                throw reader.createException(Component.text("Argument is not a valid player!"));
            }
        }
    }

    public static class Completer implements ValueCompleter
    {
        @Override
        public List<CommandCompletion> complete(CommandContext context, String currentInput)
        {
            final Map<UUID, FactionPlayer> factionPlayerMap = FactionsCache.getPlayersMap();

            final List<FactionPlayer> list = new ArrayList<>(factionPlayerMap.values());
            final List<String> resultList = new LinkedList<>();
            for (final FactionPlayer factionPlayer : list)
            {
                final String factionPlayerName = factionPlayer.getName();
                if (factionPlayerName.toLowerCase().startsWith(currentInput.toLowerCase()))
                {
                    resultList.add(factionPlayerName);
                }
            }
            return list.stream()
                    .map(FactionPlayer::getName)
                    .map(CommandCompletion::of)
                    .collect(Collectors.toList());
        }
    }
}
