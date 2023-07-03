package session;

import io.netty.util.concurrent.ScheduledFuture;

public interface Session {

    String url();

    void registExecuteTask(ScheduledFuture executeTask);

    int heartBeatCount();

    boolean isOpen();

    void close();

    void refreshHeartBeat();

    void sendMessage(String message);
}
