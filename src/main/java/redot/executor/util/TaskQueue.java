package redot.executor.util;

import java.util.LinkedList;
import java.util.Queue;

public class TaskQueue {

    private final Queue<Runnable> queue = new LinkedList<>();

    public void add(Runnable task) {
        this.queue.add(task);
    }

    public void runAll() {
        while (!this.queue.isEmpty()) {
            this.runFirst();
        }
    }

    public void runFirst() {
        Runnable task = this.queue.poll();
        if (task == null) return;
        task.run();
    }

}
