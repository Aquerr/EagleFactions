package io.github.aquerr.eaglefactions.managers;

import com.google.common.collect.ImmutableMap;
import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.entities.AllyRequest;
import io.github.aquerr.eaglefactions.api.entities.ArmisticeRequest;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionInvite;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.entities.TruceRequest;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.managers.InvitationManager;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.storage.StorageManager;
import io.github.aquerr.eaglefactions.entities.AllyRequestImpl;
import io.github.aquerr.eaglefactions.entities.ArmisticeRequestImpl;
import io.github.aquerr.eaglefactions.entities.FactionInviteImpl;
import io.github.aquerr.eaglefactions.entities.FactionPlayerImpl;
import io.github.aquerr.eaglefactions.entities.TruceRequestImpl;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.messaging.Placeholders;
import io.github.aquerr.eaglefactions.scheduling.EagleFactionsScheduler;
import io.github.aquerr.eaglefactions.scheduling.RemoveInviteTask;
import io.github.aquerr.eaglefactions.scheduling.RemoveRelationRequestTask;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

public class InvitationManagerImpl implements InvitationManager
{
    private final StorageManager storageManager;
    private final FactionLogic factionLogic;
    private final PlayerManager playerManager;

    public InvitationManagerImpl(StorageManager storageManager, FactionLogic factionLogic, PlayerManager playerManager)
    {
        this.factionLogic = factionLogic;
        this.playerManager = playerManager;
        this.storageManager = storageManager;
    }

    @Override
    public boolean acceptInvitation(FactionInvite factionInvite)
    {
        checkNotNull(factionInvite);

        final Player player = this.playerManager.getPlayer(factionInvite.getInvitedPlayerUniqueId())
            .orElseThrow(() -> new IllegalArgumentException("Player with the given UUID does not exist!"));
        final Faction faction = this.factionLogic.getFactionByName(factionInvite.getSenderFaction());
        checkNotNull(faction);

        joinAndNotify(player, faction);
        EagleFactionsPlugin.INVITE_LIST.remove(factionInvite);
        return true;
    }

