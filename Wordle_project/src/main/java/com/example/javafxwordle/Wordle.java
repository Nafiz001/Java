package com.example.javafxwordle;

import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;

import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;
import java.util.prefs.Preferences;


public class Wordle extends Application {

    private Preferences userPreferences;
    private static final String PREF_USER_COUNT = "userCount";
    private static final String PREF_USER_PREFIX = "user_";

    private ArrayList<String> wordList = new ArrayList<>();
    private String target;
    private TextField[][] wordBank = new TextField[6][5];
    private int wordIndex = 0;
    private int letterIndex = 0;

    private Backend backend; //com.example.javafxwordle.Backend instance
    Stage primaryStage;
    private  boolean isChatOpen= false;
    private MediaPlayer backgroundMusicPlayer;
    private MediaPlayer clickSoundPlayer;
    public Wordle() throws IOException {
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        backend = new Backend();  // Initialize the backend
        loadWords();  // Load words from the file
        resetTarget();  // Initialize target word
        initializeSound();
        primaryStage.setTitle("com.example.javafxwordle.Wordle");
        StackPane splashScreen = new StackPane();
        splashScreen.setStyle("-fx-background-image: url('/com/example/javafxwordle/image1.jpg');"
                + "-fx-background-size: cover; -fx-background-position: center;");
        splashScreen.setMinSize(800, 1500);

        Circle loadingCircle = new Circle(30);  // Adjust radius as needed
        loadingCircle.setStroke(Color.RED);
        loadingCircle.setStrokeWidth(8);
        loadingCircle.setFill(Color.TRANSPARENT);
        loadingCircle.setStrokeLineCap(StrokeLineCap.ROUND);  // For smooth corners

        // Add the circle to the splash screen
        splashScreen.getChildren().add(loadingCircle);

        // Create the animation for the loading circle
        Timeline timeline = new Timeline();
        KeyFrame keyFrame = new KeyFrame(Duration.millis(100), event -> {
            double currentOffset = loadingCircle.getStrokeDashOffset();
            loadingCircle.setStrokeDashOffset(currentOffset - 2);
        });
        timeline.getKeyFrames().add(keyFrame);
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        // Set the dash array to create the "broken" effect
        loadingCircle.getStrokeDashArray().addAll(15.0, 10.0);
        Text animatedText = new Text();
        animatedText.setFont(Font.font("Helvetica", FontWeight.BOLD, 150));
        LinearGradient gradient = new LinearGradient(
                0, 0, 0, 1,  // Start (x1, y1) and end (x2, y2) positions
                true,         // Proportional (relative to the bounds of the shape)
                CycleMethod.REFLECT, // No cycling

                new Stop(0.0, Color.web("#ff7f00")),
                new Stop(0.2, Color.web("#ff0000")), // Red// Orange
                new Stop(0.4, Color.web("#ffff00")), // Yellow
                new Stop(0.6, Color.web("#00ff00")), // Green
                new Stop(0.8, Color.web("#0000ff")), // Blue
                new Stop(1.0, Color.web("#8b00ff"))  // End color
        );
        animatedText.setFill(gradient);
        splashScreen.getChildren().add(animatedText);
        StackPane.setAlignment(animatedText, Pos.TOP_CENTER);

        String word = "WORDLE";
        Timeline textAnimation = new Timeline();
        for (int i = 0; i <= word.length(); i++) {
            final int index = i; // Capture current letter index
            KeyFrame frame = new KeyFrame(Duration.seconds(i * 0.75), event -> {
                animatedText.setText(word.substring(0, Math.min(index + 1, word.length()))); // Reveal letters incrementally
            });
            textAnimation.getKeyFrames().add(frame);
        }
        textAnimation.setOnFinished(event -> {
            animatedText.setText(""); // Optionally clear the animation text
        });
        textAnimation.play();
        // Intro Pane (same as before)
        StackPane introPane = new StackPane();
        Text header = new Text("WORDLE");
        header.setFont(Font.font("Helvetica", FontWeight.BOLD, 28));
        header.setFill(Color.BLACK);


        Polygon triangle = new Polygon();
        triangle.getPoints().addAll(
                0.0, 0.0,  // Top corner
                100.0, 50.0, // Bottom right corner
                0.0, 100.0   // Bottom left corner
        );
        triangle.setFill(Color.RED);  // Fill color
        triangle.setStroke(Color.BLACK); // Border color

        triangle.setStrokeWidth(1);
        Button playBtn = new Button();
        playBtn.setGraphic(triangle);
        playBtn.setOnMouseEntered(e -> {
            triangle.setFill(Color.DARKRED); // Light red color on hover
        });
        playBtn.setOnMouseExited(e -> {
            triangle.setFill(Color.RED); // Revert to the original color
        });

        playBtn.setStyle("-fx-background-color: transparent;"); // Make the button background transparent
        introPane.setStyle("-fx-background-image: url('/com/example/javafxwordle/image3.jpg');"
                + "-fx-background-size: 100% 100%; -fx-background-position: center; -fx-background-repeat: no-repeat;");

        introPane.getChildren().addAll(playBtn);
        introPane.setAlignment(playBtn, Pos.CENTER);

        primaryStage.setHeight(800);
        primaryStage.setWidth(1500);

        userPreferences = Preferences.userNodeForPackage(Wordle.class);

        playBtn.setOnAction(_ -> {
            playClickSound();
            try {

                Thread.sleep(400);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            primaryStage.setScene(new Scene(createAuthPane(), 800, 1500));
        });

        Timeline timeline2 = new Timeline(new KeyFrame(Duration.seconds(3.75), event -> {
            primaryStage.setScene(new Scene(introPane, 800, 1500));
        }));
        timeline2.play();

        primaryStage.setScene(new Scene(splashScreen, 800, 1500));
        primaryStage.show();

    }
    private VBox createAuthPane()
    {
        VBox authPane = new VBox();
        authPane.setSpacing(20); // Increase spacing between elements
        authPane.setPadding(new Insets(40, 30, 40, 30)); // Add padding
        authPane.setAlignment(Pos.CENTER);

// Style for the VBox
        authPane.setStyle("-fx-background-color: linear-gradient(to bottom, #3c1053, #ad5389);"
                + "-fx-background-radius: 15; -fx-border-color: white; -fx-border-width: 2; -fx-border-radius: 15;");

        Text authHeader = new Text("Welcome to Wordle!");
        authHeader.setFont(Font.font("Helvetica", FontWeight.BOLD, 28));
        authHeader.setFill(Color.WHITE); // Change the text color to white


        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.setMaxWidth(250);
        passwordField.setMinWidth(30);
        ComboBox<String> usernameComboBox = new ComboBox<>();
        usernameComboBox.setEditable(true); // Allow manual input
        usernameComboBox.setPromptText("Select or enter username");
        usernameComboBox.setMaxWidth(250);
        CheckBox rememberMeCheckBox = new CheckBox("Remember Me");
        rememberMeCheckBox.setStyle("-fx-text-fill: white;");
        loadSavedUsernames(usernameComboBox);
        usernameComboBox.setOnAction(event -> {
            String selectedUsername = usernameComboBox.getValue();
            String savedPassword = getSavedPassword(selectedUsername);
            if (savedPassword != null) {
                passwordField.setText(savedPassword);
            } else {
                passwordField.clear();
            }
        });

        String buttonStyle = "-fx-background-color: #6CA965; "
                + "-fx-text-fill: white; "
                + "-fx-font-size: 18px; "
                + "-fx-font-weight: bold; "
                + "-fx-padding: 10 20 10 20; "
                + "-fx-background-radius: 10; "
                + "-fx-border-radius: 10; "
                + "-fx-cursor: hand;";

        Button loginBtn = new Button("Login");
        loginBtn.setStyle(buttonStyle);
        Button signupBtn = new Button("Sign Up");
        signupBtn.setStyle(buttonStyle);
        Button quitBtn2 = new Button("Quit");
        quitBtn2.setStyle(buttonStyle);
        String hoverStyle = "-fx-background-color: #5B8E49; "
                + "-fx-text-fill: white; "
                + "-fx-font-size: 18px; "
                + "-fx-font-weight: bold; "
                + "-fx-padding: 10 20 10 20; "
                + "-fx-background-radius: 10; "
                + "-fx-border-radius: 10; "
                + "-fx-border-width: 2; "
                + "-fx-border-color: transparent; "
                + "-fx-cursor: hand;";
        loginBtn.setOnMouseEntered(e -> loginBtn.setStyle(hoverStyle));
        loginBtn.setOnMouseExited(e -> loginBtn.setStyle(buttonStyle));
        signupBtn.setOnMouseEntered(e -> signupBtn.setStyle(hoverStyle));
        signupBtn.setOnMouseExited(e -> signupBtn.setStyle(buttonStyle));
        quitBtn2.setOnMouseEntered(e -> quitBtn2.setStyle(hoverStyle));
        quitBtn2.setOnMouseExited(e -> quitBtn2.setStyle(buttonStyle));
        quitBtn2.setOnAction(_ -> {
            playClickSound();
            try {
                // Add a delay of 500 milliseconds (change as needed)
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Platform.exit();
        });
        loginBtn.setStyle(buttonStyle);


// Create a container for the text elements

        loginBtn.setOnAction(e -> {
            playClickSound();
            String username = usernameComboBox.getValue();
            String password = passwordField.getText().trim();

            if (username == null || username.isEmpty() || password.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Please enter both username and password.");
                return;
            }

            if (MySQLUtility.validateUserCredentials(username, password)) {
                // Save login details if "Remember Me" is checked
                if (rememberMeCheckBox.isSelected()) {
                    saveUserCredentials(username, password);
                }

                startGame(primaryStage,createGamePane(username),username);
            } else {
                showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid username or password.");
            }
        });



        signupBtn.setStyle(buttonStyle);
        signupBtn.setOnAction(e -> {
            playClickSound();
            String username = usernameComboBox.getValue();
            String password = passwordField.getText().trim();

            if (username == null || username.isEmpty() || password.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Signup Failed", "Please enter both username and password.");
                return;
            }
            if (password.length() < 4) {
                showAlert(Alert.AlertType.ERROR, "Signup Failed", "Password must be at least 4 characters long.");
                return;
            }

            if (MySQLUtility.saveUserCredentials(username, password)) {
                showAlert(Alert.AlertType.INFORMATION, "Signup Successful", "You can now log in!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Signup Failed", "Username already exists.");
            }
        });
        authPane.getChildren().addAll(authHeader, usernameComboBox, passwordField, loginBtn, signupBtn, quitBtn2,rememberMeCheckBox);
       return  authPane;
    }
    private BorderPane createGamePane(String username)
    {

        BorderPane gamePane = new BorderPane();

        gamePane.setStyle("-fx-background-image: url('/com/example/javafxwordle/image5.jpeg');"
                + "-fx-background-size: cover; -fx-background-position: center;");
        gamePane.setCenter(resetWordBank());
        String buttonStyle = "-fx-background-color: #6CA965; "
                + "-fx-text-fill: white; "
                + "-fx-font-size: 18px; "
                + "-fx-font-weight: bold; "
                + "-fx-padding: 10 20 10 20; "
                + "-fx-background-radius: 10; "
                + "-fx-border-radius: 10; "
                + "-fx-cursor: hand;";
        String hoverStyle = "-fx-background-color: #5B8E49; "
                + "-fx-text-fill: white; "
                + "-fx-font-size: 18px; "
                + "-fx-font-weight: bold; "
                + "-fx-padding: 10 20 10 20; "
                + "-fx-background-radius: 10; "
                + "-fx-border-radius: 10; "
                + "-fx-border-width: 2; "
                + "-fx-border-color: transparent; "
                + "-fx-cursor: hand;";

        HBox toolboxPane = new HBox();
        SVGPath restartIcon = new SVGPath();
        restartIcon.setContent("M17 12C17 15.87 13.87 19 10 19C6.13 19 3 15.87 3 12C3 8.13 6.13 5 10 5C11.91 5 13.67 5.78 14.94 7H12V9H18V3H12V5H14.94C13.67 3.22 11.91 2 10 2C5.03 2 2 5.03 2 9C2 12.97 5.03 16 10 16C13.86 16 17 13.86 17 12Z");
        restartIcon.setFill(Color.WHITE); // Set the fill color for the icon

        Button restartBtn = new Button();
        restartBtn.setGraphic(restartIcon);
        restartBtn.setStyle(buttonStyle);
        Tooltip restartTooltip = new Tooltip("Restart Game");

// Apply the tooltip to the button
        Tooltip.install(restartBtn, restartTooltip);
        SVGPath instructionIcon = new SVGPath();
        instructionIcon.setContent("M9 2C9 1.44772 9.44772 1 10 1H14C14.5523 1 15 1.44772 15 2V22C15 22.5523 14.5523 23 14 23H10C9.44772 23 9 22.5523 9 22V2ZM10 3V21H14V3H10ZM6 6H18V8H6V6ZM6 10H18V12H6V10ZM6 14H18V16H6V14ZM6 18H18V20H6V18Z");
        instructionIcon.setFill(Color.WHITE);

// For Quit Button
        SVGPath quitIcon = new SVGPath();
        quitIcon.setContent("M18.7071 5.29289C19.0976 5.68342 19.0976 6.31658 18.7071 6.70711L13.4142 12L18.7071 17.2929C19.0976 17.6834 19.0976 18.3166 18.7071 18.7071C18.3166 19.0976 17.6834 19.0976 17.2929 18.7071L12 13.4142L6.70711 18.7071C6.31658 19.0976 5.68342 19.0976 5.29289 18.7071C4.90237 18.3166 4.90237 17.6834 5.29289 17.2929L10.5858 12L5.29289 6.70711C4.90237 6.31658 4.90237 5.68342 5.29289 5.29289C5.68342 4.90237 6.31658 4.90237 6.70711 5.29289L12 10.5858L17.2929 5.29289C17.6834 4.90237 18.3166 4.90237 18.7071 5.29289Z");
        quitIcon.setFill(Color.WHITE);
// Optionally, you can change the style of the tooltip
        restartTooltip.setStyle("-fx-background-color: #333; -fx-text-fill: white;");
        Button instructBtn = new Button();
        instructBtn.setGraphic(instructionIcon);
        Tooltip instructTooltip = new Tooltip("Instructions");

// Apply the tooltip to the button
        Tooltip.install(instructBtn, instructTooltip);

// Optionally, you can change the style of the tooltip
        restartTooltip.setStyle("-fx-background-color: #333; -fx-text-fill: white;");
        instructBtn.setStyle(buttonStyle);
        Button quitBtn = new Button();
        quitBtn.setGraphic(quitIcon);
        quitBtn.setStyle(buttonStyle);
        Tooltip quitTooltip = new Tooltip("Quit Game");

// Apply the tooltip to the button
        Tooltip.install(quitBtn, quitTooltip);

// Optionally, you can change the style of the tooltip
        restartTooltip.setStyle("-fx-background-color: #333; -fx-text-fill: white;");
        SVGPath leaderboardIcon = new SVGPath();
        leaderboardIcon.setContent("M3 3H7V21H3V3ZM10 10H14V21H10V10ZM17 5H21V21H17V5Z");
        leaderboardIcon.setFill(Color.WHITE); // Set the fill color for the icon

        Button showPlayersBtn = new Button();
        Tooltip leaderboardTooltip = new Tooltip("Leaderboard");

// Apply the tooltip to the button
        Tooltip.install(showPlayersBtn, leaderboardTooltip);

// Optionally, you can change the style of the tooltip
        restartTooltip.setStyle("-fx-background-color: #333; -fx-text-fill: white;");
        showPlayersBtn.setGraphic(leaderboardIcon);
        showPlayersBtn.setStyle(buttonStyle);
        showPlayersBtn.setOnAction(e -> {
            playClickSound();
            showPlayersTable(gamePane);
        });
        SVGPath backIcon = new SVGPath();
        backIcon.setContent("M15 18L9 12L15 6");

        backIcon.setFill(Color.WHITE);

// Style and wrap the arrow shape in a button
        Button backButton = new Button();
        Tooltip backTooltip = new Tooltip("Back");

// Apply the tooltip to the button
        Tooltip.install(backButton, backTooltip);
        backButton.setGraphic(backIcon);
        backButton.setStyle(buttonStyle); // Apply consistent styling
        backButton.setOnMouseEntered(e -> backButton.setStyle(hoverStyle));
        backButton.setOnMouseExited(e -> backButton.setStyle(buttonStyle));
        SVGPath friendsIcon = new SVGPath();
        friendsIcon.setContent("M22 12C22 15.3137 19.3137 18 16 18C12.6863 18 10 15.3137 10 12C10 8.68629 12.6863 6 16 6C19.3137 6 22 8.68629 22 12ZM6 20C8.20914 20 10 21.7909 10 24V26H2V24C2 21.7909 3.79086 20 6 20ZM20 20C22.2091 20 24 21.7909 24 24V26H12V24C12 21.7909 13.7909 20 16 20H20Z");
        friendsIcon.setFill(Color.WHITE);
        Button sendFriendRequestBtn = new Button();
        Tooltip friendsTooltip = new Tooltip("Friends");

// Apply the tooltip to the button
        Tooltip.install(sendFriendRequestBtn, friendsTooltip);

// Optionally, you can change the style of the tooltip
        restartTooltip.setStyle("-fx-background-color: #333; -fx-text-fill: white;");
        sendFriendRequestBtn.setGraphic(friendsIcon);
        sendFriendRequestBtn.setStyle(buttonStyle);
        sendFriendRequestBtn.setOnMouseEntered(e -> sendFriendRequestBtn.setStyle(hoverStyle));
        sendFriendRequestBtn.setOnMouseExited(e -> sendFriendRequestBtn.setStyle(buttonStyle));

        sendFriendRequestBtn.setOnAction(e -> {
            playClickSound();
            showFriendRequestDialog(username,gamePane);
        });
        SVGPath musicIcon = new SVGPath();
        musicIcon.setContent("M12 3V14.55C11.42 14.21 10.74 14 10 14C8.34 14 7 15.34 7 17C7 18.66 8.34 20 10 20C11.66 20 13 18.66 13 17V7H19V4H12Z"                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         );
        musicIcon.setFill(Color.WHITE);
        Button musicBtn = new Button();
        Tooltip musicTooltip = new Tooltip("Music");

// Apply the tooltip to the button
        Tooltip.install(musicBtn, musicTooltip);
         // Set the fill color
        musicBtn.setGraphic(musicIcon);
        Button chatButton = new Button();
        Tooltip chatTooltip = new Tooltip("Chat");

// Apply the tooltip to the button
        Tooltip.install(chatButton, chatTooltip);

// Optionally, you can change the style of the tooltip
        restartTooltip.setStyle("-fx-background-color: #333; -fx-text-fill: white;");
        SVGPath chatIcon = new SVGPath();
        chatIcon.setContent("M21 6H3C1.89543 6 1 6.89543 1 8V18C1 19.1046 1.89543 20 3 20H7L10 23L13 20H21C22.1046 20 23 19.1046 23 18V8C23 6.89543 22.1046 6 21 6ZM21 18H12.414L10 20.414L7.586 18H3V8H21V18Z");
        chatIcon.setFill(Color.WHITE);
        chatButton.setGraphic(chatIcon);
        chatButton.setStyle(buttonStyle);
        chatButton.setOnMouseEntered(e -> chatButton.setStyle(hoverStyle));
        chatButton.setOnMouseExited(e -> chatButton.setStyle(buttonStyle));
        chatButton.setOnAction(e -> {
            playClickSound();
            showChatUI(username, gamePane);
        });



// Handle the back button action
        backButton.setOnAction(e -> {
            playClickSound();
            resetGame(gamePane,toolboxPane);
            // Create a new scene for authPane to avoid IllegalArgumentException
            Scene authScene = new Scene(createAuthPane(), 800, 1500);
            primaryStage.setScene(authScene);
        });



        restartBtn.setOnMouseEntered(e -> restartBtn.setStyle(hoverStyle));
        restartBtn.setOnMouseExited(e -> restartBtn.setStyle(buttonStyle));
        instructBtn.setOnMouseEntered(e -> instructBtn.setStyle(hoverStyle));
        instructBtn.setOnMouseExited(e -> instructBtn.setStyle(buttonStyle));
        quitBtn.setOnMouseEntered(e -> quitBtn.setStyle(hoverStyle));
        quitBtn.setOnMouseExited(e -> quitBtn.setStyle(buttonStyle));
        showPlayersBtn.setOnMouseEntered(e -> showPlayersBtn.setStyle(hoverStyle));
        showPlayersBtn.setOnMouseExited(e -> showPlayersBtn.setStyle(buttonStyle));
        quitBtn.setOnAction(_ -> {
            playClickSound();
            try {
                // Add a delay of 500 milliseconds (change as needed)
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Platform.exit();
        });
        Stage instructionStage = new Stage();
        Image instructionImage = new Image("com/example/javafxwordle/ins.png"); // Specify your image path here
        ImageView instructionImageView = new ImageView(instructionImage);
        instructionImageView.setFitWidth(500); // Adjust the width of the image
        instructionImageView.setFitHeight(500); // Adjust the height of the image

// Arrange everything in a VBox layout
        VBox instructionPane = new VBox();
        VBox.setMargin(instructionImageView, new Insets(10, 20, 20, 20));

        instructionPane.getChildren().addAll( instructionImageView);

// Set the scene for the instructionStage
        Scene instructionScene = new Scene(instructionPane);
        instructionStage.setScene(instructionScene);

        instructBtn.setOnAction(e -> {
            playClickSound();
            instructionStage.show();
        });
        musicBtn.setStyle(buttonStyle);
        musicBtn.setOnMouseEntered(e -> musicBtn.setStyle(hoverStyle));
        musicBtn.setOnMouseExited(e -> musicBtn.setStyle(buttonStyle));
        musicBtn.setOnAction(e -> {
            // Toggle music play/pause when the button is clicked
            if (backgroundMusicPlayer != null && backgroundMusicPlayer.getStatus() == MediaPlayer.Status.PLAYING) {
                stopBackgroundMusic();

                 // You can change the icon to indicate stop
            } else {
                startBackgroundMusic();

                // You can change the icon to indicate play
            }
        });
        toolboxPane.setAlignment(Pos.CENTER);
        toolboxPane.setSpacing(10);
        toolboxPane.getChildren().addAll(new Text("Enter a guess!"), restartBtn, instructBtn, showPlayersBtn,sendFriendRequestBtn,chatButton,musicBtn,backButton, quitBtn);
        BorderPane.setMargin(toolboxPane, new Insets(12, 12, 12, 12));
        gamePane.setBottom(toolboxPane);

        Text text = new Text("Wordle!\n");
        Text definitionText = new Text("Word definition will appear here.");
        text.setFill(Color.BLACK);
        text.setFont(Font.font("Arial", FontWeight.BOLD, 30));
        definitionText.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, FontPosture.ITALIC, 16));
        definitionText.setFill(Color.BLACK);
        definitionText.setWrappingWidth(280); // Adjust wrapping width
        definitionText.setStyle("-fx-padding: 10; -fx-border-radius: 5;");

// Create a VBox to hold the text and definition text
        VBox textContainer = new VBox(5); // 5px spacing between the elements
        textContainer.setAlignment(Pos.CENTER);
        textContainer.getChildren().addAll(text, definitionText);

// Create a container for the text elements
        StackPane definitionContainer = new StackPane();
        definitionContainer.setPrefSize(300, 150); // Adjust size as needed
        definitionContainer.setStyle("-fx-background-color: transparent; -fx-padding: 10; -fx-border-radius: 5;");
        definitionContainer.setAlignment(Pos.TOP_CENTER);
        definitionContainer.getChildren().add(textContainer);
        BorderPane.setAlignment(definitionContainer, Pos.TOP_CENTER);
// Set the container on the right side of the game pane
        gamePane.setTop(definitionContainer);

        restartBtn.setOnAction(e -> {
            playClickSound();
            resetGame(gamePane, toolboxPane);
        });
        return gamePane;
    }

    public void startGame(Stage primaryStage, BorderPane gamePane, String username) {
        // Save player name to MySQL
        // Proceed to the game scene
        wordIndex=0;
        letterIndex=0;
        primaryStage.setScene(new Scene(gamePane, 800, 1500));

        // Focus on the game pane to allow key events (typing)
        gamePane.requestFocus(); // Ensures the game scene is ready to receive key events
        // Enable key events for the game pane and pass toolboxPane
        HBox toolboxPane = (HBox) gamePane.getBottom();
        enableKeyEvents(gamePane, toolboxPane, username);

    }


    private void enableKeyEvents(BorderPane gamePane, HBox toolboxPane, String username) {
        // Enable key events for the game pane after it has been displayed
        gamePane.setOnKeyReleased(e -> {
            if (wordIndex >= 6) return; // Game over condition
            KeyCode entered = e.getCode();

            if (entered == KeyCode.BACK_SPACE) {
                if (letterIndex > 0) {
                    wordBank[wordIndex][--letterIndex].setText("");
                }
            } else if (entered == KeyCode.ENTER) {
                handleEnter(gamePane, toolboxPane, username); // Pass toolboxPane as argument
            } else if (entered.isLetterKey() && letterIndex < 5 && !isChatOpen) {
                wordBank[wordIndex][letterIndex++].setText(entered.getName().toUpperCase());
            }
        });
    }

    private int calculateScore(boolean guessedCorrectly) {
        if (!guessedCorrectly) {
            return 0; // If the word isn't guessed, score should be 0
        }

        int baseScore = 600;  // Maximum points for guessing in 1 attempt
        int score = baseScore - (wordIndex * 100); // Deduct 100 points per attempt
        return Math.max(score, 0);  // Ensure the score doesn't go below 0
    }


    // Handle the "Enter" key logic
    private void handleEnter(BorderPane gamePane, HBox toolboxPane, String username) {
        if (letterIndex != 5) {
            new Alert(Alert.AlertType.ERROR, "You must enter a 5-letter word.").showAndWait();
            return;
        }
        Text text = new Text("Wordle!\n");
        Text definitionText = new Text("Word definition will appear here.");
        text.setFill(Color.BLACK);
        text.setFont(Font.font("Arial", FontWeight.BOLD, 30));
        definitionText.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, FontPosture.ITALIC, 16));
        definitionText.setFill(Color.BLACK);
        definitionText.setWrappingWidth(280); // Adjust wrapping width
        definitionText.setStyle("-fx-padding: 10; -fx-border-radius: 5;");

// Create a VBox to hold the text and definition text
        VBox textContainer = new VBox(5); // 5px spacing between the elements
        textContainer.setAlignment(Pos.CENTER);
        textContainer.getChildren().addAll(text, definitionText);

// Create a container for the text elements
        StackPane definitionContainer = new StackPane();
        definitionContainer.setPrefSize(300, 150); // Adjust size as needed
        definitionContainer.setStyle("-fx-background-color: transparent; -fx-padding: 10; -fx-border-radius: 5;");
        definitionContainer.setAlignment(Pos.TOP_CENTER);
        definitionContainer.getChildren().add(textContainer);
BorderPane.setAlignment(definitionContainer, Pos.TOP_CENTER);
// Set the container on the right side of the game pane
        gamePane.setTop(definitionContainer);



