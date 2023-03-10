package lunch.record.repository;

import lombok.extern.slf4j.Slf4j;
import lunch.record.domain.LunchRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.lob.DefaultLobHandler;

import javax.sql.DataSource;
import javax.sql.rowset.serial.SerialBlob;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Slf4j
public class LunchRecordRepository implements LunchRecordRepositoryInterface {

    private final JdbcTemplate template;

    public LunchRecordRepository(DataSource dataSource) {
        template = new JdbcTemplate(dataSource);
    }

    @Override
    public LunchRecord save(LunchRecord lunchRecord) {
        String sql = "insert into lunchRecord(restaurant, menu, image, price, grade, averageGrade, updateAt, createAt) values(?, ?, ?, ?, ?, ?, ?, ?)";
        LocalTime now = LocalTime.now();
        GeneratedKeyHolder generatedKeyHolder = new GeneratedKeyHolder();

        template.update(con -> {
            PreparedStatement pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, lunchRecord.getRestaurant());
            pstmt.setString(2, lunchRecord.getMenu());
            pstmt.setBinaryStream(3, lunchRecord.getImage().getBinaryStream());
            pstmt.setBigDecimal(4, lunchRecord.getPrice());
            pstmt.setFloat(5, lunchRecord.getGrade());
            pstmt.setFloat(6, lunchRecord.getAverageGrade());
            pstmt.setTime(7, Time.valueOf(now));
            pstmt.setTime(8, Time.valueOf(now));
            return pstmt;
        }, generatedKeyHolder);

        return findById(Objects.requireNonNull(generatedKeyHolder.getKey()).intValue());
    }

    @Override
    public List<LunchRecord> findAll() {
        String sql = "select * from LunchRecord";
        return template.query(sql, lunchRecordRowMapper());
    }

    @Override
    public LunchRecord findById(int id) {
        String sql = "select * from lunchRecord where id = ?";
        return template.queryForObject(sql, lunchRecordRowMapper(), id);
    }

    @Override
    public List<LunchRecord> findByRestaurantMenu(String restaurant, String menu) {
        String sql = "select * from LunchRecord where restaurant = ? and menu = ?";
        return template.query(sql, lunchRecordRowMapper(), restaurant, menu);
    }

    @Override
    public void update(int id, String restaurant, String menu, Blob image, BigDecimal price, float grade) {
        String sql = "update lunchRecord set restaurant = ?, menu = ?, image = ?, price = ?, grade = ?, updateAt = ? where id = ?";
        template.update(sql, restaurant, menu, image, price, grade, LocalTime.now(), id);
    }

    @Override
    public void updateAverageGradeByRestaurantMenu(Float averageGrade, String restaurant, String menu) {
        String sql = "update lunchRecord set averageGrade = ?, updateAt = ? where restaurant = ? and menu = ?";
        template.update(sql, averageGrade, LocalTime.now(), restaurant, menu);
    }

    @Override
    public void delete(int id) {
        String sql = "delete from lunchRecord where id = ?";
        template.update(sql, id);
    }

    @Override
    public void deleteAll() {
        String sql = "delete from lunchRecord";
        template.update(sql);
    }

    private RowMapper<LunchRecord> lunchRecordRowMapper() {
        return (rs, rowNum) -> {
            LunchRecord lunchRecord = new LunchRecord();
            lunchRecord.setId(rs.getInt("id"));
            lunchRecord.setRestaurant(rs.getString("restaurant"));
            lunchRecord.setMenu(rs.getString("menu"));
            lunchRecord.setImage(new SerialBlob(Objects.requireNonNull(new DefaultLobHandler().getBlobAsBytes(rs, "image"))));
            lunchRecord.setPrice(rs.getBigDecimal("price"));
            lunchRecord.setGrade(rs.getFloat("grade"));
            lunchRecord.setAverageGrade(rs.getFloat("averageGrade"));
            lunchRecord.setUpdateAt(rs.getTime("updateAt").toLocalTime());
            lunchRecord.setCreateAt(rs.getTime("createAt").toLocalTime());
            return lunchRecord;
        };
    }
}
