package bootstrap;

import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;

import java.util.concurrent.ThreadPoolExecutor;

public class WebSocketExecutorContext {

    private static volatile WebSocketExecutorContext instance;
    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup msgHandleGroup;
    private DefaultEventLoopGroup heartBeatGroup;
    private DefaultEventLoopGroup taskScheduleGroup;
    private ThreadPoolExecutor taskExecutor;

    private WebSocketExecutorContext() {
    }

    static WebSocketExecutorContext init() {
        WebSocketExecutorContext instance = getInstance();
        instance.bossGroup = new NioEventLoopGroup(WebSocketConfig.BossThreads());
        instance.msgHandleGroup = new NioEventLoopGroup(WebSocketConfig.MsgHandleThreads());
        instance.heartBeatGroup = new DefaultEventLoopGroup(WebSocketConfig.HeartBeatThreads());
        instance.taskScheduleGroup = new DefaultEventLoopGroup(WebSocketConfig.TaskScheduleThreads());
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

    public NioEventLoopGroup bossGroup() {
        return this.bossGroup;
    }

    public NioEventLoopGroup msgHandleGroup() {
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
