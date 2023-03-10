package lunch.record.service;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import lunch.record.domain.LunchRecord;
import lunch.record.repository.LunchRecordRepository;
import lunch.record.repository.LunchRecordRepositoryInterface;
import lunch.record.util.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import javax.sql.rowset.serial.SerialBlob;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Connection;
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
@SpringBootTest // 테스트 시 스프링 부트를 통해 스프링 컨테이너를 생성, 스프링 빈을 등록, 의존 관계 주입을 받을 수 있도록 한다.
class LunchRecordServiceTest {

    @Autowired // 스프링 빈으로 등록된 자바 객체로 의존 관계 주입 받는다.
    private LunchRecordRepositoryInterface repository;
    @Autowired
    private LunchRecordService service;

    @TestConfiguration // 추가로 필요한 스프링 빈들을 등록하고 테스트를 수행할 수 있다.
    static class TestConfig {

        // 생성자를 통해서 스프링 부트가 만들어준 데이터소스 빈을 주입 받을 수도 있다.
        private final DataSource dataSource;

        public TestConfig(DataSource dataSource) {
            this.dataSource = dataSource;
        }

//        // 스프링 부트가 만들어준 데이터소스 빈을 의존 관계 주입 받는다.
//        @Autowired
//        DataSource dataSource;
//        // 직접 데이터소스를 빈으로 등록하면 스프링 부트는 데이터소스를 자동으로 등록하지 않는다.
//        @Bean
//        DataSource dataSource() {
//            return new DriverManagerDataSource(URL, USERNAME, PASSWORD);
//        }
//        // 직접 트랜잭션 매니저를 빈으로 등록하면 스프링 부트는 트랜잭션 매니저를 자동으로 등록하지 않는다.
//        @Bean
//        PlatformTransactionManager transactionManager() {
//            return new DataSourceTransactionManager(dataSource());
//        }

        @Bean
        LunchRecordRepositoryInterface lunchRecordRepository() {
            return new LunchRecordRepository(dataSource);
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
    void after() {
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
        Blob blob = new SerialBlob(Utils.imageToByteArray("/Users/ghc/development/img/test.png"));
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
        Blob blob = new SerialBlob(Utils.imageToByteArray("/Users/ghc/development/img/test.png"));
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

    @Test
    void create() throws SQLException {
        // given
        String restaurant = "test";
        String menu = "test";
        Blob blob = new SerialBlob(Utils.imageToByteArray("/Users/ghc/development/img/test.png"));

        LunchRecord newLunchRecord = new LunchRecord(restaurant, menu, blob, BigDecimal.ONE, 4.0f, 4.0f);
        LunchRecord savedLunchRecord = service.create(newLunchRecord);
        assertThat(newLunchRecord)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .ignoringFields("updateAt")
                .ignoringFields("createAt")
                .isEqualTo(savedLunchRecord);
    }
}