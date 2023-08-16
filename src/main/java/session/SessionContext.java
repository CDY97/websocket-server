package session;

import bootstrap.WebSocketExecutorContext;
import channal.ShortIdNioSocketChannel;
import controller.BaseWebSocketController;
import controller.ControllerMap;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.util.concurrent.ScheduledFuture;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.*;

public class SessionContext {

    private static InternalLogger logger = InternalLoggerFactory.getInstance(SessionContext.class);

    private static final Map<String, WebSocketSession> SessionContext = new ConcurrentHashMap<>();

    public static void cancel(String sessionId) {
        WebSocketSession session = SessionContext.remove(sessionId);
        if (session != null) {
            session.close();
        }
    }

    public static WebSocketSession getSession(String sessionId) {
        return SessionContext.get(sessionId);
    }

    public static void regist(ShortIdNioSocketChannel channel, String url) throws CloneNotSupportedException {
        String sessionId = channel.id().asShortText();
        SessionContext.computeIfAbsent(sessionId, k -> new WebSocketSession(channel, url));
        startScheduledTask(sessionId, null, true);
    }

    public static void startScheduledTask(String sessionId, String text, boolean afterConnected) throws CloneNotSupportedException {
        ThreadPoolExecutor taskExecutor = WebSocketExecutorContext.getInstance().taskExecutor();
        DefaultEventLoopGroup taskScheduleGroup = WebSocketExecutorContext.getInstance().taskScheduleGroup();
        WebSocketSession session = getSession(sessionId);
        if (session == null) {
            return;
        }
        // 根据url创建新的controller实例，注入session
        BaseWebSocketController controller = ControllerMap.getControllerByUrl(session);
        // 如果刚建立连接并且该接口支持连接后直接执行，或接受到客户端请求参数，则开启定时任务
        if (afterConnected && controller.executeAfterConnected() || !afterConnected) {
            // 异步调用prepare，创建定时任务
            taskExecutor.submit(() -> {
                // 调用一次prepare，触发初始化方法准备数据并传参
                controller.prepare(text);
                // 创建定时任务
                ScheduledFuture scheduledFuture = taskScheduleGroup.scheduleAtFixedRate(() -> {
                    taskExecutor.execute(() -> executeAndFlush(controller));
                }, 0, controller.period(), controller.unit());
                // 定时任务注册到session中，并且停止之前的定时任务
                session.registExecuteTask(scheduledFuture);
            });
        }
    }

    private static void executeAndFlush(BaseWebSocketController controller) {
        String result = controller.execute();
        if (result != null || controller.sendNullMsg()) {
            controller.session().sendMessage(result);
        }
    }
}
