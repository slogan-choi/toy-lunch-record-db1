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
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
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
@SpringBootTest // 테스트 시 스프링 부트를 통해 스프링 컨테이너를 생성, 스프링 빈을 등록, 의존 관계 주입을 받을 수 있도록 한다.
class LunchRecordRepositoryTest {

    @Autowired // 스프링 빈으로 등록된 자바 객체로 의존 관계 주입 받는다.
    LunchRecordRepository repository;
    @Autowired
    LunchRecordService service;

    @TestConfiguration // 추가로 필요한 스프링 빈들을 등록하고 테스트를 수행할 수 있다.
    static class TestConfig {
        @Bean
        DataSource dataSource() {
            return new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        }

        @Bean
        PlatformTransactionManager transactionManager() {
            return new DataSourceTransactionManager(dataSource());
        }

        @Bean
        LunchRecordRepository lunchRecordRepository() {
            return new LunchRecordRepository(dataSource());
        }

        @Bean
        LunchRecordService lunchRecordService() {
            return new LunchRecordService(lunchRecordRepository());
        }
    }

    @Test
    void AopCheck() {
        log.info("lunchRecordService class={}", service.getClass());
        log.info("lunchRecordRepository class={}", repository.getClass());
        assertThat(AopUtils.isAopProxy(service)).isTrue();
        assertThat(AopUtils.isAopProxy(repository)).isFalse();
    }

    @AfterEach
    void after() throws SQLException {
        service.correctAverageGrade();
        repository.deleteAll();
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