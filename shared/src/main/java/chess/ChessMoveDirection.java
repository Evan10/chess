package chess;

public class ChessMoveDirection {

    private final int x;


    private final int y;

    public ChessMoveDirection(int x, int y){
        this.x = x;
        this.y = y;
    }
    public boolean isValid(){
        return !(x==0&&y==0)&&(Math.abs(x)<=1)&&(Math.abs(y)<=1);
    }

    public int getY() {
        return y;
    }

    public int getX() {
        return x;
    }

}
