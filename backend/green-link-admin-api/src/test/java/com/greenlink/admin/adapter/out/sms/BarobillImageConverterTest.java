package com.greenlink.admin.adapter.out.sms;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BarobillImageConverterTest {

    @Test
    void 큰_이미지는_1280x1024_미만_1MB_이하_JPG로_변환된다() throws IOException {
        byte[] result = BarobillImageConverter.toBarobillJpeg(noisePng(3000, 2000));

        BufferedImage converted = ImageIO.read(new ByteArrayInputStream(result));
        assertThat(converted.getWidth()).isLessThan(1280);
        assertThat(converted.getHeight()).isLessThan(1024);
        assertThat(result.length).isLessThanOrEqualTo(BarobillImageConverter.MAX_BYTES);
        assertThat(result[0] & 0xFF).isEqualTo(0xFF);
        assertThat(result[1] & 0xFF).isEqualTo(0xD8);
    }

    @Test
    void 세로로_긴_이미지도_높이_제한을_만족한다() throws IOException {
        byte[] result = BarobillImageConverter.toBarobillJpeg(noisePng(900, 1200));

        BufferedImage converted = ImageIO.read(new ByteArrayInputStream(result));
        assertThat(converted.getHeight()).isLessThan(1024);
        assertThat(converted.getWidth()).isLessThan(1280);
    }

    @Test
    void 작은_이미지는_확대하지_않는다() throws IOException {
        byte[] result = BarobillImageConverter.toBarobillJpeg(noisePng(200, 100));

        BufferedImage converted = ImageIO.read(new ByteArrayInputStream(result));
        assertThat(converted.getWidth()).isEqualTo(200);
        assertThat(converted.getHeight()).isEqualTo(100);
    }

    @Test
    void 이미지가_아닌_데이터는_실패한다() {
        assertThatThrownBy(() -> BarobillImageConverter.toBarobillJpeg("not an image".getBytes()))
                .isInstanceOf(IOException.class);
    }

    private byte[] noisePng(int w, int h) throws IOException {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Random random = new Random(1);
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                img.setRGB(x, y, random.nextInt(0xFFFFFF));
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, "png", out);
        return out.toByteArray();
    }
}
