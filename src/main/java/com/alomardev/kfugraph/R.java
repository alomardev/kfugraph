package com.alomardev.kfugraph;

import java.io.InputStream;
import java.net.URL;

public class R {

    public static URL getURL(String resName) {
        return R.class.getClassLoader().getResource("images/" + resName);
    }
}