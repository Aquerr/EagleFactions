package io.github.aquerr.eaglefactions.entities;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.entities.AllyRequest;
import io.github.aquerr.eaglefactions.api.entities.Faction;

public class AllyRequestImpl extends AbstractRelationRequest implements AllyRequest
{
    public AllyRequestImpl(Faction inviter, Faction invited)
    {
        super(inviter, invited);
    }

    @Override
    public void accept()
    {
        EagleFactionsPlugin.getPlugin().getInvitationManager().acceptAllyRequest(this);
    }
}
