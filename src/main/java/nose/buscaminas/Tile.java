package nose.buscaminas;

import javafx.animation.ScaleTransition;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.util.Duration;


public class Tile extends StackPane {
 
    private int x, y;
    private boolean hasMine;
    private boolean isOpen = false;
    private boolean isFlagged = false;

    private Text text = new Text();
    private MinesweeperApp.BoardHandler handler; 

    public Tile(int x, int y, boolean hasMine, MinesweeperApp.BoardHandler handler) {
        this.x = x;
        this.y = y;
        this.hasMine = hasMine;
        this.handler = handler;

        // Configuración visual inicial
        this.getStyleClass().add("tile");
        this.setPrefSize(40, 40);

        text.getStyleClass().add("tile-text");
        text.setVisible(false);
        this.getChildren().add(text);

        // Eventos de Mouse
        this.setOnMouseClicked(e -> {
            if (e.getButton().name().equals("PRIMARY")) {
                handler.handleLeftClick(this);
            } else if (e.getButton().name().equals("SECONDARY")) {
                handler.handleRightClick(this);
            }
        });
    }

    // Método para abrir la casilla con animación (igual)
    public void open() {
        if (isOpen || isFlagged) return;

        isOpen = true;
        text.setVisible(true);

       
        if (isFlagged) {
            isFlagged = false;

            handler.updateMineCounter(1);
        }

        this.getStyleClass().remove("tile");
        this.getStyleClass().remove("tile-flagged"); // Asegurar que el estilo de bandera se quite
        this.getStyleClass().add("tile-revealed");

        // ANIMACIÓN: Efecto de "pop" al abrir
        ScaleTransition st = new ScaleTransition(Duration.millis(200), this);
        st.setFromX(0.5);
        st.setFromY(0.5);
        st.setToX(1.0);
        st.setToY(1.0);
        st.play();

        if (hasMine) {
            this.getStyleClass().add("tile-bomb");
            text.setText("💣");
        }
    }


    public void toggleFlag() {
        if (isOpen) return;

        isFlagged = !isFlagged;
        if (isFlagged) {
            this.getStyleClass().add("tile-flagged");
            text.setText("🚩");
            text.setVisible(true);
            
            handler.updateMineCounter(-1);
        } else {
            this.getStyleClass().remove("tile-flagged");
            text.setVisible(false);
            text.setText("");
           
            handler.updateMineCounter(1);
        }
    }

    // Setters visuales (igual)
    public void setNeighborCount(long count) {
        if (!hasMine && count > 0) {
            text.setText(String.valueOf(count));
            text.getStyleClass().add("text-" + count);
        }
    }

    // Getters necesarios (igual)
    public boolean hasMine() { return hasMine; }
    public boolean isOpen() { return isOpen; }
    public int getX() { return x; }
    public int getY() { return y; }
}
