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

    public static String substringByBytes(String str, int beginBytes, int endBytes) {
        if (str == null || str.length() == 0) {
            return "";
        }

        if (beginBytes < 0) {
            beginBytes = 0;
        }

        if (endBytes < 1) {
            return "";
        }

        int length = str.length();
        int beginIndex = -1;
        int endIndex = 0;
        int currentBytes = 0;

        for (int index = 0; index < length; index++) {
            currentBytes += String.valueOf(str.charAt(index)).getBytes().length;
            if (beginIndex == -1 && currentBytes >= beginBytes) {
                beginIndex = index;
            }
            if (currentBytes > endBytes) {
                break;
            } else {
                endIndex = index+1;
            }
        }

        return str.substring(beginIndex, endIndex);
    }
}
