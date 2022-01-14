package interfaces;

import java.io.Serializable;

public class GameStatus implements Serializable {
    public final GamePhase currentPhase;

    public final boolean isThereAMill;

    public final int whiteStonesInInventory;
    public final int blackStonesInInventory;

    public final int whiteStonesOnTheGrid;
    public final int blackStonesOnTheGrid;

    public final boolean whiteInJumpPhase;
    public final boolean blackInJumpPhase;

    public GameStatus(GamePhase currentPhase, boolean isThereAMill, int whiteStonesInInventory, int blackStonesInInventory, int whiteStonesOnTheGrid, int blackStonesOnTheGrid, boolean whiteInJumpPhase, boolean blackInJumpPhase) {
        this.currentPhase = currentPhase;
        this.isThereAMill = isThereAMill;
        this.whiteStonesInInventory = whiteStonesInInventory;
        this.blackStonesInInventory = blackStonesInInventory;
        this.whiteStonesOnTheGrid = whiteStonesOnTheGrid;
        this.blackStonesOnTheGrid = blackStonesOnTheGrid;
        this.whiteInJumpPhase = whiteInJumpPhase;
        this.blackInJumpPhase = blackInJumpPhase;
    }
}
