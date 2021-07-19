package io.github.aquerr.eaglefactions.commands.claiming;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.messaging.Placeholders;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3i;

import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;

public class FillCommand extends AbstractCommand
{
    private final ProtectionConfig protectionConfig;

    public FillCommand(EagleFactions plugin)
    {
        super(plugin);
        this.protectionConfig = plugin.getConfiguration().getProtectionConfig();
    }

    @Override
    public CommandResult execute(CommandContext context) throws CommandException
    {
        final ServerPlayer player = requirePlayerSource(context);
        final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId());

        //Check if player is in the faction.
        if (!optionalPlayerFaction.isPresent())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND, NamedTextColor.RED)));

        final boolean isAdmin = super.getPlugin().getPlayerManager().hasAdminMode(player);
        final Faction playerFaction = optionalPlayerFaction.get();
        if (!isAdmin && !super.getPlugin().getPermsManager().canClaim(player.uniqueId(), playerFaction))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.PLAYERS_WITH_YOUR_RANK_CANT_CLAIM_LANDS, NamedTextColor.RED)));

        final ServerWorld world = player.world();

        if (!canClaimInWorld(world, isAdmin))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_CANNOT_CLAIM_TERRITORIES_IN_THIS_WORLD, NamedTextColor.RED)));

        if (isFactionUnderAttack(playerFaction))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOUR_FACTION_IS_UNDER_ATTACK + " ", NamedTextColor.RED).append(MessageLoader.parseMessage(Messages.YOU_NEED_TO_WAIT_NUMBER_SECONDS_TO_BE_ABLE_TO_CLAIM_AGAIN, NamedTextColor.RED, Collections.singletonMap(Placeholders.NUMBER, Component.text(EagleFactionsPlugin.ATTACKED_FACTIONS.get(playerFaction.getName()), NamedTextColor.GOLD))))));

        fill(player, playerFaction);
        return CommandResult.success();
    }

    private boolean canClaimInWorld(ServerWorld world, boolean isAdmin)
    {
        if (this.protectionConfig.getClaimableWorldNames().contains(((TextComponent)world.properties().displayName().get()).content()))
            return true;
        return this.protectionConfig.getNotClaimableWorldNames().contains(((TextComponent)world.properties().displayName().get()).content()) && isAdmin;
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
    private void fill(final ServerPlayer player, Faction faction) throws CommandException
    {
        final UUID worldUUID = player.world().uniqueId();
        final Queue<Vector3i> chunks = new LinkedList<>();
        chunks.add(player.serverLocation().chunkPosition());
        while (!chunks.isEmpty())
        {
            if (hasReachedClaimLimit(faction))
                throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOUR_FACTION_DOES_NOT_HAVE_POWER_TO_CLAIM_MORE_LANDS, NamedTextColor.RED)));

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
