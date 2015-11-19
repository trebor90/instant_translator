package pl.trebor.instanttranslator;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import pl.trebor.instanttranslator.utils.NetworkUtils;

/**
 * Created by trebor on 11/8/2015.
 */
public class RetainFragment extends Fragment {

    interface InternetConnectionCallback {
        void onPreExecute();

        void onPostExecute(boolean isOnline);
    }

    private InternetConnectionCallback connectionCallback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        connectionCallback = (InternetConnectionCallback) getActivity();
    }

    @Override
    public void onDetach() {
        connectionCallback = null;
        super.onDetach();
    }

    public void checkInternetConnection() {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                if (connectionCallback != null) {
                    connectionCallback.onPreExecute();
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                return NetworkUtils.isOnline();
            }

            @Override
            protected void onPostExecute(Boolean isOnline) {
                super.onPostExecute(isOnline);
                if (connectionCallback != null) {
                    connectionCallback.onPostExecute(isOnline);
                }
            }
        }.execute();
    }
}
