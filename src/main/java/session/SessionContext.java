package session;

import channal.ShortIdNioSocketChannel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SessionContext {

    private static InternalLogger logger = InternalLoggerFactory.getInstance(SessionContext.class);

    private static final Map<String, WebSocketSession> SessionContext = new ConcurrentHashMap<>();

    private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    static {
        executorService.scheduleAtFixedRate(() -> {
            Iterator<Map.Entry<String, WebSocketSession>> iterator = SessionContext.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, WebSocketSession> entry = iterator.next();
                WebSocketSession session = entry.getValue();
                if (session.heartBeatCount() > 5) {
                    session.close();
                    iterator.remove();
                }
            }
        }, 0, 5, TimeUnit.SECONDS);
    }

    public static void cancel(String sessionId) {
        WebSocketSession session = SessionContext.remove(sessionId);
        if (session != null) {
            session.close();
        }
    }

    public static WebSocketSession getSession(String sessionId) {
        return SessionContext.get(sessionId);
    }

    public static void regist(ShortIdNioSocketChannel channel, String url) {
        String sessionId = channel.id().asShortText();
        SessionContext.computeIfAbsent(sessionId, k -> new WebSocketSession(channel, url));
    }
}
