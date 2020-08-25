package io.github.aquerr.eaglefactions.common.commands.args;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.common.caching.FactionsCache;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class FactionPlayerArgument extends CommandElement
{
    private final EagleFactions plugin;

    public FactionPlayerArgument(final EagleFactions plugin, final @Nullable Text key)
    {
        super(key);
        this.plugin = plugin;
    }

    @Nullable
    @Override
    protected FactionPlayer parseValue(final CommandSource source, final CommandArgs args) throws ArgumentParseException
    {
        //Just in case someone new entered the server after start.
        Set<FactionPlayer> serverPlayers = new HashSet<>(FactionsCache.getPlayersMap().values());

        if (args.hasNext())
        {
            String argument = args.next();

            for(FactionPlayer player : serverPlayers)
            {
                if(player.getName().equals(argument))
                    return player;
            }

            throw args.createError(Text.of("Argument is not a valid player!"));
        }
        else
        {
            throw args.createError(Text.of("Argument is not a valid player!"));
        }
    }

    @Override
    public List<String> complete(final CommandSource src, final CommandArgs args, final CommandContext context)
    {
        final Map<UUID, FactionPlayer> factionPlayerMap = FactionsCache.getPlayersMap();

        final List<FactionPlayer> list = new ArrayList<>(factionPlayerMap.values());
        if (args.hasNext())
        {
            String charSequence = args.nextIfPresent().get();
            final List<String> resultList = new ArrayList<>();
            for (int i = 0; i < list.size(); i++)
            {
                final FactionPlayer factionPlayer = list.get(i);
                final String factionPlayerName = factionPlayer.getName();
                if (factionPlayerName.toLowerCase().startsWith(charSequence.toLowerCase()))
                {
                    resultList.add(factionPlayerName);
                }
            }
            return resultList;
        }
        return list.stream().map(FactionPlayer::getName).collect(Collectors.toList());
    }
}
