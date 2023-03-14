package ussaid.iqbal.transactcampus.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;
import java.util.Random;

import ussaid.iqbal.transactcampus.R;
import ussaid.iqbal.transactcampus.activities.ImageActivity;
import ussaid.iqbal.transactcampus.models.ImagesModel;
import ussaid.iqbal.transactcampus.utils.Constants;
import ussaid.iqbal.transactcampus.utils.TinyDB;

public class ImagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    public ArrayList<ImagesModel> mItemList;
    public ImagesAdapter(ArrayList<ImagesModel> itemList) {
        mItemList = itemList;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
            return new ImageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof ImageViewHolder) {
            populateItemRows((ImageViewHolder) viewHolder, position);
        } else if (viewHolder instanceof LoadingViewHolder) {
            showLoadingView((LoadingViewHolder) viewHolder, position);
        }

    }

    @Override
    public int getItemCount() {
        return mItemList == null ? 0 : mItemList.size();
    }

    /**
     * The following method decides the type of ViewHolder to display in the RecyclerView
     * @param position
     * @return
     */
    @Override
    public int getItemViewType(int position) {
        return mItemList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    private static class ImageViewHolder extends RecyclerView.ViewHolder {
        TextView tvAuthorName;
        ImageView imgMain;
        View mainContainer;
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAuthorName = itemView.findViewById(R.id.tv_author_name);
            imgMain = itemView.findViewById(R.id.img_main);
            mainContainer = itemView.findViewById(R.id.mainContainer);
        }
    }

    private static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    private void showLoadingView(LoadingViewHolder viewHolder, int position) {
        //ProgressBar would be displayed
    }


    public int getRandom(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    private void populateItemRows(ImageViewHolder holder, int position) {
        ImagesModel item = mItemList.get(position);
        String url = item.getUrl();
        Context context = holder.imgMain.getContext();
        Glide.with(context)
                .load(url)
                .placeholder(R.drawable.dummy)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(300, getRandom(200, 600))
                //.transition(DrawableTransitionOptions.withCrossFade())
                .into(holder.imgMain);
        holder.tvAuthorName.setText(String.format(holder.tvAuthorName.getContext().getString(R.string.author), item.getAuthor()));
        holder.mainContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TinyDB tinyDB = new TinyDB(context);
                tinyDB.putObject(Constants.DISPLAY_IMAGE_OBJECT, item);
                context.startActivity(new Intent(context, ImageActivity.class));
            }
        });
    }



}