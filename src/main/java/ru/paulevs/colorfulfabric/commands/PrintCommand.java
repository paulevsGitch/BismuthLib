package ru.paulevs.colorfulfabric.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.paulevs.colorfulfabric.ColorfulFabricClient;

import java.util.HashSet;
import java.util.Set;

public class PrintCommand {
	private static final Logger LOGGER = LoggerFactory.getLogger(ColorfulFabricClient.MOD_ID);
	
	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(((Commands.literal("cfprint")).executes(PrintCommand::process)));
	}
	
	private static int process(CommandContext<CommandSourceStack> commandSourceStackCommandContext) {
		Set<Block> blocks = new HashSet<>();
		Registry.BLOCK.forEach(block ->
			block.getStateDefinition().getPossibleStates().stream().filter(
				state -> state.getLightEmission() > 0).forEach(state -> blocks.add(block)
			)
		);
		blocks.forEach(block -> {
			ResourceLocation id = Registry.BLOCK.getKey(block);
			LOGGER.info(id.toString());
		});
		return 0;
	}
}
