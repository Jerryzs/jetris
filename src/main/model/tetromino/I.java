package model.tetromino;

import java.util.Set;

public class I extends AbstractTetromino {
    private static final int[] STANDALONE = {0, 0, 0, 0, 1, 1, 1, 1};

    private static final int[][][] LEFT_KICK = new int[][][] {
            {{ 0,  0}, {-1,  0}, { 2,  0}, {-1,  2}, { 2, -1}},
            {{ 0,  0}, { 2,  0}, {-1,  0}, { 2,  1}, {-1, -2}},
            {{ 0,  0}, { 1,  0}, {-2,  0}, { 1, -2}, {-2,  1}},
            {{ 0,  0}, {-2,  0}, { 1,  0}, {-2, -1}, { 1,  2}},
    };

    private static final int[][][] RIGHT_KICK = new int[][][] {
            {{ 0,  0}, {-2,  0}, { 1,  0}, {-2, -1}, { 1,  2}},
            {{ 0,  0}, {-1,  0}, { 2,  0}, {-1,  2}, { 2, -1}},
            {{ 0,  0}, { 2,  0}, {-1,  0}, { 2,  1}, {-1, -2}},
            {{ 0,  0}, { 1,  0}, {-2,  0}, { 1, -2}, {-2,  1}},
    };

    @Override
    protected int[][][] getLeftKick() {
        return I.LEFT_KICK;
    }

    @Override
    protected int[][][] getRightKick() {
        return I.RIGHT_KICK;
    }

    @Override
    public int[] getStandalone() {
        return I.STANDALONE;
    }

    @Override
    public Set<Integer> getRelative(Direction orientation) {
        switch (orientation) {
            case DOWN:
            default:
                return Set.of(101000, 0, 1000, 2000);
            case LEFT:
                return Set.of(1001, 1000, 1101, 1102);
            case UP:
                return Set.of(101101, 101, 1101, 2101);
            case RIGHT:
                return Set.of(1, 0, 101, 102);
        }
    }
}
