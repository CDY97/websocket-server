package controller;

import exception.SameUrlControllerException;
import session.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ControllerMap {

    private static final Map<String, BaseWebSocketController> map = new ConcurrentHashMap<>();

    public static void registController(String url, BaseWebSocketController controller) {
        if (map.containsKey(url)) {
            throw new SameUrlControllerException("exist multiple websocket controllers with the same URL");
        }
        map.put(url, controller);
    }

    public static BaseWebSocketController getControllerByUrl(WebSocketSession session) throws CloneNotSupportedException {
        BaseWebSocketController controller = map.get(session.url());
        if (controller != null) {
            return controller.newInstance(session);
        }
        return null;
    }

    public static boolean containsUrl(String url) {
        return map.containsKey(url);
    }
}
