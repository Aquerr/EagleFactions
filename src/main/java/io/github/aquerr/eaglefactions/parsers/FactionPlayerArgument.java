package io.github.aquerr.eaglefactions.parsers;

import io.github.aquerr.eaglefactions.managers.PlayerManager;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

public class FactionPlayerArgument extends CommandElement
{
    List<Player> _serverPlayers = PlayerManager.getServerPlayers();

    public FactionPlayerArgument(@Nullable Text key)
    {
        super(key);
    }

    @Nullable
    @Override
    protected Player parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException
    {
        //Just in case someone new entered the server after start.
        this._serverPlayers = PlayerManager.getServerPlayers();

        if (args.hasNext())
        {
            String argument = args.next();

            for(Player player : _serverPlayers)
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
        List<String> list = _serverPlayers.stream().map(User::getName).collect(Collectors.toList());

        if (args.hasNext())
        {
            String charSequence = args.nextIfPresent().get();
            return list.stream().filter(x->x.contains(charSequence)).collect(Collectors.toList());
        }

        return list;
    }
}
