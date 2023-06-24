package io.github.aquerr.eaglefactions.commands.general;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.commands.AbstractCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class HelpCommand extends AbstractCommand
{
    public HelpCommand(EagleFactions eagleFactions)
    {
        super(eagleFactions);
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        context.getSource().sendSystemMessage(Component.literal("Help command from Eagle Factions!"));
        return 0;
    }
}
