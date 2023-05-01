package io.github.aquerr.eaglefactions.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;

public class EFCommandDispatcher implements Command<CommandSourceStack>
{
    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        return 0;
    }
}
