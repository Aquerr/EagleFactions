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
        final Optional<Faction> optionalFaction = context.getOne(Text.of("faction"));
//        super.execute(source, context);
        if (!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));

        final Player player = (Player) source;
        final World world = player.getWorld();
        final Vector3i chunk = player.getLocation().getChunkPosition();
        final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        final Optional<Faction> optionalChunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(world.getUniqueId(), chunk);
        final boolean hasAdminMode = super.getPlugin().getPlayerManager().hasAdminMode(player);

        if (optionalChunkFaction.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.THIS_PLACE_IS_ALREADY_CLAIMED));

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
                throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));
            final Faction faction = optionalPlayerFaction.get();
            if(hasAdminMode)
                return preformAdminClaim(player, faction, chunk);
            else return preformNormalClaim(player, faction, chunk);
        }
    }

    private CommandResult preformClaimByFaction(final Player player, final Faction faction, final Vector3i chunk) throws CommandException
    {
        final World world = player.getWorld();
        final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        final boolean isClaimableWorld = this.protectionConfig.getClaimableWorldNames().contains(world.getName());

        if(!optionalPlayerFaction.isPresent() || !optionalPlayerFaction.get().getName().equals(faction.getName()))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_DONT_HAVE_ACCESS_TO_DO_THIS));

        if(!isClaimableWorld)
            throw new CommandException(PluginInfo.ERROR_PREFIX.concat(Text.of(TextColors.RED, Messages.YOU_CANNOT_CLAIM_TERRITORIES_IN_THIS_WORLD)));

        return preformNormalClaim(player, faction, chunk);
    }

    private CommandResult preformAdminClaim(final Player player, final Faction faction, final Vector3i chunk) throws CommandException
    {
        final World world = player.getWorld();
        final boolean safeZoneWorld = this.protectionConfig.getSafeZoneWorldNames().contains(world.getName());
        final boolean warZoneWorld = this.protectionConfig.getWarZoneWorldNames().contains(world.getName());

        //Even admin cannot claim territories in safezone nor warzone world.
        if (safeZoneWorld || warZoneWorld)
            throw new CommandException(PluginInfo.ERROR_PREFIX.concat(Text.of(TextColors.RED, Messages.YOU_CANNOT_CLAIM_TERRITORIES_IN_THIS_WORLD)));

        boolean isCancelled = EventRunner.runFactionClaimEvent(player, faction, player.getWorld(), chunk);
        if (isCancelled)
            return CommandResult.empty();

        super.getPlugin().getFactionLogic().addClaim(faction, new Claim(player.getWorld().getUniqueId(), chunk));
        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, Messages.LAND + " ", TextColors.GOLD, chunk.toString(), TextColors.WHITE, " " + Messages.HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, Messages.CLAIMED, TextColors.WHITE, "!"));
        return CommandResult.success();
    }

    private CommandResult preformNormalClaim(final Player player, final Faction faction, final Vector3i chunk) throws CommandException
    {
        final World world = player.getWorld();
        final boolean isClaimableWorld = this.protectionConfig.getClaimableWorldNames().contains(world.getName());

        if(!isClaimableWorld)
            throw new CommandException(PluginInfo.ERROR_PREFIX.concat(Text.of(TextColors.RED, Messages.YOU_CANNOT_CLAIM_TERRITORIES_IN_THIS_WORLD)));

        //If not admin then check faction perms for player
        if (!this.getPlugin().getPermsManager().canClaim(player.getUniqueId(), faction))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.PLAYERS_WITH_YOUR_RANK_CANT_CLAIM_LANDS));

        //Check if faction has enough power to claim territory
        if (super.getPlugin().getPowerManager().getFactionMaxClaims(faction) <= faction.getClaims().size())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOUR_FACTION_DOES_NOT_HAVE_POWER_TO_CLAIM_MORE_LANDS));

        //If attacked then It should not be able to claim territories
        if (EagleFactionsPlugin.ATTACKED_FACTIONS.containsKey(faction.getName()))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOUR_FACTION_IS_UNDER_ATTACK + " ", MessageLoader.parseMessage(Messages.YOU_NEED_TO_WAIT_NUMBER_SECONDS_TO_BE_ABLE_TO_CLAIM_AGAIN, TextColors.RED, Collections.singletonMap(Placeholders.NUMBER, Text.of(TextColors.GOLD, EagleFactionsPlugin.ATTACKED_FACTIONS.get(faction.getName()))))));

        if (this.factionsConfig.requireConnectedClaims() && !super.getPlugin().getFactionLogic().isClaimConnected(faction, new Claim(world.getUniqueId(), chunk)))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.CLAIMS_NEED_TO_BE_CONNECTED));

        boolean isCancelled = EventRunner.runFactionClaimEvent(player, faction, world, chunk);
        if (isCancelled)
            return CommandResult.empty();

        super.getPlugin().getFactionLogic().startClaiming(player, faction, world.getUniqueId(), chunk);
        return CommandResult.success();
    }
}
