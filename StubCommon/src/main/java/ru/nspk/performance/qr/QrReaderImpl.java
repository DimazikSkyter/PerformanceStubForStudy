package ru.nspk.performance.qr;

import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.GenericMultipleBarcodeReader;
import lombok.RequiredArgsConstructor;

import java.awt.image.BufferedImage;

@RequiredArgsConstructor
public class QrReaderImpl implements QrReader {

    private final int size;

    @Override
    public QrData readQrFromBufferedImage(BufferedImage bufferedImage) throws NotFoundException {

        checkSize(bufferedImage);

        int[] pixels = bufferedImage.getRGB(0, 0, size, size, null, 0, size);
        RGBLuminanceSource source = new RGBLuminanceSource(size, size, pixels);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        MultiFormatReader reader = new MultiFormatReader();
        GenericMultipleBarcodeReader multiReader = new GenericMultipleBarcodeReader(reader);
        Result[] zxingResults = multiReader.decodeMultiple(bitmap);
        if (zxingResults != null && zxingResults.length == 1) {
            String qrDataText = zxingResults[0].getText();
            return validateQrDataFilled(QrData.fromImageData(qrDataText));
        }
        throw new RuntimeException("Failed to parse qr");
    }

    private void checkSize(BufferedImage bufferedImage) {
        if (bufferedImage.getHeight() != size || bufferedImage.getWidth() != size) {
            throw new RuntimeException("Wrong size of buffered image. Wait size %s, but get  h:%s and w:%s".formatted(size, bufferedImage.getHeight(), bufferedImage.getWidth()));
        }
    }

    private QrData validateQrDataFilled(QrData qrData) {
        if (qrData.getAmount() == 0 || qrData.getTargetAccount() == null || qrData.getPurpose() == null) {
            throw new RuntimeException("Wrong qrData " + qrData);
        }
        return qrData;
    }
}
