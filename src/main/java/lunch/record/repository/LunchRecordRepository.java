package lunch.record.repository;

import lombok.extern.slf4j.Slf4j;
import lunch.record.domain.LunchRecord;
import lunch.record.repository.exception.LunchRecordDbException;
import lunch.record.repository.exception.ValueTooLongException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
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

@Slf4j
public class LunchRecordRepository implements LunchRecordRepositoryInterface {

    // [DI + OCP]
    // LunchRecordRepository 는 DataSource 인터페이스에만 의존하기 때문에 DataSource 구현체를 변경해도 LunchRecordRepository 의 코드는 전혀 변경하지 않아도 된다.
    private final DataSource dataSource;

    public LunchRecordRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public LunchRecord save(LunchRecord lunchRecord) {
        String sql = "insert into lunchRecord(restaurant, menu, image, price, grade, averageGrade, updateAt, createAt) values(?, ?, ?, ?, ?, ?, ?, ?)";
        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, lunchRecord.getRestaurant());
            pstmt.setString(2, lunchRecord.getMenu());
            pstmt.setBinaryStream(3, lunchRecord.getImage().getBinaryStream());
            pstmt.setBigDecimal(4, lunchRecord.getPrice());
            pstmt.setFloat(5, lunchRecord.getGrade());
            pstmt.setFloat(6, lunchRecord.getAverageGrade());
            pstmt.setTime(7, Time.valueOf(lunchRecord.getUpdateAt()));
            pstmt.setTime(8, Time.valueOf(lunchRecord.getCreateAt()));
            pstmt.executeUpdate();
            return lunchRecord;
        } catch (SQLException e) {
            log.error("db error", e);
            // h2 db
            if (e.getErrorCode() == 22001) {
                throw new ValueTooLongException(e, e.getErrorCode());
            }
            throw new LunchRecordDbException(e);
        } finally {
            close(con, pstmt, null);
        }
    }

    public List<LunchRecord> findAll() {
        String sql = "select * from LunchRecord";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);

            rs = pstmt.executeQuery();

            List<LunchRecord> lunchRecordList = new ArrayList<>();

            while (rs.next()) {
                LunchRecord lunchRecord = new LunchRecord();
                lunchRecord.setId(rs.getInt("id"));
                lunchRecord.setRestaurant(rs.getString("restaurant"));
                lunchRecord.setMenu(rs.getString("menu"));

                Blob blob = getConnection().createBlob();
                blob.setBytes(1, rs.getBlob("image").getBytes(1, (int) rs.getBlob("image").length()));

                lunchRecord.setImage(blob);
                lunchRecord.setPrice(rs.getBigDecimal("price"));
                lunchRecord.setGrade(rs.getFloat("grade"));
                lunchRecord.setAverageGrade(rs.getFloat("averageGrade"));
                lunchRecord.setUpdateAt(rs.getTime("updateAt").toLocalTime());
                lunchRecord.setCreateAt(rs.getTime("createAt").toLocalTime());
                lunchRecordList.add(lunchRecord);
            }

