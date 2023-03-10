package lunch.record.repository.exception;

import lunch.record.domain.LunchRecord;
import lunch.record.repository.LunchRecordRepository;
import lunch.record.repository.LunchRecordRepositoryInterface;
import lunch.record.service.LunchRecordService;
import lunch.record.util.Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import javax.sql.rowset.serial.SerialBlob;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.LocalTime;

@SpringBootTest
class ValueTooLongExceptionTest {

    @Autowired
    LunchRecordRepositoryInterface repository;
    @Autowired
    LunchRecordService service;

    @TestConfiguration
    static class TestConfig {
        @Autowired
        DataSource dataSource;

        @Bean
        public LunchRecordRepositoryInterface lunchRecordRepository() {
            return new LunchRecordRepository(dataSource);
        }

        @Bean
        public LunchRecordService lunchRecordService() {
            return new LunchRecordService(lunchRecordRepository());
        }
    }

    @AfterEach
    void after() {
        repository.deleteAll();
    }

    @Test
    void valueTooLongSave() throws SQLException {
        StringBuilder restaurant = new StringBuilder();
        restaurant.append("test");

        StringBuilder menu = new StringBuilder();
        menu.append("The Double Ristretto Venti Half-Soy Nonfat Decaf Organic Chocolate Brownie Iced Vanilla Double-Shot Gingerbread Frappuccino Extra Hot With Foam Whipped Cream Upside Down Double Blended, One Sweet'N Low and One Nutrasweet, and Ice");
        menu.append("The Double Ristretto Venti Half-Soy Nonfat Decaf Organic Chocolate Brownie Iced Vanilla Double-Shot Gingerbread Frappuccino Extra Hot With Foam Whipped Cream Upside Down Double Blended, One Sweet'N Low and One Nutrasweet, and Ice");

        Blob blob = new SerialBlob(Utils.imageToByteArray("/Users/ghc/development/img/test.png"));

        service.create(new LunchRecord(restaurant.toString(), menu.toString(), blob, BigDecimal.ONE, 4.0f, 4.0f));
    }
}