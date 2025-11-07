package quiz;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static quiz.DBUtil.saveResult;

public class QuizController {

    @FXML private Label questionLabel;
    @FXML private Label scoreLabel;
    @FXML private Label timerLabel;

    @FXML private Button loadBtn;
    @FXML private Button startBtn;
    @FXML private Button nextBtn;

    @FXML private Button optionA;
    @FXML private Button optionB;
    @FXML private Button optionC;
    @FXML private Button optionD;

    // Quiz state
    private List<QuizQuestion> questions;
    private int currentIndex = 0;
    private int score = 0;
    private boolean quizStarted = false;
    private String username = "";

    // Timer helper (assumes you have a QuizTimer class with start(seconds, onFinish) and stop())
    private final QuizTimer timer = new QuizTimer();

    @FXML
    private void initialize() {
        // Initially disable option buttons until quiz started
        setButtonsDisabled(true);
        nextBtn.setDisable(true);
        startBtn.setDisable(true);

        // Bind timer label updates
        timer.timeRemainingProperty().addListener((obs, oldV, newV) ->
                timerLabel.setText("Time Left: " + newV + "s"));

        // Default labels
        scoreLabel.setText("Score: 0");
        timerLabel.setText("Time Left: 0s");
        questionLabel.setText("Load a quiz to begin!");
    }

    /**
     * Load CSV file and parse questions. Enables Start button if successful.
     */
    @FXML
    private void loadCSV(ActionEvent event) {
        Stage stage = (Stage) loadBtn.getScene().getWindow();
        List<QuizQuestion> loaded = CSVReaderUtil.loadQuestions(stage);
        if (loaded != null && !loaded.isEmpty()) {
            this.questions = loaded;
            score = 0;
            currentIndex = 0;
            scoreLabel.setText("Score: 0");
            questionLabel.setText("Quiz loaded successfully. Click Start Quiz to begin.");
            startBtn.setDisable(false);
            setButtonsDisabled(true);
            nextBtn.setDisable(true);
        } else {
            questionLabel.setText("No questions loaded or file invalid.");
            startBtn.setDisable(true);
        }
    }

    /**
     * Prompt for username and start the quiz (start timer + display first question).
     */
    @FXML
    private void startQuiz(ActionEvent event) {
        if (questions == null || questions.isEmpty()) {
            questionLabel.setText("Please load a quiz file first!");
            return;
        }

        // Prompt username
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Enter Username");
        dialog.setHeaderText("Please enter your username");
        dialog.setContentText("Username:");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty() || result.get().trim().isEmpty()) {
            questionLabel.setText("Username required to start the quiz.");
            return;
        }
        username = result.get().trim();

        // initialize quiz state
        quizStarted = true;
        score = 0;
        currentIndex = 0;
        scoreLabel.setText("Score: 0");
        startBtn.setDisable(true);
        loadBtn.setDisable(true);
        setButtonsDisabled(false);
        nextBtn.setDisable(true);

        // Display first question
        displayQuestion();

