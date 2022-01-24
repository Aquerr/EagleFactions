package io.github.aquerr.eaglefactions.listeners;

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
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.Monster;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.event.item.inventory.container.InteractContainerEvent;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.server.ServerWorld;
import org.spongepowered.math.vector.Vector3d;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

class PlayerInteractListenerTest
{
    private static final BlockType GRASS_BLOCK_TYPE = BlockTypes.GRASS.get();

    @Mock
    private ProtectionManager protectionManager;

    @Mock
    private EagleFactions eagleFactions;

    @Mock
    private ServerWorld world;

    @Mock
    private BlockState blockState;

    @Mock
    private BlockSnapshot blockSnapshot;

    @Mock
    private InteractBlockEvent.Secondary interactBlockEvent;

    @Mock
    private InteractItemEvent.Secondary interactItemEventSecondary;

    @Mock
    private InteractEntityEvent interactEntityEvent;

    @Mock
    private InteractContainerEvent.Open containerOpenEvent;

    @Mock
    private ServerPlayer player;

    @InjectMocks
    private PlayerInteractListener playerInteractListener;

    @BeforeEach
    public void setup()
    {
        initMocks(this);
        when(eagleFactions.getProtectionManager()).thenReturn(protectionManager);
        when(player.world()).thenReturn(world);
    }

    @Test
    void onItemUseDoesNotCancelEventWhenItemStackIsNone()
    {
        when(interactItemEventSecondary.itemStack()).thenReturn(ItemStackSnapshot.empty());
        playerInteractListener.onItemUse(interactItemEventSecondary, player);

        verify(interactItemEventSecondary, times(0)).setCancelled(anyBoolean());
    }

    @Test
    void onItemUseDoesNotCancelEventWhenInteractionPointsLocationsBlockIsAir()
    {
        Vector3d blockPosition = new Vector3d(1, 1, 1);
        when(world.block(blockPosition.toInt()).type()).thenReturn(BlockTypes.AIR.get());

        playerInteractListener.onItemUse(interactItemEventSecondary, player);

        verify(interactItemEventSecondary, times(0)).setCancelled(anyBoolean());
    }

    @Test
    void onItemUseDoesNotCancelEventWhenHitMonsterEntity()
    {
        Vector3d blockPosition = new Vector3d(1, 1, 1);
        when(world.block(blockPosition.toInt()).type()).thenReturn(GRASS_BLOCK_TYPE);

        playerInteractListener.onItemUse(interactItemEventSecondary, player);

        verify(interactItemEventSecondary, times(0)).setCancelled(anyBoolean());
    }

    @Test
    void onItemUseCancelsEventWhenPlayerHasNoAccess()
    {
        Vector3d blockPosition = new Vector3d(1, 1, 1);
        when(world.block(blockPosition.toInt()).type()).thenReturn(GRASS_BLOCK_TYPE);
        when(protectionManager.canUseItem(any(), any(), any(), anyBoolean())).thenReturn(ProtectionResult.forbidden());

        playerInteractListener.onItemUse(interactItemEventSecondary, player);

        verify(interactItemEventSecondary, times(1)).setCancelled(true);
    }

    @Test
    void onEntityInteractDoesNotCancelEventWhenEntityIsMonster()
    {
        Entity entity = Mockito.mock(Monster.class);
        when(interactEntityEvent.entity()).thenReturn(entity);

        playerInteractListener.onEntityInteract(interactEntityEvent, player);

        verify(interactEntityEvent, times(0)).setCancelled(true);
    }

    @Test
    void onEntityInteractCancelsEventWhenEntityIsArmorStandAndProtectionResultIsForbidden()
    {
        Entity entity = Mockito.mock(ArmorStand.class);
        when(interactEntityEvent.entity()).thenReturn(entity);
        when(protectionManager.canHitEntity(any(), any(), anyBoolean())).thenReturn(ProtectionResult.forbidden());

        playerInteractListener.onEntityInteract(interactEntityEvent, player);

        verify(interactEntityEvent, times(1)).setCancelled(true);
    }

