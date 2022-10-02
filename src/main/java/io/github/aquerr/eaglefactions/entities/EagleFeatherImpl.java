package io.github.aquerr.eaglefactions.entities;

import io.github.aquerr.eaglefactions.api.entities.EagleFeather;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.spongepowered.api.data.Keys;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static net.kyori.adventure.text.format.NamedTextColor.DARK_PURPLE;

public class EagleFeatherImpl implements EagleFeather
{
    public static final TextComponent EAGLE_FEATHER_DISPLAY_NAME = Component.text("Eagle's Feather", DARK_PURPLE);

    @Override
    public ItemStack asItemStack()
    {
        List<Component> eagleFeatherLore = new ArrayList<>();
        eagleFeatherLore.add(Component.text("A mystical feather that comes from")
                .append(Component.text(" The Greatest Eagle", Style.style(NamedTextColor.YELLOW, TextDecoration.BOLD, TextDecoration.ITALIC))));
        eagleFeatherLore.add(Component.newline());
        eagleFeatherLore.add(Component.text("It looks like a key. Maybe it could be used somewhere?", Style.style(NamedTextColor.GRAY, TextDecoration.ITALIC)));

        return ItemStack.builder()
                .itemType(ItemTypes.FEATHER)
                .quantity(1)
                .add(Keys.CUSTOM_NAME, EAGLE_FEATHER_DISPLAY_NAME)
                .add(Keys.LORE, eagleFeatherLore)
                .build();
    }
}
