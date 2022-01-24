package io.github.aquerr.eaglefactions.scheduling;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.entities.FactionInvite;

public class RemoveInviteTask implements EagleFactionsRunnableTask
{
    private final FactionInvite factionInvite;

    public RemoveInviteTask(final FactionInvite factionInvite)
    {
        this.factionInvite = factionInvite;
    }

    @Override
    public void run()
    {
        EagleFactionsPlugin.INVITE_LIST.remove(factionInvite);
    }
}
