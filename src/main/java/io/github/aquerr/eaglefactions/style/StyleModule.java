package io.github.aquerr.eaglefactions.style;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.github.aquerr.eaglefactions.config.Settings;
import io.github.aquerr.eaglefactions.style.implementation.EagleStyle;
import io.github.aquerr.eaglefactions.style.implementation.MassivecraftStyle;

@Singleton
public class StyleModule extends AbstractModule
{
    private Settings settings;

    @Inject
    StyleModule(Settings settings)
    {
        this.settings = settings;
    }

    @Override
    protected void configure()
    {
        bind(StyleLayout.class).to(getStyle());
    }

    private Class<? extends StyleLayout> getStyle()
    {
        switch (settings.getGameType())
        {
            case FACTIONS:
                return MassivecraftStyle.class;
            case WAR:
                return EagleStyle.class;
            case PROTECTION:
                return EagleStyle.class;
            default:
                return EagleStyle.class;
        }
    }
}
