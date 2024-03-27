package io.github.aquerr.eaglefactions.managers.creation;

import io.github.aquerr.eaglefactions.api.logic.FactionLogic;
import net.kyori.adventure.audience.Audience;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

public class FactionCreationManager
{
    private final FactionLogic factionLogic;
    private FactionCreationStrategy creationStrategy;

    public FactionCreationManager(FactionLogic factionLogic)
    {
        this.factionLogic = factionLogic;
    }

    public void setCreationStrategy(FactionCreationStrategy creationStrategy)
    {
        this.creationStrategy = creationStrategy;
    }

    public void createFaction(Audience audience, String factionName, String factionTag)
    {
        if (isServerPlayer(audience))
        {

        }
    }

    private boolean isServerPlayer(Audience audience)
    {
        return audience instanceof ServerPlayer;
    }
}
