package bootstrap;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class WebSocketConfig {
    private static int bossThreads = 1;
    private static int msgHandleThreads = 5;
    private static int heartBeatThreads = 5;
    private static int taskScheduleThreads = 10;
    private static ThreadPoolExecutor taskExecutor = new ThreadPoolExecutor(
            20,
            100,
            10,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.DiscardOldestPolicy());
    private static int port;
    private static long heartBeatPeriod = 10;
    private static TimeUnit heartBeatTimeUnit = TimeUnit.SECONDS;
    private static int timeoutHeartBeatCount = 5;

    public static int BossThreads() {
        return bossThreads;
    }

    static void setBossThreads(int bossThreads) {
        WebSocketConfig.bossThreads = bossThreads;
    }

    public static int MsgHandleThreads() {
        return msgHandleThreads;
    }

    static void setMsgHandleThreads(int msgHandleThreads) {
        WebSocketConfig.msgHandleThreads = msgHandleThreads;
    }

    public static int HeartBeatThreads() {
        return heartBeatThreads;
    }

    static void setHeartBeatThreads(int heartBeatThreads) {
        WebSocketConfig.heartBeatThreads = heartBeatThreads;
    }

    public static int TaskScheduleThreads() {
        return taskScheduleThreads;
    }

    static void setTaskScheduleThreads(int taskScheduleThreads) {
        WebSocketConfig.taskScheduleThreads = taskScheduleThreads;
    }

    public static ThreadPoolExecutor TaskExecutor() {
        return taskExecutor;
    }

    static void setTaskExecutor(ThreadPoolExecutor taskExecutor) {
        WebSocketConfig.taskExecutor = taskExecutor;
    }

    public static int Port() {
        return port;
    }

    static void setPort(int port) {
        WebSocketConfig.port = port;
    }

    public static long HeartBeatPeriod() {
        return heartBeatPeriod;
    }

    static void setHeartBeatPeriod(long heartBeatPeriod) {
        WebSocketConfig.heartBeatPeriod = heartBeatPeriod;
    }

    public static TimeUnit HeartBeatTimeUnit() {
        return heartBeatTimeUnit;
    }

    static void setHeartBeatTimeUnit(TimeUnit heartBeatTimeUnit) {
        WebSocketConfig.heartBeatTimeUnit = heartBeatTimeUnit;
    }

    public static int TimeoutHeartBeatCount() {
        return timeoutHeartBeatCount;
    }

    static void setTimeoutHeartBeatCount(int timeoutHeartBeatCount) {
        WebSocketConfig.timeoutHeartBeatCount = timeoutHeartBeatCount;
    }
}
