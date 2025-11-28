/* JOSUE CARCELEN */
package nose.buscaminas;

import java.util.Optional;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import javax.swing.*;

public class ConfigurationDialog {

    public static Optional<Pair<Integer, Integer[]>> showDialog() {
        Dialog<Pair<Integer, Integer[]>> dialog = new Dialog<>();
        dialog.setTitle("Configuración del Buscaminas");
        dialog.setHeaderText("Define las dimensiones del tablero y la cantidad de minas.");


        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Campos de texto para la entrada
        TextField rowsField = new TextField("8");
        TextField colsField = new TextField("8");
        TextField minesField = new TextField("6");

        grid.add(new Label("Filas (Min 8, Max 30):"), 0, 0);
        grid.add(rowsField, 1, 0);
        grid.add(new Label("Columnas (Min 8, Max 30):"), 0, 1);
        grid.add(colsField, 1, 1);
        grid.add(new Label("Minas :"), 0, 2);
        grid.add(minesField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                try {
                    int rows = Math.min(Math.max(Integer.parseInt(rowsField.getText()), 8), 30);
                    int cols = Math.min(Math.max(Integer.parseInt(colsField.getText()), 8), 30);
                    int maxMines = (int) (rows * cols * 0.2);
//                    int mines = Math.min(Math.max(Integer.parseInt(minesField.getText()), 1), maxMines);
                    int mines = Integer.parseInt(minesField.getText());
                    return new Pair<>(mines, new Integer[]{rows, cols});
                } catch (NumberFormatException e) {

                    return new Pair<>(40, new Integer[]{16, 16});
                }
            }
            return null;
        });

        return dialog.showAndWait();
    }
}
