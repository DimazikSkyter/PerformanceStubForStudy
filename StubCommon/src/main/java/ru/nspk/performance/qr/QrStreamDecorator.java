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

    public DataOutputStream createQrOutputStream(QrData qrData) {
        DataOutputStream dataOutputStream;
        try {
            BufferedImage bufferedImage = qrGenerator.generate(qrData);
            dataOutputStream = new DataOutputStream(new ByteArrayOutputStream());
            ImageIO.write(bufferedImage, FORMAT, dataOutputStream);
        } catch (Exception e) {
            throw  new RuntimeException("Failed to create qr");
        }
        return dataOutputStream;
    }
}
