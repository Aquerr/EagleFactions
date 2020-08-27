package io.github.aquerr.eaglefactions.common.messaging;

import com.google.inject.Singleton;

@Singleton
public class Placeholders
{
    public static final Placeholder PLAYER_NAME = new Placeholder("%PLAYER_NAME%");
    public static final Placeholder FACTION_NAME = new Placeholder("%FACTION_NAME%");
    public static final Placeholder FACTION_TAG = new Placeholder("%FACTION_TAG%");
    public static final Placeholder POWER = new Placeholder("%POWER%");
    public static final Placeholder NUMBER = new Placeholder("%NUMBER%");

    public static final Placeholder PLAYER = new Placeholder("%PLAYER%");

    public static final Placeholder CLAIM = new Placeholder("%CLAIM%");
    public static final Placeholder COORDS = new Placeholder("%COORDS%");

    public static final Placeholder MEMBER_TYPE = new Placeholder("%MEMBER_TYPE%");

    public static final Placeholder[] PLACEHOLDERS = {PLAYER_NAME, FACTION_NAME, FACTION_TAG, POWER, NUMBER, PLAYER, CLAIM, MEMBER_TYPE};
}
