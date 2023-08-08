package bootstrap;

import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.Future;

import java.util.concurrent.ThreadPoolExecutor;

public class WebSocketExecutorContext {

    private static volatile WebSocketExecutorContext instance;
    private EventLoopGroup bossGroup;
    private EventLoopGroup msgHandleGroup;
    private DefaultEventLoopGroup heartBeatGroup;
    private DefaultEventLoopGroup taskScheduleGroup;
    private ThreadPoolExecutor taskExecutor;

    private WebSocketExecutorContext() {
    }

    static WebSocketExecutorContext init() {
        WebSocketExecutorContext instance = getInstance();
        if (System.getProperty("os.name").startsWith("Linux")) {
            instance.bossGroup = new EpollEventLoopGroup(WebSocketConfig.BossThreads(),
                    new DefaultThreadFactory("ws-netty-boss"));
            instance.msgHandleGroup = new EpollEventLoopGroup(WebSocketConfig.MsgHandleThreads(),
                    new DefaultThreadFactory("ws-netty-msgHandle"));
        } else {
            instance.bossGroup = new NioEventLoopGroup(WebSocketConfig.BossThreads(),
                    new DefaultThreadFactory("ws-netty-boss"));
            instance.msgHandleGroup = new NioEventLoopGroup(WebSocketConfig.MsgHandleThreads(),
                    new DefaultThreadFactory("ws-netty-msgHandle"));
        }
        instance.heartBeatGroup = new DefaultEventLoopGroup(WebSocketConfig.HeartBeatThreads(),
                new DefaultThreadFactory("ws-netty-heartBeat"));
        instance.taskScheduleGroup = new DefaultEventLoopGroup(WebSocketConfig.TaskScheduleThreads(),
                new DefaultThreadFactory("ws-netty-schedule"));
        instance.taskExecutor = WebSocketConfig.TaskExecutor();
        return instance;
    }

    public static WebSocketExecutorContext getInstance() {
        if (instance == null) {
            synchronized (WebSocketExecutorContext.class) {
                if (instance == null) {
                    instance = new WebSocketExecutorContext();
                }
            }
        }
        return instance;
    }

    public EventLoopGroup bossGroup() {
        return this.bossGroup;
    }

    public EventLoopGroup msgHandleGroup() {
        return this.msgHandleGroup;
    }

    public DefaultEventLoopGroup heartBeatGroup() {
        return this.heartBeatGroup;
    }

    public DefaultEventLoopGroup taskScheduleGroup() {
        return this.taskScheduleGroup;
    }

    public ThreadPoolExecutor taskExecutor() {
        return this.taskExecutor;
    }

    Future<?>[] shutdown() {
        Future[] futures = new Future[4];
        futures[0] = this.bossGroup.shutdownGracefully();
        futures[1] = this.msgHandleGroup.shutdownGracefully();
        futures[2] = this.heartBeatGroup.shutdownGracefully();
        futures[3] = this.taskScheduleGroup.shutdownGracefully();
        this.taskExecutor.shutdown();
        return futures;
    }
}
