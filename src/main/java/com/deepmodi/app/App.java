package com.deepmodi.app;

import com.deepmodi.app.dao.UserDao;
import com.deepmodi.app.model.User;
import com.deepmodi.app.util.DatabaseUtil;

import java.util.Optional;
import java.util.Scanner;

public class App {

    private static final UserDao userDao = new UserDao();
    private static final Scanner scanner = new Scanner(System.in);
    public static void main(String[] args) {
        System.out.println("=== JDBC PostgreSQL Tutorial Application ===\n");

        // Step 1: Test Database Connectivity
        if(!DatabaseUtil.testConnection()){
            System.err.println("Cannot proceed without database connection. Please check your configuration.");
            return;
        }

        // Step 2: Create table if it doesn't exist
        DatabaseUtil.createUserTableIfNotExists();

        // Step 3: Run interative demo
        runInteractiveDemo();

        System.out.println("\n=== Application finished ===");
    }

    private static void runInteractiveDemo(){
        while(true){
            printMenu();

            try{
                int choice = Integer.parseInt(scanner.nextLine().trim());
                switch (choice){
                    case 1:
                        demonstrateCreateUser();
                        break;
                    case 2:
                        demonstrateFindUserById();
                        break;
                    case 3:
                        //demonstrateFindUserByEmail();
                        break;
                    case 4:
                        //demonstrateListAllUsers();
                        break;
                    case 5:
                        //demonstrateUpdateUser();
                        break;
                    case 6:
                        //demonstrateDeleteUser();
                        break;
                    case 7:
                        //demonstrateTransactionExample();
                        break;
                    case 8:
                        //demonstrateRollbackExample();
                        break;
                    case 9:
                        //createSampleData();
                        break;
                    case 10:
                        //showUserCount();
                        break;
                    case 0:
                        System.out.println("Goodbye!");
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
            }  catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            } catch (Exception e) {
                System.err.println("An error occurred: " + e.getMessage());
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("JDBC Tutorial - Choose an operation:");
        System.out.println("=".repeat(50));
        System.out.println("1. Create a new user");
        System.out.println("2. Find user by ID");
        System.out.println("3. Find user by email");
        System.out.println("4. List all users");
        System.out.println("5. Update user");
        System.out.println("6. Delete user");
        System.out.println("7. Transaction example (batch insert)");
        System.out.println("8. Rollback example (intentional failure)");
        System.out.println("9. Create sample data");
        System.out.println("10. Show user count");
        System.out.println("0. Exit");
        System.out.println("=".repeat(50));
        System.out.print("Enter your choice: ");
    }

    private static void demonstrateCreateUser(){
        System.out.println("\n-- Create new User--");

        System.out.println("Enter first name: ");
        String firstname = scanner.nextLine().trim();

        System.out.println("Enter last name: ");
        String lastname = scanner.nextLine().trim();

        System.out.println("Enter email: ");
        String email = scanner.nextLine().trim();

        if(firstname.isEmpty() || lastname.isEmpty() || email.isEmpty()){
            System.out.println("All fields are required.");
            return;
        }

        User newUser = new User(firstname, lastname, email);
        User createdUser = userDao.createUser(newUser);

        if(createdUser != null){
            System.out.println("User created successfully:");
            System.out.println(" ID: "+createdUser.getId());
            System.out.println(" Name: "+createdUser.getFirstName()+" "+createdUser.getLastName());
            System.out.println(" Email: "+createdUser.getEmail());
        }
    }

    private static void demonstrateFindUserById(){
        System.out.println("\n--- Find User by ID ---");

        System.out.print("Enter user ID: ");
        try{
            Long id = Long.parseLong(scanner.nextLine().trim());

            Optional<User> userOptional = userDao.findUserById(id);

            if(userOptional.isPresent()){
                User user = userOptional.get();
                System.out.println("User found.");
                printUserDetails(user);
            }else{
                System.out.println("No user found with ID: "+id);
            }
        }catch (NumberFormatException e){
            System.out.println("Please enter a valid number for ID.");
        }
    }

    private static void printUserDetails(User user){
        System.out.println("User details: ");
        System.out.println("Firstname: "+user.getFirstName());
        System.out.println("Lastname: "+user.getLastName());
        System.out.println("ID: "+user.getId());
        System.out.println("Created at: "+user.getCreatedAt());
        System.out.println("----------------");
    }

}
