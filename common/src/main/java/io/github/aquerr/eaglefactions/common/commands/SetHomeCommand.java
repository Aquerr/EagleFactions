package io.github.aquerr.eaglefactions.common.commands;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionHome;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

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
    public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
    {
        if(!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

        final Player player = (Player)source;
        final Optional<Faction> optionalPlayerFaction = getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());

        if (!optionalPlayerFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

        final Faction playerFaction = optionalPlayerFaction.get();
        final World world = player.getWorld();
        final FactionHome newHome = new FactionHome(world.getUniqueId(), new Vector3i(player.getLocation().getBlockPosition()));

        if(super.getPlugin().getPlayerManager().hasAdminMode(player))
        {
            super.getPlugin().getFactionLogic().setHome(playerFaction, newHome);
            source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, Messages.FACTION_HOME_HAS_BEEN_SET));
            return CommandResult.success();
        }

        if(playerFaction.getLeader().equals(player.getUniqueId()) || playerFaction.getOfficers().contains(player.getUniqueId()))
        {
            final Optional<Faction> chunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(world.getUniqueId(), player.getLocation().getChunkPosition());
            if (!chunkFaction.isPresent() && this.factionsConfig.canPlaceHomeOutsideFactionClaim())
            {
                super.getPlugin().getFactionLogic().setHome(playerFaction, newHome);
                source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, Messages.FACTION_HOME_HAS_BEEN_SET));
            }
            else if (!chunkFaction.isPresent() && !this.factionsConfig.canPlaceHomeOutsideFactionClaim())
            {
                source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.FACTION_HOME_MUST_BE_PLACED_INSIDE_FACTION_TERRITORY));
            }
            else if(chunkFaction.isPresent() && chunkFaction.get().getName().equals(playerFaction.getName()))
            {
                super.getPlugin().getFactionLogic().setHome(playerFaction, newHome);
                source.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.GREEN, Messages.FACTION_HOME_HAS_BEEN_SET));
            }
            else
            {
                source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.THIS_LAND_BELONGS_TO_SOMEONE_ELSE));
            }
        }
        else
        {
            source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS));
        }

        return CommandResult.success();
    }
}
