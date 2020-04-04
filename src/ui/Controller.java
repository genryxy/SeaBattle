package ui;

import common.Callback;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;


public class Controller {
    private Callback callback;
    private int x;
    private int y;

    @FXML
    private TextField txtYCoord;
    @FXML
    private TextField txtXCoord;
    @FXML
    private Text txtLogging;
    @FXML
    private Button btnShoot;
    @FXML
    private Text txtInfo;
    @FXML
    private GridPane gridBattleField;

    @FXML
    private void handleBtnShoot() {
        if (checkInputValues()) {
            callback.doShootAtCoordinates(x, y);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText("Coordinates must be from [0,9]. Please, enter other values");
            alert.showAndWait();
        }
    }

    void registerCallback(Callback callback) {
        this.callback = callback;
    }

    /**
     * It sets text in the Text txtLogging from .fxml
     *
     * @param text The content of Text.
     */
    void setTxtLogging(String text) {
        txtLogging.setText(text);
    }

    /**
     * It sets text in the Text txtInfo from .fxml
     *
     * @param text The content of Text.
     */
    void setTxtInfo(String text) {
        txtInfo.setText(text);
    }

    /**
     * @return Return the GridPane (battlefield).
     */
    GridPane getGridBattleField() {
        return gridBattleField;
    }

    /**
     * Checks the input values.
     *
     * @return true - valid input (x,y from [0,9]), false - otherwise
     */
    private boolean checkInputValues() {
        try {
            x = Integer.parseInt(txtXCoord.getCharacters().toString());
            y = Integer.parseInt(txtYCoord.getCharacters().toString());
            if (x < 0 || x > 9 || y < 0 || y > 9) {
                return false;
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
