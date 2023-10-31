package model.tetromino;

public abstract class CommonKickedTetromino extends AbstractTetromino {
    private static final int[][][] LEFT_KICK = new int[][][]{
            {{0, 0}, {1, 0}, {1, 1}, {0, -2}, {1, -2}},
            {{0, 0}, {1, 0}, {1, -1}, {0, 2}, {1, 2}},
            {{0, 0}, {-1, 0}, {-1, 1}, {0, -2}, {-1, -2}},
            {{0, 0}, {-1, 0}, {-1, -1}, {0, 2}, {-1, 2}},
    };

    private static final int[][][] RIGHT_KICK = new int[][][]{
            {{0, 0}, {-1, 0}, {-1, 1}, {0, -2}, {-1, -2}},
            {{0, 0}, {1, 0}, {1, -1}, {0, 2}, {1, 2}},
            {{0, 0}, {1, 0}, {1, 1}, {0, -2}, {1, -2}},
            {{0, 0}, {-1, 0}, {-1, -1}, {0, 2}, {-1, 2}},
    };

    public CommonKickedTetromino() {
        super();
    }

    public CommonKickedTetromino(int[] coords, Direction orientation) {
        super(coords, orientation);
    }

    @Override
    protected int[][][] getLeftKick() {
        return CommonKickedTetromino.LEFT_KICK;
    }

    @Override
    protected int[][][] getRightKick() {
        return CommonKickedTetromino.RIGHT_KICK;
    }
}
