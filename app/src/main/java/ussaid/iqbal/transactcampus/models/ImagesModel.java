package ussaid.iqbal.transactcampus.models;

import java.util.Comparator;
import java.util.Locale;

public class ImagesModel {
    private int id;
    private String author;
    private String url;

    public ImagesModel(int id, String author, String url) {
        this.id = id;
        this.author = author;
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public static Comparator<ImagesModel> authorNameSort = new Comparator<ImagesModel>() {
        @Override
        public int compare(ImagesModel im1, ImagesModel im2) {
            return (int) (im1.getAuthor().toLowerCase(Locale.ROOT).compareTo(im2.getAuthor().toLowerCase(Locale.ROOT)));
        }
    };



}

