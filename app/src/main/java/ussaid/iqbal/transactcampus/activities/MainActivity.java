package ussaid.iqbal.transactcampus.activities;

import static ussaid.iqbal.transactcampus.utils.Constants.COLUMNS_COUNT;
import static ussaid.iqbal.transactcampus.utils.Constants.CURRENT_PAGE_INDEX;
import static ussaid.iqbal.transactcampus.utils.Constants.LOAD_LIMIT;
import static ussaid.iqbal.transactcampus.utils.Constants.OFFLINE_FILE;
import static ussaid.iqbal.transactcampus.utils.Constants.SAVED_FILTER;
import static ussaid.iqbal.transactcampus.utils.Constants.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.leo.searchablespinner.SearchableSpinner;
import com.leo.searchablespinner.interfaces.OnItemSelectListener;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import ussaid.iqbal.transactcampus.R;
import ussaid.iqbal.transactcampus.adapters.ImagesAdapter;
import ussaid.iqbal.transactcampus.app.App;
import ussaid.iqbal.transactcampus.models.ImagesModel;
import ussaid.iqbal.transactcampus.utils.Constants;
import ussaid.iqbal.transactcampus.utils.TinyDB;

public class MainActivity extends AppCompatActivity {

    Context context = this;

    private final ArrayList<ImagesModel> photosList = new ArrayList<>();
    private final ArrayList<ImagesModel> photosListFiltered = new ArrayList<>();
    private final ArrayList<String> authors = new ArrayList<>();
    private ImagesAdapter adapter;
    private RecyclerView rvMain;
    private View viewContent, viewLoader;
    private boolean isLoading = false;
    private boolean isFiltering = false;
    private SearchableSpinner searchableSpinner;
    private Button actionRetry;
    private TextView tvMsg;
    private TinyDB tinyDB;//Our DB Manager
    private LottieAnimationView animationView;
    MenuItem menu_grid, menu_list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tinyDB = new TinyDB(context);
        SetUpViews();

    }

    private void SetUpViews(){
        /* Initiate our views */
        tvMsg = findViewById(R.id.tvMsg);
        actionRetry = findViewById(R.id.actionRetry);
        viewContent = findViewById(R.id.viewContent);
        animationView = findViewById(R.id.viewLoading);
        viewLoader = findViewById(R.id.viewLoader);
        rvMain = findViewById(R.id.rv_main);
        Button actionClearFilter = findViewById(R.id.actionClearFilter);

        actionRetry.setVisibility(View.GONE);
        viewLoader.setVisibility(View.VISIBLE);
        viewContent.setVisibility(View.GONE);

        searchableSpinner = new SearchableSpinner(this);
        adapter = new ImagesAdapter(photosList);
        StaggeredGridLayoutManager sm = new StaggeredGridLayoutManager(tinyDB.getInt(COLUMNS_COUNT, 2), StaggeredGridLayoutManager.VERTICAL);

        rvMain.setLayoutManager(sm);
        rvMain.setAdapter(adapter);
        initScrollListener();
        Button actionFilter = findViewById(R.id.actionFilter);
        searchableSpinner.setWindowTitle(getString(R.string.hint_author_search));
        searchableSpinner.setSpinnerListItems(authors);
        searchableSpinner.setOnItemSelectListener(new OnItemSelectListener() {
            @Override
            public void setOnItemSelectListener(int position, @NotNull String selectedString) {
                actionFilter.setText(selectedString);
                tinyDB.putString(SAVED_FILTER, selectedString);
                actionClearFilter.setVisibility(View.VISIBLE);
                FilterData();
            }
        });

        actionFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchableSpinner.show();
            }
        });

        actionFilter.setText((tinyDB.getString(SAVED_FILTER).isEmpty() ? getString(R.string.hint_author_search) : tinyDB.getString(SAVED_FILTER)));

        actionClearFilter.setVisibility((tinyDB.getString(SAVED_FILTER).isEmpty() ? View.GONE : View.VISIBLE));
        actionClearFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFiltering = false;
                tinyDB.putString(SAVED_FILTER, "");
                actionClearFilter.setVisibility(View.GONE);
                actionFilter.setText(getString(R.string.hint_author_search));
                adapter = new ImagesAdapter(photosList);
                rvMain.setAdapter(adapter);
            }
        });

        if(tinyDB.getListObject(OFFLINE_FILE).size() > 0){
            photosList.addAll(tinyDB.getListObject(OFFLINE_FILE));
            Collections.shuffle(photosList);
            CURRENT_PAGE_INDEX = photosList.size()/LOAD_LIMIT;
            adapter.notifyDataSetChanged();
            viewContent.setVisibility(View.VISIBLE);
            viewLoader.setVisibility(View.GONE);
            for (ImagesModel model: photosList) {
                if(!authors.contains(model.getAuthor())){
                    authors.add(model.getAuthor());
                }
            }
        }else{
            LoadImages();
        }
        actionRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoadImages();
            }
        });

        if(!tinyDB.getString(SAVED_FILTER).isEmpty()){
            FilterData();
        }
    }

    private void FilterData(){
        isFiltering = true;
        photosListFiltered.clear();
        for (ImagesModel model: photosList) {
            if(model.getAuthor().toLowerCase(Locale.ROOT).equals(tinyDB.getString(SAVED_FILTER).toLowerCase(Locale.ROOT)))
                photosListFiltered.add(model);
        }
        adapter = new ImagesAdapter(photosListFiltered);
        rvMain.setAdapter(adapter);

    }

    private void LoadImages(){
        if(App.getInstance().isConnected()){
            animationView.setAnimation(R.raw.loader);
            actionRetry.setVisibility(View.GONE);
            tvMsg.setText(getString(R.string.msg_loading));
            viewLoader.setVisibility(View.VISIBLE);
            photosList.add(null);
            adapter.notifyItemInserted(photosList.size() - 1);
            isLoading = true;
            String url = Constants.BASE_URL+"?limit="+Constants.LOAD_LIMIT+"&page="+Constants.CURRENT_PAGE_INDEX;
            Log.e(TAG, url);
            JsonArrayRequest jsonObjectRequest = new JsonArrayRequest
                    (Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            isLoading = false;
                            photosList.remove(photosList.size() - 1);
                            Log.e(Constants.TAG, response.toString());
                            if(response.length() > 0){

                                //we have data
                                try {
                                    for (int x = 0 ; x < response.length() ; x++){
                                        JSONObject object = response.getJSONObject(x);
                                        photosList.add(new ImagesModel(
                                                object.getInt("id"),
                                                object.getString("author"),
                                                object.getString("download_url")
                                        ));
                                        if(isFiltering){
                                            if (object.getString("author").toLowerCase(Locale.ROOT).equals(tinyDB.getString(SAVED_FILTER).toLowerCase(Locale.ROOT))){
                                                photosListFiltered.add(new ImagesModel(
                                                        object.getInt("id"),
                                                        object.getString("author"),
                                                        object.getString("download_url")
                                                ));
                                            }
                                        }
                                        if(!authors.contains(object.getString("author"))){
                                            authors.add(object.getString("author"));
                                        }
                                    }
                                    CURRENT_PAGE_INDEX++;
                                    tinyDB.putListObject(OFFLINE_FILE, photosList);
                                    adapter.notifyItemInserted((isFiltering ? photosListFiltered.size() : photosList.size()));
                                    viewContent.setVisibility(View.VISIBLE);
                                    viewLoader.setVisibility(View.GONE);

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    isLoading = false;
                                    tvMsg.setText(e.getLocalizedMessage());
                                    viewContent.setVisibility(View.GONE);
                                    animationView.setAnimation(R.raw.error);
                                    viewLoader.setVisibility(View.VISIBLE);
                                    actionRetry.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO: Handle error
                            photosList.remove(photosList.size() - 1);
                            Log.e(Constants.TAG, error.getLocalizedMessage());
                            isLoading = false;
                            tvMsg.setText(error.getLocalizedMessage());
                            viewContent.setVisibility(View.GONE);
                            animationView.setAnimation(R.raw.error);
                            viewLoader.setVisibility(View.VISIBLE);
                            actionRetry.setVisibility(View.VISIBLE);
                        }
                    });
            App.getInstance().addToRequestQueue(jsonObjectRequest, "PHOTO_REQUEST");
        }else{
            tvMsg.setText(getString(R.string.msg_network));
            viewContent.setVisibility(View.GONE);
            animationView.setAnimation(R.raw.internet);
            viewLoader.setVisibility(View.VISIBLE);
            actionRetry.setVisibility(View.VISIBLE);
        }

    }

    private void initScrollListener() {
        rvMain.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                 StaggeredGridLayoutManager linearLayoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
                 if(Constants.CURRENT_PAGE_INDEX < Constants.MAX_PAGES && !isLoading){
                     int[] lastVisibleItemPositions = linearLayoutManager.findLastVisibleItemPositions(null);
                    if (getLastVisibleItem(lastVisibleItemPositions) == (isFiltering ? photosListFiltered.size() - 1 : photosList.size() - 1)) {
                        isLoading = true;
                        LoadImages();
                    }
                }
            }
        });
    }

    private int getLastVisibleItem(int[] lastVisibleItemPositions) {
        int maxSize = 0;
        for (int i = 0; i < lastVisibleItemPositions.length; i++) {
            if (i == 0) {
                maxSize = lastVisibleItemPositions[i];
            } else if (lastVisibleItemPositions[i] > maxSize) {
                maxSize = lastVisibleItemPositions[i];
            }
        }
        return maxSize;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sort, menu);
        menu_grid = menu.findItem(R.id.menu_grid);
        menu_list = menu.findItem(R.id.menu_list);
        if(tinyDB.getInt(COLUMNS_COUNT) == 1){
            menu_list.setVisible(false);
        }else{
            menu_grid.setVisible(false);
        }
        return true;
    }
    @SuppressLint({"NonConstantResourceId", "NotifyDataSetChanged"})
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sort_az:
                Collections.sort(photosList, ImagesModel.authorNameSort);
                adapter.notifyDataSetChanged();
                return true;
            case R.id.menu_sort_za:
                Collections.sort(photosList, ImagesModel.authorNameSort);
                Collections.reverse(photosList);
                adapter.notifyDataSetChanged();
                return true;
            case R.id.menu_grid:
                menu_grid.setVisible(false);
                menu_list.setVisible(true);
                StaggeredGridLayoutManager sm = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
                rvMain.setLayoutManager(sm);
                tinyDB.putInt(COLUMNS_COUNT, 2);
                adapter.notifyDataSetChanged();
                return true;
            case R.id.menu_list:
                menu_grid.setVisible(true);
                menu_list.setVisible(false);
                StaggeredGridLayoutManager sm2 = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
                rvMain.setLayoutManager(sm2);
                tinyDB.putInt(COLUMNS_COUNT, 1);
                adapter.notifyDataSetChanged();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}