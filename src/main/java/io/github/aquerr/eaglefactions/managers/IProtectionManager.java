package io.github.aquerr.eaglefactions.managers;

import org.spongepowered.api.CatalogType;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public interface IProtectionManager
{
    boolean canInteract(Location location, World World, Player player);
    boolean canBreak(Location location, World world, Player player);
    boolean canBreak(Location location, World world);
    boolean canPlace(Location location, World world, Player player);

    boolean isItemWhitelisted(CatalogType itemType);
    boolean isBlockWhitelistedForPlaceDestroy(CatalogType itemOrBlockType);
    boolean isBlockWhitelistedForInteraction(CatalogType blockType);
}
