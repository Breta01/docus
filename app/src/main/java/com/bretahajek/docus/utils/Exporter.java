package com.bretahajek.docus.utils;

import android.util.Log;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;

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

        Document document = new Document(PageSize.A4, 0, 0, 0, 0);

        try {
            PdfWriter.getInstance(document, new FileOutputStream(output));
        } catch (IOException e) {
            Log.e("Export PDF", e.toString());
            return null;
        } catch (DocumentException e) {
            Log.e("Export PDF", e.toString());
            return null;
        }
        document.open();

        for (int i = 0; i < images.length; i++) {
            try {
                Image image = Image.getInstance(images[i].getAbsolutePath());

                float scaler = ((document.getPageSize().getWidth() - document.leftMargin() - document.rightMargin() - 0) / image.getWidth()) * 100; // 0 means you have no indentation. If you have any, change it.
                image.scalePercent(scaler);
                image.setAlignment(Image.ALIGN_CENTER | Image.ALIGN_TOP);
                image.setAbsolutePosition((document.getPageSize().getWidth() - image.getScaledWidth()) / 2.0f,
                        (document.getPageSize().getHeight() - image.getScaledHeight()) / 2.0f);

                document.add(image);
                document.newPage();

            } catch (BadElementException e) {
                Log.e("Export PDF", e.toString());
            } catch (IOException e) {
                Log.e("Export PDF", e.toString());
            } catch (DocumentException e) {
                Log.e("Export PDF", e.toString());
            }
        }

        document.close();
        return output;
    }
}
