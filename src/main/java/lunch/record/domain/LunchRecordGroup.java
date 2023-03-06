package lunch.record.domain;

import lombok.Data;

@Data
public class LunchRecordGroup {
    public String restaurant;
    public String menu;

    public LunchRecordGroup(LunchRecord lunchRecord) {
        this.restaurant = lunchRecord.getRestaurant();
        this.menu = lunchRecord.getMenu();
    }
}
