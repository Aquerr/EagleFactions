package io.github.aquerr.eaglefactions.entities;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.entities.ArmisticeRequest;
import io.github.aquerr.eaglefactions.api.entities.Faction;

public class ArmisticeRequestImpl extends AbstractRelationRequest implements ArmisticeRequest
{
    public ArmisticeRequestImpl(Faction sender, Faction invited)
    {
        super(sender, invited);
    }

    @Override
    public void accept()
    {
        EagleFactionsPlugin.getPlugin().getInvitationManager().acceptArmisticeRequest(this);
    }
}
