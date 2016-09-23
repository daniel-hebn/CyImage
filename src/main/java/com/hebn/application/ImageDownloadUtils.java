package com.hebn.application;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * Created by greg.lee on 2016. 9. 23..
 */
@Slf4j
public class ImageDownloadUtils {

    public static void fileDownloadByUrl(String directoryPath, String imageUrl, String imageName, Integer idx) {
        String fileExt = getFileExtension(imageUrl);
        try {
            BufferedImage image = ImageIO.read(new URL(imageUrl));
            if (image == null) {
                log.error("존재하지 않는 이미지 url 입니다. imageUrl = {}", imageUrl);
            } else {
                log.info("fileExt = {}", fileExt);
                log.info("image.getWidth() = {}" , image.getWidth());
                log.info("image.getHeight() = {}" , image.getHeight());
                log.info("directoryPath / imageName = {}", directoryPath + "/" + imageName);

                BufferedImage bufferedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_BGR);
                Graphics2D graphics = (Graphics2D) bufferedImage.getGraphics();
                graphics.setBackground(Color.WHITE);
                graphics.drawImage(image, 0, 0, null);

                if (idx > 0)
                    imageName = imageName + " [" + idx + "]";  // NOTE: 동일한 파일명이 여러개인 경우
                imageName = imageName.replace("/", ".");  // NOTE: 파일명 내 '/' 포함 시 오류 대응

                File downloadImageFile = new File(directoryPath + "/" + imageName + "." + fileExt);
                ImageIO.write(bufferedImage, fileExt, downloadImageFile);
            }
        } catch (IOException e) {
            log.error("이미지 다운로드 중 에러 발생. imageUrl = {}", imageUrl);
        }
    }

    private static String getFileExtension(String imageName) {
        return imageName.substring(imageName.lastIndexOf(".") + 1, imageName.length());
    }
}
