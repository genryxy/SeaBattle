package ui;

import battleship.Ocean;
import battleship.Ship;
import common.Callback;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Optional;


public class Main extends Application implements Callback {

    private static Ocean ocean;

    private StringBuilder loggingMsg = new StringBuilder();
    private Controller controller;

    public static void main(String[] args) {
        ocean = new Ocean();
        ocean.placeAllShipsRandomly();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
        Parent root = loader.load();
        primaryStage.setMinHeight(450);
        primaryStage.setMinWidth(400);
        primaryStage.setTitle("Battleship");
        primaryStage.setScene(new Scene(root, 600, 600));
        primaryStage.show();

        controller = loader.getController();
        controller.setTxtInfo(createInfoTextAboutShot());
        // Register callback in order to shoot with textFields.
        controller.registerCallback(this);

        for (int i = 0; i < ocean.SIZE + 1; i++) {
            for (int j = 0; j < ocean.SIZE + 1; j++) {
                if (i == 0 && j == 0) {
                    // skip this cell
                } else if (i == 0) {
                    Label lbl = new Label(String.valueOf(j - 1));
                    GridPane.setHalignment(lbl, HPos.CENTER);
                    controller.getGridBattleField().add(lbl, 0, j);
                } else if (j == 0) {
                    Label lbl = new Label(String.valueOf(i - 1));
                    GridPane.setHalignment(lbl, HPos.CENTER);
                    controller.getGridBattleField().add(lbl, i, 0);
                } else {
                    controller.getGridBattleField().add(createButton(""), j, i);
                }
            }
        }
    }

    /**
     * Callback method allows to programmatically click on the button with certain coordinates.
     *
     * @param row    The index of row in GridPane.
     * @param column The index of column in GridPane.
     */
    @Override
    public void doShootAtCoordinates(int row, int column) {
        // GridPane contains labels in the first row and in the first column (except
        // position [0,0]). Shape(11x11). Children store in ObservableList<Node>.
        ((Button) controller.getGridBattleField().getChildren().get(row * ocean.SIZE + column + 10 + (row + 1))).fire();
    }

    /**
     * Creates a new instance of the button with  specified parameters.
     * Also adds action listener.
     *
     * @param text The content of the button.
     * @return The new instance of button.
     */
    private Button createButton(String text) {
        Button btn = new Button(text);
        setBtnBackground(btn, Color.LIGHTBLUE);
        // Allows to resize the button.
        btn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        btn.setOnAction(actionEvent -> {
            clickListener(btn);
        });
        return btn;
    }

    /**
     * The method with the main logic of interaction with the user. Checks end of the game,
     * takes shots, changes color, write actions of the user.
     *
     * @param btn The instance of Button for determination of coordinates in GridPane.
     */
    private void clickListener(Button btn) {
        if (!ocean.isGameOver()) {
            // We need to subtract 1 because GridPane store labels in the
            // first row and in the first column.
            int row = GridPane.getRowIndex(btn) - 1;
            int column = GridPane.getColumnIndex(btn) - 1;
            if (ocean.getShipsArray()[row][column].isAlreadyFired(row, column)) {
                loggingMsg.append("attempt to shoot the marked cell\n");
                createAlertRepeatedShot();
            } else {
                ocean.shootAt(row, column);
                setBtnBackground(btn, Color.SILVER);
                btn.setText(ocean.getShipsArray()[row][column].toString());
                if (ocean.getShipsArray()[row][column].isSunk()) {
                    markAreaAroundShip(ocean.getShipsArray()[row][column]);
                }
                controller.setTxtInfo(createInfoTextAboutShot());
                loggingMsg.append(ocean.getShotsFired()).append(". Move: ").append(row)
                        .append(",").append(column).append("\n");
                loggingMsg.append(ocean.getInfoAboutShot());
                if (ocean.isGameOver()) {
                    createAlertPlayAgain();
                }
            }
            controller.setTxtLogging(loggingMsg.toString());
        } else {
            createAlertPlayAgain();
        }
    }

    /**
     * Creates alert that confirms the end of the game. If user press 'ok',
     * he'll begin a new game. Otherwise nothing happens.
     */
    private void createAlertPlayAgain() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Choice");
        alert.setHeaderText(null);
        alert.setContentText("The game is over! Do you want to play again?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            reset();
        }
    }

    /**
     * Creates a new instance of Ocean.Resets values of fields.
     * Reset the background color of the buttons.
     */
    private void reset() {
        ocean = new Ocean();
        ocean.placeAllShipsRandomly();
        for (Object obj : controller.getGridBattleField().getChildren()) {
            if (obj instanceof Button) {
                Button btn = (Button) obj;
                btn.setText("");
                setBtnBackground(btn, Color.LIGHTBLUE);
            }
        }
        controller.setTxtInfo(createInfoTextAboutShot());
        loggingMsg = new StringBuilder();
        controller.setTxtLogging(loggingMsg.toString());
    }

    /**
     * Creates alert with warning about repeating shot in the cell.
     */
    private void createAlertRepeatedShot() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText("You've already fired at this cell. Please, try to shoot at another cell.");
        alert.showAndWait();
    }

    /**
     * @return Gives some information about the current game. (Number shots,
     * hits, undamaged ships, partially damaged, sunk)
     */
    private String createInfoTextAboutShot() {
        return "Number shots: " + ocean.getShotsFired() +
                "\nNumber hits: " + ocean.getHitCount() +
                "\nUndamaged: " + (10 - ocean.getShipsSunk() - ocean.getShipWrecked()) +
                "\nPartially damaged: " + ocean.getShipWrecked() +
                "\nSunk: " + ocean.getShipsSunk();
    }

    /**
     * We will help player by marking the area around the sunken ship
     * to improve the game's usability.
     *
     * @param ship The instance of the sunken ship.
     */
    private void markAreaAroundShip(Ship ship) {
        int addRow = ship.isHorizontal() ? 1 : ship.getLength();
        int addColumn = ship.isHorizontal() ? ship.getLength() : 1;

        // GridPane contains labels in the first row and in the first column
        // (except position [0,0]). Shape(11x11).
        for (int i = Math.max(0, ship.getBowRow() - 1); i <= Math.min(ocean.SIZE - 1, ship.getBowRow() + addRow); i++) {
            for (int j = Math.max(0, ship.getBowColumn() - 1); j <= Math.min(ocean.SIZE - 1, ship.getBowColumn() + addColumn); j++) {
                // Children store in ObservableList<Node>.
                Button currBtn = (Button) controller.getGridBattleField().getChildren().get(i * ocean.SIZE + j + 10 + (i + 1));
                currBtn.setText(ocean.getShipsArray()[i][j].toString());
                setBtnBackground(currBtn, Color.SILVER);
                if (ocean.getShipsArray()[i][j].isSunk()) {
                    setBtnBackground(currBtn, Color.RED);
                }
            }
        }
    }

    /**
     * It sets the button's background. It helps to have the similar
     * values of corner radius and insets.
     *
     * @param btn   The instance of button for changing.
     * @param color The required color of background.
     */
    private void setBtnBackground(Button btn, Color color) {
        btn.setBackground(new Background(new BackgroundFill(color, new CornerRadii(3), new Insets(0.5))));
    }
}