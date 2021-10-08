package io.github.aquerr.eaglefactions.commands.args;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import org.apache.commons.lang3.StringUtils;
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

public class FactionArgument extends CommandElement
{
	private final EagleFactions plugin;

	public FactionArgument(final EagleFactions plugin, @Nullable Text key)
	{
		super(key);
		this.plugin = plugin;
	}

	@Nullable
	@Override
	protected Faction parseValue(final CommandSource source, final CommandArgs args) throws ArgumentParseException
	{
		if(!args.hasNext())
			throw args.createError(Text.of("Argument is not a valid faction!"));

		final String factionName = args.next();
		if (StringUtils.isBlank(factionName))
			throw args.createError(Text.of("Argument is not a valid faction!"));
		return this.plugin.getFactionLogic().getFactionByName(factionName);
	}

	@Override
	public List<String> complete(final CommandSource src, final CommandArgs args, final CommandContext context)
	{
		final Set<String> factionNames = plugin.getFactionLogic().getFactionsNames();
		final List<String> list = new ArrayList<>(factionNames);
		Collections.sort(list);

		if (args.hasNext())
		{
			String charSequence = args.nextIfPresent().get().toLowerCase();
			return list.stream().filter(x->x.contains(charSequence)).collect(Collectors.toList());
		}

		return list;
	}
}
