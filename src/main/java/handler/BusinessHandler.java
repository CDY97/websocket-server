package handler;

import bootstrap.WebSocketExecutorContext;
import com.alibaba.fastjson.JSONObject;
import controller.BaseWebSocketController;
import controller.ControllerMap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.ScheduledFuture;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import session.SessionContext;
import session.WebSocketSession;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author chengengwei
 * @description 业务处理器
 * @date 2023/6/16
 */
@ChannelHandler.Sharable
public class BusinessHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static InternalLogger logger = InternalLoggerFactory.getInstance(BusinessHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
        try {
            WebSocketSession session = SessionContext.getSession(ctx.channel().id().asShortText());
            String text = msg.text();
            startScheduledTask(session, text);
        } catch (Exception e) {
            logger.error(e);
            try {
                ctx.channel().writeAndFlush(new TextWebSocketFrame("请求异常")).sync();
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            } finally {
                ctx.close();
            }
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        logger.info("channal[{}]开启", ctx.channel().id().asShortText());
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        String sessionId = ctx.channel().id().asShortText();
        SessionContext.cancel(sessionId);
        logger.info("channal[{}]关闭", ctx.channel().id().asShortText());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause);
        ctx.close();
    }

    /**
     * 创建定时任务
     * @param session
     * @param text
     * @throws CloneNotSupportedException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private void startScheduledTask(WebSocketSession session, String text) throws CloneNotSupportedException, ExecutionException, InterruptedException {
        ThreadPoolExecutor taskExecutor = WebSocketExecutorContext.getInstance().taskExecutor();
        NioEventLoopGroup taskScheduleGroup = WebSocketExecutorContext.getInstance().taskScheduleGroup();
        // 根据url创建新的controller实例，注入session
        BaseWebSocketController controller = ControllerMap.getControllerByUrl(session);
        // 调用prepare方法，同步阻塞
        taskExecutor.submit(() -> controller.prepare(text)).get();
        // 创建定时任务
        ScheduledFuture scheduledFuture = taskScheduleGroup.scheduleAtFixedRate(() -> executeAndFlush(controller),
                0, controller.period(), controller.unit());
        // 定时任务注册到session中，并且停止之前的定时任务
        session.registExecuteTask(scheduledFuture);
    }

    private void executeAndFlush(BaseWebSocketController controller) {
        String result = controller.execute();
        controller.session().sendMessage(result);
    }
}