        String wordEntered = "";

        for (TextField tf : wordBank[wordIndex]) {
            wordEntered += tf.getText();
        }

        if (!wordList.contains(wordEntered.toLowerCase())) {
            new Alert(Alert.AlertType.ERROR, "Invalid word. Try again!").showAndWait();
            return;
        }

        // Get the feedback from the backend
        String feedback;
        try {
            feedback = backend.check(wordEntered); // Call the backend check method
        } catch (InvalidGuessException e) {
            new Alert(Alert.AlertType.ERROR, "Invalid guess. Try again!").showAndWait();
            return;
        }

        // Fetch the explanation of the word

        String wordExplanation = FreeDictionaryAPIIntegration.getWordDefinition(wordEntered.toLowerCase());
        definitionText.setText("Definition of \"" + wordEntered + "\": " + wordExplanation);
        // Apply feedback to word bank
        for (int i = 0; i < 5; i++) {
            char result = feedback.charAt(i);
            String feedbackColor;

            if (result == 'g') {
                feedbackColor = "#6ca965"; // Green for correct
            } else if (result == 'y') {
                feedbackColor = "#C8B653"; // Yellow for partial match
            } else {
                feedbackColor = "#787c7f"; // Grey for incorrect
            }

            // Apply the flip animation with the respective feedback color
            applyFlipAnimation(wordBank[wordIndex][i], feedbackColor);
        }


