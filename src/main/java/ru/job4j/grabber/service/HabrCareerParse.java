package ru.job4j.grabber.service;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import ru.job4j.grabber.model.Post;
import ru.job4j.grabber.stores.JdbcStore;
import ru.job4j.grabber.stores.Store;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HabrCareerParse implements Parse {
    private static final Logger LOG = Logger.getLogger(HabrCareerParse.class);
    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PREFIX = "/vacancies?page=";
    private static final String SUFFIX = "&q=Java%20developer&type=all";
    public static final int PAGES_TO_PARSE = 5;

    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    @Override
    public List<Post> fetch() {
        return list(SOURCE_LINK);
    }

    @Override
    public List<Post> list(String link) {
        var result = new ArrayList<Post>();
        try {
            for (int pageNumber = 1; pageNumber <= PAGES_TO_PARSE; pageNumber++) {
                String fullLink = "%s%s%d%s".formatted(link, PREFIX, pageNumber, SUFFIX);
                var connection = Jsoup.connect(fullLink);
                var document = connection.get();
                var rows = document.select(".vacancy-card__inner");
                rows.forEach(row -> {
                    var titleElement = row.select(".vacancy-card__title").first();
                    var linkElement = titleElement.child(0);
                    var dateTime = row.select(".basic-date").first();
                    String vacancyName = titleElement.text();
                    String linkPost = String.format("%s%s", link,
                            linkElement.attr("href"));
                    var post = new Post();
                    String datetimeStr = dateTime.attr("datetime");
                    long timestamp = dateTimeParser.parse(datetimeStr).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                    post.setTitle(vacancyName);
                    post.setLink(linkPost);
                    post.setTime(timestamp);
                    post.setDescription(retrieveDescription(linkPost));
                    result.add(post);
                });
            }
        } catch (IOException e) {
            LOG.error("When load page", e);
        }
        return result;
    }

    private String retrieveDescription(String link) {
        String description = null;
        try {
            var connection = Jsoup.connect(link);
            var document = connection.get();
            var descriptionElement = document.select(".vacancy-description__text").first();
            if (descriptionElement != null) {
                description = descriptionElement.html();
            }
        } catch (IOException e) {
            LOG.error("When load page's description", e);
        }
        return description;
    }
}
