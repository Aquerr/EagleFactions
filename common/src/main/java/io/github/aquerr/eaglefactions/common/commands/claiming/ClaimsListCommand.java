package io.github.aquerr.eaglefactions.common.commands.claiming;

import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.HoverAction;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Command used for showing list of cliams that belongs to the player's faction.
 *
 * This command can be used by admin.
 */
public class ClaimsListCommand extends AbstractCommand
{
    public ClaimsListCommand(final EagleFactions plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        final Optional<Faction> optionalFaction = context.getOne(Text.of("faction"));

        if (optionalFaction.isPresent())
        {
            final Faction faction = optionalFaction.get();
            if (source instanceof ConsoleSource)
            {
                showClaimsList(source, faction);
            }
            else if (source instanceof Player)
            {
                final Player player = (Player)source;
                if (super.getPlugin().getPlayerManager().hasAdminMode(player))
                {
                    showClaimsList(source, faction);
                    return CommandResult.success();
                }
                final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
                if (!optionalPlayerFaction.isPresent())
                    throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));
                final Faction playerFaction = optionalPlayerFaction.get();
                if (!faction.getName().equals(playerFaction.getName()))
                    throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_DONT_HAVE_ACCESS_TO_DO_THIS));

                //At this point we know that player belongs to the choosen faction.
                showClaimsList(player, playerFaction);
            }
            else throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_DONT_HAVE_ACCESS_TO_DO_THIS));
        }

        if (!(source instanceof Player))
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));
        final Player player = (Player)source;
        final Optional<Faction> optionalFactionPlayer = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
        if (!optionalFactionPlayer.isPresent())
            throw new CommandException(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, Messages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));
        final Faction playerFaction = optionalFactionPlayer.get();
        showClaimsList(player, playerFaction);
        return CommandResult.success();
    }

    private void showClaimsList(final CommandSource source, final Faction faction)
    {
        final List<Text> resultList = new ArrayList<>();
        final Set<Claim> claims = faction.getClaims();

        for (final Claim claim : claims)
        {
            final Text.Builder claimHoverInfo = Text.builder();
            claimHoverInfo.append(Text.of(TextColors.GOLD, "Accessible by faction: ", TextColors.RESET, claim.isAccessibleByFaction(), "\n"));
            final List<String> ownersNames = claim.getOwners().stream()
                    .map(owner -> super.getPlugin().getPlayerManager().getFactionPlayer(owner))
                    .filter(Optional::isPresent)
                    .map(factionPlayer -> factionPlayer.get().getName())
                    .collect(Collectors.toList());
            claimHoverInfo.append(Text.of(TextColors.GOLD, "Owners: ", TextColors.RESET, String.join(", ", ownersNames)));

            final Text.Builder textBuilder = Text.builder();
            final Optional<World> world = Sponge.getServer().getWorld(claim.getWorldUUID());
            String worldName = "";
            if (world.isPresent())
                worldName = world.get().getName();
            textBuilder.append(Text.of("- ", TextColors.YELLOW, "World: " , TextColors.GREEN, worldName, TextColors.RESET, " | ", TextColors.YELLOW, "Chunk: ", TextColors.GREEN, claim.getChunkPosition()))
                    .onHover(TextActions.showText(claimHoverInfo.build()));
            resultList.add(textBuilder.build());
        }

        final PaginationList paginationList = PaginationList.builder().padding(Text.of("=")).title(Text.of(TextColors.YELLOW, "Claims List")).contents(resultList).linesPerPage(10).build();
        paginationList.sendTo(source);
    }
}
