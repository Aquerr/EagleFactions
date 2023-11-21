package io.github.aquerr.eaglefactions.scheduling;

import io.github.aquerr.eaglefactions.api.config.Configuration;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.messaging.chat.ChatMessageHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.entity.living.player.tab.TabListEntry;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.util.Identifiable;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class TabListUpdater implements EagleFactionsRunnableTask
{
    private final PlayerManager playerManager;
    private final Configuration configuration;

    public static final AtomicBoolean SHOULD_UPDATE = new AtomicBoolean(false);

    public static void requestUpdate()
    {
        SHOULD_UPDATE.compareAndSet(false, true);
    }

    public TabListUpdater(Configuration configuration, PlayerManager playerManager)
    {
        this.configuration = configuration;
        this.playerManager = playerManager;
    }

    /**
     * Main entry point of the updater job.
     *
     * Updates tab-list for every online player.
     */
    @Override
    public void run()
    {
        try
        {
            if (!this.configuration.getChatConfig().shouldDisplayFactionTagsInTabList())
                return;

            if (SHOULD_UPDATE.compareAndSet(true, false))
            {
                updateTabListForServerPlayers();
            }
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }

    private void updateTabListForServerPlayers()
    {
        Collection<ServerPlayer> players = Sponge.server().onlinePlayers();
        Set<FactionPlayer> onlineFactionPlayers = getOnlineFactionPlayers();

        for (ServerPlayer player : players)
        {
            updateTabListForPlayer(player, onlineFactionPlayers);
        }
    }

    private Set<FactionPlayer> getOnlineFactionPlayers()
    {
        Collection<ServerPlayer> players = Sponge.server().onlinePlayers();
        return players.stream()
                .map(Identifiable::uniqueId)
                .map(playerManager::getFactionPlayer)
                .map(factionPlayer -> factionPlayer.orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * Used for updating player tab-list from outside TabListUpdater.
     * @param player the player
     */
    public void updateTabListForPlayer(ServerPlayer player)
    {
        if (!this.configuration.getChatConfig().shouldDisplayFactionTagsInTabList())
            return;

        updateTabListForPlayer(player, getOnlineFactionPlayers());
    }

    private void updateTabListForPlayer(ServerPlayer player, Set<FactionPlayer> onlineFactionPlayers)
    {
        player.tabList().entries().forEach(tabListEntry -> modifyTabListEntry(tabListEntry, onlineFactionPlayers));
    }

    private void modifyTabListEntry(TabListEntry tabListEntry, Set<FactionPlayer> onlineFactionPlayers)
    {
        FactionPlayer factionPlayer = findFactionPlayerForGameProfile(tabListEntry.profile(), onlineFactionPlayers);
        if (factionPlayer == null)
            return;

        TextComponent prefix = Component.empty();

        Faction faction = factionPlayer.getFaction().orElse(null);
        if (faction == null)
        {
            TextComponent nonFactionPrefix = this.configuration.getChatConfig().getNonFactionPlayerPrefix();
            if (!nonFactionPrefix.equals(Component.empty()))
            {
                prefix = nonFactionPrefix;
            }
        }
        else
        {
            prefix = ChatMessageHelper.getFactionPrefix(faction);
        }

        if (prefix.equals(Component.empty()))
            return;

        tabListEntry.setDisplayName(prefix.append(Component.text(factionPlayer.getName())));
    }

    private FactionPlayer findFactionPlayerForGameProfile(GameProfile gameProfile, Set<FactionPlayer> onlineFactionPlayers)
    {
        for (final FactionPlayer factionPlayer : onlineFactionPlayers)
        {
            GameProfile playerProfile = factionPlayer.getUser().map(User::profile).orElse(null);
            if (playerProfile != null && playerProfile.uuid().equals(gameProfile.uniqueId()))
            {
                return factionPlayer;
            }
        }
        return null;
    }
}
