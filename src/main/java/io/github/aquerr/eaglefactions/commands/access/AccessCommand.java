package io.github.aquerr.eaglefactions.commands.access;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.api.entities.Claim;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import io.github.aquerr.eaglefactions.messaging.Messages;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.exception.CommandException;
import org.spongepowered.api.command.parameter.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.pagination.PaginationList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public class AccessCommand extends AbstractCommand
{
    public AccessCommand(EagleFactionsPlugin plugin)
    {
        super(plugin);
    }

    @Override
    public CommandResult execute(final CommandContext context) throws CommandException
    {
        ServerPlayer player = requirePlayerSource(context);
        Faction playerFaction = requirePlayerFaction(player);

        // Access can be run only by leader and officers
        final Faction chunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(player.world().uniqueId(), player.serverLocation().chunkPosition())
                .orElseThrow(() -> new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.THIS_PLACE_DOES_NOT_BELONG_TO_ANYONE))));

        if (!playerFaction.equals(chunkFaction))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.THIS_PLACE_DOES_NOT_BELONG_TO_YOUR_FACTION)));

        if (!playerFaction.getLeader().equals(player.uniqueId()) && !playerFaction.getOfficers().contains(player.uniqueId()) && !super.getPlugin().getPlayerManager().hasAdminMode(player.user()))
            throw new CommandException(PluginInfo.ERROR_PREFIX.append(text(Messages.YOU_MUST_BE_THE_FACTIONS_LEADER_OR_OFFICER_TO_DO_THIS, RED)));

        // Get claim at player's location
        final Optional<Claim> optionalClaim = chunkFaction.getClaimAt(player.world().uniqueId(), player.serverLocation().chunkPosition());
        return optionalClaim.map(claim -> showAccess(player, claim))
                .orElse(CommandResult.success());
    }

    private CommandResult showAccess(final Player player, final Claim claim)
    {
        final TextComponent claimLocation = text("Location: ", AQUA).append(text(claim.getChunkPosition().toString(), GOLD));
        final TextComponent accessibleByFacionText = text("Accessible by faction: ", AQUA).append(text(claim.isAccessibleByFaction(), GOLD));
        final List<String> ownersNames = claim.getOwners().stream()
                .map(owner -> super.getPlugin().getPlayerManager().getFactionPlayer(owner))
                .filter(Optional::isPresent)
                .map(factionPlayer -> factionPlayer.get().getName())
                .collect(Collectors.toList());
        final TextComponent text1 = text("Owners: ", AQUA).append(text(String.join(", ", ownersNames), GOLD));
        final List<Component> contents = new ArrayList<>();
        contents.add(claimLocation);
        contents.add(accessibleByFacionText);
        contents.add(text1);
        final PaginationList paginationList = PaginationList.builder()
                .contents(contents)
                .padding(text("="))
                .title(text("Claim Access", GREEN))
                .build();
        paginationList.sendTo(player);
        return CommandResult.success();
    }
}
