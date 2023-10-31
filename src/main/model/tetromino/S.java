package model.tetromino;

import java.util.Set;

public class S extends CommonKickedTetromino {
    private static final int[] STANDALONE = {0, 1, 1, 0, 1, 1, 0, 0};

    public S() {
        super();
    }

    public S(int[] coords, Direction orientation) {
        super(coords, orientation);
    }


    @Override
    public int[] getStandalone() {
        return S.STANDALONE;
    }

    @Override
    public Set<Integer> getRelative(Direction orientation) {
        switch (orientation) {
            case DOWN:
            default:
                return Set.of(1, 1001, 101000, 0);
            case LEFT:
                return Set.of(1, 0, 1000, 1101);
            case UP:
                return Set.of(0, 1000, 101101, 101);
            case RIGHT:
                return Set.of(101001, 101000, 0, 101);
        }
    }
}
