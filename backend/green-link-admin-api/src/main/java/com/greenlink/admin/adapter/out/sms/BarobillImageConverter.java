package com.greenlink.admin.adapter.out.sms;

import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/** 바로빌 포토 MMS 조건(JPG, 1MB 이하, 1280x1024 미만)에 맞게 이미지를 변환한다. */
final class BarobillImageConverter {

    static final int MAX_WIDTH = 1279;
    static final int MAX_HEIGHT = 1023;
    static final int MAX_BYTES = 1_000_000;

    private static final double[] QUALITIES = {0.85, 0.7, 0.55, 0.4};
    private static final double SHRINK_STEP = 0.8;
    private static final int MAX_SHRINKS = 5;

    private BarobillImageConverter() {}

    static byte[] toBarobillJpeg(byte[] source) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(source));
        if (image == null) {
            throw new IOException("지원하지 않는 이미지 형식입니다.");
        }
        double ratio = Math.min(1.0, Math.min(
                (double) MAX_WIDTH / image.getWidth(), (double) MAX_HEIGHT / image.getHeight()));

        for (int shrink = 0; shrink <= MAX_SHRINKS; shrink++) {
            double scale = ratio * Math.pow(SHRINK_STEP, shrink);
            for (double quality : QUALITIES) {
                byte[] out = encode(source, scale, quality);
                if (out.length <= MAX_BYTES) {
                    return out;
                }
            }
        }
        throw new IOException("이미지를 1MB 이하로 줄일 수 없습니다.");
    }

    private static byte[] encode(byte[] source, double scale, double quality) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Thumbnails.of(new ByteArrayInputStream(source))
                .scale(scale)
                .outputFormat("jpg")
                .outputQuality(quality)
                .toOutputStream(out);
        return out.toByteArray();
    }
}
