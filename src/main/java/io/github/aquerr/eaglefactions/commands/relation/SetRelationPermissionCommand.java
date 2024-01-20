package io.github.aquerr.eaglefactions.commands.relation;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPermission;
import io.github.aquerr.eaglefactions.api.entities.Rank;
import io.github.aquerr.eaglefactions.api.entities.RelationType;
import io.github.aquerr.eaglefactions.api.managers.PermsManager;
import io.github.aquerr.eaglefactions.api.managers.PlayerManager;
import io.github.aquerr.eaglefactions.api.managers.RankManager;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.managers.RankManagerImpl;
import io.github.aquerr.eaglefactions.messaging.EFMessageService;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.command.parameter.Parameter;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

import java.util.List;

public class SetRelationPermissionCommand extends AbstractCommand
{
    private final PermsManager permsManager;
    private final PlayerManager playerManager;
    private final MessageService messageService;
    private final RankManager rankManager;

    public SetRelationPermissionCommand(EagleFactions plugin)
    {
        super(plugin);
        this.permsManager = plugin.getPermsManager();
        this.playerManager = plugin.getPlayerManager();
        this.messageService = plugin.getMessageService();
        this.rankManager = plugin.getRankManager();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        ServerPlayer serverPlayer = requirePlayerSource(context);
        Faction faction = requirePlayerFaction(serverPlayer);
        RelationType relationType = context.requireOne(Parameter.enumValue(RelationType.class).key("relation_type").build());
        FactionPermission permission = context.requireOne(Parameter.enumValue(FactionPermission.class).key("permission").build());

        boolean hasAdminMode = playerManager.hasAdminMode(serverPlayer.user());
        if (!hasAdminMode && !permsManager.hasPermission(serverPlayer.uniqueId(), faction, FactionPermission.MANAGE_RELATIONS))
        {
            throw messageService.resolveExceptionWithMessage(EFMessageService.ERROR_YOU_DONT_HAVE_ACCESS_TO_DO_THIS);
        }

        try
        {
            this.rankManager.setRelationPermission(faction, relationType, permission);
            serverPlayer.sendMessage(messageService.resolveMessageWithPrefix("command.relations.permission.set.rank-has-been-updated"));
        }
        catch (Exception exception)
        {
            throw messageService.resolveExceptionWithMessageAndThrowable("error.general.something-went-wrong", exception);
        }

        return CommandResult.success();
    }
}
