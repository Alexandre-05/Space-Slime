package fr.alex96x2.admin.storage;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public abstract class AbstractDataRepository implements DataRepository {

    private final ExecutorService executor;

    protected AbstractDataRepository(int poolSize) {
        this.executor = Executors.newFixedThreadPool(Math.max(2, poolSize));
    }

    @Override
    public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, executor);
    }

    @Override
    public CompletableFuture<Void> runAsync(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, executor);
    }

    @Override
    public void close() {
        executor.shutdown();
    }
}
