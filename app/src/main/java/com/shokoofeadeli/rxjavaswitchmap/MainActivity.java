package com.shokoofeadeli.rxjavaswitchmap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.widget.ProgressBar;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class MainActivity extends AppCompatActivity implements RecyclerAdapter.OnPostClickListener {

    private static final String TAG = "MainActivity";
    private static final int PERIOD = 100;
    private RecyclerView recyclerView;
    private CompositeDisposable disposables = new CompositeDisposable();
    private RecyclerAdapter adapter;
    private ProgressBar progressBar;
    private PublishSubject<Post> publishSubject = PublishSubject.create(); // for selecting a post

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);
        initRecyclerView();
        retrievePosts();
    }

    private void retrievePosts() {
        ServiceGenerator.getRequestApi()
                .getPosts()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<Post>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposables.add(d);
                    }

                    @Override
                    public void onNext(List<Post> posts) {
                        adapter.setPosts(posts);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void initSwitchMapDemo() {
        publishSubject
                .switchMap(new Function<Post, ObservableSource<Post>>() {
                    @Override
                    public ObservableSource<Post> apply(final Post post) throws Exception {
                        return Observable
                                .interval(PERIOD, TimeUnit.MILLISECONDS)
                                .subscribeOn(AndroidSchedulers.mainThread())
                                .takeWhile(new Predicate<Long>() {
                                    @Override
                                    public boolean test(Long aLong) throws Exception {
                                        Log.d(TAG, "test: " + Thread.currentThread().getName() + ", " + aLong);
                                        progressBar.setMax(3000 - PERIOD);
                                        progressBar.setProgress(Integer.parseInt(String.valueOf((aLong * PERIOD) + PERIOD)));
                                        return aLong <= (3000 / PERIOD);
                                    }
                                })
                                .filter(new Predicate<Long>() {
                                    @Override
                                    public boolean test(Long aLong) throws Exception {
                                        return aLong >= (3000 / PERIOD);
                                    }
                                })
                                .subscribeOn(Schedulers.io())
                                .flatMap(new Function<Long, ObservableSource<Post>>() {
                                    @Override
                                    public ObservableSource<Post> apply(Long aLong) throws Exception {
                                        return ServiceGenerator.getRequestApi()
                                                .getPost(post.getId());
                                    }
                                });
                    }
                })
                .subscribe(new Observer<Post>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposables.add(d);
                    }

                    @Override
                    public void onNext(Post post) {
                        Log.d(TAG, "onNext: done.");
                        navViewPostActivity(post);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: ", e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void initRecyclerView() {
        adapter = new RecyclerAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onPause: called.");
        disposables.clear();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setProgress(0);
        initSwitchMapDemo();
    }

    private void navViewPostActivity(Post post) {
        Intent intent = new Intent(this, PostActivity.class);
        intent.putExtra("post", (Parcelable) post);
        startActivity(intent);
    }

    @Override
    public void onPostClick(int position) {
        Log.d(TAG, "onPostClick: clicked.");

        // submit the selected post object to be queried
        publishSubject.onNext(adapter.getPosts().get(position));
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}