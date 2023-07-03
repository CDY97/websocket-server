package handler;

import bean.BuildConnectionParam;
import channal.ShortIdNioSocketChannel;
import com.alibaba.fastjson.JSONObject;
import controller.ControllerMap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.internal.StringUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import session.SessionContext;

/**
 * @author chengengwei
 * @description 权限校验处理器
 * @date 2023/6/16
 */
@ChannelHandler.Sharable
public class PermissionHandler extends ChannelInboundHandlerAdapter {

    private static InternalLogger logger = InternalLoggerFactory.getInstance(PermissionHandler.class);

    public PermissionHandler() {
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        FullHttpRequest req = (FullHttpRequest)msg;
        Channel channel = ctx.channel();
        // 判断是否有权限
        if (check(req)) {
            super.channelRead(ctx, msg);
            // 连接建立后移除该handler
            ctx.pipeline().remove(PermissionHandler.class);
            // 创建session
            SessionContext.regist((ShortIdNioSocketChannel) channel, req.uri().split("\\?")[0]);
        } else {
            channel.close();
            logger.info("channal[{}]权限校验未通过，连接断开", ctx.channel().id().asShortText());
        }
    }

    /**
     * 权限校验
     * @param req
     * @return
     */
    private boolean check(FullHttpRequest req) {
        String uri = req.uri();
        // 先解析url，判断url是否匹配，在解析参数，校验参数token是否合法
        String[] uriArr = uri.split("\\?");
        if (uriArr.length == 2) {
            String url = uriArr[0];
            String paramsKV = uriArr[1];
            String[] paramsArr = paramsKV.split("&");
            JSONObject paramObj = new JSONObject();
            for (String str : paramsArr) {
                String[] kvArr = str.split("=");
                if (kvArr.length != 2) {
                    return false;
                }
                paramObj.put(kvArr[0], kvArr[1]);
            }
            BuildConnectionParam param = paramObj.toJavaObject(BuildConnectionParam.class);
            // 校验token
            return validateUrl(url) && validatePermission(param);
        } else {
            return false;
        }
    }

    /**
     * 校验url
     * @param url
     * @return
     */
    private boolean validateUrl(String url) {
        return ControllerMap.containsUrl(url);
    }

    /**
     * 校验token
     * @param param
     * @return
     */
    private boolean validatePermission(BuildConnectionParam param) {
        // todo，具体校验逻辑
        return !StringUtil.isNullOrEmpty(param.getToken());
    }
}
