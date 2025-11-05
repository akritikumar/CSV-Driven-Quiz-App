package quiz;

import javafx.scene.control.TextInputDialog;
import java.util.Optional;

public class UsernameDialog {
    public static String getUsername() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Enter Username");
        dialog.setHeaderText("Welcome to the Quiz!");
        dialog.setContentText("Enter your name:");

        Optional<String> result = dialog.showAndWait();
        return result.orElse("Anonymous");
    }
}