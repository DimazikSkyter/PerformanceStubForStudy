package ru.nspk.performance.qr;


import com.google.zxing.WriterException;

import java.awt.image.BufferedImage;

public interface QrGenerator {

    BufferedImage generate(QrData qrData) throws WriterException;
}
