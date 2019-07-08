package io.github.aquerr.eaglefactions.common.parsers;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.entities.IFactionPlayer;
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
    public FactionPlayerArgument(@Nullable Text key)
    {
        super(key);
    }

    @Nullable
    @Override
    protected IFactionPlayer parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException
    {
        //Just in case someone new entered the server after start.
        Set<IFactionPlayer> serverPlayers = EagleFactionsPlugin.getPlugin().getPlayerManager().getServerPlayers();

        if (args.hasNext())
        {
            String argument = args.next();

            for(IFactionPlayer player : serverPlayers)
            {
                if(player.getName().equals(argument))
                    return player;
            }

            throw new ArgumentParseException(Text.of("Argument is not a valid player"), argument, argument.length());
        }
        else
        {
            return null;
        }
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context)
    {
        List<String> list = new ArrayList<>(EagleFactionsPlugin.getPlugin().getPlayerManager().getServerPlayerNames());

        if (args.hasNext())
        {
            String charSequence = args.nextIfPresent().get();
            return list.stream().filter(x->x.contains(charSequence)).collect(Collectors.toList());
        }

        return list;
    }
}
