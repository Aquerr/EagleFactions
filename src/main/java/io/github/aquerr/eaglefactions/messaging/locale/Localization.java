package io.github.aquerr.eaglefactions.messaging.locale;

import java.util.ResourceBundle;

public class Localization
{
    private final ResourceBundle resourceBundle;

    private Localization(ResourceBundle resourceBundle)
    {
        this.resourceBundle = resourceBundle;
    }

    public static Localization forResourceBundle(ResourceBundle resourceBundle)
    {
        return new Localization(resourceBundle);
    }

    public String getMessage(String messageKey)
    {
        return resourceBundle.getString(messageKey);
    }
}
