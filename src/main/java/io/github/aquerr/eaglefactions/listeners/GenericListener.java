package io.github.aquerr.eaglefactions.listeners;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.caching.FactionsCache;
import io.github.aquerr.eaglefactions.config.Settings;
import org.reflections.Reflections;
import org.spongepowered.api.event.EventManager;

import java.util.Set;

public abstract class GenericListener
{

    protected FactionsCache cache;
    protected Settings settings;

    @Inject
    GenericListener(FactionsCache cache, Settings settings, EagleFactions eagleFactions, EventManager eventManager)
    {
        this.cache = cache;
        this.settings = settings;
        eventManager.registerListeners(eagleFactions, this);
    }

    public static void initListeners(){
        Reflections reflections = new Reflections("io.github.aquerr");
        Set<Class<? extends GenericListener>> subTypes = reflections.getSubTypesOf(GenericListener.class);
        Injector injector = Guice.createInjector();
        for(Class<? extends GenericListener> listener : subTypes){
            System.out.println(listener.getCanonicalName());
            injector.getInstance(listener);
        }
    }
}
