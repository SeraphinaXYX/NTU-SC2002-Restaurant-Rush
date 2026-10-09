package restaurantrush.domain;

import java.util.ArrayList;

public final class Menu {
    private final ArrayList<MenuItem> items = new ArrayList<MenuItem>();

    public Menu() {
        items.add(MenuItem.SALAD);
        items.add(MenuItem.BURGER);
        items.add(MenuItem.PASTA);
    }

    public ArrayList<MenuItem> getItems() {
        return new ArrayList<MenuItem>(items);
    }
}
