package quiz;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.control.Button;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class LeaderboardController {

    @FXML
    private TableView<LeaderboardEntry> leaderboardTable;
    @FXML
    private TableColumn<LeaderboardEntry, String> usernameCol;
    @FXML
    private TableColumn<LeaderboardEntry, Integer> scoreCol;
    @FXML
    private TableColumn<LeaderboardEntry, String> dateCol;
    @FXML
    private Button closeBtn;

    public void loadLeaderboard() {
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));
        dateCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getQuizDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                ));

        List<LeaderboardEntry> entries = DBUtil.getLeaderboard();
        leaderboardTable.setItems(FXCollections.observableArrayList(entries));
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) closeBtn.getScene().getWindow();
        stage.close();
    }
}
