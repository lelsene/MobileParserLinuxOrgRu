package ks.linuxorgru;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Tag {

    @PrimaryKey
    @NonNull
    String title;

    Tag(String title) {
        this.title = title;
    }
}
