package io.github.aquerr.eaglefactions.common.commands.management;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.common.entities.FactionImpl;
import io.github.aquerr.eaglefactions.common.entities.FactionPlayerImpl;
import io.github.aquerr.eaglefactions.common.events.EventRunner;
import io.github.aquerr.eaglefactions.common.exception.RequiredItemsNotFoundException;
import io.github.aquerr.eaglefactions.common.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import io.github.aquerr.eaglefactions.common.messaging.Placeholders;
import io.github.aquerr.eaglefactions.common.util.ItemUtil;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

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
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if (source instanceof Player)
        {
            final Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(((Player)source).getUniqueId());
            if (optionalPlayerFaction.isPresent())
                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_ARE_ALREADY_IN_A_FACTION));
        }

        final String factionName = context.requireOne("name");
        final String factionTag = context.requireOne("tag");

        if(!ALPHANUMERIC_PATTERN.matcher(factionName).matches() || !ALPHANUMERIC_PATTERN.matcher(factionTag).matches())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.FACTION_NAME_AND_TAG_MUST_BE_ALPHANUMERIC));

        if (getPlugin().getFactionLogic().getFactionsTags().stream().anyMatch(x -> x.equalsIgnoreCase(factionTag)))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.PROVIDED_FACTION_TAG_IS_ALREADY_TAKEN));

        if (factionName.equalsIgnoreCase("SafeZone") || factionName.equalsIgnoreCase("WarZone"))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CANT_USE_THIS_FACTION_NAME));

        //Check tag length
        if (factionTag.length() > this.factionsConfig.getMaxTagLength())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.PROVIDED_FACTION_TAG_IS_TOO_LONG + " (" + Messages.MAX + " " + this.factionsConfig.getMaxTagLength() + " " + Messages.CHARS + ")"));
        else if (factionTag.length() < this.factionsConfig.getMinTagLength())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.PROVIDED_FACTION_TAG_IS_TOO_SHORT + " (" + Messages.MIN + " " + this.factionsConfig.getMinTagLength() + " " + Messages.CHARS + ")"));

        if (getPlugin().getFactionLogic().getFactionsNames().contains(factionName.toLowerCase()))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.FACTION_WITH_THE_SAME_NAME_ALREADY_EXISTS));

        //Check name length
        if (factionName.length() > this.factionsConfig.getMaxNameLength())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.PROVIDED_FACTION_NAME_IS_TOO_LONG + " (" + Messages.MAX + " " + this.factionsConfig.getMaxNameLength() + " " + Messages.CHARS + ")"));
        else if (factionName.length() < this.factionsConfig.getMinNameLength())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.PROVIDED_FACTION_NAME_IS_TOO_SHORT + " (" + Messages.MIN + " " + this.factionsConfig.getMinNameLength() + " " + Messages.CHARS + ")"));

        if(source instanceof Player)
        {
            if (this.factionsConfig.getFactionCreationByItems())
            {
                return createAsPlayerByItems(factionName, factionTag, (Player) source);
            }
            createAsPlayer(factionName, factionTag, (Player)source);
        }
        else
        {
            createAsConsole(factionName, factionTag, source);
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
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_DONT_HAVE_ENOUGH_RESOURCES_TO_CREATE_A_FACTION), e);
        }

        createAsPlayer(factionName, factionTag, player);
        return CommandResult.success();
    }

    private void createAsPlayer(final String factionName, final String factionTag, final Player player)
    {
        final Faction faction = FactionImpl.builder(factionName, Text.of(TextColors.GREEN, factionTag), player.getUniqueId()).build();
        final boolean isCancelled = EventRunner.runFactionCreateEventPre(player, faction);
        if (isCancelled)
            return;

        //Update player cache...
        final FactionPlayer factionPlayer = super.getPlugin().getStorageManager().getPlayer(player.getUniqueId());
        final FactionPlayer updatedPlayer = new FactionPlayerImpl(factionPlayer.getName(), factionPlayer.getUniqueId(), factionName, factionPlayer.getPower(), factionPlayer.getMaxPower(), factionPlayer.diedInWarZone());
        super.getPlugin().getStorageManager().savePlayer(updatedPlayer);

        super.getPlugin().getFactionLogic().addFaction(faction);
        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.FACTION_HAS_BEEN_CREATED, TextColors.GREEN, Collections.singletonMap(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, faction.getName())))));
        EventRunner.runFactionCreateEventPost(player, faction);
    }

    /**
     * CommandSource can actually be one of the following: console, command block, RCON client or proxy.
     */
    private void createAsConsole(final String factionName, final String factionTag, final CommandSource commandSource)
    {
        final Faction faction = FactionImpl.builder(factionName, Text.of(TextColors.GREEN, factionTag), new UUID(0, 0)).build();
        super.getPlugin().getFactionLogic().addFaction(faction);
        commandSource.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.FACTION_HAS_BEEN_CREATED, TextColors.GREEN, Collections.singletonMap(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, faction.getName())))));
    }
}
