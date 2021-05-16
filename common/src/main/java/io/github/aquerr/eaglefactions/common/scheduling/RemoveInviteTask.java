package io.github.aquerr.eaglefactions.common.scheduling;

import io.github.aquerr.eaglefactions.api.entities.FactionInvite;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;

public class RemoveInviteTask implements EagleFactionsRunnableTask
{
    private final FactionInvite factionInvite;

    public RemoveInviteTask(final FactionInvite factionInvite)
    {
        this.factionInvite = factionInvite;
    }

    @Override
    public String getName()
    {
        return "EagleFactions Remove Invite Task - " + factionInvite.getInvitedPlayerUniqueId() + " " + factionInvite.getSenderFaction();
    }

    @Override
    public void run()
    {
        EagleFactionsPlugin.INVITE_LIST.remove(factionInvite);
    }
}
