package com.example.javafxwordle;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
public class MySQLUtility {

    private static final String URL = "jdbc:mysql://localhost:3306/wordle_game";  // Your DB URL
    private static final String USER = "root";  // Your MySQL username
    private static final String PASSWORD = "nafiz";  // Your MySQL password

    /**
     * Save or update player name and score.
     */
    public static boolean saveOrUpdatePlayerName(String playerName, int score) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            System.out.println("Saving/updating player: " + playerName + " with score: " + score);

            String checkQuery = "SELECT Score FROM players WHERE player_name = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
                checkStmt.setString(1, playerName);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    int currentScore = rs.getInt("Score");
                    if (score > currentScore) {
                        // Update the score only if the new score is higher
                        String updateQuery = "UPDATE players SET Score = ? WHERE player_name = ?";
                        try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                            updateStmt.setInt(1, score);
                            updateStmt.setString(2, playerName);
                            int rowsAffected = updateStmt.executeUpdate();
                            System.out.println("Updated rows: " + rowsAffected);
                        }
                    } else {
                        System.out.println("New score is not higher. No update performed.");
                    }
                } else {
                    // Insert a new player if not found
                    String insertQuery = "INSERT INTO players (player_name, Score) VALUES (?, ?)";
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                        insertStmt.setString(1, playerName);
                        insertStmt.setInt(2, score);
                        int rowsInserted = insertStmt.executeUpdate();
                        System.out.println("Inserted rows: " + rowsInserted);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }




    /**
     * Save a new username and password during signup.
     */
    public static boolean saveUserCredentials(String username, String password) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // Check if the username already exists
            String checkQuery = "SELECT id FROM users WHERE username = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    // Username already exists
                    return false;
                }
            }
                String hp= BCrypt.hashpw(password, BCrypt.gensalt());
            // Insert new user
            String insertQuery = "INSERT INTO users (username, password) VALUES (?, ?)";
            try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, hp);
                insertStmt.executeUpdate();
            }
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Validate username and password during login.
     */
    public static boolean validateUserCredentials(String username, String password) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "SELECT password FROM users WHERE username = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String storedhp = rs.getString("password");
                    return BCrypt.checkpw(password,storedhp) ;// Match passwords
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    /**
     * Fetch the players table sorted by high scores in descending order.
     */
    public static ArrayList<String> getPlayersTable() {
        ArrayList<String> players = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "SELECT player_name, Score FROM players ORDER BY Score DESC";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String player = rs.getString("player_name");
                    int score = rs.getInt("Score");
                    players.add(player + ": " + score);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return players;
    }
    public static boolean sendFriendRequest(String sender, String receiver) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // Check if a pending or accepted request already exists
            String checkQuery = "SELECT * FROM friends WHERE (sender = ? AND receiver = ?) OR (sender = ? AND receiver = ?)";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
                checkStmt.setString(1, sender);
                checkStmt.setString(2, receiver);
                checkStmt.setString(3, receiver);
                checkStmt.setString(4, sender);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    return false; // Request already exists
                }
            }

            // Insert a new friend request
            String insertQuery = "INSERT INTO friends (sender, receiver, status) VALUES (?, ?, 'pending')";
            try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                insertStmt.setString(1, sender);
                insertStmt.setString(2, receiver);
                insertStmt.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean acceptFriendRequest(String sender, String receiver) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "UPDATE friends SET status = 'accepted' WHERE sender = ? AND receiver = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, sender);
                stmt.setString(2, receiver);
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static boolean rejectFriendRequest(String sender, String receiver) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "UPDATE friends SET status = 'rejected' WHERE sender = ? AND receiver = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, sender);
                stmt.setString(2, receiver);
                int rowsAffected = stmt.executeUpdate();
                return rowsAffected > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static ArrayList<String> getFriendRequests(String receiver) {
        ArrayList<String> friendRequests = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "SELECT sender FROM friends WHERE receiver = ? AND status = 'pending'";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, receiver);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    friendRequests.add(rs.getString("sender"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friendRequests;
    }

    /**
     * Fetch the list of friends for a specific user.
     */
    public static ArrayList<String> getFriends(String username) {
        ArrayList<String> friends = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // Query to find all accepted friends
            String query = "SELECT CASE WHEN sender = ? THEN receiver ELSE sender END AS friend " +
                    "FROM friends " +
                    "WHERE (sender = ? OR receiver = ?) AND status = 'accepted'";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, username);
                stmt.setString(2, username);
                stmt.setString(3, username);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    friends.add(rs.getString("friend"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friends;
    }

    public static boolean sendMessage(String username, String message) {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "INSERT INTO chat (username, message) VALUES (?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, username);
                stmt.setString(2, message);
                int rowsInserted = stmt.executeUpdate();
                return rowsInserted > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static ArrayList<String> getChatMessages() {
        ArrayList<String> chatMessages = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            // Fetch the last 10 messages
            String query = "SELECT username, message, timestamp FROM chat ORDER BY timestamp DESC LIMIT 20";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String username = rs.getString("username");
                    String message = rs.getString("message");
                    String timestamp = rs.getString("timestamp");
                    chatMessages.add("[" + timestamp + "] " + username + ": " + message);
                }
            }

            // Delete messages older than the last 10 (assuming timestamp is accurate)
            String deleteQuery = "DELETE FROM chat WHERE timestamp < (SELECT MIN(timestamp) FROM (SELECT timestamp FROM chat ORDER BY timestamp DESC LIMIT 20) AS last20)";
            try (PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery)) {
                deleteStmt.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return chatMessages;
    }


}