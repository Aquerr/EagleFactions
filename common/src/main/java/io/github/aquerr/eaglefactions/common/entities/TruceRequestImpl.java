package io.github.aquerr.eaglefactions.common.entities;

import io.github.aquerr.eaglefactions.api.entities.TruceRequest;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;

public class TruceRequestImpl extends AbstractRelationRequest implements TruceRequest
{
    public TruceRequestImpl(String senderFaction, String invitedFaction)
    {
        super(senderFaction, invitedFaction);
    }

    @Override
    public void accept()
    {
        EagleFactionsPlugin.getPlugin().getInvitationManager().acceptTruceRequest(this);
    }
}
