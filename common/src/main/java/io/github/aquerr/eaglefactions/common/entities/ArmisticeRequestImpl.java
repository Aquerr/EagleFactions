package io.github.aquerr.eaglefactions.common.entities;

import io.github.aquerr.eaglefactions.api.entities.ArmisticeRequest;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;

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
