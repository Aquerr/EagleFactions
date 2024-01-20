package io.github.aquerr.eaglefactions.commands.relation;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPermission;
import io.github.aquerr.eaglefactions.api.entities.RelationType;
import io.github.aquerr.eaglefactions.api.managers.PermsManager;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.pagination.PaginationList;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class ListRelationPermissionsCommand extends AbstractCommand
{
    private final PermsManager permsManager;
    private final PlayerManager playerManager;
    private final MessageService messageService;

    public ListRelationPermissionsCommand(EagleFactions plugin)
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
        RelationType relationType = context.requireOne(Parameter.enumValue(RelationType.class).key("relation_type").build());

        if (!playerManager.hasAdminMode(serverPlayer.user())
                && !permsManager.hasPermission(serverPlayer.uniqueId(), faction, FactionPermission.MANAGE_RELATIONS))
        {
            throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_DONT_HAVE_ACCESS_TO_DO_THIS);
        }

        showRelationPermissions(serverPlayer, faction, relationType);

        return CommandResult.success();
    }

    private void showRelationPermissions(ServerPlayer serverPlayer, Faction faction, RelationType relationType)
    {
        List<Component> componentList = new ArrayList<>();
        for (final FactionPermission permission : new TreeSet<>(faction.getRelationPermissions(relationType)))
        {
            componentList.add(formatPermission(permission));
        }

        PaginationList.builder()
                .title(Component.text("Permissions", NamedTextColor.GREEN))
                .contents(componentList)
                .build()
                .sendTo(serverPlayer);
    }

    private TextComponent formatPermission(FactionPermission permission)
    {
        return Component.text()
                .append(Component.text("- " + permission.name(), NamedTextColor.AQUA))
                .build();
    }
}
