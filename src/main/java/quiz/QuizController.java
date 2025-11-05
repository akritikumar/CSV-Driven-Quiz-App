package quiz;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import java.util.List;

public class QuizController {

    @FXML private Label questionLabel, scoreLabel, timerLabel;
    @FXML private Button optionA, optionB, optionC, optionD, nextBtn, loadBtn;

    private List<QuizQuestion> questions;
    private int currentIndex = 0;
    private int score = 0;
    private QuizTimer timer = new QuizTimer();

    @FXML
    private void initialize() {
        setButtonsDisabled(true);
        nextBtn.setDisable(true);
        timer.timeRemainingProperty().addListener((obs, oldVal, newVal) ->
                timerLabel.setText("Time Left: " + newVal + "s"));
    }

    @FXML
    private void loadCSV(ActionEvent event) {
        Stage stage = (Stage) loadBtn.getScene().getWindow();
        questions = CSVReaderUtil.loadQuestions(stage);
        if (!questions.isEmpty()) {
            score = 0;
            currentIndex = 0;
            scoreLabel.setText("Score: 0");
            displayQuestion();
            setButtonsDisabled(false);
            startTimer();
        } else {
            questionLabel.setText("No questions loaded!");
        }
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

    private void startTimer() {
        timer.start(60, this::endQuiz);
    }

    private void endQuiz() {
        timer.stop();
        questionLabel.setText("Quiz Complete! Final Score: " + score + "/" + questions.size());
        setButtonsDisabled(true);
        nextBtn.setDisable(true);
    }
}
