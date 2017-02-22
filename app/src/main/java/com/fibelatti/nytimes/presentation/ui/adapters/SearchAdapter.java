package com.fibelatti.nytimes.presentation.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.fibelatti.nytimes.R;
import com.fibelatti.nytimes.models.SearchDoc;
import com.fibelatti.nytimes.network.ArticleService;
import com.fibelatti.nytimes.utils.DateUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;

    private Context context;
    private List<SearchDoc> articleList;

    private boolean isLoadingVisible = false;
    private int loadingItemIndex = -1;

    public class LoadingViewHolder
            extends RecyclerView.ViewHolder {
        @BindView(R.id.progress_bar)
        public ProgressBar progressBar;

        public LoadingViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public class DataViewHolder
            extends RecyclerView.ViewHolder {
        @BindView(R.id.image_view_article_picture)
        public ImageView articlePicture;
        @BindView(R.id.text_view_article_title)
        public TextView articleTitle;
        @BindView(R.id.text_view_article_publish_date)
        public TextView articlePublishDate;
        @BindView(R.id.text_view_article_abstract)
        public TextView articleAbstract;

        public DataViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public SearchAdapter(Context context) {
        this.context = context;
        this.articleList = new ArrayList<>();
    }

    public void clearArticleList() {
        this.articleList.clear();
        notifyDataSetChanged();
    }

    public void addArticleToList(SearchDoc article) {
        this.articleList.add(article);
    }

    public void showLoadingItem() {
        this.isLoadingVisible = true;
        this.articleList.add(null);
        this.loadingItemIndex = articleList.size() - 1;
        notifyItemInserted(this.loadingItemIndex);
    }

    public void hideLoadingItem() {
        if (isLoadingVisible) {
            this.isLoadingVisible = false;
            this.articleList.remove(this.loadingItemIndex);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return articleList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return this.articleList.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_article, parent, false);
            return new DataViewHolder(itemView);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_loading, parent, false);
            return new LoadingViewHolder(itemView);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DataViewHolder) {
            DataViewHolder dataViewHolder = (DataViewHolder) holder;
            SearchDoc item = articleList.get(position);

            Stream.of(item.getMultimedia())
                    .filter(m -> m.getType().equals("image"))
                    .findFirst()
                    .ifPresent(media -> {
                        dataViewHolder.articlePicture.setVisibility(View.VISIBLE);
                        Picasso.with(context)
                                .load(ArticleService.BASE_IMG_URL + media.getUrl())
                                .into(dataViewHolder.articlePicture);
                    });

            String title = item.getHeadline().getPrintHeadline() != null ?
                    item.getHeadline().getPrintHeadline() : item.getHeadline().getMain();

            dataViewHolder.articleTitle.setText(title);
            dataViewHolder.articlePublishDate.setText(context.getString(R.string.generic_hint_publish_date,
                    DateUtils.changeDateFormat("yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy", item.getPubDate())));
            dataViewHolder.articleAbstract.setText(item.getLeadParagraph());
        } else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }
}
