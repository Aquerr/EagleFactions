package io.github.aquerr.eaglefactions.api.managers;

import org.spongepowered.api.CatalogType;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public interface IProtectionManager
{
//    boolean canInteract(Location<World> location, User player);
    boolean canBreak(Location<World> location, User player);
    boolean canBreak(Location<World> location);
    boolean canPlace(Location<World> location, User player);
    boolean canExplode(Location<World> location, User player);
    boolean canExplode(Location<World> location);

    boolean isItemWhitelisted(CatalogType itemType);
    boolean isBlockWhitelistedForPlaceDestroy(CatalogType itemOrBlockType);
    boolean isBlockWhitelistedForInteraction(CatalogType blockType);

    boolean canInteractWithBlock(Location<World> blockLocation, User player);
    boolean canUseItem(Location<World> location, User user, ItemStackSnapshot usedItem);
}
