package io.github.aquerr.eaglefactions.common.commands.claiming;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.commands.AbstractCommand;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class SquareClaimCommand extends AbstractCommand
{
    private final FactionsConfig factionsConfig;
    private final ProtectionConfig protectionConfig;

    public SquareClaimCommand(final EagleFactions plugin)
    {
        super(plugin);
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
        this.protectionConfig = plugin.getConfiguration().getProtectionConfig();
    }

    @Override
    public CommandResult execute(final CommandSource source, final CommandContext context) throws CommandException
    {
        final int number = context.requireOne(Text.of("radius"));

        if (!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

        final Player player = (Player) source;
        final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        if (!optionalPlayerFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

        final Faction playerFaction = optionalPlayerFaction.get();
        final World world = player.getWorld();
        final Vector3i playerChunk = player.getLocation().getChunkPosition();
        final boolean isAdmin = super.getPlugin().getPlayerManager().hasAdminMode(player);

        //Check if it is a claimable world
        if (!this.protectionConfig.getClaimableWorldNames().contains(world.getName()))
        {
            if(this.protectionConfig.getNotClaimableWorldNames().contains(world.getName()) && !isAdmin)
            {
                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CANNOT_CLAIM_TERRITORIES_IN_THIS_WORLD));
            }
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CANNOT_CLAIM_TERRITORIES_IN_THIS_WORLD));
        }

        CompletableFuture.runAsync(() -> {
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

                //Check if admin mode
                if (super.getPlugin().getPlayerManager().hasAdminMode(player))
                {
                    boolean isCancelled = EventRunner.runFactionClaimEventPre(player, playerFaction, world, chunk);
                    if (isCancelled)
                        continue;

                    newFactionClaims.add(new Claim(world.getUniqueId(), chunk));
                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, Messages.LAND + " ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " " + Messages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, Messages.CLAIMED, TextColors.WHITE, "!"));

                    EventRunner.runFactionClaimEventPost(player, playerFaction, world, chunk);
                    continue;
                }

                //If not admin then check faction perms for player
                if (!this.getPlugin().getPermsManager().canClaim(player.getUniqueId(), playerFaction))
                {
                    player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.PLAYERS_WITH_YOUR_RANK_CANT_CLAIM_LANDS));
                    return;
                }

                //Check if faction has enough power to claim territory
                if (super.getPlugin().getPowerManager().getFactionMaxClaims(playerFaction) <= playerFaction.getClaims().size() + newFactionClaims.size())
                {
                    source.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOUR_FACTION_DOES_NOT_HAVE_POWER_TO_CLAIM_MORE_LANDS));
                    break;
                }

                //If attacked then It should not be able to claim territories
                if (EagleFactionsPlugin.ATTACKED_FACTIONS.containsKey(playerFaction.getName()))
                {
                    player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOUR_FACTION_IS_UNDER_ATTACK + " ", MessageLoader.parseMessage(Messages.YOU_NEED_TO_WAIT_NUMBER_SECONDS_TO_BE_ABLE_TO_CLAIM_AGAIN, TextColors.RED, Collections.singletonMap(Placeholders.NUMBER, Text.of(TextColors.GOLD, EagleFactionsPlugin.ATTACKED_FACTIONS.get(playerFaction.getName()))))));
                    break;
                }

                if (playerFaction.isSafeZone() || playerFaction.isWarZone())
                {
                    boolean isCancelled = EventRunner.runFactionClaimEventPre(player, playerFaction, world, chunk);
                    if (isCancelled)
                        continue;

                    newFactionClaims.add(new Claim(world.getUniqueId(), chunk));
                    player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, Messages.LAND + " ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " " + Messages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, Messages.CLAIMED, TextColors.WHITE, "!"));
                    EventRunner.runFactionClaimEventPost(player, playerFaction, world, chunk);
                    continue;
                }

                if (this.factionsConfig.requireConnectedClaims() && !super.getPlugin().getFactionLogic().isClaimConnected(playerFaction, new Claim(world.getUniqueId(), chunk)))
                    continue;

                boolean isCancelled = EventRunner.runFactionClaimEventPre(player, playerFaction, world, chunk);
                if (isCancelled)
                    continue;

                if(this.factionsConfig.shouldDelayClaim())
                {
                    player.sendMessage(Text.of("Can't rectangleclaim if delayed claiming is turned on."));
                    break;
                }

                newFactionClaims.add(new Claim(world.getUniqueId(), chunk));
                player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, Messages.LAND + " ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " " + Messages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, Messages.CLAIMED, TextColors.WHITE, "!"));
                EventRunner.runFactionClaimEventPost(player, playerFaction, world, chunk);
            }

            super.getPlugin().getFactionLogic().addClaims(playerFaction, newFactionClaims);
        });
        return CommandResult.success();
    }
}
