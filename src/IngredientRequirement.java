public class IngredientRequirement {
    String name;
    double quantity;

    public IngredientRequirement(String name, double quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    // Getters
    public String getName() {
        return name;
    }

    public double getQuantity() {
        return quantity;
    }

    // Setters (optional if you need them)
    public void setName(String name) {
        this.name = name;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }
}
