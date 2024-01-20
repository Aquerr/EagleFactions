package io.github.aquerr.eaglefactions.commands.rank;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPermission;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.entities.Rank;
import io.github.aquerr.eaglefactions.api.managers.PermsManager;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.pagination.PaginationList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.newline;
import static net.kyori.adventure.text.Component.text;

public class RankInfoCommand extends AbstractCommand
{
    private final PermsManager permsManager;
    private final PlayerManager playerManager;
    private final MessageService messageService;

    public RankInfoCommand(EagleFactions plugin)
    {
        super(plugin);
        this.permsManager = plugin.getPermsManager();
        this.playerManager = plugin.getPlayerManager();
        this.messageService = plugin.getMessageService();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        ServerPlayer serverPlayer = requirePlayerSource(context);
        Faction faction = requirePlayerFaction(serverPlayer);
        Rank rank = context.requireOne(EagleFactionsCommandParameters.factionRank());

        if (!playerManager.hasAdminMode(serverPlayer.user())
                && !permsManager.hasPermission(serverPlayer.uniqueId(), faction, FactionPermission.MANAGE_RANKS))
        {
            throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_DONT_HAVE_ACCESS_TO_DO_THIS);
        }

        showRankInfo(serverPlayer, faction, rank);

        return CommandResult.success();
    }

    private void showRankInfo(ServerPlayer serverPlayer, Faction faction, Rank rank)
    {
        List<Component> componentList = new ArrayList<>();

        componentList.add(text("Name: " + rank.getName()).append(newline()));
        componentList.add(text("Display Name: ").append(LegacyComponentSerializer.legacyAmpersand().deserialize(rank.getDisplayName()))
                .append(newline()));
        componentList.add(text("Members: ").append(buildMembers(faction, rank)).append(newline()));
        componentList.add(text("Permissions: ").append(newline()).append(buildPermissions(rank)));

        PaginationList.builder()
                .title(text("Rank Info", NamedTextColor.GREEN))
                .contents(componentList)
                .build()
                .sendTo(serverPlayer);
    }

    private Component buildPermissions(Rank rank)
    {
        List<Component> componentList = new ArrayList<>();
        for (final FactionPermission permission : new TreeSet<>(rank.getPermissions()))
        {
            componentList.add(formatPermission(permission));
        }
        return Component.join(JoinConfiguration.newlines(), componentList);
    }

    private Component buildMembers(Faction faction, Rank rank)
    {
        Set<FactionPlayer> rankMembers = faction.getMembers().stream()
                .filter(member -> member.getRankNames().contains(rank.getName()))
                .map(member -> playerManager.getFactionPlayer(member.getUniqueId()).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        TextComponent.Builder membersTextBuilder = text();
        membersTextBuilder.append(text(rankMembers.stream().map(FactionPlayer::getName).collect(Collectors.joining(","))));
        return membersTextBuilder.build();
    }

    private TextComponent formatPermission(FactionPermission permission)
    {
        return Component.text()
                .append(Component.text("- " + permission.name(), NamedTextColor.AQUA))
                .build();
    }
}
