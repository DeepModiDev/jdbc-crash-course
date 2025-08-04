package com.deepmodi.app.dao;

import com.deepmodi.app.model.User;
import com.deepmodi.app.util.DatabaseUtil;
import org.postgresql.replication.fluent.CommonOptions;

import java.sql.*;
import java.util.Optional;

public class UserDao {
    // SQL queries as constants (easier to maintain and review)
    private static final String INSERT_USER_SQL =
            "INSERT INTO users (first_name, last_name, email) VALUES (?, ?, ?)";

    private static final String SELECT_USER_BY_ID_SQL =
            "SELECT id, first_name, last_name, email, created_at, updated_at FROM users WHERE id = ?";

    private static final String SELECT_ALL_USERS_SQL =
            "SELECT id, first_name, last_name, email, created_at, updated_at FROM users ORDER BY id";

    private static final String UPDATE_USER_SQL =
            "UPDATE users SET first_name = ?, last_name = ?, email = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

    private static final String DELETE_USER_SQL =
            "DELETE FROM users WHERE id = ?";

    private static final String SELECT_USER_BY_EMAIL_SQL =
            "SELECT id, first_name, last_name, email, created_at, updated_at FROM users WHERE email = ?";


    public User createUser(User user) {
        System.out.println("Creating user: " + user.getEmail());

        // Use try-with-resources to ensure automatic resource cleanup
        try(Connection connection = DatabaseUtil.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(
                    INSERT_USER_SQL, Statement.RETURN_GENERATED_KEYS
            )) {

            // Set parameters (? placeholders) - this prevents SQL injection
            preparedStatement.setString(1, user.getFirstName());
            preparedStatement.setString(2, user.getLastName());
            preparedStatement.setString(3, user.getEmail());

            // Execute the INSERT statement
            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows == 0){
                System.err.println("Creating user failed, no rows affected.");
                connection.rollback();
                return null;
            }

            // Retrieve the generated ID
            try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()){
                if (generatedKeys.next()){
                    user.setId(generatedKeys.getLong(1));
                    connection.commit();
                    System.out.println("✓ User created successfully with ID: " + user.getId());
                    return user;
                } else {
                    System.err.println("Creating user failed, no ID obtained.");
                    connection.rollback();
                    return null;
                }
            }
        } catch (SQLException e){
            System.err.println("✗ Error creating user: " + e.getMessage());

            // Handle specific constraint violations
            if (e.getSQLState().equals("23505")) { // Unique constraint violation
                System.err.println("  Email already exists: " + user.getEmail());
            }

            return null;
        }
    }

    public Optional<User> findUserById(Long id){
        System.out.println("Finding user by ID: "+id);

        try(Connection connection = DatabaseUtil.getConnection();
        PreparedStatement statement = connection.prepareStatement(SELECT_USER_BY_ID_SQL)){

            statement.setLong(1, id);

            try(ResultSet rs = statement.executeQuery()){
                if(rs.next()){
                    User user = mapResultSetToUser(rs);
                    connection.commit();
                    System.out.println("✓ User found: " + user.getEmail());
                    return Optional.of(user);
                } else {
                    connection.commit();
                    System.out.println("User with ID " + id + " not found");
                    return Optional.empty();
                }
            }
        }catch (SQLException e){
            System.err.println("✗ Error finding user by ID: " + e.getMessage());
            return Optional.empty();
        }
    }

    private User mapResultSetToUser(ResultSet resultSet) throws SQLException{
        User user = new User();

        user.setId(resultSet.getLong("id"));
        user.setFirstName(resultSet.getString("first_name"));
        user.setLastName(resultSet.getString("last_name"));
        user.setEmail(resultSet.getString("email"));

        Timestamp createdAtTimestamp = resultSet.getTimestamp("created_at");
        if(createdAtTimestamp != null){
            user.setCreatedAt(createdAtTimestamp.toLocalDateTime());
        }

        Timestamp updatedAtTimestamp = resultSet.getTimestamp("updated_at");
        if(updatedAtTimestamp != null){
            user.setUpdatedAt(updatedAtTimestamp.toLocalDateTime());
        }

        return user;
    }
}
