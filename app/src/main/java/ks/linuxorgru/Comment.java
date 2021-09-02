package ks.linuxorgru;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(primaryKeys = {"author", "date", "article_title"},
        foreignKeys = @ForeignKey(
                onDelete = CASCADE,
                entity = Article.class,
                parentColumns = "title",
                childColumns = "article_title"))
public class Comment {

    @NonNull
    String author;

    @NonNull
    String date;

    String title;

    String text;

    @NonNull
    String article_title;

    Comment(String author, String date, String title,
            String text, String article_title) {
        this.author = author;
        this.date = date;
        this.title = title;
        this.text = text;
        this.article_title = article_title;
    }
}
