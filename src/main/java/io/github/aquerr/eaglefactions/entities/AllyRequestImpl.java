package io.github.aquerr.eaglefactions.entities;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.entities.AllyRequest;

import java.util.Objects;

public class AllyRequestImpl extends AbstractRelationRequest implements AllyRequest
{
    public AllyRequestImpl(String senderFaction, String invitedFaction)
    {
        super(senderFaction, invitedFaction);
    }

    @Override
    public void accept()
    {
        EagleFactionsPlugin.getPlugin().getInvitationManager().acceptAllyRequest(this);
    }
}
