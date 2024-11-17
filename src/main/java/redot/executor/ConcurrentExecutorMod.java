package redot.executor;

import com.mojang.brigadier.CommandDispatcher;
import lombok.experimental.ExtensionMethod;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import redot.executor.gui.screen.ExecutorScreen;
import redot.executor.model.ConcurrentExecutor;
import redot.executor.util.Extensions;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

@ExtensionMethod(Extensions.class)
public class ConcurrentExecutorMod implements ModInitializer {

	@Override
	public void onInitialize() {
		ConcurrentExecutor.getInstance().consume(executor -> {
			executor.getLogger().info("Initialized");
			executor.setScreen(new ExecutorScreen("Concurrent Executor"));
			executor.updateClasspath();
		});
		ClientCommandRegistrationCallback.EVENT.register(this::registerCommand);
	}

	private void registerCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess cra) {
		dispatcher.register(literal("concurrentexecutor")
				.executes(context -> {
					MinecraftClient.getInstance().consume(client -> {
						client.send(() -> client.setScreen(ConcurrentExecutor.getInstance().getScreen()));
					});
					return 1;
				}));
	}

}