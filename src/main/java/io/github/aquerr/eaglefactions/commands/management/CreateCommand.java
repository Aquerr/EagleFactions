package io.github.aquerr.eaglefactions.commands.management;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.ChatConfig;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionMember;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.entities.ProtectionFlag;
import io.github.aquerr.eaglefactions.api.entities.ProtectionFlagType;
import io.github.aquerr.eaglefactions.api.entities.Rank;
import io.github.aquerr.eaglefactions.api.exception.RequiredItemsNotFoundException;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.validator.AlphaNumericFactionNameTagValidator;
import io.github.aquerr.eaglefactions.entities.FactionImpl;
import io.github.aquerr.eaglefactions.entities.FactionMemberImpl;
import io.github.aquerr.eaglefactions.entities.FactionPlayerImpl;
import io.github.aquerr.eaglefactions.entities.ProtectionFlagImpl;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.managers.RankManagerImpl;
import io.github.aquerr.eaglefactions.managers.creation.FactionCreationManager;
import io.github.aquerr.eaglefactions.util.ItemUtil;
import net.kyori.adventure.audience.Audience;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static net.kyori.adventure.text.Component.text;

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

        if (factionName.equalsIgnoreCase(EagleFactionsPlugin.SAFE_ZONE_NAME) || factionName.equalsIgnoreCase(EagleFactionsPlugin.WAR_ZONE_NAME))
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
            createFaction(context.cause().audience(), factionName, factionTag, ((ServerPlayer)context.cause().audience()).uniqueId());
        }
        else
        {
            createFaction(context.cause().audience(), factionName, factionTag, null);
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
                ItemUtil.pollItemsFromPlayer(player, ItemUtil.convertToItemStackList(this.factionsConfig.getRequiredItemsToCreateFaction()));
            }
            catch (RequiredItemsNotFoundException e)
            {
                throw messageService.resolveExceptionWithMessage("error.command.create.not-enough-resources", e.buildAllRequiredItemsMessage());
            }
        }

        createFaction(player, factionName, factionTag, player.uniqueId());
        return CommandResult.success();
    }

    private void createFaction(
            final Audience audience,
            final String factionName,
            final String factionTag,
            @Nullable final UUID leaderUUID)
    {
        Set<FactionMember> members = new HashSet<>();
        if (leaderUUID != null)
        {
            members.add(new FactionMemberImpl(leaderUUID, Set.of(RankManagerImpl.LEADER_RANK_NAME)));
        }

        final Faction faction = FactionImpl.builder(factionName, text(factionTag, this.chatConfig.getDefaultTagColor()))
                .leader(leaderUUID)
                .members(members)
                .createdDate(Instant.now())
                .ranks(prepareDefaultRanks())
                .protectionFlags(prepareDefaultProtectionFlags())
                .build();

        final boolean isCancelled = EventRunner.runFactionCreateEventPre(
                Optional.ofNullable(audience)
                .filter(Player.class::isInstance)
                .map(Player.class::cast)
                .orElse(null),
                faction
        );
        if (isCancelled)
            return;

        super.getPlugin().getFactionLogic().addFaction(faction);

        //Update player cache...
        if (leaderUUID != null)
        {
            final FactionPlayer factionPlayer = super.getPlugin().getStorageManager().getPlayer(leaderUUID);
            final FactionPlayer updatedPlayer = new FactionPlayerImpl(factionPlayer.getName(), factionPlayer.getUniqueId(), factionName, factionPlayer.getPower(), factionPlayer.getMaxPower(), factionPlayer.diedInWarZone());
            super.getPlugin().getStorageManager().savePlayer(updatedPlayer);
        }

        notifyServerPlayersAboutNewFaction(faction);
        if (audience != null)
        {
            audience.sendMessage(messageService.resolveMessageWithPrefix("command.create.success", faction.getName()));
        }
        EventRunner.runFactionCreateEventPost(Optional.ofNullable(audience)
                .filter(Player.class::isInstance)
                .map(Player.class::cast)
                .orElse(null), faction);
    }

    private List<Rank> prepareDefaultRanks()
    {
        List<Rank> defaultRanks = factionsConfig.getDefaultRanks();
        if (defaultRanks.stream().noneMatch(rank -> rank.getName().equalsIgnoreCase(RankManagerImpl.DEFAULT_RANK_NAME)))
        {
            defaultRanks = new ArrayList<>(defaultRanks);
            defaultRanks.add(RankManagerImpl.buildDefaultRank());
        }
        if (defaultRanks.stream().noneMatch(rank -> rank.getName().equalsIgnoreCase(RankManagerImpl.LEADER_RANK_NAME)))
        {
            defaultRanks = new ArrayList<>(defaultRanks);
            defaultRanks.add(RankManagerImpl.buildLeaderRank());
        }

        return defaultRanks;
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
            Sponge.server().sendMessage(messageService.resolveMessageWithPrefix("command.create.notify-server-about-new-faction", faction.getName()));
        }
    }
}
