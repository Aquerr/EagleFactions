package io.github.aquerr.eaglefactions.common.commands;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.events.EventRunner;
import io.github.aquerr.eaglefactions.common.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import io.github.aquerr.eaglefactions.common.messaging.Placeholders;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.util.Collections;
import java.util.Optional;

//@RequiredRank(rank = FactionMemberType.OFFICER)
public class ClaimCommand extends AbstractCommand
{
    private final ProtectionConfig protectionConfig;
    private final FactionsConfig factionsConfig;

    public ClaimCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.protectionConfig = plugin.getConfiguration().getProtectionConfig();
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
    {
//        super.execute(source, context);
        if (!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

        final Player player = (Player) source;
        final World world = player.getWorld();
        final Vector3i chunk = player.getLocation().getChunkPosition();
        final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        if (!optionalPlayerFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

        final Faction playerFaction = optionalPlayerFaction.get();

        //Check if it is a claimable world
        if (!this.protectionConfig.getClaimableWorldNames().contains(player.getWorld().getName()))
        {
            //If it is not claimable world but player is in admin mode
            if(this.protectionConfig.getNotClaimableWorldNames().contains(player.getWorld().getName()) && EagleFactionsPlugin.ADMIN_MODE_PLAYERS.contains(player.getUniqueId()))
            {
                final Optional<Faction> optionalChunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(world.getUniqueId(), chunk);
                if (optionalChunkFaction.isPresent())
                    throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.THIS_PLACE_IS_ALREADY_CLAIMED));

                return runClaimEventAndClaim(player, playerFaction, world, chunk);
            }
            else
            {
                throw new CommandException(PluginInfo.ERROR_PREFIX.concat(Text.of(TextColors.RED, Messages.YOU_CANNOT_CLAIM_TERRITORIES_IN_THIS_WORLD)));
            }
        }

        final Optional<Faction> optionalChunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(world.getUniqueId(), chunk);
        if (optionalChunkFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.THIS_PLACE_IS_ALREADY_CLAIMED));

        //Check if admin mode
        if (EagleFactionsPlugin.ADMIN_MODE_PLAYERS.contains(player.getUniqueId()))
        {
            return runClaimEventAndClaim(player, playerFaction, world, chunk);
        }

        //If not admin then check faction flags for player
        if (!this.getPlugin().getFlagManager().canClaim(player.getUniqueId(), playerFaction))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.PLAYERS_WITH_YOUR_RANK_CANT_CLAIM_LANDS));

        //Check if faction has enough power to claim territory
        if (super.getPlugin().getPowerManager().getFactionMaxClaims(playerFaction) <= playerFaction.getClaims().size())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOUR_FACTION_DOES_NOT_HAVE_POWER_TO_CLAIM_MORE_LANDS));

        //If attacked then It should not be able to claim territories
        if (EagleFactionsPlugin.ATTACKED_FACTIONS.containsKey(playerFaction.getName()))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOUR_FACTION_IS_UNDER_ATTACK + " " + MessageLoader.parseMessage(Messages.YOU_NEED_TO_WAIT_NUMBER_MINUTES_TO_BE_ABLE_TO_CLAIM_AGAIN, Collections.singletonMap(Placeholders.NUMBER, Text.of(TextColors.GOLD, EagleFactionsPlugin.ATTACKED_FACTIONS.get(playerFaction.getName()))))));

        if (playerFaction.getName().equalsIgnoreCase("SafeZone") || playerFaction.getName().equalsIgnoreCase("WarZone"))
        {
            return runClaimEventAndClaim(player, playerFaction, world, chunk);
        }

        if (this.factionsConfig.requireConnectedClaims() && !super.getPlugin().getFactionLogic().isClaimConnected(playerFaction, new Claim(world.getUniqueId(), chunk)))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.CLAIMS_NEED_TO_BE_CONNECTED));

        boolean isCancelled = EventRunner.runFactionClaimEvent(player, playerFaction, world, chunk);
        if (isCancelled)
            return CommandResult.empty();

        super.getPlugin().getFactionLogic().startClaiming(player, playerFaction, world.getUniqueId(), chunk);
        return CommandResult.success();
    }

    private CommandResult runClaimEventAndClaim(final Player player, Faction playerFaction, final World world, final Vector3i chunk) throws CommandException
    {
        boolean isCancelled = EventRunner.runFactionClaimEvent(player, playerFaction, world, chunk);
        if (isCancelled)
            return CommandResult.empty();

        super.getPlugin().getFactionLogic().addClaim(playerFaction, new Claim(world.getUniqueId(), chunk));
        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, Messages.LAND + " ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " " + Messages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, Messages.CLAIMED, TextColors.WHITE, "!"));
        return CommandResult.success();
    }
}
