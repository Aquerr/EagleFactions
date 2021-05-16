package io.github.aquerr.eaglefactions.common.commands.args;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.common.caching.FactionsCache;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class OwnFactionPlayerArgument extends CommandElement
{
    private final EagleFactions plugin;

    public OwnFactionPlayerArgument(final EagleFactions plugin, final @Nullable Text key)
    {
        super(key);
        this.plugin = plugin;
    }

    @Nullable
    @Override
    protected FactionPlayer parseValue(final CommandSource source, final CommandArgs args) throws ArgumentParseException
    {
        if (source instanceof Player)
        {
            final UUID playerUUID = ((Player) source).getUniqueId();
            final Optional<Faction> optionalFaction = plugin.getFactionLogic().getFactionByPlayerUUID(playerUUID);
            if (!optionalFaction.isPresent())
                throw args.createError(Text.of(Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

            final Faction faction = optionalFaction.get();
            final List<FactionPlayer> factionPlayers = new LinkedList<>();
            for (final UUID uuid : faction.getPlayers())
            {
                this.plugin.getPlayerManager().getFactionPlayer(uuid)
                        .ifPresent(factionPlayers::add);
            }

            if (args.hasNext())
            {
                String argument = args.next();

                for(FactionPlayer player : factionPlayers)
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
        else
        {
            final String argument = args.nextIfPresent().orElseThrow(() -> args.createError(Text.of("Argument is not a valid player!")));
            final Set<FactionPlayer> players = this.plugin.getPlayerManager().getServerPlayers();
            for (final FactionPlayer factionPlayer : players)
            {
                if (argument.equals(factionPlayer.getName()))
                    return factionPlayer;
            }
            throw args.createError(Text.of("Argument is not a valid player!"));
        }
    }

    @Override
    public List<String> complete(final CommandSource src, final CommandArgs args, final CommandContext context)
    {
        if (src instanceof Player)
        {
            final UUID playerUUID = ((Player) src).getUniqueId();
            final Optional<Faction> optionalFaction = plugin.getFactionLogic().getFactionByPlayerUUID(playerUUID);
            if (!optionalFaction.isPresent())
                return Collections.EMPTY_LIST;
            final Faction faction = optionalFaction.get();
            final List<FactionPlayer> factionPlayers = new LinkedList<>();
            for (final UUID uuid : faction.getPlayers())
            {
                final Optional<FactionPlayer> factionPlayer = this.plugin.getPlayerManager().getFactionPlayer(uuid);
                factionPlayers.add(factionPlayer.get());
            }

            if (args.hasNext())
            {
                String charSequence = args.nextIfPresent().get();
                final List<String> resultList = new ArrayList<>();
                for (int i = 0; i < factionPlayers.size(); i++)
                {
                    final FactionPlayer factionPlayer = factionPlayers.get(i);
                    final String factionPlayerName = factionPlayer.getName();
                    if (factionPlayerName.toLowerCase().startsWith(charSequence.toLowerCase()))
                    {
                        resultList.add(factionPlayerName);
                    }
                }
                return resultList;
            }
            return factionPlayers.stream().map(FactionPlayer::getName).collect(Collectors.toList());
        }
        else
        {
            final Set<FactionPlayer> factionPlayers = this.plugin.getPlayerManager().getServerPlayers();
            if (args.hasNext())
            {
                String argument = args.nextIfPresent().get();
                final List<String> resultList = new ArrayList<>();
                for (final FactionPlayer factionPlayer : factionPlayers)
                {
                    if (factionPlayer.getName().toLowerCase().startsWith(argument.toLowerCase()))
                        resultList.add(factionPlayer.getName());
                }
                return resultList;
            }
            else
            {
                return factionPlayers.stream().map(FactionPlayer::getName).collect(Collectors.toList());
            }
        }
    }
}
