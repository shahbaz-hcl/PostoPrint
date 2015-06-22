package com.blinduck.Postalgia;

/**
 * Created with IntelliJ IDEA.
 * User: deepan
 * Date: 7/26/13
 * Time: 4:09 PM
 * To change this template use File | Settings | File Templates.
 */
import android.os.AsyncTask;
import android.support.v4.content.Loader;

public abstract class ContentChangingTask<T1, T2, T3> extends
        AsyncTask<T1, T2, T3> {
    private Loader<?> loader=null;

    ContentChangingTask(Loader<?> loader) {
        this.loader=loader;
    }

    @Override
    protected void onPostExecute(T3 param) {
        loader.onContentChanged();
    }
}
