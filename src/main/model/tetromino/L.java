package model.tetromino;

import java.util.Set;

public class L extends CommonKickedTetromino {
    private static final int[] STANDALONE = {0, 0, 1, 0, 1, 1, 1, 0};

    public L() {
        super();
    }

    public L(int[] coords, Direction orientation, int test) {
        super(coords, orientation, test);
    }


    @Override
    public int[] getStandalone() {
        return L.STANDALONE;
    }

    @Override
    public Set<Integer> getRelative(Direction orientation) {
        switch (orientation) {
            case DOWN:
            default:
                return Set.of(1001, 101000, 0, 1000);
            case LEFT:
                return Set.of(1, 0, 101, 1101);
            case UP:
                return Set.of(101000, 0, 1000, 101101);
            case RIGHT:
                return Set.of(101001, 1, 0, 101);
        }
    }
}
