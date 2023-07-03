package bootstrap;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public interface Bootstrap {
    WebSocketServerBootstrap bossThreads(int threads);
    WebSocketServerBootstrap msgHandleThreads(int threads);
    WebSocketServerBootstrap heartBeatThreads(int threads);
    WebSocketServerBootstrap taskScheduleThreads(int threads);
    WebSocketServerBootstrap taskExecutor(ThreadPoolExecutor taskExecutor);
    WebSocketServerBootstrap port(int port);
    WebSocketServerBootstrap heartBeatPeriod(long heartBeatPeriod);
    WebSocketServerBootstrap heartBeatTimeUnit(TimeUnit heartBeatTimeUnit);
    WebSocketServerBootstrap timeoutHeartBeatCount(int timeoutHeartBeatCount);
    WebSocketServerBootstrap start();
    WebSocketServerBootstrap shutdown();
}
