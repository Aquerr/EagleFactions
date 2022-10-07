package io.github.aquerr.eaglefactions.entities;

import io.github.aquerr.eaglefactions.EagleFactionsPlugin;
import io.github.aquerr.eaglefactions.api.entities.EagleFeather;
import io.github.aquerr.eaglefactions.api.messaging.MessageService;
import net.kyori.adventure.text.Component;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EagleFeatherImpl implements EagleFeather
{
    @Override
    public ItemStack asItemStack()
    {
        MessageService messageService = EagleFactionsPlugin.getPlugin().getMessageService();
        List<Component> eagleFeatherLore = new ArrayList<>();
        eagleFeatherLore.add(messageService.resolveComponentWithMessage("eaglefeather.lore.line1"));
        eagleFeatherLore.add(messageService.resolveComponentWithMessage("eaglefeather.lore.line2"));

        ItemStack itemStack = ItemStack.builder()
                .itemType(ItemTypes.FEATHER)
                .quantity(1)
                .add(Keys.CUSTOM_NAME, messageService.resolveComponentWithMessage("eaglefeather.name"))
                .add(Keys.LORE, eagleFeatherLore)
                .build();
        itemStack.offer(EagleFactionsPlugin.IS_EAGLE_FEATHER_KEY, true);
        return itemStack;
    }
}
