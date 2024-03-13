package io.github.aquerr.eaglefactions.managers.claim;

import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.api.exception.RequiredItemsNotFoundException;
import io.github.aquerr.eaglefactions.api.managers.claim.ClaimContext;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import io.github.aquerr.eaglefactions.util.ItemUtil;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.server.ServerLocation;

import java.util.List;

public class ClaimContextImpl implements ClaimContext
{
    private final ServerLocation serverLocation;
    private final ServerPlayer serverPlayer;
    private final Faction faction;
    private final MessageService messageService;

    public ClaimContextImpl(ServerLocation serverLocation,
                            ServerPlayer serverPlayer,
                            Faction faction,
                            MessageService messageService)
    {
        this.serverLocation = serverLocation;
        this.serverPlayer = serverPlayer;
        this.faction = faction;
        this.messageService = messageService;
    }

    @Override
    public ServerLocation getServerLocation()
    {
        return this.serverLocation;
    }

    @Override
    public ServerPlayer getServerPlayer()
    {
        return this.serverPlayer;
    }

    @Override
    public Faction getFaction()
    {
        return this.faction;
    }

    @Override
    public MessageService getMessageService()
    {
        return messageService;
    }

    @Override
    public void consumePlayerItems(List<ItemStack> itemStacks) throws RequiredItemsNotFoundException
    {
        ItemUtil.pollItemsFromPlayer(getServerPlayer(), itemStacks);
    }
}
