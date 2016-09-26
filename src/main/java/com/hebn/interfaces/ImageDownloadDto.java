package com.hebn.interfaces;

import lombok.Data;

/**
 * Created by greg.lee on 2016. 9. 25..
 */
public class ImageDownloadDto {

    @Data
    public static class Simple {
        private String tid;
        private String directoryPath;
    }

    @Data
    public static class Complex {
        private String email;
        private String passwdRsa;
        private String directoryPath;
    }
}
