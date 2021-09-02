package ks.linuxorgru;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CommentDao {

    @Query("SELECT * FROM comment WHERE article_title IS :article_title")
    List<Comment> getCommentsByArticle(String article_title);

    @Insert
    void insert(Comment comment);

    @Delete
    void delete(Comment comment);
}
