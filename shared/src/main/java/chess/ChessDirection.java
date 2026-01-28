package chess;

public class ChessDirection {

    public static final ChessDirection UP = new ChessDirection(1, 0);
    public static final ChessDirection DOWN = new ChessDirection(-1, 0);
    public static final ChessDirection LEFT = new ChessDirection(0, -1);
    public static final ChessDirection RIGHT = new ChessDirection(0, 1);

    public static final ChessDirection UP_LEFT = new ChessDirection(1, -1);
    public static final ChessDirection UP_RIGHT = new ChessDirection(1, 1);
    public static final ChessDirection DOWN_LEFT = new ChessDirection(-1, -1);
    public static final ChessDirection DOWN_RIGHT = new ChessDirection(-1, 1);

    private final int x;
    private final int y;

    private ChessDirection(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }
}
