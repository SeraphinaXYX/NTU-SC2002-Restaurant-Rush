package restaurantrush.domain;

import java.util.ArrayList;

public final class ComboMeal extends MenuItem {
    private final MenuItem first;
    private final MenuItem second;

    public ComboMeal(MenuItem first, MenuItem second) {
        super(component(first).getName() + " + " + component(second).getName(),
                ((first.getPriceCents() + second.getPriceCents()) * 90 + 50) / 100,
                first.getPreparationUnits() + second.getPreparationUnits());
        this.first = first;
        this.second = second;
    }

    private static MenuItem component(MenuItem item) {
        if (item == null) { throw new IllegalArgumentException("A combo needs two components."); }
        return item;
    }

    public ArrayList<MenuItem> getComponents() {
        ArrayList<MenuItem> result = new ArrayList<MenuItem>();
        result.add(first);
        result.add(second);
        return result;
    }
}
