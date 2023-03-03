package lunch.record.util;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

@Slf4j
public class Utils {

    public static byte[] imageToByteArray(String filePath) {
        byte[] returnValue = null;

        ByteArrayOutputStream baos = null;
        FileInputStream fis = null;

        try {
            baos = new ByteArrayOutputStream();
            fis = new FileInputStream(filePath);

            byte[] buf = new byte[1024];
            int read = 0;

            while ((read = fis.read(buf, 0, buf.length)) != -1) {
                baos.write(buf, 0, read);
            }
            returnValue = baos.toByteArray();
        } catch (IOException e) {
            log.error("io error", e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    log.info("error", e);
                }
            }

            if (baos != null) {
                try {
                    baos.close();
                } catch (IOException e) {
                    log.info("error", e);
                }
            }
        }
        return returnValue;
    }
}
