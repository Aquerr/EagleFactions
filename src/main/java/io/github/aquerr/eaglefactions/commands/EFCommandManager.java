package io.github.aquerr.eaglefactions.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.PluginPermissions;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.commands.general.HelpCommand;
import io.github.aquerr.eaglefactions.commands.management.CreateCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.server.permission.PermissionAPI;
import net.minecraftforge.server.permission.nodes.PermissionNode;
import net.minecraftforge.server.permission.nodes.PermissionTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

public final class EFCommandManager
{
    private final List<List<LiteralArgumentBuilder<CommandSourceStack>>> subcommands;
    private final EagleFactions eagleFactions;

    public EFCommandManager(EagleFactions eagleFactions)
    {
        this.eagleFactions = eagleFactions;
        this.subcommands = new ArrayList<>();
    }

    public void initializeCommands(RegisterCommandsEvent event)
    {
        // Help Command
        HelpCommand helpCommandExecutor = new HelpCommand(this.eagleFactions);
        subcommands.add(createCommandLiterals(List.of("help"), PluginPermissions.HELP_COMMAND, helpCommandExecutor, (literalArgumentBuilder, command) -> literalArgumentBuilder));

        // Create Command
        subcommands.add(createCommandLiterals(List.of("create", "c"),
                PluginPermissions.CREATE_COMMAND,
                new CreateCommand(this.eagleFactions),
                (literalArgumentBuilder, command) -> {
                    literalArgumentBuilder.then(Commands.argument("tag", StringArgumentType.word())
                            .then(Commands.argument("name", StringArgumentType.word()).executes(command)));
                    return literalArgumentBuilder;
                }
        ));

        // Eagle Factions Command - Root
        LiteralArgumentBuilder<CommandSourceStack> eagleFactionsCommand = Commands.literal("f").executes(helpCommandExecutor);
        subcommands.forEach(subcommandLiterals -> subcommandLiterals.forEach(eagleFactionsCommand::then));

        event.getDispatcher().register(eagleFactionsCommand);
    }

    private List<LiteralArgumentBuilder<CommandSourceStack>> createCommandLiterals(List<String> aliases,
                                                                                   String permission,
                                                                                   AbstractCommand command,
                                                                                   BiFunction<LiteralArgumentBuilder, AbstractCommand, LiteralArgumentBuilder> argumentPopulator)
    {
        return aliases.stream()
                .map(Commands::literal)
                .map(commandLiteral -> commandLiteral.requires(commandSourceStack -> hasPermission(commandSourceStack, permission)))
                .map(commandLiteral -> commandLiteral.executes(command))
                .peek(commandLiteral -> argumentPopulator.apply(commandLiteral, command))
                .toList();
    }

    private boolean hasPermission(CommandSourceStack commandSourceStack, String permissionNode)
    {
        if (!commandSourceStack.isPlayer())
            return true;
        else return !"".equals(PermissionAPI.getPermission(commandSourceStack.getPlayer(), new PermissionNode<>(PluginInfo.ID, permissionNode, PermissionTypes.STRING, (player, playerUUID, context) -> "")));
    }
}
