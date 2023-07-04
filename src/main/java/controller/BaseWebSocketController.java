package controller;

import annotation.WebSocketController;
import org.springframework.beans.factory.InitializingBean;
import session.Session;

import java.util.concurrent.TimeUnit;

public abstract class BaseWebSocketController implements InitializingBean, ScheduledTaskProcessor, Cloneable {

    private Session session;

    private long period;

    private TimeUnit unit;

    private boolean sendNullMsg;

    private void setSession(Session session) {
        this.session = session;
    }

    public Session session() {
        return this.session;
    }

    public long period() {
        return this.period;
    }

    public TimeUnit unit() {
        return this.unit;
    }

    public boolean sendNullMsg() {
        return this.sendNullMsg;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        WebSocketController annotation = this.getClass().getAnnotation(WebSocketController.class);
        this.period = annotation.period();
        this.unit = annotation.unit();
        this.sendNullMsg = annotation.sendNullMsg();
        ControllerMap.registController(dealUrl(annotation.url()), this);
    }

    public BaseWebSocketController newInstance(Session session) throws CloneNotSupportedException {
        BaseWebSocketController controller = (BaseWebSocketController) super.clone();
        controller.setSession(session);
        return controller;
    }

    private String dealUrl(String url) {
        url = url.trim();
        if (!url.startsWith("/")) {
            url = "/" + url;
        }
        if (url.endsWith("/") && url.length() > 1) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }
}
