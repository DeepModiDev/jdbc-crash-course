package com.deepmodi.app.dao;

import com.deepmodi.app.model.User;
import com.deepmodi.app.util.DatabaseUtil;
import org.postgresql.replication.fluent.CommonOptions;

import javax.xml.crypto.Data;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
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

    public Optional<User> findUserByEmail(String email){
        System.out.println("Finding user by email.");

        try(Connection connection = DatabaseUtil.getConnection();
            PreparedStatement statement = connection.prepareStatement(SELECT_USER_BY_EMAIL_SQL)){
            statement.setString(1, email);

            try(ResultSet rs = statement.executeQuery()){
                if(rs.next()){
                    User user = mapResultSetToUser(rs);
                    connection.commit();
                    System.out.println("✓ User found by email: " +user.getEmail());
                    return Optional.of(user);
                }else{
                    connection.commit();
                    System.out.println("User with email "+email+" not found.");
                    return Optional.empty();
                }
            }
        }catch (Exception e){
            System.err.println("User not found "+e.getMessage());
            return Optional.empty();
        }
    }

    public List<User> findAllUsers(){
        System.out.println("Retrieving all users....");
        List<User> users = new ArrayList<>();

        try(Connection connection = DatabaseUtil.getConnection();
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(SELECT_ALL_USERS_SQL)){
            while(rs.next()){
                User user = mapResultSetToUser(rs);
                users.add(user);
            }
            connection.commit();
            System.out.println("✓ Retrieved " + users.size() + " users");
        }catch (SQLException e){
            System.err.println("X Error retrieving all users: "+e.getMessage());
        }

        return users;
    }

    public boolean updateUser(User user){
        System.out.println("Updating user ID: "+user.getId());

        try(Connection connection = DatabaseUtil.getConnection();
        PreparedStatement statement = connection.prepareStatement(UPDATE_USER_SQL)){
            statement.setString(1, user.getFirstName());
            statement.setString(2, user.getLastName());
            statement.setString(3, user.getEmail());
            statement.setLong(4, user.getId());

            int affectedRows = statement.executeUpdate();
            if(affectedRows > 0){
                connection.commit();
                System.out.println("✓ User updated successfully");
                return true;
            }else{
                connection.rollback();
                System.out.println("No user found with ID: " + user.getId());
                return false;
            }
        }catch (SQLException e){
            System.err.println("X Error updating user: "+e.getMessage());
            return false;
        }
    }

    public boolean deleteUser(Long id){
        System.out.println("Deleting user ID: "+id);

        try(Connection connection = DatabaseUtil.getConnection();
        PreparedStatement statement = connection.prepareStatement(DELETE_USER_SQL)){
            statement.setLong(1, id);

            int affectedRows = statement.executeUpdate();

            if(affectedRows > 0){
                connection.commit();
                System.out.println("✓ User deleted successfully");
                return true;
            } else {
                connection.rollback();
                System.out.println("No user found with ID: "+id);
                return false;
            }

        }catch (SQLException e){
            System.err.println("✗ Error deleting user: "+e.getMessage());
            return false;
        }
    }

    public boolean createUsersInTransaction(List<User> users){
        System.out.println("Creating " + users.size() + " users in a single transaction...");

        Connection connection = null;
        PreparedStatement stmt = null;

        try{
            connection = DatabaseUtil.getConnection();
            stmt = connection.prepareStatement(INSERT_USER_SQL, Statement.RETURN_GENERATED_KEYS);

            for(User user : users){
                stmt.setString(1, user.getFirstName());
                stmt.setString(2, user.getLastName());
                stmt.setString(3, user.getEmail());
                stmt.addBatch();
            }

            int[] affectedRows = stmt.executeBatch();

            for(int rows : affectedRows){
                if(rows == 0) {
                    System.err.println("One or more user creations failed");
                    connection.rollback();
                    return false;
                }
            }

            connection.commit();
            System.out.println("✓ All " + users.size() + " users created successfully in transaction");
            return true;
        }catch (SQLException e){
            System.err.println("✗ Error creating users in transaction: " + e.getMessage());
            if(connection != null){
                try{
                    connection.rollback();
                    System.out.println("✓ Transaction rolled back due to error");
                }catch (SQLException rollbackEx){
                    System.err.println("✗ Error during rollback: " + rollbackEx.getMessage());
                }
            }
            return false;
        }finally {
            DatabaseUtil.closeResources(connection, stmt, null);
        }
    }

    public long getUserCount(){
        String countSQL = "SELECT COUNT(*) FROM users";
        try(Connection connection = DatabaseUtil.getConnection();
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(countSQL)){

            if(rs.next()){
                long count = rs.getLong(1);
                connection.commit();
                return count;
            }
        }catch (SQLException e){
            System.err.println("X Error getting user count: "+e.getMessage());
        }

        return 0;
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
