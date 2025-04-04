package ru.job4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.job4j.grabber.service.*;
import ru.job4j.grabber.stores.JdbcStore;
import ru.job4j.grabber.stores.Store;
import ru.job4j.grabber.model.Post;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {
        private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

        public static void main(String[] args) {
            var config = new Config();
            config.load("src/main/resources/application.properties");
            try (var connection = DriverManager.getConnection(config.get("db.url"),
                    config.get("db.username"),
                    config.get("db.password"))) {
                Store store = new JdbcStore(connection);
                DateTimeParser habrCareerDateTimeParser = new HabrCareerDateTimeParser();
                HabrCareerParse habrCareerParse = new HabrCareerParse(habrCareerDateTimeParser);
                habrCareerParse.fetch().forEach(store::save);
                var scheduler = new SchedulerManager();
                scheduler.init();
                scheduler.load(
                        Integer.parseInt(config.get("rabbit.interval")),
                        SuperJobGrab.class,
                        store
                );
                new Web(store).start(Integer.parseInt(config.get("server.port")));
            } catch (SQLException e) {
                LOGGER.error("When creating a connection", e);
            }
        }
}