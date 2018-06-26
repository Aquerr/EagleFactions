package io.github.aquerr.eaglefactions.commands.annotations;

import io.github.aquerr.eaglefactions.entities.FactionMemberType;

import java.lang.annotation.*;

@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiredRank
{
    FactionMemberType minimumRank() default FactionMemberType.RECRUIT;
}
