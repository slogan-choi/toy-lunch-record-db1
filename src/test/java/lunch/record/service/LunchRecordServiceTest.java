package lunch.record.service;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import lunch.record.connection.DBConnectionUtil;
import lunch.record.domain.LunchRecord;
import lunch.record.repository.LunchRecordRepository;
import lunch.record.util.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static lunch.record.connection.ConnectionConst.PASSWORD;
import static lunch.record.connection.ConnectionConst.URL;
import static lunch.record.connection.ConnectionConst.USERNAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
class LunchRecordServiceTest {

    private LunchRecordRepository repository;
    private LunchRecordService service;

    @BeforeEach
    void before() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);

        repository = new LunchRecordRepository(dataSource);
        service = new LunchRecordService(dataSource, repository);
    }

    @AfterEach
    void after() throws SQLException {
        List<LunchRecord> byRestaurantMenu = repository.findByRestaurantMenu("test", "test");

        try {
            service.correctAverageGrade();
        } catch (IllegalStateException e) {
            assertThatThrownBy(() -> {
                service.correctAverageGrade();
            }).isInstanceOf(IllegalStateException.class);
        }
        List<LunchRecord> updatedByRestaurantMenu = repository.findByRestaurantMenu("test", "test");

        // 평균 평점 업데이트 반영 전과 반영 후가 그대로이다.
        assertThat(updatedByRestaurantMenu.stream().map(LunchRecord::getAverageGrade).collect(Collectors.toList()))
                .usingRecursiveComparison()
                .isEqualTo(byRestaurantMenu.stream().map(LunchRecord::getAverageGrade).collect(Collectors.toList()));

        repository.deleteAll();
    }

    @Test
    @DisplayName("정상 평점 적용")
    void calculateGrade() throws SQLException {
        // given
        String restaurant = "test";
        String menu = "test";
        Blob blob = DBConnectionUtil.getConnection().createBlob();
        blob.setBytes(1, Utils.imageToByteArray("/Users/ghc/development/img/test.png"));
        LocalTime createAt = LocalTime.now();
        LocalTime updateAt = LocalTime.now().plusHours(24);

        repository.save(new LunchRecord(restaurant, menu, blob, BigDecimal.ONE, 4.0f, 4.0f, createAt, createAt));
        repository.save(new LunchRecord(restaurant, menu, blob, BigDecimal.ONE, 4.0f, 4.0f, updateAt, updateAt));

        // when
        List<LunchRecord> byRestaurantMenu = repository.findByRestaurantMenu(restaurant, menu);
        int maxId = byRestaurantMenu.stream()
                .max(Comparator.comparing(LunchRecord::getId))
                .orElseThrow()
                .getId();

        LunchRecord lunchRecord = new LunchRecord(
                maxId,
                restaurant,
                menu,
                blob,
                new BigDecimal(BigInteger.valueOf(3)),
                5.0f,
                updateAt.plusHours(24),
                updateAt
        );
        repository.update(
                maxId,
                lunchRecord.getRestaurant(),
                lunchRecord.getMenu(),
                lunchRecord.getImage(),
                lunchRecord.getPrice(),
                lunchRecord.getGrade()
        );
        repository.updateAverageGradeByRestaurantMenu(service.getAverageGrade(lunchRecord), lunchRecord.getRestaurant(), lunchRecord.getMenu());

        // then
        List<LunchRecord> updatedByRestaurantMenu = repository.findByRestaurantMenu(restaurant, menu);

        for (LunchRecord lr : updatedByRestaurantMenu) {
            assertThat(lr.getAverageGrade()).isEqualTo(4.500f);
        }
    }

    @Test
    @DisplayName("평점 적용 중 예외 발생")
    void calculateGradeEx() throws SQLException {
        // given
        String restaurant = "test";
        String menu = "test";
        Blob blob = DBConnectionUtil.getConnection().createBlob();
        blob.setBytes(1, Utils.imageToByteArray("/Users/ghc/development/img/test.png"));
        LocalTime createAt = LocalTime.now();
        LocalTime updateAt = LocalTime.now().plusHours(24);

        repository.save(new LunchRecord(restaurant, menu, blob, BigDecimal.ONE, 0.0f, 0.0f, createAt, createAt));
        repository.save(new LunchRecord(restaurant, menu, blob, BigDecimal.ONE, 0.0f, 0.0f, updateAt, updateAt));

        // when
        List<LunchRecord> byRestaurantMenu = repository.findByRestaurantMenu(restaurant, menu);
        int maxId = byRestaurantMenu.stream()
                .max(Comparator.comparing(LunchRecord::getId))
                .orElseThrow()
                .getId();

        LunchRecord lunchRecord = new LunchRecord(
                maxId,
                restaurant,
                menu,
                blob,
                new BigDecimal(BigInteger.valueOf(3)),
                5.0f,
                updateAt.plusHours(24),
                updateAt
        );
        repository.update(
                maxId,
                lunchRecord.getRestaurant(),
                lunchRecord.getMenu(),
                lunchRecord.getImage(),
                lunchRecord.getPrice(),
                lunchRecord.getGrade()
        );
        assertThatThrownBy(() -> {
            repository.updateAverageGradeByRestaurantMenu(service.getAverageGrade(lunchRecord), lunchRecord.getRestaurant(), lunchRecord.getMenu());
        }).isInstanceOf(IllegalStateException.class);

        // then
        List<LunchRecord> updatedByRestaurantMenu = repository.findByRestaurantMenu(restaurant, menu);

        // 평균 평점 업데이트 반영 전과 반영 후가 그대로이다.
        assertThat(updatedByRestaurantMenu.stream().map(LunchRecord::getAverageGrade).collect(Collectors.toList()))
                .usingRecursiveComparison()
                .isEqualTo(byRestaurantMenu.stream().map(LunchRecord::getAverageGrade).collect(Collectors.toList()));
    }
}