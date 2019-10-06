package com.bretahajek.docus.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.pdf.PdfDocument;
import android.util.Log;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;

public class Exporter {

    private static String[] EXTENSION_WHITELIST = new String[]{"JPG"};

    public static File exportAsPdf(File documentFolder, File output) {
        File[] images = documentFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                for (String extension : EXTENSION_WHITELIST) {
                    if (extension.equals(FilenameUtils.getExtension(s).toUpperCase()))
                        return true;
                }
                return false;
            }
        });

        if (images == null || images.length == 0) {
            return null;
        }

        PdfDocument document = new PdfDocument();

        for (int i = 0; i < images.length; i++) {
            Bitmap image = BitmapFactory.decodeFile(images[i].getAbsolutePath());

            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                    image.getWidth(), image.getHeight(), i + 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            page.getCanvas().drawBitmap(
                    image, null,
                    new Rect(0, 0, image.getWidth(), image.getHeight()),
                    new Paint());

            document.finishPage(page);
        }

        try {
            document.writeTo(new FileOutputStream(output));
            return output;
        } catch (IOException e) {
            Log.e("Export PDF", e.toString());
            return null;
        } finally {
            document.close();
        }
    }
}
