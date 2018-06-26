package io.github.aquerr.eaglefactions.commands.annotations;

import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.commands.enums.BasicCommandArgument;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Subcommand
{
    String[] aliases();

    String description();

    BasicCommandArgument[] arguments() default {};

    String permission();

}
