package com.rs256.crossPatch.command;

import com.rs256.crossPatch.CrossPatch;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.*;

public class TemplateModCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext) {
        dispatcher.register(
                Commands.literal(CrossPatch.MOD_ID)
                        .then(Commands.literal("reload")
                                .executes(commandContext -> executeReload())
                        )
        );
    }

    private static int executeReload() {
        return 1;
    }
}