package restaurantrush.domain;

/** Shares the one-task allowance, without forcing unrelated roles into one work method. */
public abstract class Staff {
    private boolean available = true;

    public final void startTurn() { available = true; }
    public final boolean isAvailable() { return available; }
    protected final void requireAvailable() {
        if (!available) { throw new IllegalStateException("This staff member already worked this turn."); }
    }
    protected final void completeTask() { available = false; }
}
