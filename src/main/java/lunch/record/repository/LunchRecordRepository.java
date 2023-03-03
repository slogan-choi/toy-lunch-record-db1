package lunch.record.repository;

import lombok.extern.slf4j.Slf4j;
import lunch.record.connection.DBConnectionUtil;
import lunch.record.domain.LunchRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;

@Slf4j
public class LunchRecordRepository {

    public LunchRecord save(LunchRecord lunchRecord) throws SQLException {
        String sql = "insert into lunchRecord(restaurant, menu, image, price, grade, updateAt, createAt) values(?, ?, ?, ?, ?, ?, ?)";
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
            pstmt.setTime(6, Time.valueOf(lunchRecord.getUpdateAt()));
            pstmt.setTime(7, Time.valueOf(lunchRecord.getCreateAt()));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("db error", e);
            throw e;
        } finally {
            close(con, pstmt, null);
        }
        return lunchRecord;
    }

    private void close(Connection con, Statement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }

        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }

        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                log.info("error", e);
            }
        }
    }

    private Connection getConnection() {
        return DBConnectionUtil.getConnection();
    }
}
