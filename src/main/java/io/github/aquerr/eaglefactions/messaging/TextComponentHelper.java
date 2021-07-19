package io.github.aquerr.eaglefactions.messaging;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class TextComponentHelper
{
    public static String toPlain(Component component)
    {
        return PlainTextComponentSerializer.plainText().serialize(component);
    }

    public static TextComponent toComponent(String message)
    {
        return PlainTextComponentSerializer.plainText().deserialize(message);
    }
}
