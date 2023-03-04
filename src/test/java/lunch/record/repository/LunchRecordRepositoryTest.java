package lunch.record.repository;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import lunch.record.connection.DBConnectionUtil;
import lunch.record.domain.LunchRecord;
import lunch.record.util.Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.math.BigDecimal;
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
    }

    @Test
    void crud() throws SQLException {
        Blob blob = DBConnectionUtil.getConnection().createBlob();
        blob.setBytes(1, Utils.imageToByteArray("/Users/ghc/development/img/test.png"));
        LocalTime now = LocalTime.now();

        // save
        repository.save(new LunchRecord("test", "test", blob, BigDecimal.ONE, 5.0f, now, now));

        // select
        List<LunchRecord> all = repository.findAll();
        log.info("findAll={}", all);

        // update
        LunchRecord lunchRecord = all.stream().max(Comparator.comparing(LunchRecord::getId)).orElseThrow();
        repository.update(lunchRecord.getId(), "testtest", "testtest", blob, BigDecimal.ONE, 5.0f);
        LunchRecord updatedLunchRecord = repository.findById(lunchRecord.getId());
        assertThat(updatedLunchRecord.getRestaurant()).isEqualTo("testtest");
        assertThat(updatedLunchRecord.getMenu()).isEqualTo("testtest");

        // delete
        repository.delete(lunchRecord.getId());
        assertThatThrownBy(() -> repository.findById(lunchRecord.getId()))
                        .isInstanceOf(NoSuchElementException.class);
    }
}