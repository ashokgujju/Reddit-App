package com.ashok.simplereader.sync;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.ashok.simplereader.data.PostColumns;
import com.ashok.simplereader.data.PostProvider;

import net.dean.jraw.RedditClient;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.SubredditPaginator;

import java.util.ArrayList;

/**
 * Created by ashok on 3/4/17.
 */

public class PostSyncJob {
    public static void getPosts(Context context) {
        RedditClient redditClient = AuthenticationManager.get().getRedditClient();
        if (redditClient.isAuthenticated()) {
            SubredditPaginator paginator = new SubredditPaginator(redditClient);
            Listing<Submission> posts = paginator.next();

            ArrayList<ContentValues> postCVs = new ArrayList<>();
            for (Submission post : posts) {
                ContentValues postCV = new ContentValues();
                postCV.put(PostColumns.DATA, post.getDataNode().toString());
                postCVs.add(postCV);
            }

            if (postCVs.size() > 0) {
                context.getContentResolver().delete(PostProvider.Posts.CONTENT_URI, null, null);
                context.getContentResolver().bulkInsert(PostProvider.Posts.CONTENT_URI, postCVs.toArray(
                        new ContentValues[postCVs.size()]
                ));
            }
        }
    }

    public static synchronized void initialize(final Context context) {
        syncImmediately(context);
    }

    public static synchronized void syncImmediately(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            Intent nowIntent = new Intent(context, PostIntentService.class);
            context.startService(nowIntent);
        }
    }
}
