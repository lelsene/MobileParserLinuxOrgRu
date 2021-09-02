package ks.linuxorgru;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ArticleDao {

    @Query("SELECT * FROM article")
    List<Article> getAll();

    @Query("SELECT * FROM article WHERE title = :title")
    Article getByTitle(String title);

    @Query("SELECT * FROM article WHERE url = :url")
    Article getByUrl(String url);

    @Query("DELETE FROM article WHERE url = :url")
    void deleteByUrl(String url);

    @Insert
    void insert(Article article);
}
