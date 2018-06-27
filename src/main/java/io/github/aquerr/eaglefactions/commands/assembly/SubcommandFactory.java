package io.github.aquerr.eaglefactions.commands.assembly;

import com.google.inject.*;
import com.google.inject.name.Named;
import io.github.aquerr.eaglefactions.commands.annotations.GameTypes;
import io.github.aquerr.eaglefactions.commands.annotations.Subcommand;
import io.github.aquerr.eaglefactions.config.Settings;
import io.github.aquerr.eaglefactions.config.enums.GameType;
import org.reflections.Reflections;
import org.spongepowered.api.command.args.CommandElement;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.text.Text;

import java.util.*;

@Singleton
public class SubcommandFactory extends AbstractModule
{
    private Map<List<String>, CommandSpec> subcommands = new HashMap<>();

    @Inject
    SubcommandFactory(@Named("main injector") Injector injector, Settings settings)
    {
        Reflections reflections = new Reflections("io.github.aquerr.eaglefactions");
        Set<Class<?>> subTypes = reflections.getTypesAnnotatedWith(Subcommand.class);
        GameType currentType = settings.getGameType();
        for (Class<?> listener : subTypes)
        {
            Subcommand instance = (Subcommand) injector.getInstance(listener);
            if(instance instanceof GameTypes){
                if(!Arrays.asList(((GameTypes) instance).types()).contains(currentType)){
                    continue;
                }
            }
            CommandSpec.Builder builder = CommandSpec.builder()
                    .description(Text.of(instance.description()))
                    .permission(instance.permission())
                    .executor((CommandExecutor) instance);
            if (instance.arguments().length > 0)
            {
                CommandElement[] elements = new CommandElement[instance.arguments().length];
                for(int i = 0; i < elements.length; i++){
                    elements[i] = instance.arguments()[i].toCommandElement();
                }
                builder.arguments(elements);
            }
            subcommands.put(Arrays.asList(instance.aliases()), builder.build());
        }
    }

    @Override
    protected void configure()
    {
    }

    @Provides
    @Named("subcommands")
    Map<List<String>, CommandSpec> getSubcommands()
    {
        return subcommands;
    }
}
