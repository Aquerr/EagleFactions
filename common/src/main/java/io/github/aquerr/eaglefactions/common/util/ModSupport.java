package io.github.aquerr.eaglefactions.common.util;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.cause.entity.damage.source.EntityDamageSource;

public class ModSupport
{
    /**
     * Checks if the given class comes from TechGuns
     * @param clazz class to check
     * @param <T> the type of class
     * @return <tt>true</tt> if class comes from TechGuns, <tt>false</tt> if not.
     */
    public static <T> boolean isTechGuns(final Class<T> clazz)
    {
        return clazz.getName().contains("techguns");
    }

    /**
     * Gets the attacking entity from the EntityDamageSource. EntityDamageSource must come from TechGuns.
     * @param entityDamageSource the source
     * @return attacking/source entity.
     */
    public static Entity getAttackerFromTechGuns(final EntityDamageSource entityDamageSource)
    {
        Object attacker = null;
        try
        {
            attacker = entityDamageSource.getClass().getField("attacker").get(entityDamageSource);
        }
        catch (IllegalAccessException | NoSuchFieldException e)
        {
            e.printStackTrace();
        }
        if (attacker instanceof Entity)
            return (Entity)attacker;
        else return null;
    }
}