        // Display the word explanation


        // Check win condition
        // Calculate score
        if (wordEntered.equalsIgnoreCase(backend.getTarget())) {
            int score = calculateScore(true); // Pass true as the word is guessed
            toolboxPane.getChildren().set(0, new Text("Congratulations! You guessed the word! Score: " + score));
            saveScore(username, score); // Save the score to the database
        } else if (++wordIndex == 6) {
            int score = calculateScore(false); // Pass false as the word wasn't guessed
            toolboxPane.getChildren().set(0, new Text("Game over. The word was: " + backend.getTarget() + ". Score: " + score));
            saveScore(username, score); // Save the score to the database
        }

        letterIndex = 0;
    }


    // Reset the word bank grid
    public VBox resetWordBank() {
        VBox wordsPane = new VBox();
        wordBank = new TextField[6][5];
        wordsPane.setSpacing(10); // Increased spacing between rows
        wordsPane.setAlignment(Pos.CENTER);
        wordsPane.setMinSize(300, 300); // Set minimum size for the VBox

// Apply black stroke to the VBox
        for (int i = 0; i < 6; i++) {
            HBox wordPane = new HBox();
            wordPane.setAlignment(Pos.CENTER);
            wordPane.setSpacing(10); // Increased spacing between text fields
            for (int j = 0; j < 5; j++) {
                wordBank[i][j] = new TextField("");
                wordBank[i][j].setFont(Font.font("Arial", FontWeight.BOLD, 25));
                wordBank[i][j].setPrefSize(55, 50);
                wordBank[i][j].setEditable(false);
                wordBank[i][j].setStyle("-fx-border-color: black; -fx-border-width: 1;");
                wordPane.getChildren().add(wordBank[i][j]);
            }
            wordsPane.getChildren().add(wordPane);
        }
        wordsPane.setAlignment(Pos.CENTER);
        return wordsPane;
    }

    private void resetGame(BorderPane gamePane, HBox toolboxPane) {
        wordIndex = 0;  // Reset the current word index
        letterIndex = 0;  // Reset the current letter index

        backend.reset();  // Reset the backend target word


        // Refresh the word bank UI
        gamePane.setCenter(resetWordBank());

        // Set the toolbox to show the "Enter a guess!" message
        toolboxPane.getChildren().set(0, new Text("Enter a guess!"));
    }


    private void loadWords() {
        try (BufferedReader br = new BufferedReader(new FileReader("src/main/java/com/example/javafxwordle/words.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                wordList.add(line.trim().toLowerCase());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Add this method to store the score in the database
    private void saveScore(String playerName, int score) {
        System.out.println("Saving score for player: " + playerName + " with score: " + score);
        if (playerName == null || playerName.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Error", "Player name is not set. Unable to save the score.");
            return;
        }

        boolean success = MySQLUtility.saveOrUpdatePlayerName(playerName, score);
        if (!success) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to save the score to the database.");
        }
    }

    private void resetTarget() {
        Random random = new Random();
        target = wordList.get(random.nextInt(wordList.size())); // Get a new random word

    }

    private void showPlayersTable(BorderPane gamePane) {
        // Fetch the players table
        String buttonStyle = "-fx-background-color: #6CA965; "
                + "-fx-text-fill: white; "
                + "-fx-font-size: 18px; "
                + "-fx-font-weight: bold; "
                + "-fx-padding: 10 20 10 20; "
                + "-fx-background-radius: 10; "
                + "-fx-border-radius: 10; "
                + "-fx-cursor: hand;";
        String hoverStyle = "-fx-background-color: #5B8E49; "
                + "-fx-text-fill: white; "
                + "-fx-font-size: 18px; "
                + "-fx-font-weight: bold; "
                + "-fx-padding: 10 20 10 20; "
                + "-fx-background-radius: 10; "
                + "-fx-border-radius: 10; "
                + "-fx-border-width: 2; "
                + "-fx-border-color: transparent; "
                + "-fx-cursor: hand;";
        ArrayList<String> playersTable = MySQLUtility.getPlayersTable();

        // Create a VBox to display the players table
        VBox playersPane = new VBox();
        playersPane.setSpacing(10);
        playersPane.setPadding(new Insets(20));
        playersPane.setMinWidth(100);
        playersPane.setStyle("-fx-background-color: transparent; -fx-padding: 10;");
        playersPane.setAlignment(Pos.BOTTOM_LEFT);

        Text title = new Text("Players Table");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setFill(Color.BLACK);
        playersPane.getChildren().add(title);

        // Add each player's score to the VBox
        for (String entry : playersTable) {
            Text playerText = new Text(entry);
            playerText.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
            playerText.setFill(Color.BLACK);
            playersPane.getChildren().add(playerText);
        }

        // Create a small cross icon for the close button
        SVGPath closeIcon = new SVGPath();
        closeIcon.setContent("M18.7071 16.7071L13.7071 11.7071L18.7071 6.70711L17.2929 5.29289L12.2929 10.2929L7.29289 5.29289L5.87868 6.70711L10.8787 11.7071L5.87868 16.7071L7.29289 18.1213L12.2929 13.1213L17.2929 18.1213L18.7071 16.7071Z");
        closeIcon.setFill(Color.WHITE);
        Button closeBtn = new Button();
        closeBtn.setGraphic(closeIcon);
        closeBtn.setStyle(buttonStyle);
       closeBtn.setOnMouseEntered(e -> closeBtn.setStyle(hoverStyle));
        closeBtn.setOnMouseExited(e -> closeIcon.setStyle(buttonStyle));

        closeBtn.setOnAction(e -> gamePane.getChildren().remove(playersPane)); // Remove only the players pane

        // Add the close button to the top-right of the players pane
        HBox closeContainer = new HBox(closeBtn);
        closeContainer.setAlignment(Pos.TOP_RIGHT);
        playersPane.getChildren().add(closeContainer);

        // Set the players pane to the left side of the gamePane
        gamePane.setLeft(playersPane);
    }


    private void applyFlipAnimation(TextField tile, String feedbackColor) {
        // Create a rotate transition for the tile
        RotateTransition flip = new RotateTransition(Duration.millis(500), tile);
        flip.setFromAngle(0);
        flip.setToAngle(360);
        flip.setCycleCount(1);

        // Create a scale transition for a "pop" effect
        ScaleTransition scale = new ScaleTransition(Duration.millis(250), tile);
        scale.setFromX(1.0);
        scale.setToX(1.1);
        scale.setFromY(1.0);
        scale.setToY(1.1);
        scale.setCycleCount(2);
        scale.setAutoReverse(true); // Make it shrink back after the "pop"

        // Play scale first, then rotate
        SequentialTransition sequence = new SequentialTransition(scale, flip);
        sequence.setOnFinished(e -> {
            // Apply feedback color after the animation sequence
            tile.setStyle("-fx-text-fill: white; -fx-background-color: " + feedbackColor + "; -fx-font-weight: bold;");
        });

        sequence.play();
    }
    private void loadSavedUsernames(ComboBox<String> usernameComboBox) {
        int userCount = userPreferences.getInt(PREF_USER_COUNT, 0);
        for (int i = 1; i <= userCount; i++) {
            String username = userPreferences.get(PREF_USER_PREFIX + i + "_username", null);
            if (username != null) {
                usernameComboBox.getItems().add(username);
            }
        }
    }

    private String getSavedPassword(String username) {
        int userCount = userPreferences.getInt(PREF_USER_COUNT, 0);
        for (int i = 1; i <= userCount; i++) {
            String savedUsername = userPreferences.get(PREF_USER_PREFIX + i + "_username", null);
            if (username.equals(savedUsername)) {
                return userPreferences.get(PREF_USER_PREFIX + i + "_password", null);
            }
        }
        return null;
    }

    private void saveUserCredentials(String username, String password) {
        int userCount = userPreferences.getInt(PREF_USER_COUNT, 0);

        // Check if username already exists
        for (int i = 1; i <= userCount; i++) {
            String savedUsername = userPreferences.get(PREF_USER_PREFIX + i + "_username", null);
            if (username.equals(savedUsername)) {
                userPreferences.put(PREF_USER_PREFIX + i + "_password", password);
                return;
            }
        }

        // Save new username and password
        userCount++;
        userPreferences.putInt(PREF_USER_COUNT, userCount);
        userPreferences.put(PREF_USER_PREFIX + userCount + "_username", username);
        userPreferences.put(PREF_USER_PREFIX + userCount + "_password", password);
    }


    private void showFriendRequestDialog(String currentUsername, BorderPane gamePane) {
        isChatOpen=true;
        VBox dialogPane = new VBox(10);
        dialogPane.setPadding(new Insets(20));
        dialogPane.setAlignment(Pos.CENTER);
        dialogPane.setStyle("-fx-background-color: transparent; - -fx-border-radius: 5;");

        // Declare the refreshContent as an array to work around the "effectively final" issue
        Runnable[] refreshContent = new Runnable[1];

        refreshContent[0] = () -> {
            dialogPane.getChildren().clear();

            // Fetch and display pending requests
            ArrayList<String> pendingRequests = MySQLUtility.getFriendRequests(currentUsername);
            if (!pendingRequests.isEmpty()) {
                Label pendingHeader = new Label("Pending Friend Requests:");
                pendingHeader.setFont(Font.font("Arial", FontWeight.BOLD, 24));

                dialogPane.getChildren().add(pendingHeader);

                for (String sender : pendingRequests) {
                    HBox requestPane = new HBox(10);
                    requestPane.setAlignment(Pos.CENTER);
                    Label senderLabel = new Label(sender);

                    Button acceptBtn = new Button("Accept");
                    acceptBtn.setOnAction(e -> {
                        if (MySQLUtility.acceptFriendRequest(sender, currentUsername)) {
                            showAlert(Alert.AlertType.INFORMATION, "Request Accepted", "You are now friends with " + sender + "!");
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Error", "Failed to accept request.");
                        }
                        refreshContent[0].run();
                    });

                    Button rejectBtn = new Button("Reject");
                    rejectBtn.setOnAction(e -> {
                        if (MySQLUtility.rejectFriendRequest(sender, currentUsername)) {
                            showAlert(Alert.AlertType.INFORMATION, "Request Rejected", "You rejected " + sender + "'s request.");
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Error", "Failed to reject request.");
                        }
                        refreshContent[0].run();
                    });

                    requestPane.getChildren().addAll(senderLabel, acceptBtn, rejectBtn);
                    dialogPane.getChildren().add(requestPane);
                }
            }

            // Fetch and display friends list
            ArrayList<String> friends = MySQLUtility.getFriends(currentUsername);

            if (!friends.isEmpty()) {
                Label friendsHeader = new Label("Your Friends:");
                friendsHeader.setFont(Font.font("Arial", FontWeight.BOLD, 24));

                dialogPane.getChildren().add(friendsHeader);

                for (String friend : friends) {
                    Label friendLabel = new Label(friend);
                    dialogPane.getChildren().add(friendLabel);
                }
            }

            // Allow sending new friend requests
            Label label = new Label("Send a Friend Request:");
            TextField usernameField = new TextField();
            usernameField.setPromptText("Friend's username");

            Button sendRequestBtn = new Button("Send Request");
            sendRequestBtn.setOnAction(e -> {
                String receiver = usernameField.getText().trim();
                if (!receiver.isEmpty()) {
                    boolean success = MySQLUtility.sendFriendRequest(currentUsername, receiver);
                    if (success) {
                        showAlert(Alert.AlertType.INFORMATION, "Request Sent", "Friend request sent to " + receiver + "!");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Request Failed", "Unable to send request. It may already exist.");
                    }
                    refreshContent[0].run();
                } else {
                    showAlert(Alert.AlertType.WARNING, "Input Required", "Please enter a username.");
                }
            });
            SVGPath closeIcon = new SVGPath();
            closeIcon.setContent("M18.7071 16.7071L13.7071 11.7071L18.7071 6.70711L17.2929 5.29289L12.2929 10.2929L7.29289 5.29289L5.87868 6.70711L10.8787 11.7071L5.87868 16.7071L7.29289 18.1213L12.2929 13.1213L17.2929 18.1213L18.7071 16.7071Z");
            closeIcon.setFill(Color.BLACK);
            Button closeBtn=new Button();
            closeBtn.setGraphic(closeIcon);
            closeBtn.setOnAction(e -> {
                gamePane.getChildren().remove(dialogPane);
                isChatOpen=false;
                gamePane.requestFocus();
            });

            dialogPane.getChildren().addAll(label, usernameField, sendRequestBtn, closeBtn);
        };

        refreshContent[0].run();

        // Add dialogPane to the existing gamePane
         // Clear existing content if necessary
        gamePane.setRight(dialogPane);
    }
    private void showChatUI(String username, BorderPane gamePane) {
        isChatOpen =true;
        VBox chatBox = new VBox(10);
        chatBox.setPadding(new Insets(20));
        chatBox.setStyle("-fx-background-color: transparent;  -fx-border-radius: 5;");
        chatBox.setAlignment(Pos.TOP_LEFT);

        // Display chat messages
        ListView<String> chatList = new ListView<>();
        chatList.setPrefHeight(300);
        chatList.setMaxHeight(300);
        chatBox.getChildren().add(chatList);

        // Refresh method to update chat content dynamically
        Runnable refreshChat = () -> {
            ArrayList<String> messages = MySQLUtility.getChatMessages();
            chatList.getItems().clear();
            chatList.getItems().addAll(messages);
        };

        // Input field for new message
        TextField messageField = new TextField();
        messageField.setPromptText("Enter your message...");
        messageField.requestFocus();
        SVGPath sendIcon = new SVGPath();
        sendIcon.setContent("M2 2L22 12L2 22L5 12L2 2Z");
        sendIcon.setFill(Color.BLACK);
        Button sendMessageBtn = new Button();
        sendMessageBtn.setGraphic(sendIcon);
        SVGPath closeIcon = new SVGPath();
        closeIcon.setContent("M18.7071 16.7071L13.7071 11.7071L18.7071 6.70711L17.2929 5.29289L12.2929 10.2929L7.29289 5.29289L5.87868 6.70711L10.8787 11.7071L5.87868 16.7071L7.29289 18.1213L12.2929 13.1213L17.2929 18.1213L18.7071 16.7071Z");
        closeIcon.setFill(Color.BLACK);
        Button closeBtn=new Button();
        closeBtn.setGraphic(closeIcon);
        sendMessageBtn.setOnAction(e -> {
            String message = messageField.getText().trim();
            if (!message.isEmpty()) {
                boolean success = MySQLUtility.sendMessage(username, message);
                if (success) {
                    messageField.clear();
                    refreshChat.run();  // Refresh chat to show the new message
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to send message.");
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "Input Required", "Please enter a message.");
            }
        });
    closeBtn.setOnAction(e -> {
        gamePane.getChildren().remove(chatBox);
        isChatOpen=false;
        gamePane.requestFocus();
    });
        HBox inputBox = new HBox(10);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.getChildren().addAll(messageField, sendMessageBtn,closeBtn);
        chatBox.getChildren().add(inputBox);

        // Initially load the chat messages
        refreshChat.run();

        // Add the chatBox to the gamePane (the main pane of the game)
        // Optional: Clear existing content if needed
        gamePane.setRight(chatBox);
    }

    public void initializeSound() {
        try {
            // Initialize click sound player
            String clickSoundFile = "D:/click_sound.mp3"; // Use your correct file path
            Media clickSound = new Media(new File(clickSoundFile).toURI().toString());
            clickSoundPlayer = new MediaPlayer(clickSound);
            clickSoundPlayer.setVolume(0.5); // Set volume for click sound

            // Initialize background music player
            String musicFile = "D:/y2mate-com-harry-potter-ringtone-bgm-tone-54095.mp3"; // Replace with your file path
            Media backgroundMusic = new Media(new File(musicFile).toURI().toString());
            backgroundMusicPlayer = new MediaPlayer(backgroundMusic);
            backgroundMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE); // Loop the background music
            backgroundMusicPlayer.setVolume(0.5); // Set volume for background music
        } catch (Exception e) {
            System.err.println("Error loading sound: " + e.getMessage());
        }
    }

    // Call this to start background music
    public void startBackgroundMusic() {
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.play();
        }
    }

    // Call this to play click sound
    private void playClickSound() {
        if (clickSoundPlayer != null) {
            clickSoundPlayer.stop(); // Stop any ongoing click sound
            clickSoundPlayer.play(); // Play the new click sound immediately
        } else {
            System.err.println("Click sound player not initialized.");
        }
    }
    public void stopBackgroundMusic() {
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.pause(); // Pause the music
        }
    }

}