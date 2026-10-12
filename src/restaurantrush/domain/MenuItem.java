package restaurantrush.domain;

/** An immutable menu entry. Prices are stored in cents. */
public class MenuItem {
    public static final MenuItem SALAD = new MenuItem("Salad", 900, 1);
    public static final MenuItem BURGER = new MenuItem("Burger", 1500, 1);
    public static final MenuItem PASTA = new MenuItem("Pasta", 1800, 2);

    private final String name;
    private final int priceCents;
    private final int preparationUnits;

    public MenuItem(String name, int priceCents, int preparationUnits) {
        if (name == null || name.trim().isEmpty() || priceCents <= 0 || preparationUnits <= 0) {
            throw new IllegalArgumentException("A meal needs a name, positive price and preparation units.");
        }
        this.name = name;
        this.priceCents = priceCents;
        this.preparationUnits = preparationUnits;
    }

    public String getName() { return name; }
    public int getPriceCents() { return priceCents; }
    public int getPreparationUnits() { return preparationUnits; }
}
