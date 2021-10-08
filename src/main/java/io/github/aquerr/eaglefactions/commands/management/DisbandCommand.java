package io.github.aquerr.eaglefactions.commands.management;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.messaging.Messages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class DisbandCommand extends AbstractCommand
{
    public DisbandCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if(!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

        final Player player = (Player) source;
        final Optional<Faction> optionalFaction = context.getOne("faction");
        final Faction faction = optionalFaction.orElse(super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId())
                        .orElseThrow(() -> new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND))));

        //Even admins should not be able to disband SafeZone nor WarZone
        if(faction.isSafeZone() || faction.isWarZone())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.THIS_FACTION_CANNOT_BE_DISBANDED));

        if (player.getUniqueId().equals(faction.getLeader()))
        {
            runDisbandEventAndDisband(player, faction, false);
        }
        else
        {
            final boolean hasAdminMode = super.getPlugin().getPlayerManager().hasAdminMode(player);
            if (!hasAdminMode)
                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_NEED_TO_TOGGLE_FACTION_ADMIN_MODE_TO_DO_THIS));
            runDisbandEventAndDisband(player, faction, true);
        }
        return CommandResult.success();
    }

    private void runDisbandEventAndDisband(final Player player, final Faction faction, final boolean forceRemovedByAdmin)
    {
        final boolean isCancelled = EventRunner.runFactionDisbandEventPre(player, faction, forceRemovedByAdmin, false);
        if(!isCancelled)
            playerDisband(player, faction, forceRemovedByAdmin);
    }

    private void playerDisband(final Player player, final Faction faction, final boolean forceRemovedByAdmin)
    {
        super.getPlugin().getFactionLogic().disbandFaction(faction.getName());
        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, Messages.FACTION_HAS_BEEN_DISBANDED));
        EagleFactionsPlugin.AUTO_CLAIM_LIST.remove(player.getUniqueId());
        EagleFactionsPlugin.CHAT_LIST.remove(player.getUniqueId());
        EventRunner.runFactionDisbandEventPost(player, faction, forceRemovedByAdmin, false);
    }
}
