package handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import session.SessionContext;
import session.WebSocketSession;

import java.util.List;

/**
 * @author chengengwei
 * @description WebSocketServerProtocolHandler增强类
 * @date 2023/6/16
 */
@ChannelHandler.Sharable
public class WebSocketServerProtocolEnhancingHandler extends WebSocketServerProtocolHandler {

    private PermissionHandler permissionHandler;

    public WebSocketServerProtocolEnhancingHandler() {
        super("", true);
        this.permissionHandler = new PermissionHandler();
    }

    protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> out) throws Exception {
        // 如果是pong则心跳计数清空
        if (frame instanceof PongWebSocketFrame) {
            WebSocketSession session = SessionContext.getSession(ctx.channel().id().asShortText());
            if (session != null) {
                session.refreshHeartBeat();
            }
        } else {
            super.decode(ctx, frame, out);
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        ChannelPipeline cp = ctx.pipeline();
        // 添加自定义url校验处理器
        if (cp.get(PermissionHandler.class) == null) {
            cp.addBefore(ctx.name(), PermissionHandler.class.getName(), permissionHandler);
        }
        // 添加完自定义的url校验处理器后再调用父类方法
        // 父类方法会创建WebSocketServerProtocolHandshakeHandler和Utf8FrameValidator处理器，
        // Utf8FrameValidator处理器不会继续将事件向下传递，因此自定义处理器需要加在它俩之前
        super.handlerAdded(ctx);
    }
}
