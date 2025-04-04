package ru.job4j.grabber.service;

import io.javalin.Javalin;
import ru.job4j.grabber.stores.Store;

public class Web {
    private final Store store;

    public Web(Store store) {
        this.store = store;
    }

    public void start(int port) {
        var app = Javalin.create(config -> {
            config.http.defaultContentType = "text/html; charset=utf-8";
        });
        app.start(port);
        var page = new StringBuilder();

        store.getAll().forEach(post -> page
                .append(post.getId()).append("|")
                .append(post.getTitle()).append("|")
                .append(post.getLink()).append("|")
                .append(String.valueOf(post.getTime())).append("|")
                .append(post.getDescription()).append("|")
                .append("<br><br>"));

        app.get("/", ctx -> ctx.result(page.toString()));
    }
}
