package io.github.aquerr.eaglefactions.entities;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.entities.ArmisticeRequest;

public class ArmisticeRequestImpl extends AbstractRelationRequest implements ArmisticeRequest
{
    public ArmisticeRequestImpl(String senderFaction, String invitedFaction)
    {
        super(senderFaction, invitedFaction);
    }

    @Override
    public void accept()
    {
        EagleFactionsPlugin.getPlugin().getInvitationManager().acceptArmisticeRequest(this);
    }
}
