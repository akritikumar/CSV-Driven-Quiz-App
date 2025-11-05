package quiz;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.*;
import java.util.*;

public class CSVReaderUtil {

    public static List<QuizQuestion> loadQuestions(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Quiz CSV File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showOpenDialog(stage);

        if (file == null) return Collections.emptyList();

        List<QuizQuestion> questions = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 6) {
                    String question = parts[0];
                    String[] options = { parts[1], parts[2], parts[3], parts[4] };
                    String correct = parts[5].trim();
                    questions.add(new QuizQuestion(question, options, correct));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return questions;
    }
}
