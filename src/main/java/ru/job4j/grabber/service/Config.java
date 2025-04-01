package ru.job4j.grabber.service;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class Config {
    private static final Logger LOG = Logger.getLogger(Config.class);
    private final Properties properties = new Properties();

    public void load(String file) {
        try (var input = new BufferedReader(new FileReader(file))) {
            properties.load(input);
        } catch (IOException io) {
            LOG.error(String.format("When load file : %s", file), io);
        }
    }

    public String get(String key) {
        return properties.getProperty(key);
    }
}