package ru.nspk.performance.qr;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.RequiredArgsConstructor;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Hashtable;


@RequiredArgsConstructor
public class QrGeneratorImpl implements QrGenerator {

    private final int size;

    @Override
    public BufferedImage generate(QrData qrData) throws WriterException {

        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);;
        image.createGraphics();

        Hashtable<EncodeHintType, ErrorCorrectionLevel> hintMap = new Hashtable<>();
        hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

        BitMatrix byteMatrix = new QRCodeWriter().encode(qrData.toString(), BarcodeFormat.QR_CODE, size, size, hintMap);

        Graphics2D graphics = initGraphics(image, byteMatrix);

        fillQr(graphics, byteMatrix);
        return image;
    }

    private static Graphics2D initGraphics(BufferedImage image, BitMatrix byteMatrix) {
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, byteMatrix.getWidth(), byteMatrix.getWidth());
        return graphics;
    }

    private void fillQr(Graphics2D graphics, BitMatrix byteMatrix) {
        graphics.setColor(Color.BLACK);
        int count = 0;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (byteMatrix.get(i, j)) {
                    count++;
                    graphics.fillRect(i, j, 1, 1);
                }
            }
        }

        if (count == 0) {
            throw new RuntimeException("Qr is blank!");
        }
    }
}
