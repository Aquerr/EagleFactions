package io.github.aquerr.eaglefactions.parsers;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.ArgumentParseException;
import org.spongepowered.api.command.args.CommandArgs;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
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
        EagleFactions.getEagleFactions().getLogger().info(args.toString());
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
        List<String> factionNames = FactionLogic.getFactionsNames();
        Collections.sort(factionNames);

        if (args.hasNext())
        {
            String charSequence = args.nextIfPresent().get();
            return factionNames.stream().filter(x->x.contains(charSequence)).collect(Collectors.toList());
        }

        return factionNames;
    }
}