//            if (lunchRecordList.isEmpty()) {
//                throw new NoSuchElementException();
//            }

            return lunchRecordList;
        } catch (SQLException e) {
            log.error("db error", e);
            throw new ValueTooLongException(e);
        } finally {
            close(con, pstmt, rs);
        }
    }

    public LunchRecord findById(int id) {
        String sql = "select * from lunchRecord where id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                LunchRecord lunchRecord = new LunchRecord();
                lunchRecord.setId(rs.getInt("id"));
                lunchRecord.setRestaurant(rs.getString("restaurant"));
                lunchRecord.setMenu(rs.getString("menu"));

                Blob blob = getConnection().createBlob();
                blob.setBytes(1, rs.getBlob("image").getBytes(1, (int) rs.getBlob("image").length()));

                lunchRecord.setImage(blob);
                lunchRecord.setPrice(rs.getBigDecimal("price"));
                lunchRecord.setGrade(rs.getFloat("grade"));
                lunchRecord.setAverageGrade(rs.getFloat("averageGrade"));
                lunchRecord.setUpdateAt(rs.getTime("updateAt").toLocalTime());
                lunchRecord.setCreateAt(rs.getTime("createAt").toLocalTime());
                return lunchRecord;
            } else {
                throw new NoSuchElementException();
            }
        } catch (SQLException e) {
            log.error("db error");
            throw new ValueTooLongException(e);
        } finally {
            close(con, pstmt, rs);
        }
    }

    public List<LunchRecord> findByRestaurantMenu(String restaurant, String menu) {
        String sql = "select * from LunchRecord where restaurant = ? and menu = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, restaurant);
            pstmt.setString(2, menu);
            rs = pstmt.executeQuery();

            List<LunchRecord> lunchRecordList = new ArrayList<>();

            while (rs.next()) {
                LunchRecord lunchRecord = new LunchRecord();
                lunchRecord.setId(rs.getInt("id"));
                lunchRecord.setRestaurant(rs.getString("restaurant"));
                lunchRecord.setMenu(rs.getString("menu"));

                Blob blob = getConnection().createBlob();
                blob.setBytes(1, rs.getBlob("image").getBytes(1, (int) rs.getBlob("image").length()));

                lunchRecord.setImage(blob);
                lunchRecord.setPrice(rs.getBigDecimal("price"));
                lunchRecord.setGrade(rs.getFloat("grade"));
                lunchRecord.setAverageGrade(rs.getFloat("averageGrade"));
                lunchRecord.setUpdateAt(rs.getTime("updateAt").toLocalTime());
                lunchRecord.setCreateAt(rs.getTime("createAt").toLocalTime());

                lunchRecordList.add(lunchRecord);
            }

//            if (lunchRecordList.isEmpty()) {
//                throw new NoSuchElementException();
//            }

            return lunchRecordList;
        } catch (SQLException e) {
            log.info("db error", e);
            throw new ValueTooLongException(e);
        } finally {
            close(con, pstmt, rs);
        }
    }

    public void update(int id, String restaurant, String menu, Blob image, BigDecimal price, float grade) {
        String sql = "update lunchRecord set restaurant = ?, menu = ?, image = ?, price = ?, grade = ?, updateAt = ? where id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, restaurant);
            pstmt.setString(2, menu);
            pstmt.setBinaryStream(3, image.getBinaryStream());
            pstmt.setBigDecimal(4, price);
            pstmt.setFloat(5, grade);
            pstmt.setTime(6, Time.valueOf(LocalTime.now()));
            pstmt.setInt(7, id);

            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}", resultSize);
        } catch (SQLException e) {
            log.error("db error");
            throw new ValueTooLongException(e);
        } finally {
            close(con, pstmt, null);
        }
    }

    public void updateAverageGradeByRestaurantMenu(Float averageGrade, String restaurant, String menu) {
        String sql = "update lunchRecord set averageGrade = ?, updateAt = ? where restaurant = ? and menu = ?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setFloat(1, averageGrade);
            pstmt.setTime(2, Time.valueOf(LocalTime.now()));
            pstmt.setString(3, restaurant);
            pstmt.setString(4, menu);

            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}", resultSize);
        } catch (SQLException e) {
            log.error("db error");
            throw new ValueTooLongException(e);
        } finally {
            close(con, pstmt, null);
        }
    }

    public void delete(int id) {
        String sql = "delete from lunchRecord where id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, id);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("db error");
            throw new ValueTooLongException(e);
        } finally {
            close(con, pstmt, null);
        }
    }

    public void deleteAll() {
        String sql = "delete from lunchRecord";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("db error");
            throw new LunchRecordDbException(e);
        } finally {
            close(con, pstmt, null);
        }

    }

    private void close(Connection con, Statement stmt, ResultSet rs) {
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        // 주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils를 사용해야 한다.
        DataSourceUtils.releaseConnection(con, dataSource);
    }

    private Connection getConnection() throws SQLException {
        // 주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils를 사용해야 한다.
        Connection con = DataSourceUtils.getConnection(dataSource);
        log.info("get connection={}, class={}", con, con.getClass());
        return con;
    }
}
