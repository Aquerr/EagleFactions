package io.github.aquerr.eaglefactions.common.listeners;

import com.flowpowered.math.vector.Vector3d;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.managers.ProtectionManager;
import io.github.aquerr.eaglefactions.api.managers.ProtectionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.monster.Monster;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class PlayerInteractListenerTest
{
    private static final BlockType GRASS_BLOCK_TYPE = BlockTypes.GRASS;

    @Mock
    private ProtectionManager protectionManager;

    @Mock
    private EagleFactions eagleFactions;

    @Mock
    private World world;

    @Mock
    private BlockState blockState;

    @Mock
    private BlockSnapshot blockSnapshot;

    @Mock
    private InteractBlockEvent.Secondary interactBlockEvent;

    @Mock
    private InteractItemEvent interactItemEvent;

    @Mock
    private InteractEntityEvent interactEntityEvent;

    @Mock
    private Player player;

    @InjectMocks
    private PlayerInteractListener playerInteractListener;

    @BeforeEach
    public void setup()
    {
        initMocks(this);
        when(eagleFactions.getProtectionManager()).thenReturn(protectionManager);
        when(player.getWorld()).thenReturn(world);
    }

    @Test
    void onItemUseDoesNotCancelEventWhenItemStackIsNone()
    {
        when(interactItemEvent.getItemStack()).thenReturn(ItemStackSnapshot.NONE);
        playerInteractListener.onItemUse(interactItemEvent, player);

        verify(interactItemEvent, times(0)).setCancelled(anyBoolean());
    }

    @Test
    void onItemUseDoesNotCancelEventWhenInteractionPointsLocationsBlockIsAir()
    {
        Vector3d blockPosition = new Vector3d(1, 1, 1);
        when(world.getBlockType(blockPosition.toInt())).thenReturn(BlockTypes.AIR);
        when(interactItemEvent.getInteractionPoint()).thenReturn(Optional.of(blockPosition));

        playerInteractListener.onItemUse(interactItemEvent, player);

        verify(interactItemEvent, times(0)).setCancelled(anyBoolean());
    }

    @Test
    void onItemUseDoesNotCancelEventWhenHitMonsterEntity()
    {
        Vector3d blockPosition = new Vector3d(1, 1, 1);
        Entity entity = Mockito.mock(Monster.class);
        when(world.getBlockType(blockPosition.toInt())).thenReturn(GRASS_BLOCK_TYPE);
        when(interactItemEvent.getInteractionPoint()).thenReturn(Optional.of(blockPosition));
        EventContext eventContext = EventContext.builder().add(EventContextKeys.ENTITY_HIT, entity).build();
        when(interactItemEvent.getContext()).thenReturn(eventContext);

        playerInteractListener.onItemUse(interactItemEvent, player);

        verify(interactItemEvent, times(0)).setCancelled(anyBoolean());
    }

    @Test
    void onItemUseCancelsEventWhenPlayerHasNoAccess()
    {
        Vector3d blockPosition = new Vector3d(1, 1, 1);
        when(world.getBlockType(blockPosition.toInt())).thenReturn(GRASS_BLOCK_TYPE);
        when(interactItemEvent.getInteractionPoint()).thenReturn(Optional.of(blockPosition));
        when(protectionManager.canUseItem(any(), any(), any(), anyBoolean())).thenReturn(ProtectionResult.forbidden());
        EventContext eventContext = EventContext.empty();
        when(interactItemEvent.getContext()).thenReturn(eventContext);

        playerInteractListener.onItemUse(interactItemEvent, player);

        verify(interactItemEvent, times(1)).setCancelled(true);
    }

    @Test
    void onEntityInteractDoesNotCancelEventWhenEntityIsMonster()
    {
        Entity entity = Mockito.mock(Monster.class);
        when(interactEntityEvent.getTargetEntity()).thenReturn(entity);

        playerInteractListener.onEntityInteract(interactEntityEvent, player);

        verify(interactEntityEvent, times(0)).setCancelled(true);
    }

    @Test
    void onEntityInteractCancelsEventWhenEntityIsArmorStandAndProtectionResultIsForbidden()
    {
        Entity entity = Mockito.mock(ArmorStand.class);
        Vector3d interactionPoint = new Vector3d(1, 1, 1);
        when(entity.getWorld()).thenReturn(world);
        when(interactEntityEvent.getTargetEntity()).thenReturn(entity);
        when(interactEntityEvent.getInteractionPoint()).thenReturn(Optional.of(interactionPoint));
        when(protectionManager.canInteractWithBlock(any(), any(), anyBoolean())).thenReturn(ProtectionResult.forbidden());

        playerInteractListener.onEntityInteract(interactEntityEvent, player);

        verify(interactEntityEvent, times(1)).setCancelled(true);
    }

    @Test
    void onEntityInteractEventDoesNotCancelEventWhenProtectionResultIsOk()
    {
        Entity entity = Mockito.mock(Monster.class);
        Vector3d interactionPoint = new Vector3d(1, 1, 1);
        when(entity.getWorld()).thenReturn(world);
        when(interactEntityEvent.getTargetEntity()).thenReturn(entity);
        when(interactEntityEvent.getInteractionPoint()).thenReturn(Optional.of(interactionPoint));
        when(protectionManager.canInteractWithBlock(any(), any(), anyBoolean())).thenReturn(ProtectionResult.ok());

        playerInteractListener.onEntityInteract(interactEntityEvent, player);

        verify(interactEntityEvent, times(0)).setCancelled(true);
    }

    @Test
    void onBlockInteractInvokesCanUseItemWhenUsedItemIsSpawnEgg()
    {
        Location<World> location = new Location<>(world, 1, 1, 1);
        when(blockState.getType()).thenReturn(GRASS_BLOCK_TYPE);
        when(blockSnapshot.getState()).thenReturn(blockState);
        when(blockSnapshot.getLocation()).thenReturn(Optional.of(location));
        when(interactBlockEvent.getTargetBlock()).thenReturn(blockSnapshot);
        ItemStackSnapshot itemStackSnapshot = Mockito.mock(ItemStackSnapshot.class);
        when(itemStackSnapshot.getType()).thenReturn(ItemTypes.SPAWN_EGG);
        EventContext eventContext = EventContext.builder().add(EventContextKeys.USED_ITEM, itemStackSnapshot).build();
        when(interactBlockEvent.getContext()).thenReturn(eventContext);
        when(protectionManager.canUseItem(location, player, itemStackSnapshot, true)).thenReturn(ProtectionResult.ok());

        playerInteractListener.onBlockInteract(interactBlockEvent, player);

        verify(interactItemEvent, times(0)).setCancelled(true);
        verify(protectionManager, times(1)).canUseItem(any(), any(), any(), anyBoolean());
    }

    @Test
    void onBlockInteractInvokesCanInteractWithBlockWhenContextDoesNotContainItemUsedKey()
    {
        Location<World> location = new Location<>(world, 1, 1, 1);
        when(blockState.getType()).thenReturn(GRASS_BLOCK_TYPE);
        when(blockSnapshot.getState()).thenReturn(blockState);
        when(blockSnapshot.getLocation()).thenReturn(Optional.of(location));
        when(interactBlockEvent.getTargetBlock()).thenReturn(blockSnapshot);
        EventContext eventContext = EventContext.empty();
        when(interactBlockEvent.getContext()).thenReturn(eventContext);
        when(protectionManager.canInteractWithBlock(location, player, true)).thenReturn(ProtectionResult.ok());

        playerInteractListener.onBlockInteract(interactBlockEvent, player);

        verify(interactBlockEvent, times(0)).setCancelled(true);
        verify(protectionManager, times(1)).canInteractWithBlock(any(), any(), anyBoolean());
    }

    @Test
    void onBlockInteractCancelsEventWhenProtectionResultIsForbidden()
    {
        Location<World> location = new Location<>(world, 1, 1, 1);
        when(blockState.getType()).thenReturn(GRASS_BLOCK_TYPE);
        when(blockSnapshot.getState()).thenReturn(blockState);
        when(blockSnapshot.getLocation()).thenReturn(Optional.of(location));
        when(interactBlockEvent.getTargetBlock()).thenReturn(blockSnapshot);
        EventContext eventContext = EventContext.empty();
        when(interactBlockEvent.getContext()).thenReturn(eventContext);
        when(protectionManager.canInteractWithBlock(location, player, true)).thenReturn(ProtectionResult.forbidden());

        playerInteractListener.onBlockInteract(interactBlockEvent, player);

        verify(interactBlockEvent, times(1)).setCancelled(true);
        verify(protectionManager, times(1)).canInteractWithBlock(any(), any(), anyBoolean());
    }

    @Test
    void onBlockInteractDoesNotCancelEventWhenProtectionResultIsEagleFeatherAndRemovesOneEagleFeather()
    {
        Location<World> location = new Location<>(world, 1, 1, 1);
        when(blockState.getType()).thenReturn(GRASS_BLOCK_TYPE);
        when(blockSnapshot.getState()).thenReturn(blockState);
        when(blockSnapshot.getLocation()).thenReturn(Optional.of(location));
        when(interactBlockEvent.getTargetBlock()).thenReturn(blockSnapshot);
        EventContext eventContext = EventContext.empty();
        when(interactBlockEvent.getContext()).thenReturn(eventContext);
        when(protectionManager.canInteractWithBlock(location, player, true)).thenReturn(ProtectionResult.okEagleFeather());
        ItemStack eagleFeather = Mockito.mock(ItemStack.class);
        when(player.getItemInHand(HandTypes.MAIN_HAND)).thenReturn(Optional.of(eagleFeather));

        playerInteractListener.onBlockInteract(interactBlockEvent, player);

        verify(interactBlockEvent, times(0)).setCancelled(true);
        verify(protectionManager, times(1)).canInteractWithBlock(any(), any(), anyBoolean());
        verify(eagleFeather, times(1)).setQuantity(anyInt());
    }
}