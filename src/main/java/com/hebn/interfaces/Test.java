package com.hebn.interfaces;

import com.google.common.base.Strings;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by greg.lee on 2016. 9. 22..
 */
public class Test {

    public static void main(String[] args) {
//        String test = "123456";
//        System.out.println(Strings.padEnd(test, 25, '0'));



        String testUrl = "http://cythumb.cyworld.com/810x0/cyimg13.cyworld.com/common/file_down.asp?redirect=%2Fp40801%2F2006%2F8%2F17%2F13%2F%BB%E7%C1%F8060815_2%289758%29.jpg";
        String directoryPath = "/Users/coupang/Downloads/cyImage";
        String imageName = "[스크랩][8/14-16 광천]풍성아트강습중";
        String fileExt = "jpg";

        BufferedImage bufferedImage = new BufferedImage(378, 259, BufferedImage.TYPE_INT_BGR);
        File downloadImageFile = null;
        try {
            imageName = imageName.replace("/", ".");
            downloadImageFile = new File(directoryPath + "/" + imageName + "." + fileExt);
            System.out.println("downloadImageFile.getAbsolutePath() = " + downloadImageFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            ImageIO.write(bufferedImage, fileExt, downloadImageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
