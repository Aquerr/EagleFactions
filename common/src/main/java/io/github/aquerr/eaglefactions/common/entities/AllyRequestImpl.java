package io.github.aquerr.eaglefactions.common.entities;

import io.github.aquerr.eaglefactions.api.entities.AllyRequest;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;

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
