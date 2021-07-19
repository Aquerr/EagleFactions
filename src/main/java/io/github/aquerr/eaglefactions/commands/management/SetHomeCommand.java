package io.github.aquerr.eaglefactions.commands.management;

import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionHome;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerWorld;

import java.util.Optional;

public class SetHomeCommand extends AbstractCommand
{
    private final FactionsConfig factionsConfig;

    public SetHomeCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final ServerPlayer player = requirePlayerSource(context);
        final Faction playerFaction = requirePlayerFaction(player);
        final ServerWorld world = player.world();
        final FactionHome newHome = new FactionHome(world.uniqueId(), player.serverLocation().blockPosition());

        if(super.getPlugin().getPlayerManager().hasAdminMode(player))
        {
            super.getPlugin().getFactionLogic().setHome(playerFaction, newHome);
            player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.FACTION_HOME_HAS_BEEN_SET, NamedTextColor.GREEN)));
            return CommandResult.success();
        }

        if(playerFaction.getLeader().equals(player.uniqueId()) || playerFaction.getOfficers().contains(player.uniqueId()))
        {
            final Optional<Faction> chunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(world.uniqueId(), player.serverLocation().chunkPosition());
            if (!chunkFaction.isPresent() && this.factionsConfig.canPlaceHomeOutsideFactionClaim())
            {
                super.getPlugin().getFactionLogic().setHome(playerFaction, newHome);
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.FACTION_HOME_HAS_BEEN_SET, NamedTextColor.GREEN)));
            }
            else if (!chunkFaction.isPresent() && !this.factionsConfig.canPlaceHomeOutsideFactionClaim())
            {
                player.sendMessage(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.FACTION_HOME_MUST_BE_PLACED_INSIDE_FACTION_TERRITORY, NamedTextColor.RED)));
            }
            else if(chunkFaction.isPresent() && chunkFaction.get().getName().equals(playerFaction.getName()))
            {
                super.getPlugin().getFactionLogic().setHome(playerFaction, newHome);
                player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.FACTION_HOME_HAS_BEEN_SET, NamedTextColor.GREEN)));
            }
            else
            {
                player.sendMessage(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.THIS_LAND_BELONGS_TO_SOMEONE_ELSE, NamedTextColor.RED)));
            }
        }
        else
        {
            player.sendMessage(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS, NamedTextColor.RED)));
        }

        return CommandResult.success();
    }
}
