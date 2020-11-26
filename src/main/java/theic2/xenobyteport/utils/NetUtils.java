package theic2.xenobyteport.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class NetUtils {

    public static InputStream getInputStream(String address) {
        return getInputStream(address, 5);
    }

    public static BufferedImage getImage(String address) {
        try {
            return ImageIO.read(getInputStream(address));
        } catch (Exception e) {
            return null;
        }
    }

    public static InputStream getInputStream(String address, int setTimeout) {
        try {
            URLConnection con = new URL(address).openConnection();
            con.setReadTimeout((setTimeout * 1000) * 2);
            con.setConnectTimeout(setTimeout * 1000);
            con.setDoOutput(true);
            con.connect();
            return con.getInputStream();
        } catch (Exception e) {
            return null;
        }
    }

    public static String getContent(String address) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(getInputStream(address)));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } catch (Exception e) {
            return null;
        }
    }

}
