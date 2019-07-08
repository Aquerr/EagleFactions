package io.github.aquerr.eaglefactions.common.parsers;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FactionNameArgument extends CommandElement
{
    public FactionNameArgument(@Nullable Text key)
    {
        super(key);
    }

    @Nullable
    @Override
    protected String parseValue(CommandSource source, CommandArgs args) throws ArgumentParseException
    {
        if (args.hasNext())
        {
            return args.next();
        }
        else
        {
            return null;
        }
    }

    @Override
    public List<String> complete(CommandSource src, CommandArgs args, CommandContext context)
    {
        Set<String> factionNames = EagleFactionsPlugin.getPlugin().getFactionLogic().getFactionsNames();
        List<String> list = new ArrayList<>(factionNames);
        Collections.sort(list);

        if (args.hasNext())
        {
            String charSequence = args.nextIfPresent().get().toLowerCase();
            return list.stream().filter(x->x.contains(charSequence)).collect(Collectors.toList());
        }

        return list;
    }
}
