package fa25.group.evtrainticket.dto;

public enum TicketType {
    ADULT("Người lớn", 1.0),
    CHILD("Trẻ em", 0.8),
    ELDERLY("Người cao tuổi", 0.8);

    private final String displayName;
    private final double priceMultiplier;

    TicketType(String displayName, double priceMultiplier) {
        this.displayName = displayName;
        this.priceMultiplier = priceMultiplier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getPriceMultiplier() {
        return priceMultiplier;
    }
}