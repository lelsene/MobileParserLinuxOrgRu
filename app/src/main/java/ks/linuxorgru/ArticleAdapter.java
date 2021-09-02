package ks.linuxorgru;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Html;
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
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.FlexboxLayout;


public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder> {

    public static class ArticleViewHolder extends RecyclerView.ViewHolder {

        Context context;
        TextView article_title;
        TextView article_text;
        TextView article_source;
        TextView article_author;
        TextView article_date;
        ImageView article_save;
        FlexboxLayout tags;

        TextView comment_title;
        TextView comment_text;
        TextView comment_author;
        TextView comment_date;

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
            } else if (type == 1) {
                comment_title = itemView.findViewById(R.id.comment_title);
                comment_text = itemView.findViewById(R.id.comment_text);
                comment_author = itemView.findViewById(R.id.comment_author);
                comment_date = itemView.findViewById(R.id.comment_date);
            }
        }
    }

    final int ARTICLE = 0;
    final int COMMENT = 1;

    Article article;

    ArticleAdapter(Article article) {
        this.article = article;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return ARTICLE;
        else {
            return COMMENT;
        }
    }

    @Override
    public ArticleViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case ARTICLE:
                return new ArticleViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.article_view, viewGroup, false), ARTICLE);
            case COMMENT:
                return new ArticleViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.comment_view, viewGroup, false), COMMENT);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final ArticleViewHolder articleViewHolder, int i) {
        AppDatabase db = App.getInstance().getDatabase();
        final ArticleDao articleDao = db.articleDao();
        final TagDao tagDao = db.tagDao();

        switch (articleViewHolder.getItemViewType()) {
            case ARTICLE:
                final String url = article.url;
                final String source_url = article.source;
                final Context c = articleViewHolder.context;

                articleViewHolder.article_title.setText(article.title);
                articleViewHolder.article_text.setText(Html.fromHtml(article.text, Html.FROM_HTML_MODE_LEGACY));
                articleViewHolder.article_text.setMovementMethod(LinkMovementMethod.getInstance());

                SpannableString source = new SpannableString("Источник");
                source.setSpan(new URLSpan(article.source), 0, source.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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

                articleViewHolder.article_author.setText(article.author);
                articleViewHolder.article_date.setText(article.date);


                Article article_db = articleDao.getByTitle(article.title);
                if (article_db == null) {
                    articleViewHolder.article_save.setImageDrawable(ContextCompat.getDrawable(c, R.drawable.baseline_bookmark_border_white_18));
                } else {
                    articleViewHolder.article_save.setImageDrawable(ContextCompat.getDrawable(c, R.drawable.baseline_bookmark_white_18));
                }

                articleViewHolder.article_save.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String message;
                        if (articleDao.getByTitle(article.title) == null) {
                            articleViewHolder.article_save.setImageDrawable(ContextCompat.getDrawable(c, R.drawable.baseline_bookmark_white_18));
                            message = "insert";
                        } else {
                            articleViewHolder.article_save.setImageDrawable(ContextCompat.getDrawable(c, R.drawable.baseline_bookmark_border_white_18));
                            message = "delete";
                        }
                        Intent intent = new Intent("db");
                        intent.putExtra(message, url);
                        LocalBroadcastManager.getInstance(articleViewHolder.context).sendBroadcast(intent);
                    }
                });

                articleViewHolder.tags.removeAllViews();

                for (final String tag : article.tags.split(";")) {
                    final TextView tv_tag = new TextView(articleViewHolder.context);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    if (tag != article.tags.split(";")[0]) {
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
                            Intent intent = new Intent("tags");
                            intent.putExtra(message, tag);
                            LocalBroadcastManager.getInstance(articleViewHolder.context).sendBroadcast(intent);
                        }
                    });
                    articleViewHolder.tags.addView(tv_tag);

                }
                break;
            case COMMENT:
                articleViewHolder.comment_title.setText(article.comments.get(i - 1).title);
                articleViewHolder.comment_text.setText(Html.fromHtml(article.comments.get(i - 1).text, Html.FROM_HTML_MODE_LEGACY));
                articleViewHolder.comment_text.setMovementMethod(LinkMovementMethod.getInstance());
                articleViewHolder.comment_author.setText(article.comments.get(i - 1).author);
                articleViewHolder.comment_date.setText(article.comments.get(i - 1).date);
                break;
        }
    }

    @Override
    public int getItemCount() {
        if (article != null) {
            return article.comments.size() + 1;
        } else {
            return 0;
        }
    }
}