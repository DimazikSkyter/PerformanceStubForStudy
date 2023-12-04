package ru.nspk.performance.qr;

import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

public class QrStreamDecorator {

    private static final String FORMAT = "png";
    private final QrGenerator qrGenerator;
    private final QrReader qrReader;

    public QrStreamDecorator(int imageSize) {
        this.qrGenerator = new QrGeneratorImpl(imageSize);
        this.qrReader = new QrReaderImpl(imageSize);
    }

    public QrData read(DataInputStream dataInputStream) throws IOException, NotFoundException {
        int len = dataInputStream.readInt();
        byte[] buffer = new byte[len];
        dataInputStream.readFully(buffer, 0, len);
        BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(buffer));
        return qrReader.readQrFromBufferedImage(bufferedImage);
    }

    public ByteArrayOutputStream createQrOutputStream(QrData qrData) {
        ByteArrayOutputStream byteArrayOutputStream;
        try {
            BufferedImage bufferedImage = qrGenerator.generate(qrData);
            byteArrayOutputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, FORMAT, byteArrayOutputStream);
        } catch (Exception e) {
            throw  new RuntimeException("Failed to create qr");
        }
        return byteArrayOutputStream;
    }
}
