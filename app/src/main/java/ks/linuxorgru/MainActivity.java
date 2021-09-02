package ks.linuxorgru;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MainActivity extends Activity {

    private RecyclerView recyclerView;
    private Context context = this;
    private LinuxParse linuxParse = new LinuxParse();
    private List<Article> articles = new ArrayList<>();
    SwipeRefreshLayout swipeContainer;
    ListAdapter adapter;
    BottomNavigationView bottomNavigationView;

    AppDatabase db = App.getInstance().getDatabase();
    ArticleDao articleDao = db.articleDao();
    CommentDao commentDao = db.commentDao();
    TagDao tagDao = db.tagDao();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);

        linuxParse.execute();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("message");
        intentFilter.addAction("article");
        intentFilter.addAction("url");
        intentFilter.addAction("database");
        intentFilter.addAction("tag");
        intentFilter.addAction("add_tag");
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                intentFilter);


        swipeContainer = findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                clearAdapter();
                Intent intent = new Intent("message");
                intent.putExtra("message", "refresh");
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.tags:
                                NavigationTag();
                                break;
                            case R.id.saved:
                                NavigationSaved();
                                break;
                            case R.id.news:
                                NavigationNews();
                                break;
                        }
                        return true;
                    }
                };
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
    }

    private void NavigationTag() {
        clearAdapter();
        adapter.save = false;
        adapter.tags = true;
        linuxParse = new LinuxParse();
        linuxParse.execute();
    }

    private void NavigationSaved() {
        clearAdapter();
        adapter.save = true;
        adapter.tags = false;
        articles = articleDao.getAll();
        initializeAdapter();
    }

    private void NavigationNews() {
        clearAdapter();
        adapter.save = false;
        adapter.tags = false;
        linuxParse = new LinuxParse();
        linuxParse.execute();
    }

    private void update() {
        clearAdapter();
        linuxParse = new LinuxParse();
        linuxParse.execute();
    }

    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("message")) {
                String message = intent.getStringExtra("message");
                int offset = linuxParse.offset;
                if (message != "refresh") {
                    offset += 20;
                } else {
                    offset = 0;
                }
                if (offset < 220) {
                    offset = (offset <= 200) ? (offset) : (200 + (200 - offset));
                    linuxParse = new LinuxParse();
                    linuxParse.offset = offset;
                    linuxParse.execute();
                    Toast.makeText(MainActivity.this, "Запас новостей обновляется", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Запас новостей исчерпан", Toast.LENGTH_SHORT).show();
                }

            } else if (intent.getAction().equals("article")) {
                String message = intent.getStringExtra("article");
                Intent myIntent = new Intent(MainActivity.this, ArticleActivity.class);
                myIntent.putExtra("url", message);
                MainActivity.this.startActivity(myIntent);

            } else if (intent.getAction().equals("url")) {
                String message = intent.getStringExtra("url");
                MainActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(message)));

            } else if (intent.getAction().equals("database")) {
                String insert_url = intent.getStringExtra("insert");
                String delete_url = intent.getStringExtra("delete");

                if (insert_url != null) {
                    ArticleParse articleParse = new ArticleParse();
                    articleParse.article_url = insert_url;
                    articleParse.execute();

                } else if (delete_url != null) {
                    articleDao.deleteByUrl(delete_url);
                }

            } else if (intent.getAction().equals("tag")) {
                final String insert = intent.getStringExtra("insert");
                final String delete = intent.getStringExtra("delete");
                if (insert != null) {
                    alert(true, insert);

                } else if (delete != null) {
                    alert(false, delete);
                }
            } else if (intent.getAction().equals("add_tag")) {
                alertAddTag();
            }
        }
    };

    private void alert(final Boolean flag, final String tag) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        String message = flag ? getString(R.string.insert_tag) : getString(R.string.delete_tag);
        builder.setMessage(Html.fromHtml(message.replace("tag", tag), Html.FROM_HTML_MODE_LEGACY));

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (flag) {
                    tagDao.insert(new Tag(tag));
                } else {
                    tagDao.delete(new Tag(tag));
                }
                update();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        if (!isFinishing()) {
            builder.show();
        }
    }

    private void alertAddTag() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        final View dialogView = getLayoutInflater().inflate(R.layout.tag_view, null);
        builder.setView(dialogView);

        builder.setPositiveButton(R.string.add, null);
        builder.setNegativeButton(R.string.cancel, null);

        if (!isFinishing()) {
            final AlertDialog dialog = builder.create();
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TextInputLayout textLayout = dialogView.findViewById(R.id.textLayout);
                    TextInputEditText textField = dialogView.findViewById(R.id.textField);
                    textLayout.setHelperTextEnabled(true);
                    if (textField.getText().length() != 0) {
                        String tag_name = textField.getText().toString();
                        if (tagDao.getByTitle(tag_name) == null) {
                            tagDao.insert(new Tag(tag_name));
                            update();
                            dialog.dismiss();
                        } else {
                            textLayout.setHelperText("Такой тэг уже добавлен");
                        }
                    } else {
                        textLayout.setHelperText("");
                    }
                }
            });
        }
    }

    private void initializeAdapter() {
        adapter = (ListAdapter) recyclerView.getAdapter();

        if (adapter != null) {
            adapter.articles = articles;
            adapter.notifyDataSetChanged();
            swipeContainer.setRefreshing(false);

        } else {
            adapter = new ListAdapter(articles);
            recyclerView.setAdapter(adapter);
        }
    }

    public void clearAdapter() {
        articles.clear();
        adapter.notifyDataSetChanged();
    }

    private <T> T getRequest(String url) throws Exception {
        URL obj = new URL(getString(R.string.server) + url);
        HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        Gson gson = new Gson();
        T res;
        if (url.contains("articles")) {
            Type articlesList = new TypeToken<Collection<Article>>() {
            }.getType();
            res = gson.fromJson(String.valueOf(response), articlesList);
        } else {
            Type articleObj = new TypeToken<Article>() {
            }.getType();
            res = gson.fromJson(String.valueOf(response), articleObj);
        }
        return res;
    }

    private class LinuxParse extends AsyncTask<Void, Void, Void> {
        List<Article> news = null;
        int offset = 0;
        Boolean tags_check = adapter != null ? adapter.tags : false;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                news = getRequest("/articles/" + offset);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            List<Tag> tagList = tagDao.getAll();

            for (Article article : news) {
                if (tags_check) {
                    boolean flag = false;
                    for (String tag : article.tags.split(";")) {
                        for (Tag db_tag : tagList) {
                            if (db_tag.title.equals(tag)) {
                                flag = true;
                                break;
                            }
                        }
                        if (flag) {
                            break;
                        }
                    }
                    if (flag) {
                        articles.add(article);
                    }
                } else {
                    articles.add(article);
                }
            }
            initializeAdapter();
        }
    }

    private class ArticleParse extends AsyncTask<Void, Void, Void> {
        String article_url;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Article article = getRequest("/article" + article_url.substring("/news".length()));

                articleDao.insert(article);
                for (Comment comment : article.comments) {
                    commentDao.insert(comment);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}