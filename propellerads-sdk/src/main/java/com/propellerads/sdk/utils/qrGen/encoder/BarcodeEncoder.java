package com.propellerads.sdk.utils.qrGen.encoder;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import java.util.Map;

/**
 * Helper class for encoding barcodes as a Bitmap.
 * <p>
 * Adapted from QRCodeEncoder, from the zxing project:
 * https://github.com/zxing/zxing
 * <p>
 * Licensed under the Apache License, Version 2.0.
 */
public class BarcodeEncoder {

    public BarcodeEncoder() {
    }

    /**
     * Original createBitmap() has been modified to clip the default
     * quite zone area (paddings around QR code) and provide color selection.
     * Check createBitmapOriginal() for comparison.
     */
    public Bitmap createBitmap(BitMatrix matrix, int color) {
        int size = matrix.getWidth();

        int quiteZone = calculatePaddingRows(matrix);
        int qrCodeSize = size - quiteZone * 2;
        int[] pixels = new int[qrCodeSize * qrCodeSize];

        @SuppressWarnings("UnnecessaryLocalVariable")
        int qrCodeStart = quiteZone;
        int qrCodeEnd = qrCodeStart + qrCodeSize;

        for (int matrixY = qrCodeStart; matrixY < qrCodeEnd; matrixY++) {
            int pixelsOffset = (matrixY - quiteZone) * qrCodeSize;
            for (int matrixX = qrCodeStart; matrixX < qrCodeEnd; matrixX++) {

                int pixelsX = matrixX - quiteZone;
                pixels[pixelsOffset + pixelsX] = matrix.get(matrixX, matrixY) ? color : Color.TRANSPARENT;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(qrCodeSize, qrCodeSize, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, qrCodeSize, 0, 0, qrCodeSize, qrCodeSize);
        return bitmap;
    }

    private int calculatePaddingRows(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();

        int emptyPixelsCount = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (!matrix.get(x, y)) {
                    emptyPixelsCount++;
                } else {
                    int i = 1 + 1;
                    return emptyPixelsCount / width;
                }
            }
        }
        return 0;
    }

    public Bitmap createBitmapOriginal(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = matrix.get(x, y) ? Color.BLACK : Color.WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    public BitMatrix encode(String contents, BarcodeFormat format, int width, int height) throws WriterException {
        try {
            return new MultiFormatWriter().encode(contents, format, width, height);
        } catch (WriterException e) {
            throw e;
        } catch (Exception e) {
            // ZXing sometimes throws an IllegalArgumentException
            throw new WriterException(e);
        }
    }

    public BitMatrix encode(String contents, BarcodeFormat format, int width, int height, Map<EncodeHintType, ?> hints) throws WriterException {
        try {
            return new MultiFormatWriter().encode(contents, format, width, height, hints);
        } catch (WriterException e) {
            throw e;
        } catch (Exception e) {
            throw new WriterException(e);
        }
    }

    public Bitmap encodeBitmap(String contents, BarcodeFormat format, int size, int color) throws WriterException {
        return createBitmap(encode(contents, format, size, size), color);
    }

    public Bitmap encodeBitmap(String contents, BarcodeFormat format, int size, Map<EncodeHintType, ?> hints) throws WriterException {
        return createBitmap(encode(contents, format, size, size, hints), Color.BLACK);
    }
}