package io.github.aquerr.eaglefactions.commands;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.commands.assembly.FactionPlayerCommand;
import io.github.aquerr.eaglefactions.commands.annotations.RequiredRank;
import io.github.aquerr.eaglefactions.commands.annotations.Subcommand;
import io.github.aquerr.eaglefactions.config.Settings;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.FactionMemberType;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * An example of how easy it can be made!
 */
@Singleton
@RequiredRank(minimumRank = FactionMemberType.LEADER)
@Subcommand(aliases = {"disband"}, description = "Disband Faction Command", permission = PluginPermissions.DisbandCommand)
public class DisbandCommand extends FactionPlayerCommand
{
    @Inject
    public DisbandCommand(FactionsCache cache, Settings settings, FactionLogic factionLogic, @Named("factions") Logger logger)
    {
        super(cache, settings, factionLogic, logger);
    }

    @Override
    protected boolean executeCommand(Player player, Faction faction, CommandContext context)
    {
        faction.forEachMember(e -> EagleFactions.AutoClaimList.remove(UUID.fromString(e)));
        cache.removeFaction(faction.Name);
        return true;
    }
}
