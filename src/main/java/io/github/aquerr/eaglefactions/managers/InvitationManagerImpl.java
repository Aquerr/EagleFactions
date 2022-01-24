package io.github.aquerr.eaglefactions.managers;

import com.google.common.collect.ImmutableMap;
import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.entities.*;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import io.github.aquerr.eaglefactions.api.managers.InvitationManager;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.storage.StorageManager;
import io.github.aquerr.eaglefactions.entities.*;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.messaging.Placeholders;
import io.github.aquerr.eaglefactions.scheduling.EagleFactionsScheduler;
import io.github.aquerr.eaglefactions.scheduling.RemoveInviteTask;
import io.github.aquerr.eaglefactions.scheduling.RemoveRelationRequestTask;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;
import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

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

        final ServerPlayer player = this.playerManager.getPlayer(factionInvite.getInvitedPlayerUniqueId())
            .orElseThrow(() -> new IllegalArgumentException("Player with the given UUID does not exist!"));
        final Faction faction = this.factionLogic.getFactionByName(factionInvite.getSenderFaction());
        checkNotNull(faction);

        joinAndNotify(player, faction);
        EagleFactionsPlugin.INVITE_LIST.remove(factionInvite);
        return true;
    }

    @Override
    public boolean sendInvitation(ServerPlayer senderPlayer, ServerPlayer invitedPlayer, final Faction senderFaction)
    {
        final boolean isCancelled = EventRunner.runFactionInviteEventPre(senderPlayer, invitedPlayer, senderFaction);
        if (isCancelled)
            return false;

        final FactionInvite invite = new FactionInviteImpl(senderFaction.getName(), invitedPlayer.uniqueId());
        EagleFactionsPlugin.INVITE_LIST.add(invite);

        invitedPlayer.sendMessage(getInviteReceivedMessage(senderFaction));
        senderPlayer.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text(Messages.YOU_INVITED + " ", GREEN)).append(text(invitedPlayer.name(), GOLD)).append(text(" " + Messages.TO_YOUR_FACTION, GREEN)));

        EagleFactionsScheduler.getInstance().scheduleWithDelay(new RemoveInviteTask(invite), 2, TimeUnit.MINUTES);
        EventRunner.runFactionInviteEventPost(senderPlayer, invitedPlayer, senderFaction);
        return true;
    }

    @Override
    public boolean joinAndNotify(ServerPlayer player, Faction faction)
    {
        checkNotNull(player);
        checkNotNull(faction);

        final boolean isCancelled = EventRunner.runFactionJoinEventPre(player, faction);
        if (isCancelled)
            return false;

        final Set<UUID> recruits = new HashSet<>(faction.getRecruits());
        recruits.add(player.uniqueId());
        Faction updatedFaction = faction.toBuilder().setRecruits(recruits).build();
        this.storageManager.saveFaction(updatedFaction);

        //Save player...
        final FactionPlayer factionPlayer = this.playerManager.getFactionPlayer(player.uniqueId()).get();
        final FactionPlayer updatedPlayer = new FactionPlayerImpl(factionPlayer.getName(), factionPlayer.getUniqueId(), faction.getName(), factionPlayer.getPower(), factionPlayer.getMaxPower(), factionPlayer.diedInWarZone());
        this.storageManager.savePlayer(updatedPlayer);

        player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.SUCCESSFULLY_JOINED_FACTION, GREEN, Collections.singletonMap(Placeholders.FACTION_NAME, text(faction.getName(), GOLD)))));
        EventRunner.runFactionJoinEventPost(player, factionLogic.getFactionByName(faction.getName()));
        return true;
    }

    @Override
    public boolean sendAllyRequest(ServerPlayer player, Faction sourceFaction, Faction targetFaction)
    {
        checkNotNull(player);
        checkNotNull(sourceFaction);
        checkNotNull(targetFaction);

        if(sourceFaction.getName().equals(targetFaction.getName()))
        {
            player.sendMessage(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_CANNOT_INVITE_YOURSELF_TO_THE_ALLIANCE, RED)));
            return false;
        }
        else if (targetFaction.isSafeZone() || targetFaction.isWarZone())
        {
            player.sendMessage(PluginInfo.ERROR_PREFIX.append(text("You can't do this!", RED)));
            return false;
        }

        boolean hasAdminMode = this.playerManager.hasAdminMode(player.user());
        boolean isLeader = isLeader(sourceFaction, player);
        boolean isOfficer = isOfficer(sourceFaction, player);

        if(sourceFaction.isAlly(targetFaction) && (hasAdminMode || isLeader || isOfficer))
        {
            //Remove ally
            this.factionLogic.removeAlly(sourceFaction.getName(), targetFaction.getName());
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.YOU_DISBANDED_YOUR_ALLIANCE_WITH_FACTION, GREEN, Collections.singletonMap(Placeholders.FACTION_NAME, text(targetFaction.getName(), GOLD)))));
            return true;
        }

        if (hasAdminMode)
        {
            forceAllianceBetween(sourceFaction, targetFaction);
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage("You force set allinace between you and " + Placeholders.FACTION_NAME.getPlaceholder(), GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, text(targetFaction.getName(), GOLD)))));
            return true;
        }
        else
        {
            if(!isLeader && !isOfficer)
            {
                player.sendMessage(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS, RED)));
                return false;
            }
            else if(sourceFaction.isEnemy(targetFaction))
            {
                player.sendMessage(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_ARE_IN_WAR_WITH_THIS_FACTION + " " + Messages.SEND_THIS_FACTION_A_PEACE_REQUEST_FIRST_BEFORE_INVITING_THEM_TO_ALLIES, RED)));
                return false;
            }
            else if(sourceFaction.isTruce(targetFaction))
            {
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text(Messages.DISBAND_TRUCE_FIRST_TO_INVITE_FACTION_TO_THE_ALLIANCE, RED)));
                return false;
            }
            else
            {
                // Preform send operation
                final AllyRequest invite = new AllyRequestImpl(sourceFaction.getName(), targetFaction.getName());
                EagleFactionsPlugin.RELATION_INVITES.add(invite);

                final Optional<ServerPlayer> optionalInvitedFactionLeader = this.playerManager.getPlayer(targetFaction.getLeader());

                optionalInvitedFactionLeader.ifPresent(x-> optionalInvitedFactionLeader.get().sendMessage(getAllyInviteGetMessage(sourceFaction)));
                targetFaction.getOfficers().forEach(x-> this.playerManager.getPlayer(x)
                        .ifPresent(y-> y.sendMessage(getAllyInviteGetMessage(sourceFaction))));

                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.YOU_HAVE_INVITED_FACTION_TO_THE_ALLIANCE, GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, text(targetFaction.getName(), GOLD)))));

                EagleFactionsScheduler.getInstance().scheduleWithDelay(new RemoveRelationRequestTask(invite), 2, TimeUnit.MINUTES);
                return true;
            }
        }
    }

    @Override
    public boolean sendTruceRequest(ServerPlayer player, Faction sourceFaction, Faction targetFaction)
    {
        checkNotNull(player);
        checkNotNull(sourceFaction);
        checkNotNull(targetFaction);

        if(sourceFaction.getName().equals(targetFaction.getName()))
        {
            player.sendMessage(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_CANNOT_INVITE_YOURSELF_TO_THE_TRUCE, RED)));
            return false;
        }
        else if (targetFaction.isSafeZone() || targetFaction.isWarZone())
        {
            player.sendMessage(PluginInfo.ERROR_PREFIX.append(text("You can't do this!", RED)));
            return false;
        }

        boolean hasAdminMode = this.playerManager.hasAdminMode(player.user());
        boolean isLeader = isLeader(sourceFaction, player);
        boolean isOfficer = isOfficer(sourceFaction, player);

        if(sourceFaction.isTruce(targetFaction) && (hasAdminMode || isLeader || isOfficer))
        {
            this.factionLogic.removeAlly(sourceFaction.getName(), targetFaction.getName());
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.YOU_DISBANDED_YOUR_TRUCE_WITH_FACTION, GREEN, Collections.singletonMap(Placeholders.FACTION_NAME, text(targetFaction.getName(), GOLD)))));
            return true;
        }

        if(hasAdminMode)
        {
            forceTruceBetween(sourceFaction, targetFaction);
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage("You force set truce between you and " + Placeholders.FACTION_NAME.getPlaceholder(), GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, text(targetFaction.getName(), GOLD)))));
            return true;
        }
        else
        {
            if(!isLeader && !isOfficer)
            {
                player.sendMessage(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS, RED)));
                return false;
            }
            else if(sourceFaction.isEnemy(targetFaction))
            {
                player.sendMessage(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_ARE_IN_WAR_WITH_THIS_FACTION + " " + Messages.SEND_THIS_FACTION_A_PEACE_REQUEST_FIRST_BEFORE_INVITING_THEM_TO_ALLIES, RED)));
                return false;
            }
            else if(sourceFaction.isAlly(targetFaction))
            {
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text(Messages.DISBAND_ALLIANCE_FIRST_TO_INVITE_FACTION_TO_THE_TRUCE, RED)));
                return false;
            }
            else
            {
                // Preform send operation
                final TruceRequest invite = new TruceRequestImpl(sourceFaction.getName(), targetFaction.getName());
                EagleFactionsPlugin.RELATION_INVITES.add(invite);

                final Optional<ServerPlayer> optionalInvitedFactionLeader = this.playerManager.getPlayer(targetFaction.getLeader());

                optionalInvitedFactionLeader.ifPresent(x-> optionalInvitedFactionLeader.get().sendMessage(getTruceInviteGetMessage(sourceFaction)));
                targetFaction.getOfficers().forEach(x-> this.playerManager.getPlayer(x)
                        .ifPresent(y-> y.sendMessage(getTruceInviteGetMessage(sourceFaction))));

                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.YOU_HAVE_INVITED_FACTION_TO_THE_TRUCE, GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, text(targetFaction.getName(), RED)))));

                EagleFactionsScheduler.getInstance().scheduleWithDelay(new RemoveRelationRequestTask(invite), 2, TimeUnit.MINUTES);
                return true;
            }
        }
    }

    @Override
    public boolean sendArmisticeOrWarRequest(ServerPlayer player, Faction sourceFaction, Faction targetFaction)
    {
        checkNotNull(player);
        checkNotNull(sourceFaction);
        checkNotNull(targetFaction);

        if(sourceFaction.getName().equals(targetFaction.getName()))
        {
            player.sendMessage(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_CANNOT_BE_IN_WAR_WITH_YOURSELF, RED)));
            return false;
        }
        else if (targetFaction.isSafeZone() || targetFaction.isWarZone())
        {
            player.sendMessage(PluginInfo.ERROR_PREFIX.append(text("You can't do this!", RED)));
            return false;
        }

        boolean hasAdminMode = this.playerManager.hasAdminMode(player.user());
        boolean isLeader = isLeader(sourceFaction, player);
        boolean isOfficer = isOfficer(sourceFaction, player);

        if(hasAdminMode)
        {
            if (sourceFaction.isEnemy(targetFaction))
            {
                forceArmisticeBetween(sourceFaction, targetFaction);
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage("You force set armistice between you and " + Placeholders.FACTION_NAME.getPlaceholder(), GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, text(targetFaction.getName(), GOLD)))));
            }
            else
            {
                forceWarBetween(sourceFaction, targetFaction);
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage("You force set war between you and " + Placeholders.FACTION_NAME.getPlaceholder(), GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, text(targetFaction.getName(), GOLD)))));
            }
            return true;
        }
        else
        {
            if (!isLeader && !isOfficer)
            {
                player.sendMessage(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS, RED)));
                return false;
            }
            else if (sourceFaction.isAlly(targetFaction) || sourceFaction.isTruce(targetFaction))
            {
                player.sendMessage(PluginInfo.ERROR_PREFIX.append(text(Messages.THIS_FACTION_IS_YOUR_ALLY + " " + Messages.DISBAND_ALLIANCE_FIRST_TO_DECLARE_A_WAR, RED)));
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
        final Optional<ServerPlayer> optionalSenderFactionLeader = this.playerManager.getPlayer(senderFaction.getLeader());
        optionalSenderFactionLeader.ifPresent(x -> optionalSenderFactionLeader.get().sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.FACTION_ACCEPTED_YOUR_INVITE_TO_THE_ALLIANCE, GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, text(allyRequest.getInvitedFaction(), GOLD))))));
        senderFaction.getOfficers().forEach(x -> this.playerManager.getPlayer(x)
                .ifPresent(y -> y.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.FACTION_ACCEPTED_YOUR_INVITE_TO_THE_ALLIANCE, GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, text(allyRequest.getInvitedFaction(), GOLD)))))));
        EagleFactionsPlugin.RELATION_INVITES.remove(allyRequest);
    }

    @Override
    public void acceptTruceRequest(TruceRequest truceRequest)
    {
        this.factionLogic.addTruce(truceRequest.getInvitedFaction(), truceRequest.getSenderFaction());
        final Faction senderFaction = this.factionLogic.getFactionByName(truceRequest.getSenderFaction());
        final Optional<ServerPlayer> optionalSenderFactionLeader = this.playerManager.getPlayer(senderFaction.getLeader());
        optionalSenderFactionLeader.ifPresent(x -> optionalSenderFactionLeader.get().sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.FACTION_ACCEPTED_YOUR_INVITE_TO_THE_TRUCE, GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, text(truceRequest.getInvitedFaction(), GOLD))))));
        senderFaction.getOfficers().forEach(x-> this.playerManager.getPlayer(x)
                .ifPresent(y -> y.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.FACTION_ACCEPTED_YOUR_INVITE_TO_THE_TRUCE, GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, text(truceRequest.getInvitedFaction(), GOLD)))))));
        EagleFactionsPlugin.RELATION_INVITES.remove(truceRequest);
    }

    @Override
    public void acceptArmisticeRequest(ArmisticeRequest armisticeRequest)
    {
        this.factionLogic.removeEnemy(armisticeRequest.getInvitedFaction(), armisticeRequest.getSenderFaction());
        final Faction senderFaction = this.factionLogic.getFactionByName(armisticeRequest.getSenderFaction());
        final Optional<ServerPlayer> senderFactionLeader = this.playerManager.getPlayer(senderFaction.getLeader());
        senderFactionLeader.ifPresent(x->x.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.FACTION_ACCEPTED_YOUR_ARMISTICE_REQUEST, GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, text(armisticeRequest.getInvitedFaction(), GOLD))))));
        senderFaction.getOfficers().forEach(x-> this.playerManager.getPlayer(x)
                .ifPresent(y->y.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.FACTION_ACCEPTED_YOUR_ARMISTICE_REQUEST, GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, text(armisticeRequest.getInvitedFaction(), GOLD)))))));
        EagleFactionsPlugin.RELATION_INVITES.remove(armisticeRequest);
    }

    private boolean isLeader(final Faction faction, final Player player)
    {
        return player.uniqueId().equals(faction.getLeader());
    }

    private boolean isOfficer(final Faction faction, final Player player)
    {
        return faction.getOfficers().contains(player.uniqueId());
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
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(text(Messages.YOU_HAVE_ALREADY_SENT_ARMISTICE_REQUEST, RED)));
            return false;
        }
        EagleFactionsPlugin.RELATION_INVITES.add(armisticeRequest);

        final Optional<ServerPlayer> targetFactionLeader = this.playerManager.getPlayer(targetFaction.getLeader());
        targetFactionLeader.ifPresent(x->x.sendMessage(getArmisticeRequestMessage(sourceFaction)));
        targetFaction.getOfficers().forEach(x-> playerManager.getPlayer(x).ifPresent(y->y.sendMessage(getArmisticeRequestMessage(sourceFaction))));
        player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.YOU_REQUESTED_ARMISTICE_WITH_FACTION, GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, text(targetFaction.getName(), GOLD)))));

        EagleFactionsScheduler.getInstance().scheduleWithDelayAsync(new RemoveRelationRequestTask(armisticeRequest), 2, TimeUnit.MINUTES);
        return true;
    }

    private boolean declareWar(Player player, Faction sourceFaction, Faction targetFaction)
    {
        factionLogic.addEnemy(sourceFaction.getName(), targetFaction.getName());
        player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.YOUR_FACTION_IS_NOW_ENEMIES_WITH_FACTION, RED, ImmutableMap.of(Placeholders.FACTION_NAME, text(targetFaction.getName(), GOLD)))));

        //Send message to enemy leader.
        playerManager.getPlayer(targetFaction.getLeader()).ifPresent(x->x.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.FACTION_HAS_HAS_DECLARED_YOU_A_WAR, RED, ImmutableMap.of(Placeholders.FACTION_NAME, text(sourceFaction.getName(), GOLD))))));

        //Send message to enemy officers.
        targetFaction.getOfficers().forEach(x-> playerManager.getPlayer(x).ifPresent(y-> y.sendMessage(PluginInfo.PLUGIN_PREFIX.append(MessageLoader.parseMessage(Messages.FACTION_HAS_HAS_DECLARED_YOU_A_WAR, RED, ImmutableMap.of(Placeholders.FACTION_NAME, text(sourceFaction.getName(), GOLD)))))));
        return true;
    }

    private TextComponent getInviteReceivedMessage(final Faction senderFaction)
    {
        final TextComponent clickHereText = text()
                .append(text("[", AQUA).append(text(Messages.CLICK_HERE, GOLD)).append(text("]", AQUA)))
                .clickEvent(ClickEvent.runCommand("/f join " + senderFaction.getName()))
                .hoverEvent(HoverEvent.showText(text("/f join " + senderFaction.getName(), GOLD)))
                .build();

        return PluginInfo.PLUGIN_PREFIX
                .append(MessageLoader.parseMessage(Messages.FACTION_HAS_SENT_YOU_AN_INVITE, GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, text(senderFaction.getName(), GOLD))))
                .append(text(Messages.YOU_HAVE_TWO_MINUTES_TO_ACCEPT_IT))
                .append(newline())
                .append(clickHereText)
                .append(text(" " + Messages.TO_ACCEPT_INVITATION_OR_TYPE + " ", GREEN))
                .append(text("/f join " + senderFaction.getName(), GOLD));
    }

    private TextComponent getAllyInviteGetMessage(final Faction senderFaction)
    {
        final TextComponent clickHereText = text()
                .append(text("[", AQUA).append(text(Messages.CLICK_HERE, GOLD)).append(text("]", AQUA)))
                .clickEvent(ClickEvent.runCommand("/f ally " + senderFaction.getName()))
                .hoverEvent(HoverEvent.showText(text("/f ally " + senderFaction.getName(), GOLD)))
                .build();

        return PluginInfo.PLUGIN_PREFIX
                .append(MessageLoader.parseMessage(Messages.FACTION_HAS_SENT_YOU_AN_INVITE_TO_THE_ALLIANCE, GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, text(senderFaction.getName(), GOLD))))
                .append(newline())
                .append(text(Messages.YOU_HAVE_TWO_MINUTES_TO_ACCEPT_IT))
                .append(newline())
                .append(clickHereText)
                .append(text(" " + Messages.TO_ACCEPT_INVITATION_OR_TYPE + " ", GREEN))
                .append(text("/f ally " + senderFaction.getName(), GOLD));
    }

    private TextComponent getTruceInviteGetMessage(final Faction senderFaction)
    {
        final TextComponent clickHereText = text()
                .append(text("[", AQUA).append(text(Messages.CLICK_HERE, GOLD)).append(text("]", AQUA)))
                .clickEvent(ClickEvent.runCommand("/f truce " + senderFaction.getName()))
                .hoverEvent(HoverEvent.showText(text("/f truce " + senderFaction.getName(), GOLD))).build();

        return PluginInfo.PLUGIN_PREFIX
                .append(MessageLoader.parseMessage(Messages.FACTION_HAS_SENT_YOU_AN_INVITE_TO_THE_TRUCE, GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, text(senderFaction.getName(), GOLD))))
                .append(newline())
                .append(text(Messages.YOU_HAVE_TWO_MINUTES_TO_ACCEPT_IT))
                .append(newline())
                .append(clickHereText)
                .append(text(" " + Messages.TO_ACCEPT_INVITATION_OR_TYPE + " ", GREEN))
                .append(text("/f truce " + senderFaction.getName(), GOLD));
    }

    private TextComponent getArmisticeRequestMessage(final Faction senderFaction)
    {
        final TextComponent clickHereText = text()
                .append(text("[", AQUA).append(text(Messages.CLICK_HERE, GOLD)).append(text("]", AQUA)))
                .clickEvent(ClickEvent.runCommand("/f enemy " + senderFaction.getName()))
                .hoverEvent(HoverEvent.showText(text("/f enemy " + senderFaction.getName(), GOLD))).build();

        return PluginInfo.PLUGIN_PREFIX
                .append(MessageLoader.parseMessage(Messages.FACTION_HAS_SENT_YOU_AN_ARMISTICE_REQUEST, GREEN, ImmutableMap.of(Placeholders.FACTION_NAME, text(senderFaction.getName(), GOLD))))
                .append(newline())
                .append(text(Messages.YOU_HAVE_TWO_MINUTES_TO_ACCEPT_IT))
                .append(newline())
                .append(clickHereText)
                .append(text(" " + Messages.TO_ACCEPT_IT_OR_TYPE + " ", GREEN))
                .append(text("/f enemy " + senderFaction.getName(), GOLD));
    }
}
