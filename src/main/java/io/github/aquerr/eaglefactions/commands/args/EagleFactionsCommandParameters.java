package io.github.aquerr.eaglefactions.commands.args;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.FactionPlayer;
import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import org.spongepowered.api.command.parameter.Parameter;

public class EagleFactionsCommandParameters
{
    private static Parameter.Value<Faction> FACTION;
    private static Parameter.Value<FactionPlayer> FACTION_PLAYER;

    public static void init(FactionLogic factionLogic)
    {
        FACTION = Parameter.builder(Faction.class)
                .key("faction")
                .addParser(new FactionArgument.ValueParser(factionLogic))
                .completer(new FactionArgument.Completer(factionLogic))
                .optional()
                .build();

        FACTION_PLAYER = Parameter.builder(FactionPlayer.class)
                .key("player")
                .addParser(new FactionPlayerArgument.ValueParser())
                .completer(new FactionPlayerArgument.Completer())
                .optional()
                .build();

    }

    public static Parameter.Value<Faction> faction()
    {
        return FACTION;
    }

    public static Parameter.Value<FactionPlayer> factionPlayer()
    {
        return FACTION_PLAYER;
    }
}
