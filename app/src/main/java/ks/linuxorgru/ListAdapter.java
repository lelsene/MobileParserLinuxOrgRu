package ks.linuxorgru;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayout;

import java.util.List;


public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ArticleViewHolder> {

    public static class ArticleViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout relativeLayout;
        TextView article_title;
        TextView article_text;
        TextView article_source;
        TextView article_author;
        TextView article_date;
        ImageView article_save;
        FlexboxLayout tags;
        Context context;

        RelativeLayout tags_relativeLayout;
        FlexboxLayout fl_tags;

        ArticleViewHolder(View itemView, int type) {
            super(itemView);
            context = itemView.getContext();
            if (type == 0) {
                article_title = itemView.findViewById(R.id.article_title);
                article_text = itemView.findViewById(R.id.article_text);
                article_source = itemView.findViewById(R.id.article_source);
                article_author = itemView.findViewById(R.id.article_author);
                article_date = itemView.findViewById(R.id.article_date);
                article_save = itemView.findViewById(R.id.article_save);
                tags = itemView.findViewById(R.id.linearLayout_tags);
                relativeLayout = itemView.findViewById(R.id.relativeLayout);
            } else if (type == 1) {
                fl_tags = itemView.findViewById(R.id.fl_tags);
                tags_relativeLayout = itemView.findViewById(R.id.relativeLayout);
            }
        }
    }

    List<Article> articles;
    Boolean save = false;
    Boolean tags = false;
    final int TAGS = 1;
    final int ARTICLES = 0;

    ListAdapter(List<Article> articles) {
        this.articles = articles;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemViewType(int position) {
        if (tags && position == 0)
            return TAGS;
        else {
            return ARTICLES;
        }
    }

    @Override
    public ArticleViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case ARTICLES:
                return new ArticleViewHolder(LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.article_view, viewGroup, false), ARTICLES);
            case TAGS:
                return new ArticleViewHolder(LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.tags_view, viewGroup, false), TAGS);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final ArticleViewHolder articleViewHolder, int y) {
        final Context c = articleViewHolder.context;
        AppDatabase db = App.getInstance().getDatabase();
        final ArticleDao articleDao = db.articleDao();
        final TagDao tagDao = db.tagDao();

        switch (articleViewHolder.getItemViewType()) {
            case ARTICLES:
                final int i;
                if (tags) {
                    i = y - 1;
                } else {
                    i = y;
                }

                final String url = articles.get(i).url;
                final String source_url = articles.get(i).source;

                articleViewHolder.article_title.setText(articles.get(i).title);
                articleViewHolder.article_text.setText(articles.get(i).mini_text);

                SpannableString source = new SpannableString("Источник");
                source.setSpan(new URLSpan(articles.get(i).source), 0, source.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                articleViewHolder.article_source.setText(source);
                articleViewHolder.article_source.setMovementMethod(LinkMovementMethod.getInstance());
                articleViewHolder.article_source.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent("url");
                        intent.putExtra("url", source_url);
                        LocalBroadcastManager.getInstance(articleViewHolder.context).sendBroadcast(intent);
                    }
                });

                articleViewHolder.article_author.setText(articles.get(i).author);
                articleViewHolder.article_date.setText(articles.get(i).date);


                Article article = articleDao.getByTitle(articles.get(i).title);
                if (article == null) {
                    articleViewHolder.article_save.setImageDrawable(ContextCompat.getDrawable(c, R.drawable.baseline_bookmark_border_white_18));
                } else {
                    articleViewHolder.article_save.setImageDrawable(ContextCompat.getDrawable(c, R.drawable.baseline_bookmark_white_18));
                }

                articleViewHolder.article_save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String message;
                        if (articleDao.getByTitle(articles.get(i).title) == null) {
                            articleViewHolder.article_save.setImageDrawable(ContextCompat.getDrawable(c, R.drawable.baseline_bookmark_white_18));
                            message = "insert";
                        } else {
                            articleViewHolder.article_save.setImageDrawable(ContextCompat.getDrawable(c, R.drawable.baseline_bookmark_border_white_18));
                            message = "delete";
                        }
                        Intent intent = new Intent("database");
                        intent.putExtra(message, url);
                        LocalBroadcastManager.getInstance(articleViewHolder.context).sendBroadcast(intent);
                    }
                });


                articleViewHolder.relativeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent("article");
                        intent.putExtra("article", url);
                        LocalBroadcastManager.getInstance(articleViewHolder.context).sendBroadcast(intent);
                    }
                });

                articleViewHolder.tags.removeAllViews();

                for (final String tag : articles.get(i).tags.split(";")) {
                    final TextView tv_tag = new TextView(articleViewHolder.context);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    if (tag != articles.get(i).tags.split(";")[0]) {
                        params.setMarginStart((int) (5 * articleViewHolder.context.getResources().getDisplayMetrics().density));
                    }
                    tv_tag.setTextColor(Color.parseColor("#fcaf3e"));
                    tv_tag.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    tv_tag.setText(tag);
                    tv_tag.setLayoutParams(params);
                    tv_tag.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String message;
                            if (tagDao.getByTitle(tag) == null) {
                                message = "insert";
                            } else {
                                message = "delete";
                            }
                            Intent intent = new Intent("tag");
                            intent.putExtra(message, tag);
                            LocalBroadcastManager.getInstance(articleViewHolder.context).sendBroadcast(intent);
                        }
                    });
                    articleViewHolder.tags.addView(tv_tag);
                }
                if ((i == articles.size() - 1) && (!save)) {
                    Intent intent = new Intent("message");
                    intent.putExtra("message", "load more");
                    LocalBroadcastManager.getInstance(articleViewHolder.context).sendBroadcast(intent);
                }
                break;
            case TAGS:
                articleViewHolder.tags_relativeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent("add_tag");
                        LocalBroadcastManager.getInstance(articleViewHolder.context).sendBroadcast(intent);
                    }
                });

                articleViewHolder.fl_tags.removeAllViews();

                List<Tag> tags_list = tagDao.getAll();
                for (final Tag tag : tags_list) {
                    final TextView tv_tag = new TextView(articleViewHolder.context);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    if (tag != tags_list.get(0)) {
                        params.setMarginStart((int) (5 * articleViewHolder.context.getResources().getDisplayMetrics().density));
                    }
                    tv_tag.setTextColor(Color.parseColor("#fcaf3e"));
                    tv_tag.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                    tv_tag.setText(tag.title);
                    tv_tag.setLayoutParams(params);
                    tv_tag.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String message;
                            if (tagDao.getByTitle(tag.title) == null) {
                                message = "insert";
                            } else {
                                message = "delete";
                            }
                            Intent intent = new Intent("tag");
                            intent.putExtra(message, tag.title);
                            LocalBroadcastManager.getInstance(articleViewHolder.context).sendBroadcast(intent);
                        }
                    });
                    articleViewHolder.fl_tags.addView(tv_tag);
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        if (tags) {
            return articles.size() + 1;
        } else {
            return articles.size();
        }
    }
}