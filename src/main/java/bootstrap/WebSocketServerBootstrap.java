package bootstrap;

import channal.ShortIdNioServerSocketChannel;
import handler.BusinessHandler;
import handler.WebSocketServerProtocolEnhancingHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import javax.annotation.PostConstruct;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author chengengwei
 * @description websocket-netty服务启动类
 * @date 2023/6/16
 */
public class WebSocketServerBootstrap implements Bootstrap {

    private static InternalLogger logger = InternalLoggerFactory.getInstance(WebSocketServerBootstrap.class);

    private static volatile WebSocketServerBootstrap instance;

    private WebSocketExecutorContext executorContext;

    private volatile boolean started;

    private Thread closeFutureThread;

    private WebSocketServerBootstrap() {
        this.started = false;
    }

    public static WebSocketServerBootstrap getInstance() {
        if (instance == null) {
            synchronized (WebSocketServerBootstrap.class) {
                if (instance == null) {
                    instance = new WebSocketServerBootstrap();
                }
            }
        }
        return instance;
    }

    @Override
    public WebSocketServerBootstrap bossThreads(int threads) {
        WebSocketConfig.setBossThreads(threads);
        return this;
    }

    @Override
    public WebSocketServerBootstrap msgHandleThreads(int threads) {
        WebSocketConfig.setMsgHandleThreads(threads);
        return this;
    }

    @Override
    public WebSocketServerBootstrap heartBeatThreads(int threads) {
        WebSocketConfig.setHeartBeatThreads(threads);
        return this;
    }

    @Override
    public WebSocketServerBootstrap taskScheduleThreads(int threads) {
        WebSocketConfig.setTaskScheduleThreads(threads);
        return this;
    }

    @Override
    public WebSocketServerBootstrap taskExecutor(ThreadPoolExecutor taskExecutor) {
        WebSocketConfig.setTaskExecutor(taskExecutor);
        return this;
    }

    @Override
    public WebSocketServerBootstrap port(int port) {
        WebSocketConfig.setPort(port);
        return this;
    }

    @Override
    public WebSocketServerBootstrap heartBeatPeriod(long heartBeatPeriod) {
        WebSocketConfig.setHeartBeatPeriod(heartBeatPeriod);
        return this;
    }

    @Override
    public WebSocketServerBootstrap heartBeatTimeUnit(TimeUnit heartBeatTimeUnit) {
        WebSocketConfig.setHeartBeatTimeUnit(heartBeatTimeUnit);
        return this;
    }

    @Override
    public WebSocketServerBootstrap timeoutHeartBeatCount(int timeoutHeartBeatCount) {
        WebSocketConfig.setTimeoutHeartBeatCount(timeoutHeartBeatCount);
        return this;
    }

    @Override
    @PostConstruct
    public synchronized WebSocketServerBootstrap start() {
        if (this.started) {
            return this;
        }
        this.executorContext = WebSocketExecutorContext.init();
        try {
            WebSocketServerProtocolEnhancingHandler protocolEnhancingHandler = new WebSocketServerProtocolEnhancingHandler();
            BusinessHandler businessHandler = new BusinessHandler();
            ChannelFuture future = new ServerBootstrap()
                    .group(this.executorContext.bossGroup(), this.executorContext.msgHandleGroup())
                    .channel(ShortIdNioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel channel) throws Exception {
                            channel.pipeline()
                                    .addLast(new HttpServerCodec())
                                    .addLast(new ChunkedWriteHandler())
                                    .addLast(new HttpObjectAggregator(65536))
                                    .addLast(protocolEnhancingHandler)
                                    .addLast(businessHandler);
                        }
                    })
                    .bind(WebSocketConfig.Port())
                    .sync();
            logger.info("websocket server opened");
            this.closeFutureThread = new Thread(() -> {
                try {
                    future.channel().closeFuture().sync();
                } catch (InterruptedException e) {
                    logger.error(e);
                } finally {
                    executorContext.shutdown();
                    logger.info("websocket server closed");
                }
            });
            this.closeFutureThread.start();
        } catch (InterruptedException e) {
            logger.error(e);
            executorContext.shutdown();
        }
        this.started = true;
        return this;
    }

    @Override
    public synchronized WebSocketServerBootstrap shutdown() {
        this.executorContext.shutdown();
        return this;
    }
}

