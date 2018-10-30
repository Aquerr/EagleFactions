package io.github.aquerr.eaglefactions.entities;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

import java.util.ArrayList;
import java.util.List;

public final class EagleFeather
{
    private EagleFeather()
    {

    }

    public static Text getDisplayName()
    {
        return Text.of(TextColors.DARK_PURPLE, "Eagle's Feather");
    }

    public static ItemStack getEagleFeatherItem()
    {
        List<Text> eagleFeatherLore = new ArrayList<>();
        eagleFeatherLore.add(Text.of("A mystical feather that comes from", TextStyles.BOLD, TextStyles.ITALIC, TextColors.YELLOW, " The Greatest Eagle"));
        eagleFeatherLore.add(Text.of());
        eagleFeatherLore.add(Text.of(TextColors.GRAY, TextStyles.ITALIC, "It looks like a key. Maybe it could be used somewhere?"));

        return ItemStack.builder()
               .itemType(ItemTypes.FEATHER)
               .quantity(1)
               .add(Keys.DISPLAY_NAME, getDisplayName())
               .add(Keys.ITEM_LORE, eagleFeatherLore)
               .build();
    }
}
