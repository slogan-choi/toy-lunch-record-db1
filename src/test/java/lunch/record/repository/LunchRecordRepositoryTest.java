package lunch.record.repository;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import lunch.record.connection.DBConnectionUtil;
import lunch.record.domain.LunchRecord;
import lunch.record.service.LunchRecordService;
import lunch.record.util.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

import static lunch.record.connection.ConnectionConst.PASSWORD;
import static lunch.record.connection.ConnectionConst.URL;
import static lunch.record.connection.ConnectionConst.USERNAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
class LunchRecordRepositoryTest {

    LunchRecordRepository repository;
    LunchRecordService service;

    @BeforeEach
    void beforeEach() {
        // 기본 DriverManager - 항상 새로운 커넥션 획득
//        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);

        // 커넥션 풀링: HikariProxyConnection -> JdbcConnection
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);

        repository = new LunchRecordRepository(dataSource); // DataSource 의존관계 주입
        service = new LunchRecordService(repository);
    }

    @AfterEach
    void after() throws SQLException {
        service.correctAverageGrade();
    }

    @Test
    void crud() throws SQLException {
        Blob blob = DBConnectionUtil.getConnection().createBlob();
        blob.setBytes(1, Utils.imageToByteArray("/Users/ghc/development/img/test.png"));
        LocalTime createAt = LocalTime.now();
        LocalTime updateAt = LocalTime.now().plusHours(24);

        // save
        LunchRecord lunchRecordForSave = new LunchRecord("test", "test", blob, BigDecimal.ONE, 4.0f, createAt, createAt);
        float averageGrade = service.getAverageGrade(lunchRecordForSave);
        lunchRecordForSave.setAverageGrade(averageGrade);
        repository.save(lunchRecordForSave);
        repository.updateAverageGradeByRestaurantMenu(averageGrade, lunchRecordForSave.getRestaurant(), lunchRecordForSave.getMenu());

        // select
        List<LunchRecord> all = repository.findAll();
        log.info("findAll={}", all);

        // update
        int minId = all.stream().min(Comparator.comparing(LunchRecord::getId)).orElseThrow().getId();
        LunchRecord lunchRecordForUpdate = new LunchRecord(minId,"testtest", "testtest", blob, new BigDecimal(BigInteger.valueOf(3)), 5.0f, updateAt, createAt);
        repository.update(
                minId,
                lunchRecordForUpdate.getRestaurant(),
                lunchRecordForUpdate.getMenu(),
                lunchRecordForUpdate.getImage(),
                lunchRecordForUpdate.getPrice(),
                lunchRecordForUpdate.getGrade()
        );
        repository.updateAverageGradeByRestaurantMenu(service.getAverageGrade(lunchRecordForUpdate), lunchRecordForUpdate.getRestaurant(), lunchRecordForUpdate.getMenu());
        LunchRecord updatedLunchRecord = repository.findById(minId);
        assertThat(updatedLunchRecord.getRestaurant()).isEqualTo("testtest");
        assertThat(updatedLunchRecord.getMenu()).isEqualTo("testtest");

        // delete
        int maxId = all.stream()
                .max(Comparator.comparing(LunchRecord::getId))
                .orElseThrow()
                .getId();
        repository.delete(maxId);
        assertThatThrownBy(() -> repository.findById(maxId))
                        .isInstanceOf(NoSuchElementException.class);
    }
}