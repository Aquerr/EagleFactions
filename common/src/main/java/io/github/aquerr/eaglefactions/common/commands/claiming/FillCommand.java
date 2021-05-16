package io.github.aquerr.eaglefactions.common.commands.claiming;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.commands.AbstractCommand;
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

import java.util.*;

public class FillCommand extends AbstractCommand
{
    private final ProtectionConfig protectionConfig;

    public FillCommand(EagleFactions plugin)
    {
        super(plugin);
        this.protectionConfig = plugin.getConfiguration().getProtectionConfig();
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if (!isPlayer(source))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

        final Player player = (Player)source;
        final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());

        //Check if player is in the faction.
        if (!optionalPlayerFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));

        final boolean isAdmin = super.getPlugin().getPlayerManager().hasAdminMode(player);
        final Faction playerFaction = optionalPlayerFaction.get();
        if (!isAdmin && !super.getPlugin().getPermsManager().canClaim(player.getUniqueId(), playerFaction))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.PLAYERS_WITH_YOUR_RANK_CANT_CLAIM_LANDS));

        final World world = player.getWorld();

        if (!canClaimInWorld(world, isAdmin))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_CANNOT_CLAIM_TERRITORIES_IN_THIS_WORLD));

        if (isFactionUnderAttack(playerFaction))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOUR_FACTION_IS_UNDER_ATTACK + " ", MessageLoader.parseMessage(Messages.YOU_NEED_TO_WAIT_NUMBER_SECONDS_TO_BE_ABLE_TO_CLAIM_AGAIN, TextColors.RED, Collections.singletonMap(Placeholders.NUMBER, Text.of(TextColors.GOLD, EagleFactionsPlugin.ATTACKED_FACTIONS.get(playerFaction.getName()))))));

        fill(player, playerFaction);
        return CommandResult.success();
    }

    private boolean canClaimInWorld(World world, boolean isAdmin)
    {
        if (this.protectionConfig.getClaimableWorldNames().contains(world.getName()))
            return true;
        else return this.protectionConfig.getNotClaimableWorldNames().contains(world.getName()) && isAdmin;
    }

    private boolean hasReachedClaimLimit(Faction faction)
    {
        return super.getPlugin().getPowerManager().getFactionMaxClaims(faction) <= faction.getClaims().size();
    }

    private boolean isFactionUnderAttack(Faction faction)
    {
        return EagleFactionsPlugin.ATTACKED_FACTIONS.containsKey(faction.getName());
    }

    // Starts where player is standing
    private void fill(final Player player, Faction faction) throws CommandException
    {
        final UUID worldUUID = player.getWorld().getUniqueId();
        final Queue<Vector3i> chunks = new LinkedList<>();
        chunks.add(player.getLocation().getChunkPosition());
        while (!chunks.isEmpty())
        {
            if (hasReachedClaimLimit(faction))
                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOUR_FACTION_DOES_NOT_HAVE_POWER_TO_CLAIM_MORE_LANDS));

            final Vector3i chunkPosition = chunks.poll();
            if (!super.getPlugin().getFactionLogic().isClaimed(worldUUID, chunkPosition))
            {
                faction = super.getPlugin().getFactionLogic().getFactionByName(faction.getName());
                super.getPlugin().getFactionLogic().startClaiming(player, faction, worldUUID, chunkPosition);
                chunks.add(chunkPosition.add(1, 0, 0));
                chunks.add(chunkPosition.add(-1, 0, 0));
                chunks.add(chunkPosition.add(0, 0, 1));
                chunks.add(chunkPosition.add(0, 0, -1));
            }
        }
    }
}
