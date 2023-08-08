package session;

import bootstrap.WebSocketConfig;
import bootstrap.WebSocketExecutorContext;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.ScheduledFuture;

public class WebSocketSession implements Session {

    private volatile boolean status;
    private String url;
    private Channel channel;
    private ScheduledFuture heartBeatTask;
    private ScheduledFuture executeTask;
    private volatile int heartBeatCount;

    WebSocketSession(Channel channel, String url) {
        this.status = true;
        this.url = url;
        this.channel = channel;
        this.heartBeatTask = createHeartBeatTask();
    }

    @Override
    public String url() {
        return this.url;
    }

    @Override
    public void registExecuteTask(ScheduledFuture executeTask) {
        if (this.executeTask != null) {
            this.executeTask.cancel(true);
        }
        this.executeTask = executeTask;
    }

    @Override
    public int heartBeatCount() {
        return this.heartBeatCount;
    }

    @Override
    public boolean isOpen() {
        return this.status;
    }

    @Override
    public void close() {
        this.status = false;
        this.heartBeatTask.cancel(true);
        if (this.executeTask != null) {
            this.executeTask.cancel(true);
        }
        this.heartBeatCount = 0;
        if (this.channel.isOpen()) {
            synchronized (this) {
                if (this.channel.isOpen()) {
                    this.channel.close();
                }
            }
        }
    }

    @Override
    public void refreshHeartBeat() {
        this.heartBeatCount = 0;
    }

    @Override
    public void sendMessage(String message) {
        this.channel.writeAndFlush(new TextWebSocketFrame(message));
    }

    private ScheduledFuture createHeartBeatTask() {
        return WebSocketExecutorContext.getInstance().heartBeatGroup().scheduleAtFixedRate(() -> {
            this.channel.writeAndFlush(new PingWebSocketFrame());
            this.heartBeatCount++;
            if (this.heartBeatCount > WebSocketConfig.TimeoutHeartBeatCount()) {
                close();
            }
        }, 0, WebSocketConfig.HeartBeatPeriod(), WebSocketConfig.HeartBeatTimeUnit());
    }
}
