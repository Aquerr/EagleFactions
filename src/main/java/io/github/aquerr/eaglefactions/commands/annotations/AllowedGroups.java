package io.github.aquerr.eaglefactions.commands.annotations;

import io.github.aquerr.eaglefactions.commands.enums.CommandUser;

import java.lang.annotation.*;

@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowedGroups
{
    CommandUser[] getGroups() default CommandUser.PLAYER;
}
