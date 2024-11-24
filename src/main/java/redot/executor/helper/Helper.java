package redot.executor.helper;

import lombok.experimental.ExtensionMethod;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import redot.executor.model.ConcurrentExecutor;
import redot.executor.util.Extensions;
import redot.executor.util.TaskQueue;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/*
* This class exists solely to assist
* in writing code during runtime, so
* authors at least somewhat don't have
* to use intermediary/official mappings.
*/
@ExtensionMethod(Extensions.class)
public class Helper {

    private static final TaskQueue TASK_QUEUE = new TaskQueue();

    public static MinecraftClient getClientInstance() {
        return MinecraftClient.getInstance();
    }

    public static ClientPlayerEntity getPlayer() {
        return getClientInstance().player;
    }

    public static ClientPlayNetworkHandler getNetworkHandler() {
        return getClientInstance().getNetworkHandler();
    }

    public static ClientWorld getWorld() {
        return getClientInstance().world;
    }

    public static Collection<PlayerListEntry> getOnlinePlayers() {
        return getNetworkHandler().getPlayerList();
    }

    public static List<String> getOnlinePlayerNames() {
        return getOnlinePlayers().stream()
                .map(entry -> entry.getProfile().getName())
                .toList();
    }

    public static void sendMessage(final String message) {
        getPlayer().optional().ifPresentOrElse(
                player -> player.sendMessage(textLiteral(message)),
                () -> log("Player returned null!")
        );
    }

    public static void sendChat(final String chat) {
        getNetworkHandler().optional()
                .ifPresentOrElse(
                        nh -> nh.sendChatMessage(chat),
                        () -> log("NetworkHandler returned null!")
                );
    }

    public static void sendCommand(final String command) {
        MinecraftClient.getInstance().getNetworkHandler().optional()
                .ifPresentOrElse(
                        nh -> nh.sendChatCommand(command),
                        () -> log("NetworkHandler returned null!")
                );
    }

    public static Text textLiteral(String message) {
        return Text.literal(message);
    }

    public static void log(String message) {
        ConcurrentExecutor.getInstance().log(message);
    }

    public static UUID getUUID() {
        return getPlayer().getUuid();
    }

    public static void queueTask(Runnable task) {
        TASK_QUEUE.add(task);
    }

    public static void runTaskQueue() {
        TASK_QUEUE.runAll();
    }

}
