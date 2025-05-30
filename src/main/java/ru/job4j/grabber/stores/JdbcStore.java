package ru.job4j.grabber.stores;

import ru.job4j.grabber.model.Post;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcStore implements Store {
    private final Connection connection;

    public JdbcStore(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void save(Post post) {
        try (PreparedStatement checkStatement = connection.prepareStatement(
                "SELECT id FROM post WHERE link = ?")) {
            checkStatement.setString(1, post.getLink());
            ResultSet resultSet = checkStatement.executeQuery();
            if (!resultSet.next()) {
                try (PreparedStatement preparedStatement = connection.prepareStatement("insert into post(name, text, link, created) values(?, ?, ?, ?)")) {
                    preparedStatement.setString(1, post.getTitle());
                    preparedStatement.setString(2, post.getDescription());
                    preparedStatement.setString(3, post.getLink());
                    preparedStatement.setTimestamp(4, new Timestamp(post.getTime()));
                    preparedStatement.execute();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> post = new ArrayList<>();
        try (PreparedStatement preparedStatement = connection.prepareStatement("select * from post;");
             ResultSet rs = preparedStatement.executeQuery()) {
            while (rs.next()) {
                post.add(getPost(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all: ", e);
        }
        return post;
    }

    @Override
    public Optional<Post> findById(Long id) {
        Post post = new Post();
        try (PreparedStatement preparedStatement = connection.prepareStatement("select * from post where id = ?;")) {
            preparedStatement.setLong(1, id);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(getPost(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find by id: " + id + " ", e);
        }
        return Optional.empty();
    }

    private Post getPost(ResultSet rs) throws SQLException {
        Post post = new Post();
        post.setId(rs.getLong("id"));
        post.setDescription(rs.getString("text"));
        post.setLink(rs.getString("link"));
        post.setTime(rs.getTimestamp("Created").getTime());
        post.setTitle(rs.getString("name"));
        return post;
    }
}