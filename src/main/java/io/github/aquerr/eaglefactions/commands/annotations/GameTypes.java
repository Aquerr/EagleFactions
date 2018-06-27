package io.github.aquerr.eaglefactions.commands.annotations;

import io.github.aquerr.eaglefactions.config.enums.GameType;

import java.lang.annotation.*;

@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GameTypes
{
    GameType[] types() default {GameType.FACTIONS, GameType.PROTECTION, GameType.WAR};
}
