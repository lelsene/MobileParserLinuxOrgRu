package ks.linuxorgru;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity
public class Article {

    @PrimaryKey
    @NonNull
    String title;

    String author;

    String date;

    String text;

    String tags;

    String url;

    String source;

    String mini_text;

    @Ignore
    List<Comment> comments;

    Article(String title, String author, String date, String text,
            String mini_text, String tags, String url, String source) {
        this.author = author;
        this.date = date;
        this.title = title;
        this.text = text;
        this.mini_text = mini_text;
        this.tags = tags;
        this.url = url;
        this.source = source;
    }
}
