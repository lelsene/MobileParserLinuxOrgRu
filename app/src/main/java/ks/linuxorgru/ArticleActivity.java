package ks.linuxorgru;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;

public class ArticleActivity extends Activity {

    String article_url;
    Article article_main;

    private RecyclerView recyclerView;
    private final Context contextActivity = this;

    SwipeRefreshLayout swipeContainer;
    ArticleAdapter adapter;

    LinuxParse linuxParse = new LinuxParse();

    AppDatabase db = App.getInstance().getDatabase();
    ArticleDao articleDao = db.articleDao();
    CommentDao commentDao = db.commentDao();
    TagDao tagDao = db.tagDao();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        article_url = getIntent().getStringExtra("url");

        recyclerView = findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(contextActivity);
        recyclerView.setLayoutManager(linearLayoutManager);

        article_main = articleDao.getByUrl(article_url);
        if (article_main == null) {
            linuxParse.execute();
        } else {
            article_main.comments = commentDao.getCommentsByArticle(article_main.title);
            initializeAdapter();
        }

        swipeContainer = findViewById(R.id.swipeContainer);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Intent intent = new Intent("refresh");
                intent.putExtra("refresh", "refresh");
                LocalBroadcastManager.getInstance(contextActivity).sendBroadcast(intent);
            }
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("refresh");
        intentFilter.addAction("db");
        intentFilter.addAction("tags");
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReceiver, intentFilter);
    }


    public BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("refresh")) {
                clearAdapter();
                linuxParse = new LinuxParse();
                linuxParse.execute();

            } else if (intent.getAction().equals("db")) {
                String insert_url = intent.getStringExtra("insert");
                String delete_url = intent.getStringExtra("delete");

                if (insert_url != null) {
                    LinuxParse articleParse = new LinuxParse();
                    articleParse.insert = true;
                    articleParse.execute();

                } else if (delete_url != null) {
                    articleDao.deleteByUrl(delete_url);
                }

            } else if (intent.getAction().equals("tags")) {
                final String insert = intent.getStringExtra("insert");
                final String delete = intent.getStringExtra("delete");
                if (insert != null) {
                    alert(true, insert);

                } else if (delete != null) {
                    alert(false, delete);
                }
            }
        }
    };

    private void alert(final Boolean flag, final String tag) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ArticleActivity.this);

        String message = flag ? getString(R.string.insert_tag) : getString(R.string.delete_tag);

        builder.setMessage(Html.fromHtml(message.replace("tag", tag), Html.FROM_HTML_MODE_LEGACY));

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (flag) {
                    tagDao.insert(new Tag(tag));
                } else {
                    tagDao.delete(new Tag(tag));
                }
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

    private void initializeAdapter() {
        adapter = (ArticleAdapter) recyclerView.getAdapter();

        if (adapter != null) {
            adapter.article = article_main;
            adapter.notifyDataSetChanged();
            swipeContainer.setRefreshing(false);
        } else {
            adapter = new ArticleAdapter(article_main);
            recyclerView.setAdapter(adapter);
        }
    }

    public void clearAdapter() {
        adapter.article = null;
        adapter.notifyDataSetChanged();
    }

    private Article getRequest(String url) throws Exception {
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
        Type articleObj = new TypeToken<Article>() {
        }.getType();

        return gson.fromJson(String.valueOf(response), articleObj);
    }

    private class LinuxParse extends AsyncTask<Void, Void, Void> {
        Boolean insert = false;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                article_main = getRequest("/article" + article_url.substring("/news".length()));
                if (insert) {
                    articleDao.deleteByUrl(article_main.url);
                    articleDao.insert(article_main);
                    for (Comment comment : article_main.comments) {
                        commentDao.insert(comment);
                    }
                }
            } catch (
                    Exception e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            initializeAdapter();
        }
    }
}
