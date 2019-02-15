package cc.blynk.server.core;

import cc.blynk.utils.BlynkTPFactory;

import java.io.Closeable;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Wrapper around ThreadPoolExecutor that should perform blocking IO operations.
 * Due to async nature of netty performing Blocking operations withing netty pipeline
 * will cause performance issues. So Blocking operations should always
 * executed via this wrapper.
 *
 * The Blynk Project.
 * Created by Dmitriy Dumanskiy.
 * Created on 07.04.15.
 */
public class BlockingIOProcessor implements Closeable {

    private static final int MINIMUM_ALLOWED_POOL_SIZE = 3;

    //pool for messaging
    public final ThreadPoolExecutor messagingExecutor;

    //DB pool is needed as in case DB goes down messaging still should work
    public final ThreadPoolExecutor dbExecutor;
    //separate pool for history graph data
    public final ThreadPoolExecutor dbReportingExecutor;
    public final ThreadPoolExecutor dbGetServerExecutor;
    private final ThreadPoolExecutor dbReportingEventExecutor;

    public BlockingIOProcessor(int poolSize, int maxQueueSize) {
        //pool size can't be less than 3.
        poolSize = Math.max(MINIMUM_ALLOWED_POOL_SIZE, poolSize);
        this.messagingExecutor = new ThreadPoolExecutor(
                poolSize / 4, poolSize / 3,
                2L, TimeUnit.MINUTES,
                new ArrayBlockingQueue<>(maxQueueSize),
                BlynkTPFactory.build("Messaging")
        );

        this.dbExecutor = new ThreadPoolExecutor(
                poolSize / 3,
                poolSize / 2, 2L,
                TimeUnit.MINUTES,
                new ArrayBlockingQueue<>(250),
                BlynkTPFactory.build("db"));
        this.dbReportingEventExecutor = new ThreadPoolExecutor(
                1,
                1,
                10L,
                TimeUnit.MINUTES,
                new ArrayBlockingQueue<>(1000),
                BlynkTPFactory.build("reportingEvents"));

        this.dbGetServerExecutor = new ThreadPoolExecutor(poolSize / 3, poolSize / 3, 2L,
                TimeUnit.MINUTES, new ArrayBlockingQueue<>(250),
                BlynkTPFactory.build("getServer"));

        this.dbReportingExecutor = new ThreadPoolExecutor(poolSize / 4, poolSize / 2, 2L,
                TimeUnit.MINUTES, new ArrayBlockingQueue<>(250),
                BlynkTPFactory.build("reporting"));
    }

    public void execute(Runnable task) {
        messagingExecutor.execute(task);
    }

    public void executeDB(Runnable task) {
        dbExecutor.execute(task);
    }

    public void executeReportingEvent(Runnable task) {
        dbReportingEventExecutor.execute(task);
    }

    public void executeReporting(Runnable task) {
        dbReportingExecutor.execute(task);
    }

    public void executeDBGetServer(Runnable task) {
        dbGetServerExecutor.execute(task);
    }

    @Override
    public void close() {
        dbExecutor.shutdown();
        messagingExecutor.shutdown();
        dbReportingEventExecutor.shutdown();
        dbReportingExecutor.shutdown();
        dbGetServerExecutor.shutdown();
    }
}
