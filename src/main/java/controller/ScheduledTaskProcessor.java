package controller;

import session.Session;

import java.util.concurrent.TimeUnit;

public interface ScheduledTaskProcessor {

    void prepare(String params);

    String execute();

    Session session();

    long period();

    TimeUnit unit();

    boolean sendNullMsg();

    BaseWebSocketController newInstance(Session session) throws CloneNotSupportedException;
}
