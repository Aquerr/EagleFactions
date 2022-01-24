package io.github.aquerr.eaglefactions.commands.management;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.ChatConfig;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.exception.RequiredItemsNotFoundException;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.validator.AlphaNumericFactionNameTagValidator;
import io.github.aquerr.eaglefactions.entities.FactionImpl;
import io.github.aquerr.eaglefactions.entities.FactionPlayerImpl;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.messaging.Placeholders;
import io.github.aquerr.eaglefactions.util.ItemUtil;
import net.kyori.adventure.audience.Audience;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

/**
 * Created by Aquerr on 2017-07-12.
 */
public class CreateCommand extends AbstractCommand
{
    private final ChatConfig chatConfig;
    private final FactionsConfig factionsConfig;
    private final PlayerManager playerManager;
    private final AlphaNumericFactionNameTagValidator alphaNumericFactionNameTagValidator = AlphaNumericFactionNameTagValidator.getInstance();

    public CreateCommand(EagleFactions plugin)
    {
        super(plugin);
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
        this.chatConfig = plugin.getConfiguration().getChatConfig();
        this.playerManager = plugin.getPlayerManager();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        if (isServerPlayer(context.cause().audience()))
        {
            validateNotInFaction((Player) context.cause().audience());
        }

        final String factionName = context.requireOne(Parameter.string().key("name").build());
        final String factionTag = context.requireOne(Parameter.string().key("tag").build());

        alphaNumericFactionNameTagValidator.validate(factionName, factionTag);

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

        if(isServerPlayer(context.cause().audience()))
        {
            if (this.factionsConfig.getFactionCreationByItems())
            {
                return createAsPlayerByItems(factionName, factionTag, (ServerPlayer) context.cause().audience());
            }
            createAsPlayer(factionName, factionTag, (ServerPlayer)context.cause().audience());
        }
        else
        {
            createAsConsole(factionName, factionTag, context.cause().audience());
        }
        return CommandResult.success();
    }

    private void validateNotInFaction(Player player) throws CommandException
    {
        final Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId());
        if (optionalPlayerFaction.isPresent())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_ARE_ALREADY_IN_A_FACTION, RED)));
    }

    private CommandResult createAsPlayerByItems(String factionName, String factionTag, ServerPlayer player) throws CommandException
    {
        if (!this.playerManager.hasAdminMode(player.user()))
        {
            try
            {
                ItemUtil.pollItemsNeededForCreationFromPlayer(player);
            }
            catch (RequiredItemsNotFoundException e)
            {
                throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_DONT_HAVE_ENOUGH_RESOURCES_TO_CREATE_A_FACTION + " Required items: " + e.buildAllRequiredItemsMessage(), RED)), e);
            }
        }

        createAsPlayer(factionName, factionTag, player);
        return CommandResult.success();
    }

    private void createAsPlayer(final String factionName, final String factionTag, final Player player)
    {
        final Faction faction = FactionImpl.builder(factionName, text(factionTag, this.chatConfig.getDefaultTagColor()), player.uniqueId()).build();
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
     * CommandSource can actually be one of the following: console, command block, RCON client or proxy.
     */
    private void createAsConsole(final String factionName, final String factionTag, final Audience audience)
    {
        final Faction faction = FactionImpl.builder(factionName, text(factionTag, this.chatConfig.getDefaultTagColor()), new UUID(0, 0)).build();
        super.getPlugin().getFactionLogic().addFaction(faction);
        audience.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.FACTION_HAS_BEEN_CREATED, GREEN, Collections.singletonMap(Placeholders.FACTION_NAME, text(faction.getName(), GOLD)))));
    }
}
