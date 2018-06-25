package io.github.aquerr.eaglefactions.commands;

import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.commands.Helper.FactionPlayerCommand;
import io.github.aquerr.eaglefactions.commands.Helper.RequiredRank;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.FactionMemberType;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;

import java.util.UUID;

/**
 * An example of how easy it can be made!
 */
@RequiredRank(minimumRank = FactionMemberType.LEADER)
public class DisbandCommandTrial extends FactionPlayerCommand
{
    @Override
    protected boolean executeCommand(Player player, Faction faction, CommandContext context)
    {
        faction.forEachMember(e -> EagleFactions.AutoClaimList.remove(UUID.fromString(e)));
        cache.removeFaction(faction.Name);
        return true;
    }
}
