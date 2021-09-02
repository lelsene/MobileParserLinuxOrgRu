package ks.linuxorgru;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TagDao {

    @Query("SELECT * FROM tag")
    List<Tag> getAll();

    @Query("SELECT * FROM tag WHERE title = :title")
    Tag getByTitle(String title);

    @Insert
    void insert(Tag tag);

    @Delete
    void delete(Tag tag);
}
