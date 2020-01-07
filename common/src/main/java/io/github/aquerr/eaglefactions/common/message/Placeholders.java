package io.github.aquerr.eaglefactions.common.message;

import com.google.inject.Singleton;

@Singleton
public class Placeholders
{
    public static final Placeholder PLAYER_NAME = new Placeholder("%PLAYER_NAME%");
    public static final Placeholder FACTION_NAME = new Placeholder("%FACTION_NAME%");
    public static final Placeholder POWER = new Placeholder("%POWER%");
    public static final Placeholder NUMBER = new Placeholder("%NUMBER%");

    public static final Placeholder PLAYER = new Placeholder("%PLAYER%");

    public static final Placeholder[] PLACEHOLDERS = {PLAYER_NAME, FACTION_NAME, POWER, NUMBER, PLAYER};

    public static String fillPlaceholders(String text, Object objectToGetPlaceholdersFrom)
    {
//        if(text.contains())
        return "";
    }

    public static class Placeholder
    {
        private final String placeholder;

        Placeholder(final String placeholder)
        {
            this.placeholder = placeholder;
        }

        public String getPlaceholder()
        {
            return this.placeholder;
        }

        @Override
        public String toString()
        {
            return this.placeholder;
        }
    }
}
