package ks.linuxorgru;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Article.class, Comment.class, Tag.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ArticleDao articleDao();

    public abstract CommentDao commentDao();

    public abstract TagDao tagDao();
}