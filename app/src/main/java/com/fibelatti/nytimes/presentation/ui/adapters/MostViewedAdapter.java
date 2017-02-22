package com.fibelatti.nytimes.presentation.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.fibelatti.nytimes.R;
import com.fibelatti.nytimes.models.MostViewedResult;
import com.fibelatti.nytimes.utils.DateUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MostViewedAdapter
        extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private List<MostViewedResult> articleList;

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

    public MostViewedAdapter(Context context) {
        this.context = context;
        this.articleList = new ArrayList<>();
    }

    public void clearArticleList() {
        this.articleList.clear();
        notifyDataSetChanged();
    }

    public void addArticleToList(MostViewedResult article) {
        this.articleList.add(article);
    }

    @Override
    public int getItemCount() {
        return Math.min(10, this.articleList.size());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_article, parent, false);
        return new DataViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        DataViewHolder dataViewHolder = (DataViewHolder) holder;
        MostViewedResult item = articleList.get(position);

        Stream.of(item.getMedia())
                .filter(m -> m.getType().equals("image"))
                .findFirst()
                .ifPresent(medium -> Stream.of(medium.getMediaMetadata())
                        .filter(m -> m.getFormat().equals("square320"))
                        .findFirst()
                        .ifPresent(metadata -> {
                            dataViewHolder.articlePicture.setVisibility(View.VISIBLE);
                            Picasso.with(context)
                                    .load(metadata.getUrl())
                                    .into(dataViewHolder.articlePicture);
                        }));

        dataViewHolder.articleTitle.setText(item.getTitle());
        dataViewHolder.articlePublishDate.setText(context.getString(R.string.generic_hint_publish_date,
                        DateUtils.changeDateFormat("yyyy-MM-dd", "dd/MM/yyyy", item.getPublishedDate())));
        dataViewHolder.articleAbstract.setText(item.getAbstract());
    }
}
