package com.example.command;

import com.example.TemplateMod;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.*;
import net.minecraft.network.chat.*;

public class TemplateModCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext) {
        dispatcher.register(
                Commands.literal(TemplateMod.MOD_ID)
                        .then(Commands.literal("reload")
                                .executes(commandContext -> executeReload())
                        )
        );
    }

    private static int executeReload() {
        return 1;
    }
}