        // Start timer (example: 60 seconds; you can make configurable)
        timer.start(60, this::endQuiz);
    }

    /**
     * Display the current question (or end quiz if beyond last).
     */
    private void displayQuestion() {
        if (questions == null || currentIndex >= questions.size()) {
            endQuiz();
            return;
        }
        QuizQuestion q = questions.get(currentIndex);
        questionLabel.setText((currentIndex + 1) + ". " + q.getQuestion());

        String[] opts = q.getOptions();
        optionA.setText(opts.length > 0 ? opts[0] : "");
        optionB.setText(opts.length > 1 ? opts[1] : "");
        optionC.setText(opts.length > 2 ? opts[2] : "");
        optionD.setText(opts.length > 3 ? opts[3] : "");

        // reset enable/disable and visual states
        setButtonsDisabled(false);
        nextBtn.setDisable(true);
    }

    /**
     * Handler for option buttons A-D.
     */
    @FXML
    private void handleOption(ActionEvent event) {
        if (!quizStarted) return;
        Button selected = (Button) event.getSource();
        if (selected == null) return;

        // guard: if already answered, ignore
        if (nextBtn.isDisable() == false && setOfOptionButtonsDisabled()) {
            // if nextBtn is enabled and option buttons disabled, it means user already answered
            return;
        }

        String selectedText = selected.getText();
        QuizQuestion current = questions.get(currentIndex);
        boolean correct = current.isCorrect(selectedText);

        if (correct) {
            score++;
            scoreLabel.setText("Score: " + score);
            // optional: color green; but keep styling in CSS or inline
            selected.setStyle("-fx-background-color: linear-gradient(#00c853, #00b26a); -fx-text-fill: white;");
        } else {
            // highlight selected red and show correct green
            selected.setStyle("-fx-background-color: linear-gradient(#ff5252, #ff1744); -fx-text-fill: white;");
            Button correctBtn = findButtonForAnswer(current.getCorrectAnswerNormalized());
            if (correctBtn != null) {
                correctBtn.setStyle("-fx-background-color: linear-gradient(#00c853, #00b26a); -fx-text-fill: white;");
            }
        }

        // After answering, disable options and enable next
        setButtonsDisabled(true);
        nextBtn.setDisable(false);
    }

    /**
     * Move to next question when Next Question button clicked.
     */
    @FXML
    private void nextQuestion(ActionEvent event) {
        currentIndex++;
        // reset button styles
        resetOptionStyles();
        if (currentIndex >= questions.size()) {
            endQuiz();
            return;
        }
        setButtonsDisabled(false);
        nextBtn.setDisable(true);
        displayQuestion();
    }

    private void showResultSavedAlert() {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Quiz Completed");
        alert.setHeaderText("Result Saved");
        alert.setContentText("Your score has been saved to the database successfully!");
        alert.showAndWait();
    }

    private void showLeaderboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/quiz/LeaderboardView.fxml"));
            Parent root = loader.load();

            LeaderboardController controller = loader.getController();
            controller.loadLeaderboard();

            Stage stage = new Stage();
            stage.setTitle("Leaderboard");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Ends the quiz: stops timer, disables UI, saves result to DB.
     */
    private void endQuiz() {
        quizStarted = false;
        timer.stop();

        questionLabel.setText("Quiz Complete! Final Score: " + score + "/" + questions.size());
        setButtonsDisabled(true);
        nextBtn.setDisable(true);
        startBtn.setDisable(false);
        loadBtn.setDisable(false);

        if (username != null && !username.isEmpty()) {
            DBUtil.saveResult(username, score);
            showLeaderboard(); // ✅ Show leaderboard immediately
        } else {
            System.out.println("⚠️ No username set, skipping DB save.");
        }
    }

    // -----------------------
    // Helper utility methods
    // -----------------------

    /**
     * Enable/disable all four option buttons.
     * This is the method you reported an error about — it's defined here.
     */
    private void setButtonsDisabled(boolean disabled) {
        optionA.setDisable(disabled);
        optionB.setDisable(disabled);
        optionC.setDisable(disabled);
        optionD.setDisable(disabled);
    }

    /**
     * Returns true if option buttons are currently disabled.
     */
    private boolean setOfOptionButtonsDisabled() {
        return optionA.isDisabled() && optionB.isDisabled() && optionC.isDisabled() && optionD.isDisabled();
    }

    /**
     * Reset inline styles applied to option buttons (useful between questions).
     */
    private void resetOptionStyles() {
        optionA.setStyle(null);
        optionB.setStyle(null);
        optionC.setStyle(null);
        optionD.setStyle(null);
    }

    /**
     * Find which Button corresponds to a normalized answer string.
     * The parameter should be normalized (lowercase, trimmed) if necessary.
     */
    private Button findButtonForAnswer(String normalizedAnswer) {
        if (normalizedAnswer == null) return null;
        String n = normalizedAnswer.trim().toLowerCase();

        if (normalize(optionA.getText()).equals(n)) return optionA;
        if (normalize(optionB.getText()).equals(n)) return optionB;
        if (normalize(optionC.getText()).equals(n)) return optionC;
        if (normalize(optionD.getText()).equals(n)) return optionD;
        return null;
    }

    private String normalize(String s) {
        return s == null ? "" : s.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
