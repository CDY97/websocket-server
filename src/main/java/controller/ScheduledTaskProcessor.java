package controller;

import session.Session;

public interface ScheduledTaskProcessor {

    void prepare(String params);

    String execute();

    Session session();
}
