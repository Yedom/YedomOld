package ru.mralexeimk.yedom.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.LinkedList;

@Data
@AllArgsConstructor
public class BigFile {
    private File file;
    private LinkedList<byte[]> parts;
    private int partsCount;
    private int sizeOfPart;

    public BigFile(File file, byte[] bytes, int sizeOfPart) {
        parts = new LinkedList<>();
        this.sizeOfPart = sizeOfPart;
        for(int i = 0; i < bytes.length; i += sizeOfPart) {
            int minLen = Math.min(sizeOfPart, bytes.length - i);
            byte[] sub = new byte[minLen];
            System.arraycopy(bytes, i, sub, 0, minLen);
            parts.add(sub);
        }
        partsCount = parts.size();
        this.file = file;
    }

    public byte[] send() {
        try {
            byte[] next = parts.get(0);
            parts.removeFirst();
            try (FileOutputStream outputStream = new FileOutputStream(file, true)) {
                outputStream.write(next);
                outputStream.flush();
            } catch (Exception ignored) {}
            return next;
        } catch (Exception ex) {
            return null;
        }
    }

    public double getProgress() {
        if(partsCount == 0) return 100;
        return (int)(100.0 * (double)(partsCount - parts.size()) / partsCount);
    }
}
