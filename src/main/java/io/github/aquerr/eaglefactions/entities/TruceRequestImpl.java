package io.github.aquerr.eaglefactions.entities;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.entities.RelationType;
import io.github.aquerr.eaglefactions.api.entities.TruceRequest;

public class TruceRequestImpl extends AbstractRelationRequest implements TruceRequest
{
    public TruceRequestImpl(Faction inviter, Faction invited)
    {
        super(inviter, invited, RelationType.TRUCE);
    }

    @Override
    public void accept()
    {
        EagleFactionsPlugin.getPlugin().getInvitationManager().acceptTruceRequest(this);
    }
}