    @Test
    void onEntityInteractEventDoesNotCancelEventWhenProtectionResultIsOk()
    {
        Entity entity = Mockito.mock(Monster.class);
        when(interactEntityEvent.entity()).thenReturn(entity);

        playerInteractListener.onEntityInteract(interactEntityEvent, player);

        verify(interactEntityEvent, times(0)).setCancelled(true);
    }

//    @Test
//    void openInventoryEventCancelsDoesNotCancelEventWhenProtectionResultIsEagleFeather()
//    {
//        Location<World> location = new Location<>(world, 1, 1, 1);
//        when(blockState.getType()).thenReturn(GRASS_BLOCK_TYPE);
//        when(blockSnapshot.getState()).thenReturn(blockState);
//        when(blockSnapshot.getLocation()).thenReturn(Optional.of(location));
//        EventContext eventContext = EventContext.builder().add(EventContextKeys.BLOCK_HIT, blockSnapshot).build();
//        when(containerOpenEvent.getContext()).thenReturn(eventContext);
//        when(protectionManager.canInteractWithBlock(location, player, true)).thenReturn(ProtectionResult.okEagleFeather());
//        ItemStack eagleFeather = Mockito.mock(ItemStack.class);
//        when(player.getItemInHand(HandTypes.MAIN_HAND)).thenReturn(Optional.of(eagleFeather));
//
//        playerInteractListener.onInventoryOpenEvent(containerOpenEvent, player);
//
//        verify(containerOpenEvent, times(0)).setCancelled(true);
//        verify(protectionManager, times(1)).canInteractWithBlock(any(), any(), anyBoolean());
//        verify(eagleFeather, times(1)).setQuantity(anyInt());
//    }
//
//    @Test
//    void onBlockInteractInvokesCanUseItemWhenUsedItemIsSpawnEgg()
//    {
//        Location<World> location = new Location<>(world, 1, 1, 1);
//        when(blockState.getType()).thenReturn(GRASS_BLOCK_TYPE);
//        when(blockSnapshot.getState()).thenReturn(blockState);
//        when(blockSnapshot.getLocation()).thenReturn(Optional.of(location));
//        when(interactBlockEvent.getTargetBlock()).thenReturn(blockSnapshot);
//        ItemStackSnapshot itemStackSnapshot = Mockito.mock(ItemStackSnapshot.class);
//        when(itemStackSnapshot.getType()).thenReturn(ItemTypes.SPAWN_EGG);
//        EventContext eventContext = EventContext.builder().add(EventContextKeys.USED_ITEM, itemStackSnapshot).build();
//        when(interactBlockEvent.getContext()).thenReturn(eventContext);
//        when(protectionManager.canUseItem(location, player, itemStackSnapshot, true)).thenReturn(ProtectionResult.ok());
//
//        playerInteractListener.onBlockInteract(interactBlockEvent, player);
//
//        verify(interactItemEventSecondary, times(0)).setCancelled(true);
//        verify(protectionManager, times(1)).canUseItem(any(), any(), any(), anyBoolean());
//    }
//
//    @Test
//    void onBlockInteractInvokesCanInteractWithBlockWhenContextDoesNotContainItemUsedKey()
//    {
//        Location<World> location = new Location<>(world, 1, 1, 1);
//        when(blockState.getType()).thenReturn(GRASS_BLOCK_TYPE);
//        when(blockSnapshot.getState()).thenReturn(blockState);
//        when(blockSnapshot.getLocation()).thenReturn(Optional.of(location));
//        when(interactBlockEvent.getTargetBlock()).thenReturn(blockSnapshot);
//        EventContext eventContext = EventContext.empty();
//        when(interactBlockEvent.getContext()).thenReturn(eventContext);
//        when(protectionManager.canInteractWithBlock(location, player, true)).thenReturn(ProtectionResult.ok());
//
//        playerInteractListener.onBlockInteract(interactBlockEvent, player);
//
//        verify(interactBlockEvent, times(0)).setCancelled(true);
//        verify(protectionManager, times(1)).canInteractWithBlock(any(), any(), anyBoolean());
//    }
//
//    @Test
//    void onBlockInteractCancelsEventWhenProtectionResultIsForbidden()
//    {
//        Location<World> location = new Location<>(world, 1, 1, 1);
//        when(blockState.getType()).thenReturn(GRASS_BLOCK_TYPE);
//        when(blockSnapshot.getState()).thenReturn(blockState);
//        when(blockSnapshot.getLocation()).thenReturn(Optional.of(location));
//        when(interactBlockEvent.getTargetBlock()).thenReturn(blockSnapshot);
//        EventContext eventContext = EventContext.empty();
//        when(interactBlockEvent.getContext()).thenReturn(eventContext);
//        when(protectionManager.canInteractWithBlock(location, player, true)).thenReturn(ProtectionResult.forbidden());
//
//        playerInteractListener.onBlockInteract(interactBlockEvent, player);
//
//        verify(interactBlockEvent, times(1)).setCancelled(true);
//        verify(protectionManager, times(1)).canInteractWithBlock(any(), any(), anyBoolean());
//    }
//
//    @Test
//    void onBlockInteractDoesNotCancelEventWhenProtectionResultIsNotForbidden()
//    {
//        ServerLocation location = ServerLocation.of(world, 1, 1, 1);
//        when(blockState.type()).thenReturn(GRASS_BLOCK_TYPE);
//        when(blockSnapshot.state()).thenReturn(blockState);
//        when(blockSnapshot.location()).thenReturn(Optional.of(location));
//        when(interactBlockEvent.block()).thenReturn(blockSnapshot);
//        EventContext eventContext = EventContext.empty();
//        when(interactBlockEvent.context()).thenReturn(eventContext);
//        when(protectionManager.canInteractWithBlock(location, player, true)).thenReturn(ProtectionResult.ok());
//
//        playerInteractListener.onBlockInteract(interactBlockEvent, player);
//
//        verify(interactBlockEvent, times(0)).setCancelled(true);
//        verify(protectionManager, times(1)).canInteractWithBlock(any(), any(), anyBoolean());
//    }
}