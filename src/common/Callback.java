package common;

public interface Callback {
    /**
     * Callback method in order to shoot with textFields.
     *
     * @param row    The index of row in the GridPane.
     * @param column The index of column in the GridPane.
     */
    void doShootAtCoordinates(int row, int column);
}
