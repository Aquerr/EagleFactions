package io.github.aquerr.eaglefactions.commands.args;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.CommandCompletion;
import org.spongepowered.api.command.exception.ArgumentParseException;
import org.spongepowered.api.command.parameter.ArgumentReader;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.command.parameter.managed.ValueCompleter;
import org.spongepowered.configurate.util.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class FactionArgument
{
	private FactionArgument()
	{

	}

	public static class ValueParser implements org.spongepowered.api.command.parameter.managed.ValueParser<Faction>
	{
		private final FactionLogic factionLogic;

		public ValueParser(FactionLogic factionLogic)
		{
			this.factionLogic = factionLogic;
		}

		@Override
		public Optional<Faction> parseValue(Parameter.Key<? super Faction> parameterKey, ArgumentReader.Mutable reader, CommandContext.Builder context) throws ArgumentParseException
		{
			if(!reader.canRead())
				throw reader.createException(Component.text("Argument is not a valid faction!"));

			final String factionName = reader.parseUnquotedString();
			if (Strings.isBlank(factionName))
				throw reader.createException(Component.text("Argument is not a valid faction!"));
			Faction faction = Optional.ofNullable(this.factionLogic.getFactionByName(factionName))
					.orElseThrow(() -> reader.createException(Component.text("Argument is not a valid faction!")));
			return Optional.ofNullable(faction);
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
			final Set<String> factionNames = this.factionLogic.getFactionsNames();
			final List<String> list = new ArrayList<>(factionNames);
			Collections.sort(list);

			String charSequence = currentInput.toLowerCase();
			return list.stream()
					.filter(x->x.contains(charSequence))
					.map(CommandCompletion::of)
					.collect(Collectors.toList());
		}
	}
}
