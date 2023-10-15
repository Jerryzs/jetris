package model.tetromino;

import java.util.Set;

public class Z extends CommonKickedTetromino {
    private static final int[] STANDALONE = {1, 1, 0, 0, 0, 1, 1, 0};

    @Override
    public int[] getStandalone() {
        return Z.STANDALONE;
    }

    @Override
    public Set<Integer> getRelative(Direction orientation) {
        switch (orientation) {
            case DOWN:
            default:
                return Set.of(101001, 1, 0, 1000);
            case LEFT:
                return Set.of(1001, 0, 1000, 101);
            case UP:
                return Set.of(101000, 0, 101, 1101);
            case RIGHT:
                return Set.of(1, 101000, 0, 101101);
        }
    }
}
