package io.github.aquerr.eaglefactions.commands.management;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.ChatConfig;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.entities.ProtectionFlag;
import io.github.aquerr.eaglefactions.api.entities.ProtectionFlagType;
import io.github.aquerr.eaglefactions.api.exception.CommandException;
import io.github.aquerr.eaglefactions.api.exception.RequiredItemsNotFoundException;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.api.text.Text;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.validator.AlphaNumericFactionNameTagValidator;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.model.FactionImpl;
import io.github.aquerr.eaglefactions.model.FactionPlayerImpl;
import io.github.aquerr.eaglefactions.model.ProtectionFlagImpl;
import io.github.aquerr.eaglefactions.util.ItemUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

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
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        try
        {
            if (isServerPlayer(context.getSource()))
            {
                validateNotInFaction(context.getSource().getPlayer());
            }

            final String factionName = context.getArgument("name", String.class);
            final String factionTag = context.getArgument("tag", String.class);

            alphaNumericFactionNameTagValidator.validate(factionName, factionTag);

            if (getPlugin().getFactionManager().getFactionsTags().stream().anyMatch(x -> x.getText().equalsIgnoreCase(factionTag)))
                throw messageService.resolveExceptionWithMessage("error.command.create.tag-already-taken");

            if (factionName.equalsIgnoreCase(EagleFactionsPlugin.SAFE_ZONE_NAME) || factionName.equalsIgnoreCase(EagleFactionsPlugin.WAR_ZONE_NAME))
                throw messageService.resolveExceptionWithMessage("error.command.create.you-cant-use-this-faction-name");

            //Check tag length
            if (factionTag.length() > this.factionsConfig.getMaxTagLength())
                throw messageService.resolveExceptionWithMessage("error.command.create.tag-too-long", this.factionsConfig.getMaxTagLength());
            else if (factionTag.length() < this.factionsConfig.getMinTagLength())
                throw messageService.resolveExceptionWithMessage("error.command.create.tag-too-short", this.factionsConfig.getMinTagLength());

            if (getPlugin().getFactionManager().getFactionsNames().contains(factionName.toLowerCase()))
                throw messageService.resolveExceptionWithMessage("error.command.create.faction-with-same-name-already-exists");

            //Check name length
            if (factionName.length() > this.factionsConfig.getMaxNameLength())
                throw messageService.resolveExceptionWithMessage("error.command.create.faction-name-too-long", this.factionsConfig.getMaxNameLength());
            else if (factionName.length() < this.factionsConfig.getMinNameLength())
                throw messageService.resolveExceptionWithMessage("error.command.create.faction-name-too-short", this.factionsConfig.getMinNameLength());

            if(isServerPlayer(context.getSource()))
            {
                if (this.factionsConfig.getFactionCreationByItems())
                {
                    return createAsPlayerByItems(factionName, factionTag, context.getSource().getPlayer());
                }
                createAsPlayer(factionName, factionTag, context.getSource().getPlayer());
            }
            else
            {
                createAsConsole(factionName, factionTag, context.getSource());
            }
            return 0;
        }
        catch (CommandException exception)
        {
            context.getSource().sendFailure(Component.literal(exception.getMessage()));
        }
        return 1;
    }

    private void validateNotInFaction(ServerPlayer player) throws CommandException
    {
        final Optional<Faction> optionalPlayerFaction = getPlugin().getFactionManager().getFactionByPlayerUUID(player.getUUID());
        if (optionalPlayerFaction.isPresent())
            throw messageService.resolveExceptionWithMessage("error.command.join.you-are-already-in-a-faction");
    }

    private int createAsPlayerByItems(String factionName, String factionTag, ServerPlayer player) throws CommandException
    {
        if (!this.playerManager.hasAdminMode(player))
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
        return 0;
    }

    private void createAsPlayer(final String factionName, final String factionTag, final ServerPlayer player)
    {
        final Faction faction = FactionImpl.builder(factionName, new Text(factionTag, this.chatConfig.getDefaultTagColor().serialize()), player.getUUID())
                .setCreatedDate(Instant.now())
                .setProtectionFlags(prepareDefaultProtectionFlags())
                .build();
        final boolean isCancelled = EventRunner.runFactionCreateEventPre(player, faction);
        if (isCancelled)
            return;

        //Update player cache...
        final FactionPlayer factionPlayer = super.getPlugin().getStorageManager().getPlayer(player.getUUID());
        final FactionPlayer updatedPlayer = new FactionPlayerImpl(factionPlayer.getName(), factionPlayer.getUniqueId(), factionName, factionPlayer.getPower(), factionPlayer.getMaxPower(), factionPlayer.diedInWarZone());
        super.getPlugin().getStorageManager().savePlayer(updatedPlayer);

        super.getPlugin().getFactionManager().addFaction(faction);
        notifyServerPlayersAboutNewFaction(faction);
        player.sendSystemMessage(messageService.resolveMessageWithPrefix("command.create.success", faction.getName()));
        EventRunner.runFactionCreateEventPost(player, faction);
    }

    /**
     * Audience can actually be one of the following: player, console, command block, RCON client or proxy.
     */
    private void createAsConsole(final String factionName, final String factionTag, final CommandSourceStack audience)
    {
        final Faction faction = FactionImpl.builder(factionName, new Text(factionTag, this.chatConfig.getDefaultTagColor().serialize()), new UUID(0, 0))
                .setCreatedDate(Instant.now())
                .setProtectionFlags(prepareDefaultProtectionFlags())
                .build();
        super.getPlugin().getFactionManager().addFaction(faction);
        notifyServerPlayersAboutNewFaction(faction);
        audience.sendSystemMessage(messageService.resolveMessageWithPrefix("command.create.success", faction.getName()));
    }

    private Set<ProtectionFlag> prepareDefaultProtectionFlags()
    {
        return new HashSet<>(Arrays.asList(
                new ProtectionFlagImpl(ProtectionFlagType.TERRITORY_POWER_LOSS, true),
                new ProtectionFlagImpl(ProtectionFlagType.ALLOW_EXPLOSION, true),
                new ProtectionFlagImpl(ProtectionFlagType.MOB_GRIEF, true),
                new ProtectionFlagImpl(ProtectionFlagType.PVP, true),
                new ProtectionFlagImpl(ProtectionFlagType.FIRE_SPREAD, true),
                new ProtectionFlagImpl(ProtectionFlagType.SPAWN_ANIMALS, true),
                new ProtectionFlagImpl(ProtectionFlagType.SPAWN_MONSTERS, true)
        ));
    }

    private void notifyServerPlayersAboutNewFaction(Faction faction)
    {
        if (this.factionsConfig.shouldNotifyWHenFactionCreated())
        {
            ServerLifecycleHooks.getCurrentServer().sendSystemMessage(messageService.resolveMessageWithPrefix("command.create.notify-server-about-new-faction", faction.getName()));
        }
    }
}
