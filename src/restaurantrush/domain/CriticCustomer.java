package restaurantrush.domain;

public final class CriticCustomer extends Customer {
    public CriticCustomer(int id, int arrivalTurn) { super(id, arrivalTurn); }

    public int getWaitingLoss() { return 12; }
    public String getType() { return "Critic"; }
    public MenuItem chooseMeal(Menu menu) {
        MenuItem selected = menu.getItems().get(0);
        for (MenuItem item : menu.getItems()) {
            if (item.getPreparationUnits() > selected.getPreparationUnits()
                    || (item.getPreparationUnits() == selected.getPreparationUnits()
                    && item.getPriceCents() > selected.getPriceCents())) {
                selected = item;
            }
        }
        return selected;
    }
}
