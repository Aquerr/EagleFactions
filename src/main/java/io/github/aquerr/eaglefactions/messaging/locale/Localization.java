package io.github.aquerr.eaglefactions.messaging.locale;

import java.util.Locale;
import java.util.ResourceBundle;

public class Localization
{
    private static final String RESOURCE_BASE_NAME = "lang/messages";

    private final ResourceBundle resourceBundle;

    public static Localization getDefault()
    {
        return forTag("en_US");
    }

    public static Localization forTag(String languageTag)
    {
        if (languageTag == null || languageTag.isBlank())
        {
            throw new IllegalArgumentException("languageTag cannot be empty!");
        }

        Locale locale = Locale.forLanguageTag(languageTag);
        ResourceBundle resourceBundle;
        try
        {
            resourceBundle = ResourceBundle.getBundle(RESOURCE_BASE_NAME, locale);
        }
        catch (Exception exception)
        {
            try
            {
                resourceBundle = ResourceBundle.getBundle(RESOURCE_BASE_NAME, Locale.US);
            }
            catch (Exception exception1)
            {
                exception1.addSuppressed(exception);
                throw new IllegalStateException(exception1);
            }
        }
        return new Localization(resourceBundle);
    }

    private Localization(ResourceBundle resourceBundle)
    {
        this.resourceBundle = resourceBundle;
    }

    public String getMessage(String messageKey)
    {
        return resourceBundle.getString(messageKey);
    }
}
