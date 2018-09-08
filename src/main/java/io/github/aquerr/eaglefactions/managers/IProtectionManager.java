package io.github.aquerr.eaglefactions.managers;

import org.spongepowered.api.entity.living.player.Player;

import javax.xml.stream.Location;
import java.util.UUID;

public interface IProtectionManager
{
    boolean canInteract(Player player, Location location, UUID worldUUID);
    boolean canBreak(Player player, Location location);
    boolean canPlace(Player player, Location location);
}
