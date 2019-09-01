package io.github.aquerr.eaglefactions.api.managers;

import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public interface IProtectionManager
{
    /**
     * Checks if a {@link User} can break blocks at the given {@link Location<World>}
     * @param location that should be checked for block break.
     * @param player who will be tested for the given location.
     * @return <tt>true</tt> if player can break block or <tt>false</tt> if not
     */
    boolean canBreak(final Location<World> location, final User player);

    /**
     * Checks if a block can be destroyed at the given {@link Location<World>}
     * @param location that should be checked for block break.
     * @return <tt>true</tt> if block can be destroyed at the given location or <tt>false</tt> if not
     */
    boolean canBreak(final Location<World> location);

    /**
     * Checks if a {@link User} can place blocks at the given {@link Location<World>}
     * @param location that should be checked for block place.
     * @param player who will be tested for the given location.
     * @return <tt>true</tt> if block can be placed at the given location or <tt>false</tt> if not
     */
    boolean canPlace(final Location<World> location, final User player);

    /**
     * Checks if a {@link User} can explode blocks at the given {@link Location<World>}
     * @param location that should be checked for block explosion.
     * @param player who will be tested for the given location.
     * @return <tt>true</tt> if blocks can be exploded at the given location or <tt>false</tt> if not
     */
    boolean canExplode(final Location<World> location, final User player);

    /**
     * Checks if blocks can explode at the given {@link Location<World>}
     * @param location that should be check for block explosion.
     * @return <tt>true</tt> if blocks can be exploded at the given location or <tt>false</tt> if not
     */
    boolean canExplode(final Location<World> location);

    /**
     * Checks if the given item id is white-listed by Eagle Factions.
     * @param itemId for the item that should be tested.
     *
     * Item id should be in the following format: modid:name or minecraft:name
     * e.g. minecraft:bucket or enderio:windmill
     *
     * @return <tt>true</tt> if the item is white-listed or <tt>false</tt> if it is not.
     *
     */
    boolean isItemWhitelisted(final String itemId);

    /**
     * Checks if the given item or block id is white-listed for placing and destroying by Eagle Factions.
     * @param itemOrBlockId of the item/block that should be tested.
     * @return <tt>true</tt> if item/block is white-listed or <tt>false</tt> if it is not.
     */
    boolean isBlockWhitelistedForPlaceDestroy(final String itemOrBlockId);

    /**
     * Checks if the given block id is white-listed for interaction by Eagle Factions.
     * @param blockId of the block that should be tested.
     * @return <tt>true</tt> if block is white-listed or <tt>false</tt> if not.
     */
    boolean isBlockWhitelistedForInteraction(final String blockId);

    /**
     * Checks if a {@link User} can interact with block at the given location.
     * @param blockLocation block location that should be checked.
     * @param player who should be tested for block interaction.
     * @return <tt>true</tt> if player can interact with block or <tt>false</tt> if not
     */
    boolean canInteractWithBlock(final Location<World> blockLocation, final User player);

    /**
     * Checks if a {@link User} can use an item at the given location.
     * @param location that should be checked for item use.
     * @param user who should be tested for the given location.
     * @param usedItem {@link ItemStackSnapshot} that should be tested.
     * @return <tt>true</tt> if user can use the item in the given location or <tt>false</tt> if not
     */
    boolean canUseItem(final Location<World> location, final User user, final ItemStackSnapshot usedItem);
}
