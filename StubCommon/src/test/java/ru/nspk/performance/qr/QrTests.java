package ru.nspk.performance.qr;

import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class QrTests {

    QrGenerator qrGenerator = new QrGeneratorImpl(256);
    QrReader qrReader = new QrReaderImpl(256);

    @Test
    void create() throws WriterException, IOException {
        BufferedImage qrImage = qrGenerator.generate(QrData.builder()
                .purpose("create")
                .amount(333)
                .targetAccount("12305678941230567894")
                .build());

        BufferedImage referenceQr = ImageIO.read(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("create-test-qr.png")));

        Assertions.assertTrue(isBufferedImagesEqual(referenceQr, qrImage));
        Assertions.assertNotNull(qrImage);
    }

    @Test
    void read() throws IOException, NotFoundException {
        BufferedImage qrImage = ImageIO.read(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("create-test-qr.png")));
        QrData referenceQrData = QrData.builder()
                .targetAccount("12305678941230567894")
                .purpose("create")
                .amount(333)
                .build();

        QrData qrData = qrReader.readQrFromBufferedImage(qrImage);

        Assertions.assertNotNull(qrData);
        Assertions.assertEquals(referenceQrData, qrData);
    }

    @Test
    void shouldGenerateQrDataFromString() {
        int amount = 33122;
        String purpose = "Цель оплаты";
        String account = "123451234512345123450987654321";

        QrData referenceQrData = QrData.builder()
                .amount(amount)
                .purpose(purpose)
                .targetAccount(account)
                .build();
        QrData qrData = QrData.fromImageData("P:" + purpose + ";TA:" + account + ";A:" + amount);

        Assertions.assertEquals(referenceQrData, qrData);
    }

    private boolean isBufferedImagesEqual(BufferedImage bi1, BufferedImage bi2) {
        if (bi1.getWidth() == bi2.getWidth() && bi1.getHeight() == bi2.getHeight()) {
            for (int x = 0; x < bi1.getWidth(); x++) {
                for (int y = 0; y < bi1.getHeight(); y++) {
                    if (bi1.getRGB(x, y) != bi2.getRGB(x, y))
                        return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }
}
