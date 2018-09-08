package io.github.aquerr.eaglefactions.managers;

import io.github.aquerr.eaglefactions.EagleFactions;
import org.spongepowered.api.entity.living.player.Player;

import javax.xml.stream.Location;
import java.util.UUID;

public class ProtectionManager implements IProtectionManager
{
    EagleFactions plugin;

    public ProtectionManager(EagleFactions plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public boolean canInteract(Player player, Location location, UUID worldUUID)
    {
        return false;
    }

    @Override
    public boolean canBreak(Player player, Location location)
    {
        return false;
    }

    @Override
    public boolean canPlace(Player player, Location location)
    {
        return false;
    }
}
