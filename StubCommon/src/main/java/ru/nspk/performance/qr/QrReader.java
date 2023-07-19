package ru.nspk.performance.qr;

import com.google.zxing.NotFoundException;

import java.awt.image.BufferedImage;

public interface QrReader {

    QrData readQrFromBufferedImage(BufferedImage bufferedImage) throws NotFoundException;
}
