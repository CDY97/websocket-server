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

    private boolean executeAfterConnected;

    @Override
    public final Session session() {
        return this.session;
    }

    @Override
    public final long period() {
        return this.period;
    }

    @Override
    public final TimeUnit unit() {
        return this.unit;
    }

    @Override
    public final boolean sendNullMsg() {
        return this.sendNullMsg;
    }

    @Override
    public final boolean executeAfterConnected() {
        return this.executeAfterConnected;
    }

    @Override
    public final void afterPropertiesSet() throws Exception {
        WebSocketController annotation = this.getClass().getAnnotation(WebSocketController.class);
        this.period = annotation.period();
        this.unit = annotation.unit();
        this.sendNullMsg = annotation.sendNullMsg();
        this.executeAfterConnected = annotation.executeAfterConnected();
        ControllerMap.registController(dealUrl(annotation.url()), this);
    }

    @Override
    public final BaseWebSocketController newInstance(Session session) throws CloneNotSupportedException {
        BaseWebSocketController controller = (BaseWebSocketController) super.clone();
        controller.session = session;
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
