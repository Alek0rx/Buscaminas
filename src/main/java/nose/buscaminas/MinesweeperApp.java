/* JOSUE CARCELEN */
package nose.buscaminas;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Collections;
import java.util.HashSet;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.Pair;

public class MinesweeperApp extends Application {


    private static int COLS;
    private static int ROWS;
    private static final double MINE_PROBABILITY = 0.15;
    private int TOTAL_MINES;
    private Tile[][] grid;
    private boolean gameOver = false;
    private int minesRemaining;
    private Label mineCounterLabel = new Label();
    private Label timerLabel = new Label("000");
    private Button resetButton = new Button("Reiniciar");
    private Timeline timeline;
    private int secondsElapsed = 0;

    public interface BoardHandler {
        void handleLeftClick(Tile t);
        void handleRightClick(Tile t);
        void updateMineCounter(int delta);
    }

    @Override
    public void start(Stage stage) {
        showConfigurationDialog();

        Scene scene = new Scene(createContent());
        scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
        stage.setTitle("Buscaminas");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
        startGame();
    }

    private void startGame() {
        gameOver = false;
        secondsElapsed = 0;
        grid = new Tile[ROWS][COLS];

        minesRemaining = TOTAL_MINES;

        mineCounterLabel.setText(String.format("%03d", minesRemaining));
        timerLabel.setText(formatTime(secondsElapsed));

        if (timeline != null) {
            timeline.stop();
        }

        BorderPane root = (BorderPane) timerLabel.getScene().getRoot();
        root.setCenter(generateBoard());

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondsElapsed++;
            timerLabel.setText(formatTime(secondsElapsed));
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }


    private Parent createContent() {
        BorderPane root = new BorderPane();
        root.setPrefSize(40 * COLS + 60, 40 * ROWS + 120);

        mineCounterLabel.getStyleClass().add("game-display");
        timerLabel.getStyleClass().add("game-display");

        resetButton.setOnAction(e -> startGame());

        HBox topPanel = new HBox(20, mineCounterLabel, resetButton, timerLabel);
        topPanel.setPadding(new Insets(15, 15, 15, 15));
        topPanel.setStyle("-fx-alignment: center; -fx-background-color: #34495e;");

        root.setTop(topPanel);

        return root;
    }

    private GridPane generateBoard() {
        GridPane gridPane = new GridPane();
        gridPane.setStyle("-fx-padding: 20; -fx-alignment: center; -fx-hgap: 5; -fx-vgap: 5;");

        BoardHandler handler = new BoardHandler() {
            @Override
            public void handleLeftClick(Tile tile) {
                if (gameOver) return;

                if (tile.hasMine()) {
                    gameOver = true;
                    timeline.stop();
                    revealAll();
                    showAlert("¡BOOM!", "Has perdido.", Alert.AlertType.ERROR);
                } else {
                    openRecursively(tile);
                    checkWin();
                }
            }

            @Override
            public void handleRightClick(Tile tile) {
                if (!gameOver) tile.toggleFlag();
            }

            @Override
            public void updateMineCounter(int delta) {
                minesRemaining += delta;
                mineCounterLabel.setText(String.format("%03d", minesRemaining));
            }
        };
        List<Pair<Integer, Integer>> allPositions = new ArrayList<>();
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                allPositions.add(new Pair<>(x, y));
            }
        }
        int minesToPlace = Math.min(TOTAL_MINES, ROWS * COLS);
        Collections.shuffle(allPositions);
        List<Pair<Integer, Integer>> minePositions = allPositions.subList(0, minesToPlace);
        java.util.Set<Pair<Integer, Integer>> mineSet = new HashSet<>(minePositions);
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                boolean hasMine = mineSet.contains(new Pair<>(x, y));

                Tile tile = new Tile(x, y, hasMine, handler);
                grid[y][x] = tile;
                gridPane.add(tile, x, y);
            }
        }

        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                Tile t = grid[y][x];
                if (t.hasMine()) continue;

                long mines = getNeighbors(t).stream().filter(Tile::hasMine).count();
                t.setNeighborCount(mines);
            }
        }

        return gridPane;
    }

    private List<Tile> getNeighbors(Tile t) {
        List<Tile> neighbors = new ArrayList<>();
        int[] points = {-1, 0, 1};

        for (int dx : points) {
            for (int dy : points) {
                if (dx == 0 && dy == 0) continue;

                int newX = t.getX() + dx;
                int newY = t.getY() + dy;

                if (newX >= 0 && newX < COLS && newY >= 0 && newY < ROWS) {
                    neighbors.add(grid[newY][newX]);
                }
            }
        }
        return neighbors;
    }

    private void openRecursively(Tile t) {
        if (t.isOpen()) return;

        t.open();

        long minesNear = getNeighbors(t).stream().filter(Tile::hasMine).count();

        if (minesNear == 0) {
            getNeighbors(t).forEach(this::openRecursively);
        }
    }

    private void revealAll() {
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {
                grid[y][x].open();
            }
        }
    }

    private void checkWin() {
        if (gameOver) return;
        long hiddenTiles = 0;
        for (int y = 0; y < ROWS; y++) {
            for (int x = 0; x < COLS; x++) {

                if (!grid[y][x].isOpen()) {
                    hiddenTiles++;
                }
            }
        }

        if (hiddenTiles == TOTAL_MINES) {
            gameOver = true;
            timeline.stop();
            // formato de tiempo HH:MM:SS en el mensaje de victoria
            showAlert("¡Victoria!", "Felicidades, campo despejado en " + formatTime(secondsElapsed) + ".", Alert.AlertType.INFORMATION);
        }
    }

    private void showConfigurationDialog() {
        Optional<Pair<Integer, Integer[]>> result = ConfigurationDialog.showDialog();
        result.ifPresentOrElse(config -> {
            TOTAL_MINES = config.getKey();
            ROWS = config.getValue()[0];
            COLS = config.getValue()[1];
        }, () -> {
            System.exit(0);
        });
    }


    private String formatTime(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
    // --------------------------------------------------------

    private void showAlert(String header, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.show();
    }
}
