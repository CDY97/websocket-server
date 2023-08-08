package handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import session.SessionContext;

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
            String text = msg.text();
            SessionContext.startScheduledTask(ctx.channel().id().asShortText(), text, false);
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
}
