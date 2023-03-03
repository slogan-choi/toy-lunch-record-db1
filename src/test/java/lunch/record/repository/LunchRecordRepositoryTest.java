package lunch.record.repository;

import lunch.record.connection.DBConnectionUtil;
import lunch.record.domain.LunchRecord;
import lunch.record.util.Utils;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.LocalTime;

class LunchRecordRepositoryTest {

    LunchRecordRepository repository = new LunchRecordRepository();

    @Test
    void crud() throws SQLException {
        Blob blob = DBConnectionUtil.getConnection().createBlob();
        blob.setBytes(1, Utils.imageToByteArray("/Users/ghc/development/img/test.png"));
        LocalTime now = LocalTime.now();

        repository.save(new LunchRecord("test", "test", blob, BigDecimal.ONE, 5.0f, now, now));
    }
}