package restaurantrush.domain;

import java.util.ArrayList;

public final class Menu {
    private final ArrayList<MenuItem> items = new ArrayList<MenuItem>();

    public Menu() {
        items.add(MenuItem.SALAD);
        items.add(MenuItem.BURGER);
        items.add(MenuItem.PASTA);
    }

    public static Menu withCombo() {
        Menu menu = new Menu();
        menu.items.add(new ComboMeal(MenuItem.SALAD, MenuItem.PASTA));
        return menu;
    }

    public ArrayList<MenuItem> getItems() {
        return new ArrayList<MenuItem>(items);
    }
}