    @Override
    public boolean sendInvitation(Player senderPlayer, Player invitedPlayer, final Faction senderFaction)
    {
        final boolean isCancelled = EventRunner.runFactionInviteEventPre(senderPlayer, invitedPlayer, senderFaction);
        if (isCancelled)
            return false;

        final FactionInvite invite = new FactionInviteImpl(senderFaction.getName(), invitedPlayer.getUniqueId());
        EagleFactionsPlugin.INVITE_LIST.add(invite);

        invitedPlayer.sendMessage(getInviteReceivedMessage(senderFaction));
        senderPlayer.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX,TextColors.GREEN, Messages.YOU_INVITED + " ", TextColors.GOLD, invitedPlayer.getName(), TextColors.GREEN, " " + Messages.TO_YOUR_FACTION));

        EagleFactionsScheduler.getInstance().scheduleWithDelay(new RemoveInviteTask(invite), 2, TimeUnit.MINUTES);
        EventRunner.runFactionInviteEventPost(senderPlayer, invitedPlayer, senderFaction);
        return true;
    }

    @Override
    public boolean joinAndNotify(Player player, Faction faction)
    {
        checkNotNull(player);
        checkNotNull(faction);

        final boolean isCancelled = EventRunner.runFactionJoinEventPre(player, faction);
        if (isCancelled)
            return false;

        final Set<UUID> recruits = new HashSet<>(faction.getRecruits());
        recruits.add(player.getUniqueId());
        Faction updatedFaction = faction.toBuilder().setRecruits(recruits).build();
        this.storageManager.saveFaction(updatedFaction);

        //Save player...
        final FactionPlayer factionPlayer = this.playerManager.getFactionPlayer(player.getUniqueId()).get();
        final FactionPlayer updatedPlayer = new FactionPlayerImpl(factionPlayer.getName(), factionPlayer.getUniqueId(), faction.getName(), factionPlayer.getPower(), factionPlayer.getMaxPower(), factionPlayer.diedInWarZone());
        this.storageManager.savePlayer(updatedPlayer);

        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.SUCCESSFULLY_JOINED_FACTION, TextColors.GREEN, Collections.singletonMap(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, faction.getName())))));
        EventRunner.runFactionJoinEventPost(player, factionLogic.getFactionByName(faction.getName()));
        return true;
    }

    @Override
    public boolean sendAllyRequest(Player player, Faction sourceFaction, Faction targetFaction)
    {
        checkNotNull(player);
        checkNotNull(sourceFaction);
        checkNotNull(targetFaction);

        if(sourceFaction.getName().equals(targetFaction.getName()))
        {
            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CANNOT_INVITE_YOURSELF_TO_THE_ALLIANCE));
            return false;
        }
        else if (targetFaction.isSafeZone() || targetFaction.isWarZone())
        {
            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "You can't do this!"));
            return false;
        }

        boolean hasAdminMode = this.playerManager.hasAdminMode(player);
        boolean isLeader = isLeader(sourceFaction, player);
        boolean isOfficer = isOfficer(sourceFaction, player);

        if(sourceFaction.isAlly(targetFaction) && (hasAdminMode || isLeader || isOfficer))
        {
            //Remove ally
            this.factionLogic.removeAlly(sourceFaction.getName(), targetFaction.getName());
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.YOU_DISBANDED_YOUR_ALLIANCE_WITH_FACTION, TextColors.GREEN, Collections.singletonMap(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, targetFaction.getName())))));
            return true;
        }

        if (hasAdminMode)
        {
            forceAllianceBetween(sourceFaction, targetFaction);
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage("You force set allinace between you and " + Placeholders.FACTION_NAME.getPlaceholder(), TextColors.GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, targetFaction.getName())))));
            return true;
        }
        else
        {
            if(!isLeader && !isOfficer)
            {
                player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS));
                return false;
            }
            else if(sourceFaction.isEnemy(targetFaction))
            {
                player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_ARE_IN_WAR_WITH_THIS_FACTION + " " + Messages.SEND_THIS_FACTION_A_PEACE_REQUEST_FIRST_BEFORE_INVITING_THEM_TO_ALLIES));
                return false;
            }
            else if(sourceFaction.isTruce(targetFaction))
            {
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, Messages.DISBAND_TRUCE_FIRST_TO_INVITE_FACTION_TO_THE_ALLIANCE));
                return false;
            }
            else
            {
                // Preform send operation
                final AllyRequest invite = new AllyRequestImpl(sourceFaction.getName(), targetFaction.getName());
                EagleFactionsPlugin.RELATION_INVITES.add(invite);

                final Optional<Player> optionalInvitedFactionLeader = this.playerManager.getPlayer(targetFaction.getLeader());

                optionalInvitedFactionLeader.ifPresent(x-> optionalInvitedFactionLeader.get().sendMessage(getAllyInviteGetMessage(sourceFaction)));
                targetFaction.getOfficers().forEach(x-> this.playerManager.getPlayer(x)
                        .ifPresent(y-> getAllyInviteGetMessage(sourceFaction)));

                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.YOU_HAVE_INVITED_FACTION_TO_THE_ALLIANCE, TextColors.GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, targetFaction.getName())))));

                EagleFactionsScheduler.getInstance().scheduleWithDelay(new RemoveRelationRequestTask(invite), 2, TimeUnit.MINUTES);
                return true;
            }
        }
    }

    @Override
    public boolean sendTruceRequest(Player player, Faction sourceFaction, Faction targetFaction)
    {
        checkNotNull(player);
        checkNotNull(sourceFaction);
        checkNotNull(targetFaction);

        if(sourceFaction.getName().equals(targetFaction.getName()))
        {
            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CANNOT_INVITE_YOURSELF_TO_THE_TRUCE));
            return false;
        }
        else if (targetFaction.isSafeZone() || targetFaction.isWarZone())
        {
            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "You can't do this!"));
            return false;
        }

        boolean hasAdminMode = this.playerManager.hasAdminMode(player);
        boolean isLeader = isLeader(sourceFaction, player);
        boolean isOfficer = isOfficer(sourceFaction, player);

        if(sourceFaction.isTruce(targetFaction) && (hasAdminMode || isLeader || isOfficer))
        {
            this.factionLogic.removeAlly(sourceFaction.getName(), targetFaction.getName());
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.YOU_DISBANDED_YOUR_TRUCE_WITH_FACTION, TextColors.GREEN, Collections.singletonMap(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, targetFaction.getName())))));
            return true;
        }

        if(hasAdminMode)
        {
            forceTruceBetween(sourceFaction, targetFaction);
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage("You force set truce between you and " + Placeholders.FACTION_NAME.getPlaceholder(), TextColors.GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, targetFaction.getName())))));
            return true;
        }
        else
        {
            if(!isLeader && !isOfficer)
            {
                player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS));
                return false;
            }
            else if(sourceFaction.isEnemy(targetFaction))
            {
                player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_ARE_IN_WAR_WITH_THIS_FACTION + " " + Messages.SEND_THIS_FACTION_A_PEACE_REQUEST_FIRST_BEFORE_INVITING_THEM_TO_ALLIES));
                return false;
            }
            else if(sourceFaction.isAlly(targetFaction))
            {
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, Messages.DISBAND_ALLIANCE_FIRST_TO_INVITE_FACTION_TO_THE_TRUCE));
                return false;
            }
            else
            {
                // Preform send operation
                final TruceRequest invite = new TruceRequestImpl(sourceFaction.getName(), targetFaction.getName());
                EagleFactionsPlugin.RELATION_INVITES.add(invite);

                final Optional<Player> optionalInvitedFactionLeader = this.playerManager.getPlayer(targetFaction.getLeader());

                optionalInvitedFactionLeader.ifPresent(x-> optionalInvitedFactionLeader.get().sendMessage(getTruceInviteGetMessage(sourceFaction)));
                targetFaction.getOfficers().forEach(x-> this.playerManager.getPlayer(x)
                        .ifPresent(y-> getTruceInviteGetMessage(sourceFaction)));

                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.YOU_HAVE_INVITED_FACTION_TO_THE_TRUCE, TextColors.GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, targetFaction.getName())))));

                EagleFactionsScheduler.getInstance().scheduleWithDelay(new RemoveRelationRequestTask(invite), 2, TimeUnit.MINUTES);
                return true;
            }
        }
    }

    @Override
    public boolean sendArmisticeOrWarRequest(Player player, Faction sourceFaction, Faction targetFaction)
    {
        checkNotNull(player);
        checkNotNull(sourceFaction);
        checkNotNull(targetFaction);

        if(sourceFaction.getName().equals(targetFaction.getName()))
        {
            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CANNOT_BE_IN_WAR_WITH_YOURSELF));
            return false;
        }
        else if (targetFaction.isSafeZone() || targetFaction.isWarZone())
        {
            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "You can't do this!"));
            return false;
        }

        boolean hasAdminMode = this.playerManager.hasAdminMode(player);
        boolean isLeader = isLeader(sourceFaction, player);
        boolean isOfficer = isOfficer(sourceFaction, player);

        if(hasAdminMode)
        {
            if (sourceFaction.isEnemy(targetFaction))
            {
                forceArmisticeBetween(sourceFaction, targetFaction);
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage("You force set armistice between you and " + Placeholders.FACTION_NAME.getPlaceholder(), TextColors.GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, targetFaction.getName())))));
            }
            else
            {
                forceWarBetween(sourceFaction, targetFaction);
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage("You force set war between you and " + Placeholders.FACTION_NAME.getPlaceholder(), TextColors.GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, targetFaction.getName())))));
            }
            return true;
        }
        else
        {
            if (!isLeader && !isOfficer)
            {
                player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS));
                return false;
            }
            else if (sourceFaction.isAlly(targetFaction) || sourceFaction.isTruce(targetFaction))
            {
                player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.THIS_FACTION_IS_YOUR_ALLY + " " + Messages.DISBAND_ALLIANCE_FIRST_TO_DECLARE_A_WAR));
                return false;
            }
            else if (sourceFaction.isEnemy(targetFaction))
            {
                return sendArmisticeRequest(player, sourceFaction, targetFaction);
            }
            else
            {
                return declareWar(player, sourceFaction, targetFaction);
            }
        }
    }

    @Override
    public void acceptAllyRequest(AllyRequest allyRequest)
    {
        this.factionLogic.addAlly(allyRequest.getInvitedFaction(), allyRequest.getSenderFaction());
        final Faction senderFaction = this.factionLogic.getFactionByName(allyRequest.getSenderFaction());
        final Optional<Player> optionalSenderFactionLeader = this.playerManager.getPlayer(senderFaction.getLeader());
        optionalSenderFactionLeader.ifPresent(x -> optionalSenderFactionLeader.get().sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.FACTION_ACCEPTED_YOUR_INVITE_TO_THE_ALLIANCE, TextColors.GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, allyRequest.getInvitedFaction()))))));
        senderFaction.getOfficers().forEach(x-> this.playerManager.getPlayer(x)
                .ifPresent(y -> Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.FACTION_ACCEPTED_YOUR_INVITE_TO_THE_ALLIANCE, TextColors.GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, allyRequest.getInvitedFaction()))))));
        EagleFactionsPlugin.RELATION_INVITES.remove(allyRequest);
    }

    @Override
    public void acceptTruceRequest(TruceRequest truceRequest)
    {
        this.factionLogic.addTruce(truceRequest.getInvitedFaction(), truceRequest.getSenderFaction());
        final Faction senderFaction = this.factionLogic.getFactionByName(truceRequest.getSenderFaction());
        final Optional<Player> optionalSenderFactionLeader = this.playerManager.getPlayer(senderFaction.getLeader());
        optionalSenderFactionLeader.ifPresent(x -> optionalSenderFactionLeader.get().sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.FACTION_ACCEPTED_YOUR_INVITE_TO_THE_TRUCE, TextColors.GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, truceRequest.getInvitedFaction()))))));
        senderFaction.getOfficers().forEach(x-> this.playerManager.getPlayer(x)
                .ifPresent(y -> Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.FACTION_ACCEPTED_YOUR_INVITE_TO_THE_TRUCE, TextColors.GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, truceRequest.getInvitedFaction()))))));
        EagleFactionsPlugin.RELATION_INVITES.remove(truceRequest);
    }

    @Override
    public void acceptArmisticeRequest(ArmisticeRequest armisticeRequest)
    {
        this.factionLogic.removeEnemy(armisticeRequest.getInvitedFaction(), armisticeRequest.getSenderFaction());
        final Faction senderFaction = this.factionLogic.getFactionByName(armisticeRequest.getSenderFaction());
        final Optional<Player> senderFactionLeader = this.playerManager.getPlayer(senderFaction.getLeader());
        senderFactionLeader.ifPresent(x->x.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.FACTION_ACCEPTED_YOUR_ARMISTICE_REQUEST, TextColors.GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, armisticeRequest.getInvitedFaction()))))));
        senderFaction.getOfficers().forEach(x-> this.playerManager.getPlayer(x)
                .ifPresent(y->y.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.FACTION_ACCEPTED_YOUR_ARMISTICE_REQUEST, TextColors.GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, armisticeRequest.getInvitedFaction())))))));
        EagleFactionsPlugin.RELATION_INVITES.remove(armisticeRequest);
    }

    private boolean isLeader(final Faction faction, final Player player)
    {
        return player.getUniqueId().equals(faction.getLeader());
    }

    private boolean isOfficer(final Faction faction, final Player player)
    {
        return faction.getOfficers().contains(player.getUniqueId());
    }

    private void forceAllianceBetween(Faction senderFaction, Faction selectedFaction)
    {
        this.factionLogic.removeEnemy(senderFaction.getName(), selectedFaction.getName());
        this.factionLogic.removeTruce(senderFaction.getName(), selectedFaction.getName());
        this.factionLogic.addAlly(senderFaction.getName(), selectedFaction.getName());
    }

    private void forceTruceBetween(Faction senderFaction, Faction targetFaction)
    {
        this.factionLogic.removeEnemy(senderFaction.getName(), targetFaction.getName());
        this.factionLogic.removeAlly(senderFaction.getName(), targetFaction.getName());
        this.factionLogic.addTruce(senderFaction.getName(), targetFaction.getName());
    }

    private void forceWarBetween(Faction senderFaction, Faction targetFaction)
    {
        this.factionLogic.removeAlly(senderFaction.getName(), targetFaction.getName());
        this.factionLogic.removeTruce(senderFaction.getName(), targetFaction.getName());
        this.factionLogic.addEnemy(senderFaction.getName(), targetFaction.getName());
    }

    private void forceArmisticeBetween(Faction senderFaction, Faction targetFaction)
    {
        this.factionLogic.removeEnemy(senderFaction.getName(), targetFaction.getName());
    }

    private boolean sendArmisticeRequest(Player player, Faction sourceFaction, Faction targetFaction)
    {
        final ArmisticeRequest armisticeRequest = new ArmisticeRequestImpl(sourceFaction.getName(), targetFaction.getName());
        if(EagleFactionsPlugin.RELATION_INVITES.contains(armisticeRequest))
        {
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, Messages.YOU_HAVE_ALREADY_SENT_ARMISTICE_REQUEST));
            return false;
        }
        EagleFactionsPlugin.RELATION_INVITES.add(armisticeRequest);

        final Optional<Player> targetFactionLeader = this.playerManager.getPlayer(targetFaction.getLeader());
        targetFactionLeader.ifPresent(x->x.sendMessage(getArmisticeRequestMessage(sourceFaction)));
        targetFaction.getOfficers().forEach(x-> playerManager.getPlayer(x).ifPresent(y->y.sendMessage(getArmisticeRequestMessage(sourceFaction))));
        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.YOU_REQUESTED_ARMISTICE_WITH_FACTION, TextColors.GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, targetFaction.getName())))));

        EagleFactionsScheduler.getInstance().scheduleWithDelayAsync(new RemoveRelationRequestTask(armisticeRequest), 2, TimeUnit.MINUTES);
        return true;
    }

    private boolean declareWar(Player player, Faction sourceFaction, Faction targetFaction)
    {
        factionLogic.addEnemy(sourceFaction.getName(), targetFaction.getName());
        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.RED, MessageLoader.parseMessage(Messages.YOUR_FACTION_IS_NOW_ENEMIES_WITH_FACTION, TextColors.RESET, ImmutableMap.of(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, targetFaction.getName())))));

        //Send message to enemy leader.
        playerManager.getPlayer(targetFaction.getLeader()).ifPresent(x->x.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.FACTION_HAS_HAS_DECLARED_YOU_A_WAR, TextColors.RED, ImmutableMap.of(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, sourceFaction.getName()))))));

        //Send message to enemy officers.
        targetFaction.getOfficers().forEach(x-> playerManager.getPlayer(x).ifPresent(y-> y.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.FACTION_HAS_HAS_DECLARED_YOU_A_WAR, TextColors.RED, ImmutableMap.of(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, sourceFaction.getName())))))));
        return true;
    }

    private Text getInviteReceivedMessage(final Faction senderFaction)
    {
        final Text clickHereText = Text.builder()
                .append(Text.of(TextColors.AQUA, "[", TextColors.GOLD, Messages.CLICK_HERE, TextColors.AQUA, "]"))
                .onClick(TextActions.runCommand("/f join " + senderFaction.getName()))
                .onHover(TextActions.showText(Text.of(TextColors.GOLD, "/f join " + senderFaction.getName())))
                .build();

        return Text.of(PluginInfo.PLUGIN_PREFIX,
                MessageLoader.parseMessage(Messages.FACTION_HAS_SENT_YOU_AN_INVITE, TextColors.GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, senderFaction.getName()))),
                Messages.YOU_HAVE_TWO_MINUTES_TO_ACCEPT_IT,
                "\n",
                clickHereText,
                TextColors.GREEN,
                " ",
                Messages.TO_ACCEPT_INVITATION_OR_TYPE,
                " ",
                TextColors.GOLD, "/f join " + senderFaction.getName());
    }

    private Text getAllyInviteGetMessage(final Faction senderFaction)
    {
        final Text clickHereText = Text.builder()
                .append(Text.of(TextColors.AQUA, "[", TextColors.GOLD, Messages.CLICK_HERE, TextColors.AQUA, "]"))
                .onClick(TextActions.runCommand("/f ally " + senderFaction.getName()))
                .onHover(TextActions.showText(Text.of(TextColors.GOLD, "/f ally " + senderFaction.getName()))).build();

        return Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.FACTION_HAS_SENT_YOU_AN_INVITE_TO_THE_ALLIANCE, TextColors.GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, senderFaction.getName()))),
                "\n",
                Messages.YOU_HAVE_TWO_MINUTES_TO_ACCEPT_IT,
                "\n",
                clickHereText,
                TextColors.GREEN,
                " ",
                Messages.TO_ACCEPT_INVITATION_OR_TYPE,
                " ",
                TextColors.GOLD, "/f ally ", senderFaction.getName());
    }

    private Text getTruceInviteGetMessage(final Faction senderFaction)
    {
        final Text clickHereText = Text.builder()
                .append(Text.of(TextColors.AQUA, "[", TextColors.GOLD, Messages.CLICK_HERE, TextColors.AQUA, "]"))
                .onClick(TextActions.runCommand("/f truce " + senderFaction.getName()))
                .onHover(TextActions.showText(Text.of(TextColors.GOLD, "/f truce " + senderFaction.getName()))).build();

        return Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.FACTION_HAS_SENT_YOU_AN_INVITE_TO_THE_TRUCE, TextColors.GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, senderFaction.getName()))),
                "\n",
                Messages.YOU_HAVE_TWO_MINUTES_TO_ACCEPT_IT,
                "\n",
                clickHereText,
                TextColors.GREEN,
                " ",
                Messages.TO_ACCEPT_INVITATION_OR_TYPE,
                " ",
                TextColors.GOLD, "/f truce ", senderFaction.getName());
    }

    private Text getArmisticeRequestMessage(final Faction senderFaction)
    {
        final Text clickHereText = Text.builder()
                .append(Text.of(TextColors.AQUA, "[", TextColors.GOLD, Messages.CLICK_HERE, TextColors.AQUA, "]"))
                .onClick(TextActions.runCommand("/f enemy " + senderFaction.getName()))
                .onHover(TextActions.showText(Text.of(TextColors.GOLD, "/f enemy " + senderFaction.getName()))).build();

        return Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.FACTION_HAS_SENT_YOU_AN_ARMISTICE_REQUEST, TextColors.GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, Text.of(TextColors.GOLD, senderFaction.getName()))),
                "\n",
                Messages.YOU_HAVE_TWO_MINUTES_TO_ACCEPT_IT,
                "\n",
                clickHereText,
                TextColors.GREEN,
                " ",
                Messages.TO_ACCEPT_IT_OR_TYPE,
                " ",
                TextColors.GOLD, "/f enemy " + senderFaction.getName());
    }
}
