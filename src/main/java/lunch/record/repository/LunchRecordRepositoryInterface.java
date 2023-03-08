package lunch.record.repository;

import lunch.record.domain.LunchRecord;

import java.math.BigDecimal;
import java.sql.Blob;
import java.util.List;

public interface LunchRecordRepositoryInterface {

    LunchRecord save(LunchRecord lunchRecord);

    List<LunchRecord> findAll();
    LunchRecord findById(int id);
    List<LunchRecord> findByRestaurantMenu(String restaurant, String menu);

    void update(int id, String restaurant, String menu, Blob image, BigDecimal price, float grade);
    void updateAverageGradeByRestaurantMenu(Float averageGrade, String restaurant, String menu);

    void delete(int id);
    void deleteAll();

}
