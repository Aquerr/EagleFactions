package io.github.aquerr.eaglefactions.commands.claiming;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.commands.args.EagleFactionsCommandParameters;
import io.github.aquerr.eaglefactions.events.EventRunner;
import io.github.aquerr.eaglefactions.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.messaging.Messages;
import io.github.aquerr.eaglefactions.messaging.Placeholders;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3i;

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
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        final Optional<Faction> optionalFaction = context.one(EagleFactionsCommandParameters.faction());
//        super.execute(source, context);
        final ServerPlayer player = requirePlayerSource(context);
        final ServerWorld world = player.world();
        final Vector3i chunk = player.serverLocation().chunkPosition();
        final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId());
        final Optional<Faction> optionalChunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(world.uniqueId(), chunk);
        final boolean hasAdminMode = super.getPlugin().getPlayerManager().hasAdminMode(player);

        if (optionalChunkFaction.isPresent())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.THIS_PLACE_IS_ALREADY_CLAIMED, NamedTextColor.RED)));

        if(optionalFaction.isPresent())
        {
            final Faction faction = optionalFaction.get();
            if(hasAdminMode)
            {
                return preformAdminClaim(player, faction, chunk);
            }
            return preformClaimByFaction(player, faction, chunk);
        }
        else
        {
            if (!optionalPlayerFaction.isPresent())
                throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND, NamedTextColor.RED)));
            final Faction faction = optionalPlayerFaction.get();
            if(hasAdminMode)
                return preformAdminClaim(player, faction, chunk);
            else return preformNormalClaim(player, faction, chunk);
        }
    }

    private CommandResult preformClaimByFaction(final ServerPlayer player, final Faction faction, final Vector3i chunk) throws CommandException
    {
        final ServerWorld world = player.world();
        final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.uniqueId());
        final boolean isClaimableWorld = this.protectionConfig.getClaimableWorldNames().contains(world.properties().displayName().get().toString());

        if(!optionalPlayerFaction.isPresent() || !optionalPlayerFaction.get().getName().equals(faction.getName()))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_DONT_HAVE_ACCESS_TO_DO_THIS, NamedTextColor.RED)));

        if(!isClaimableWorld)
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_CANNOT_CLAIM_TERRITORIES_IN_THIS_WORLD, NamedTextColor.RED)));

        return preformNormalClaim(player, faction, chunk);
    }

    private CommandResult preformAdminClaim(final ServerPlayer player, final Faction faction, final Vector3i chunk) throws CommandException
    {
        final ServerWorld world = player.world();
        final boolean safeZoneWorld = this.protectionConfig.getSafeZoneWorldNames().contains(world.properties().displayName().get().toString());
        final boolean warZoneWorld = this.protectionConfig.getWarZoneWorldNames().contains(world.properties().displayName().get().toString());

        //Even admin cannot claim territories in safezone nor warzone world.
        if (safeZoneWorld || warZoneWorld)
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_CANNOT_CLAIM_TERRITORIES_IN_THIS_WORLD, NamedTextColor.RED)));

        boolean isCancelled = EventRunner.runFactionClaimEventPre(player, faction, player.world(), chunk);
        if (isCancelled)
            return CommandResult.empty();

        super.getPlugin().getFactionLogic().addClaim(faction, new Claim(player.world().uniqueId(), chunk));
        player.sendMessage(PluginInfo.PLUGIN_PREFIX.append(Component.text(Messages.LAND + " ").append(Component.text(chunk.toString(), NamedTextColor.GOLD).append(Component.text(" " + Messages.HAS_BEEN_SUCCESSFULLY + " ").append(Component.text(Messages.CLAIMED, NamedTextColor.GOLD).append(Component.text("!")))))));

        EventRunner.runFactionClaimEventPost(player, faction, player.world(), chunk);
        return CommandResult.success();
    }

    private CommandResult preformNormalClaim(final ServerPlayer player, final Faction faction, final Vector3i chunk) throws CommandException
    {
        final ServerWorld world = player.world();
        final boolean isClaimableWorld = this.protectionConfig.getClaimableWorldNames().contains(world.properties().displayName().get().toString());

        if(!isClaimableWorld)
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOU_CANNOT_CLAIM_TERRITORIES_IN_THIS_WORLD, NamedTextColor.RED)));

        //If not admin then check faction perms for player
        if (!this.getPlugin().getPermsManager().canClaim(player.uniqueId(), faction))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.PLAYERS_WITH_YOUR_RANK_CANT_CLAIM_LANDS, NamedTextColor.RED)));

        //Check if faction has enough power to claim territory
        if (super.getPlugin().getPowerManager().getFactionMaxClaims(faction) <= faction.getClaims().size())
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOUR_FACTION_DOES_NOT_HAVE_POWER_TO_CLAIM_MORE_LANDS, NamedTextColor.RED)));

        //If attacked then It should not be able to claim territories
        if (EagleFactionsPlugin.ATTACKED_FACTIONS.containsKey(faction.getName()))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.YOUR_FACTION_IS_UNDER_ATTACK + " ", NamedTextColor.RED).append(MessageLoader.parseMessage(Messages.YOU_NEED_TO_WAIT_NUMBER_SECONDS_TO_BE_ABLE_TO_CLAIM_AGAIN, NamedTextColor.RED, Collections.singletonMap(Placeholders.NUMBER, Component.text(EagleFactionsPlugin.ATTACKED_FACTIONS.get(faction.getName()), NamedTextColor.GOLD))))));

        if (this.factionsConfig.requireConnectedClaims() && !super.getPlugin().getFactionLogic().isClaimConnected(faction, new Claim(world.uniqueId(), chunk)))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(Component.text(Messages.CLAIMS_NEED_TO_BE_CONNECTED, NamedTextColor.RED)));

        boolean isCancelled = EventRunner.runFactionClaimEventPre(player, faction, world, chunk);
        if (isCancelled)
            return CommandResult.empty();

        super.getPlugin().getFactionLogic().startClaiming(player, faction, world.uniqueId(), chunk);
        return CommandResult.success();
    }
}
