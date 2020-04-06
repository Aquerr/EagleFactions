package io.github.aquerr.eaglefactions.common.commands.args;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
        Set<FactionPlayer> serverPlayers = plugin.getPlayerManager().getServerPlayers();

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
        List<String> list = new ArrayList<>(this.plugin.getPlayerManager().getServerPlayerNames());

        if (args.hasNext())
        {
            String charSequence = args.nextIfPresent().get();
            return list.stream().filter(x->x.contains(charSequence)).collect(Collectors.toList());
        }

        return list;
    }
}
