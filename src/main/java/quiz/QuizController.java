package quiz;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.util.List;
import java.util.Optional;

public class QuizController {

    @FXML private Label questionLabel, scoreLabel, timerLabel;
    @FXML private Button optionA, optionB, optionC, optionD;
    @FXML private Button nextBtn, loadBtn, startBtn;

    private List<QuizQuestion> questions;
    private int currentIndex = 0;
    private int score = 0;
    private QuizTimer timer = new QuizTimer();
    private boolean quizStarted = false;
    private String username;

    @FXML
    private void initialize() {
        setButtonsDisabled(true);
        nextBtn.setDisable(true);
        startBtn.setDisable(true);
        timer.timeRemainingProperty().addListener((obs, oldVal, newVal) ->
                timerLabel.setText("Time Left: " + newVal + "s"));

        username = UsernameDialog.getUsername();
    }

    @FXML
    private void loadCSV(ActionEvent event) {
        Stage stage = (Stage) loadBtn.getScene().getWindow();
        questions = CSVReaderUtil.loadQuestions(stage);
        if (!questions.isEmpty()) {
            scoreLabel.setText("Score: 0");
            questionLabel.setText("Quiz loaded successfully! Click 'Start Quiz' to begin.");
            startBtn.setDisable(false);
        } else {
            questionLabel.setText("No questions loaded!");
        }
    }

    @FXML
    private void startQuiz(ActionEvent event) {
        if (questions == null || questions.isEmpty()) {
            questionLabel.setText("Please load a quiz file first!");
            return;
        }
        quizStarted = true;
        score = 0;
        currentIndex = 0;
        displayQuestion();
        setButtonsDisabled(false);
        nextBtn.setDisable(true);
        startBtn.setDisable(true);
        loadBtn.setDisable(true);
        timer.start(60, this::endQuiz); // 60s countdown
    }

    private void displayQuestion() {
        if (currentIndex >= questions.size()) {
            endQuiz();
            return;
        }
        QuizQuestion q = questions.get(currentIndex);
        questionLabel.setText(q.getQuestion());
        String[] opts = q.getOptions();
        optionA.setText(opts[0]);
        optionB.setText(opts[1]);
        optionC.setText(opts[2]);
        optionD.setText(opts[3]);
        nextBtn.setDisable(true);
    }

    @FXML
    private void handleOption(ActionEvent e) {
        if (!quizStarted) return;
        Button selected = (Button) e.getSource();
        String answer = selected.getText();
        QuizQuestion current = questions.get(currentIndex);

        if (answer.equalsIgnoreCase(current.getCorrectAnswer())) {
            score++;
            scoreLabel.setText("Score: " + score);
        }

        setButtonsDisabled(true);
        nextBtn.setDisable(false);
    }

    @FXML
    private void nextQuestion() {
        currentIndex++;
        setButtonsDisabled(false);
        displayQuestion();
    }

    private void setButtonsDisabled(boolean state) {
        optionA.setDisable(state);
        optionB.setDisable(state);
        optionC.setDisable(state);
        optionD.setDisable(state);
    }

    /**
     * Shows a dialog with final score and restart/exit options
     */
    private void showFinalScore(String username, int score) {
        // Create the alert
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Quiz Complete!");
        alert.setHeaderText("Good job, " + username + "!");
        alert.setContentText("Your final score is: " + score + "/" + questions.size());

        // Add Restart / Exit buttons
        ButtonType restartButton = new ButtonType("Restart");
        ButtonType exitButton = new ButtonType("Exit");
        alert.getButtonTypes().setAll(restartButton, exitButton);

        // Show and wait for user choice
        Optional<ButtonType> response = alert.showAndWait();

        if (response.isPresent() && response.get() == restartButton) {
            restartQuiz();
        } else {
            System.exit(0);
        }
    }

    /**
     * Called when quiz ends (timer runs out or all questions answered)
     */
    private void endQuiz() {
        quizStarted = false;
        timer.stop();

        ResultDAO resultDAO = new ResultDAO();
        resultDAO.saveResult(username, score);

        showFinalScore(username, score);

        questionLabel.setText("Quiz Complete! Final Score: " + score + "/" + questions.size());
        setButtonsDisabled(true);
        nextBtn.setDisable(true);
        startBtn.setDisable(false);
        loadBtn.setDisable(false);
    }

    /**
     * Restarts the quiz with the same loaded questions
     */
    private void restartQuiz() {
        score = 0;
        currentIndex = 0;
        scoreLabel.setText("Score: 0");
        timer.stop();
        timer.start(60, this::endQuiz);
        quizStarted = true;
        setButtonsDisabled(false);
        displayQuestion();
        nextBtn.setDisable(true);
        startBtn.setDisable(true);
        loadBtn.setDisable(true);
    }
}
