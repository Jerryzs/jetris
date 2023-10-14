package model.tetromino;

import java.util.Set;

public class O extends AbstractTetromino {
    private static final int[] STANDALONE = {0, 1, 1, 0, 0, 1, 1, 0};

    @Override
    protected int[][][] getLeftKick() {
        return new int[5][4][2];
    }

    @Override
    protected int[][][] getRightKick() {
        return new int[5][4][2];
    }

    @Override
    public void rotate() {
        // do nothing
    }

    @Override
    protected Set<Integer> getRotation(int test, int direction) {
        return this.findAbsoluteCoords(this.getRelative());
    }

    @Override
    public boolean occupies(int x, int y) {
        return !(x < this.coords[0] || y < this.coords[1]) && x - this.coords[0] < 2 && y - this.coords[1] < 2;
    }

    @Override
    public int[] getStandalone() {
        return O.STANDALONE;
    }

    @Override
    public Set<Integer> getRelative(Direction orientation) {
        return Set.of(0, 1, 1000, 1001);
    }
}
