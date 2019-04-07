package io.github.aquerr.eaglefactions.commands;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.entities.Claim;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.events.FactionClaimEvent;
import io.github.aquerr.eaglefactions.message.PluginMessages;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RectangleClaimCommand extends AbstractCommand
{
    public RectangleClaimCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
    {
        final Optional<Integer> optionalNumber = context.getOne(Text.of("number"));

        if (!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

        if(!optionalNumber.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.WRONG_COMMAND_ARGUMENTS), true);

        final Player player = (Player) source;
        final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        if (!optionalPlayerFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));


        //Check if it is a claimable world
        if (!super.getPlugin().getConfiguration().getConfigFields().getClaimableWorldNames().contains(player.getWorld().getName()))
            throw new CommandException(PluginInfo.ERROR_PREFIX.concat(Text.of(TextColors.RED, "You can not claim territories in this world!")));

        final int number = optionalNumber.get();
        final Faction playerFaction = optionalPlayerFaction.get();
        final World world = player.getWorld();
        final Vector3i playerChunk = player.getLocation().getChunkPosition();


        //Radius claim
        final int startX = playerChunk.getX() - number;
        final int startZ = playerChunk.getZ() - number;
        final int endX = playerChunk.getX() + number;
        final int endZ = playerChunk.getZ() + number;

        final List<Vector3i> chunksToClaim = new ArrayList<>();
        final List<Claim> newFactionClaims = new ArrayList<>();

        for(int x = startX; x <= endX; x++)
        {
            for(int z = startZ; z <= endZ; z++)
            {
                final Vector3i chunk = new Vector3i(x, 0, z);
                chunksToClaim.add(chunk);
            }
        }

        for(final Vector3i chunk : chunksToClaim)
        {
            final Optional<Faction> optionalChunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(world.getUniqueId(), chunk);
            if (optionalChunkFaction.isPresent())
                continue;
                //throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.THIS_PLACE_IS_ALREADY_CLAIMED));

            //Check if admin mode
            if (EagleFactions.AdminList.contains(player.getUniqueId()))
            {
                boolean isCancelled = FactionClaimEvent.runEvent(player, playerFaction, world, chunk);
                if (isCancelled)
                    continue;
//                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "Something prevented claiming territory."));

//                super.getPlugin().getFactionLogic().addClaim(playerFaction, new Claim(world.getUniqueId(), chunk));
                newFactionClaims.add(new Claim(world.getUniqueId(), chunk));
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.LAND + " ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " " + PluginMessages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.CLAIMED, TextColors.WHITE, "!"));
                continue;
            }

            //If not admin then check faction flags for player
            if (!this.getPlugin().getFlagManager().canClaim(player.getUniqueId(), playerFaction))
                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.PLAYERS_WITH_YOUR_RANK_CANT_CLAIM_LANDS));

            //Check if faction has enough power to claim territory
            if (super.getPlugin().getPowerManager().getFactionMaxClaims(playerFaction) <= playerFaction.getClaims().size() + newFactionClaims.size())
            {
                source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOUR_FACTION_DOES_NOT_HAVE_POWER_TO_CLAIM_MORE_LANDS));
                break;
            }

            //If attacked then It should not be able to claim territories
            if (EagleFactions.AttackedFactions.containsKey(playerFaction.getName()))
                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOUR_FACTION_IS_UNDER_ATTACK + " " + PluginMessages.YOU_NEED_TO_WAIT + " ", TextColors.GOLD, PluginMessages.TWO_MINUTES, TextColors.RED, " " + PluginMessages.TO_BE_ABLE_TO_CLAIM_AGAIN));

            if (playerFaction.getName().equalsIgnoreCase("SafeZone") || playerFaction.getName().equalsIgnoreCase("WarZone"))
            {
                boolean isCancelled = FactionClaimEvent.runEvent(player, playerFaction, world, chunk);
                if (isCancelled)
                    continue;
//                    throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "Something prevented claiming territory."));

//                super.getPlugin().getFactionLogic().addClaim(playerFaction, new Claim(world.getUniqueId(), chunk));
                newFactionClaims.add(new Claim(world.getUniqueId(), chunk));
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.LAND + " ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " " + PluginMessages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.CLAIMED, TextColors.WHITE, "!"));
                continue;
            }

            if (super.getPlugin().getConfiguration().getConfigFields().requireConnectedClaims() && !super.getPlugin().getFactionLogic().isClaimConnected(playerFaction, new Claim(world.getUniqueId(), chunk)))
                continue;
//                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.CLAIMS_NEED_TO_BE_CONNECTED));

            boolean isCancelled = FactionClaimEvent.runEvent(player, playerFaction, world, chunk);
            if (isCancelled)
                continue;
//                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, "Something prevented claiming territory."));

//            super.getPlugin().getFactionLogic().startClaiming(player, playerFaction, world.getUniqueId(), chunk);
            if(super.getPlugin().getConfiguration().getConfigFields().shouldDelayClaim())
                throw new CommandException(Text.of("Can't rectangleclaim if delayed claiming is turned on."));

            newFactionClaims.add(new Claim(world.getUniqueId(), chunk));
            player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, PluginMessages.LAND + " ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " " + PluginMessages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.CLAIMED, TextColors.WHITE, "!"));
        }

        super.getPlugin().getFactionLogic().addClaims(playerFaction, newFactionClaims);
        return CommandResult.success();
    }
}
