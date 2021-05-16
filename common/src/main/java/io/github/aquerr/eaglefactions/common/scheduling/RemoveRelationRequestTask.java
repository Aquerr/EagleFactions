package io.github.aquerr.eaglefactions.common.scheduling;

import io.github.aquerr.eaglefactions.api.entities.AcceptableInvite;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;

public class RemoveRelationRequestTask implements EagleFactionsRunnableTask
{
    private final AcceptableInvite acceptableInvite;

    public RemoveRelationRequestTask(final AcceptableInvite acceptableInvite)
    {
        this.acceptableInvite = acceptableInvite;
    }

    @Override
    public String getName()
    {
        return "EagleFactions Remove Relation Invite Task";
    }

    @Override
    public void run()
    {
        EagleFactionsPlugin.RELATION_INVITES.remove(this.acceptableInvite);
    }
}
