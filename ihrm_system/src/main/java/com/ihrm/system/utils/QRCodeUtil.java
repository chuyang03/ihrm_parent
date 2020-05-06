package com.ihrm.system.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;


@Component
public class QRCodeUtil {

    /**
     * 以dataUrl的形式
     * 生成Base64 二维码
     */
    public String crateQRCode(String content) throws IOException {
        System.out.println(content);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            QRCodeWriter writer = new QRCodeWriter();
            //生成二维码
            BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 200, 200);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            //将二维码数据以byte数组的形式写入到ByteArrayOutputStream
            ImageIO.write(bufferedImage, "png", os);
            //将这个编码后的数据加上dataUrl的前缀才能访问到，拼接前缀
            return new String("data:image/png;base64," + Base64.encode(os.toByteArray()));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            os.close();
        }
        return null;
    }

}
