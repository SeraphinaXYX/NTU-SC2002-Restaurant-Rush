package restaurantrush.domain;

/** A once-per-game effect, active on its activation turn and the next turn. */
public final class HappyHour {
    private int currentTurn;
    private int activationTurn;

    public void startTurn(int turn, boolean activate) {
        if (turn != currentTurn + 1 || turn > 18) {
            throw new IllegalStateException("Happy Hour must follow the game turn sequence.");
        }
        if (activate && hasBeenUsed()) { throw new IllegalStateException("Happy Hour can be activated only once."); }
        currentTurn = turn;
        if (activate) { activationTurn = turn; }
    }

    public boolean hasBeenUsed() { return activationTurn != 0; }
    public boolean isActive() { return getRemainingTurns() > 0; }
    public int getRemainingTurns() {
        if (!hasBeenUsed()) { return 0; }
        int remaining = 2 - (currentTurn - activationTurn);
        if (remaining < 0) { return 0; }
        return remaining;
    }
}
