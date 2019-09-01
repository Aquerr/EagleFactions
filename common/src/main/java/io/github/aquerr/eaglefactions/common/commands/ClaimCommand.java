package io.github.aquerr.eaglefactions.common.commands;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.events.EventRunner;
import io.github.aquerr.eaglefactions.common.events.FactionClaimEventImpl;
import io.github.aquerr.eaglefactions.common.message.PluginMessages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class ClaimCommand extends AbstractCommand
{
    public ClaimCommand(EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if (!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

        final Player player = (Player) source;
        final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        if (!optionalPlayerFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

        //Check if it is a claimable world
        if (!super.getPlugin().getConfiguration().getConfigFields().getClaimableWorldNames().contains(player.getWorld().getName()))
        {
            throw new CommandException(PluginInfo.ERROR_PREFIX.concat(Text.of(TextColors.RED, "You can not claim territories in this world!")));
        }

        final Faction playerFaction = optionalPlayerFaction.get();
        final World world = player.getWorld();
        final Vector3i chunk = player.getLocation().getChunkPosition();
        final Optional<Faction> optionalChunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(world.getUniqueId(), chunk);
        if (optionalChunkFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.THIS_PLACE_IS_ALREADY_CLAIMED));

        //Check if admin mode
        if (EagleFactionsPlugin.ADMIN_MODE_PLAYERS.contains(player.getUniqueId()))
        {
            boolean isCancelled = EventRunner.runFactionClaimEvent(player, playerFaction, world, chunk);
            if (isCancelled)
                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "Something prevented claiming territory."));

            super.getPlugin().getFactionLogic().addClaim(playerFaction, new Claim(world.getUniqueId(), chunk));
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.LAND + " ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " " + PluginMessages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.CLAIMED, TextColors.WHITE, "!"));
            return CommandResult.success();
        }

        //If not admin then check faction flags for player
        if (!this.getPlugin().getFlagManager().canClaim(player.getUniqueId(), playerFaction))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.PLAYERS_WITH_YOUR_RANK_CANT_CLAIM_LANDS));

        //Check if faction has enough power to claim territory
        if (super.getPlugin().getPowerManager().getFactionMaxClaims(playerFaction) <= playerFaction.getClaims().size())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOUR_FACTION_DOES_NOT_HAVE_POWER_TO_CLAIM_MORE_LANDS));

        //If attacked then It should not be able to claim territories
        if (EagleFactionsPlugin.ATTACKED_FACTIONS.containsKey(playerFaction.getName()))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOUR_FACTION_IS_UNDER_ATTACK + " " + PluginMessages.YOU_NEED_TO_WAIT + " ", TextColors.GOLD, PluginMessages.TWO_MINUTES, TextColors.RED, " " + PluginMessages.TO_BE_ABLE_TO_CLAIM_AGAIN));

        if (playerFaction.getName().equalsIgnoreCase("SafeZone") || playerFaction.getName().equalsIgnoreCase("WarZone"))
        {
            boolean isCancelled = EventRunner.runFactionClaimEvent(player, playerFaction, world, chunk);
            if (isCancelled)
                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "Something prevented claiming territory."));

            super.getPlugin().getFactionLogic().addClaim(playerFaction, new Claim(world.getUniqueId(), chunk));
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.LAND + " ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " " + PluginMessages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.CLAIMED, TextColors.WHITE, "!"));
            return CommandResult.success();
        }

        if (super.getPlugin().getConfiguration().getConfigFields().requireConnectedClaims() && !super.getPlugin().getFactionLogic().isClaimConnected(playerFaction, new Claim(world.getUniqueId(), chunk)))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.CLAIMS_NEED_TO_BE_CONNECTED));

        boolean isCancelled = EventRunner.runFactionClaimEvent(player, playerFaction, world, chunk);
        if (isCancelled)
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "Something prevented claiming territory."));

        super.getPlugin().getFactionLogic().startClaiming(player, playerFaction, world.getUniqueId(), chunk);
//        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.LAND + " ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " " + PluginMessages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.CLAIMED, TextColors.WHITE, "!"));
        return CommandResult.success();
    }
}
