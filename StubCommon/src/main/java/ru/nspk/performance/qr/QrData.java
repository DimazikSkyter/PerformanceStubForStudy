package ru.nspk.performance.qr;

import lombok.*;

import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QrData {


    //private long qrId;
    private String purpose;
    private String targetAccount;
    private double amount;

    //todo раскомментировать
    //private Instant closeTimestamp;

    @Override
    public String toString() {
        return "P:" + purpose + ";TA:" + targetAccount + ";A:" + amount;
    }

    public static QrData fromImageData(@NonNull String data) {
        String[] parts = data.split(";");

        if (parts.length != 3) {
            throw new RuntimeException("Wrong data size, wait 3, but get " + parts.length);
        }

        QrData qrData = new QrData();

        for (String part: parts) {
            int indexOfSplitter = part.indexOf(':');
            String type = part.substring(0, indexOfSplitter);
            String value = part.substring(indexOfSplitter + 1);
            setField(qrData, type, value);
        }
        return qrData;
    }

    private static void setField(QrData qrData, String fieldAlias, String value) {
        Logger logger = Logger.getLogger("QrDataTransform");
        switch (fieldAlias) {
            case "P":
                qrData.purpose = value;
                break;
            case "TA":
                qrData.targetAccount = value;
                break;
            case "A":
                qrData.amount = Double.parseDouble(value);
                break;
            default:
                logger.log(Level.ALL, "Failed to find field {}", fieldAlias);
        }
    }
}
