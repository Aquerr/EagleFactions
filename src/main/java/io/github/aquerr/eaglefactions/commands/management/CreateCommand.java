package io.github.aquerr.eaglefactions.commands.management;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.ChatConfig;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.exception.RequiredItemsNotFoundException;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.validator.AlphaNumericFactionNameTagValidator;
import io.github.aquerr.eaglefactions.entities.FactionImpl;
import io.github.aquerr.eaglefactions.entities.FactionPlayerImpl;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.util.ItemUtil;
import net.kyori.adventure.audience.Audience;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.Optional;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;

/**
 * Created by Aquerr on 2017-07-12.
 */
public class CreateCommand extends AbstractCommand
{
    private final ChatConfig chatConfig;
    private final FactionsConfig factionsConfig;
    private final PlayerManager playerManager;
    private final AlphaNumericFactionNameTagValidator alphaNumericFactionNameTagValidator = AlphaNumericFactionNameTagValidator.getInstance();
    private final MessageService messageService;

    public CreateCommand(EagleFactions plugin)
    {
        super(plugin);
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
        this.chatConfig = plugin.getConfiguration().getChatConfig();
        this.playerManager = plugin.getPlayerManager();
        this.messageService = plugin.getMessageService();
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
            throw messageService.resolveExceptionWithMessage("error.command.create.tag-already-taken");

        if (factionName.equalsIgnoreCase("SafeZone") || factionName.equalsIgnoreCase("WarZone"))
            throw messageService.resolveExceptionWithMessage("error.command.create.you-cant-use-this-faction-name");

        //Check tag length
        if (factionTag.length() > this.factionsConfig.getMaxTagLength())
            throw messageService.resolveExceptionWithMessage("error.command.create.tag-too-long", this.factionsConfig.getMaxTagLength());
        else if (factionTag.length() < this.factionsConfig.getMinTagLength())
            throw messageService.resolveExceptionWithMessage("error.command.create.tag-too-short", this.factionsConfig.getMinTagLength());

        if (getPlugin().getFactionLogic().getFactionsNames().contains(factionName.toLowerCase()))
            throw messageService.resolveExceptionWithMessage("error.command.create.faction-with-same-name-already-exists");

        //Check name length
        if (factionName.length() > this.factionsConfig.getMaxNameLength())
            throw messageService.resolveExceptionWithMessage("error.command.create.faction-name-too-long", this.factionsConfig.getMaxNameLength());
        else if (factionName.length() < this.factionsConfig.getMinNameLength())
            throw messageService.resolveExceptionWithMessage("error.command.create.faction-name-too-short", this.factionsConfig.getMinNameLength());

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
            throw messageService.resolveExceptionWithMessage("error.command.join.you-are-already-in-a-faction");
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
                throw messageService.resolveExceptionWithMessage("error.command.create.not-enough-resources", e.buildAllRequiredItemsMessage());
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
        player.sendMessage(messageService.resolveMessageWithPrefix("command.create.success", faction.getName()));
        EventRunner.runFactionCreateEventPost(player, faction);
    }

    /**
     * Audience can actually be one of the following: player, console, command block, RCON client or proxy.
     */
    private void createAsConsole(final String factionName, final String factionTag, final Audience audience)
    {
        final Faction faction = FactionImpl.builder(factionName, text(factionTag, this.chatConfig.getDefaultTagColor()), new UUID(0, 0)).build();
        super.getPlugin().getFactionLogic().addFaction(faction);
        audience.sendMessage(messageService.resolveMessageWithPrefix("command.create.success", faction.getName()));
    }
}
