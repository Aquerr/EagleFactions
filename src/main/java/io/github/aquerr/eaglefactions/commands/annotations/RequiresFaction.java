package io.github.aquerr.eaglefactions.commands.annotations;

import java.lang.annotation.*;

@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresFaction
{
    boolean value() default true;
}
