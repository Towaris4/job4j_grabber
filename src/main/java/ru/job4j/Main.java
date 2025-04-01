package ru.job4j;

import ru.job4j.grabber.model.Post;
import ru.job4j.grabber.service.Config;
import ru.job4j.grabber.service.SchedulerManager;
import ru.job4j.grabber.service.SuperJobGrab;
import ru.job4j.grabber.stores.MemStore;

public class Main {
    public static void main(String[] args) {
        var config = new Config();
        config.load("application.properties");
        var store = new MemStore();
        var post = new Post();
        post.setTitle("Super Java Job");
        store.save(post);
        var scheduler = new SchedulerManager();
        scheduler.init();
        scheduler.load(
                Integer.parseInt(config.get("rabbit.interval")),
                SuperJobGrab.class,
                store);
    }
}