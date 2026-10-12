package restaurantrush.application;

public final class TurnPhase {
    private TurnPhase() { }

    public static final String BETWEEN_TURNS = "BETWEEN_TURNS";
    public static final String WAITER = "WAITER";
    public static final String CHEF = "CHEF";
    public static final String END_OF_TURN = "END_OF_TURN";
    public static final String FINISHED = "FINISHED";
}
