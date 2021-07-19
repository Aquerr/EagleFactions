package io.github.aquerr.eaglefactions.commands.management;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.exception.RequiredItemsNotFoundException;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.entities.FactionImpl;
import io.github.aquerr.eaglefactions.entities.FactionPlayerImpl;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.messaging.Placeholders;
import io.github.aquerr.eaglefactions.util.ItemUtil;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

/**
 * Created by Aquerr on 2017-07-12.
 */
public class CreateCommand extends AbstractCommand
{
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("[A-Za-z][A-Za-z0-9]*$");

    private final FactionsConfig factionsConfig;

    public CreateCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        if (isPlayer(context))
        {
            final Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(((Player)context.cause().audience()).uniqueId());
            if (optionalPlayerFaction.isPresent())
                throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_ARE_ALREADY_IN_A_FACTION, RED)));
        }

        final String factionName = context.requireOne(Parameter.string().key("name").build());
        final String factionTag = context.requireOne(Parameter.string().key("tag").build());

        if(!ALPHANUMERIC_PATTERN.matcher(factionName).matches() || !ALPHANUMERIC_PATTERN.matcher(factionTag).matches())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.FACTION_NAME_AND_TAG_MUST_BE_ALPHANUMERIC, RED)));

        if (getPlugin().getFactionLogic().getFactionsTags().stream().anyMatch(x -> x.equalsIgnoreCase(factionTag)))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.PROVIDED_FACTION_TAG_IS_ALREADY_TAKEN, RED)));

        if (factionName.equalsIgnoreCase("SafeZone") || factionName.equalsIgnoreCase("WarZone"))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_CANT_USE_THIS_FACTION_NAME, RED)));

        //Check tag length
        if (factionTag.length() > this.factionsConfig.getMaxTagLength())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.PROVIDED_FACTION_TAG_IS_TOO_LONG + " (" + Messages.MAX + " " + this.factionsConfig.getMaxTagLength() + " " + Messages.CHARS + ")", RED)));
        else if (factionTag.length() < this.factionsConfig.getMinTagLength())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.PROVIDED_FACTION_TAG_IS_TOO_SHORT + " (" + Messages.MIN + " " + this.factionsConfig.getMinTagLength() + " " + Messages.CHARS + ")", RED)));

        if (getPlugin().getFactionLogic().getFactionsNames().contains(factionName.toLowerCase()))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.FACTION_WITH_THE_SAME_NAME_ALREADY_EXISTS, RED)));

        //Check name length
        if (factionName.length() > this.factionsConfig.getMaxNameLength())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.PROVIDED_FACTION_NAME_IS_TOO_LONG + " (" + Messages.MAX + " " + this.factionsConfig.getMaxNameLength() + " " + Messages.CHARS + ")", RED)));
        else if (factionName.length() < this.factionsConfig.getMinNameLength())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.PROVIDED_FACTION_NAME_IS_TOO_SHORT + " (" + Messages.MIN + " " + this.factionsConfig.getMinNameLength() + " " + Messages.CHARS + ")", RED)));

        if(isPlayer(context))
        {
            if (this.factionsConfig.getFactionCreationByItems())
            {
                return createAsPlayerByItems(factionName, factionTag, (Player) context.cause().audience());
            }
            createAsPlayer(factionName, factionTag, (Player)context.cause().audience());
        }
        else
        {
            createAsConsole(factionName, factionTag, context.cause().audience());
        }
        return CommandResult.success();
    }

    private CommandResult createAsPlayerByItems(String factionName, String factionTag, Player player) throws CommandException
    {
        try
        {
            ItemUtil.pollItemsNeededForCreationFromPlayer(player);
        }
        catch (RequiredItemsNotFoundException e)
        {
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_DONT_HAVE_ENOUGH_RESOURCES_TO_CREATE_A_FACTION, RED)), e);
        }

        createAsPlayer(factionName, factionTag, player);
        return CommandResult.success();
    }

    private void createAsPlayer(final String factionName, final String factionTag, final Player player)
    {
        final Faction faction = FactionImpl.builder(factionName, text(factionTag, GREEN), player.uniqueId()).build();
        final boolean isCancelled = EventRunner.runFactionCreateEventPre(player, faction);
        if (isCancelled)
            return;

        //Update player cache...
        final FactionPlayer factionPlayer = super.getPlugin().getStorageManager().getPlayer(player.uniqueId());
        final FactionPlayer updatedPlayer = new FactionPlayerImpl(factionPlayer.getName(), factionPlayer.getUniqueId(), factionName, factionPlayer.getPower(), factionPlayer.getMaxPower(), factionPlayer.diedInWarZone());
        super.getPlugin().getStorageManager().savePlayer(updatedPlayer);

        super.getPlugin().getFactionLogic().addFaction(faction);
        player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.FACTION_HAS_BEEN_CREATED, GREEN, Collections.singletonMap(Placeholders.FACTION_NAME, text(faction.getName(), GOLD)))));
        EventRunner.runFactionCreateEventPost(player, faction);
    }

    /**
     * Audience can actually be one of the following: console, command block, RCON client or proxy.
     */
    private void createAsConsole(final String factionName, final String factionTag, final Audience console)
    {
        final Faction faction = FactionImpl.builder(factionName, text(factionTag, GREEN), new UUID(0, 0)).build();
        super.getPlugin().getFactionLogic().addFaction(faction);
        console.sendMessage(Identity.nil(), PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.FACTION_HAS_BEEN_CREATED, GREEN, Collections.singletonMap(Placeholders.FACTION_NAME, text(faction.getName(), GOLD)))));
    }
}
