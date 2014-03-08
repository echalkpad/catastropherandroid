package main.jf.catastropherandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.facebook.*;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

/**
 * Main login fragment class for Catastropher.
 *
 * @author Johan Stenberg
 */
public class MainLoginFragment extends Fragment {

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    private UiLifecycleHelper uiHelper;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main_login_fragment, container, false);

        LoginButton authButton = (LoginButton) view.findViewById(R.id.authButton);
        authButton.setFragment(this);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(getActivity(), callback);
        uiHelper.onCreate(savedInstanceState);
    }

    private void onSessionStateChange(final Session session, SessionState state, Exception exception) {
        if (state.isOpened()) {
            getView().setVisibility(View.INVISIBLE);
            Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
                @Override
                public void onCompleted(GraphUser user, Response response) {

                    if (session == Session.getActiveSession()) {
                        if (user != null) {
                            String userId = user.getId();
                            getView().setVisibility(View.INVISIBLE);
                            MainActivity mainActivity = (MainActivity) getActivity();
                            mainActivity.hasLoggedIn(userId);
                        }
                    } else {
                        getView().setVisibility(View.VISIBLE);
                    }
                }
            });

            Request.executeBatchAsync(request);
        } else if (state.isClosed()) {
            tellMainActivityLogout();
        }
    }

    @Override
    public void onResume() {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity.getHasLoggedIn()) {
            getView().setVisibility(View.INVISIBLE);
        }

        super.onResume();

        Session session = Session.getActiveSession();
        if (session != null &&
                (session.isOpened() || session.isClosed())) {
            onSessionStateChange(session, session.getState(), null);
        }

        uiHelper.onResume();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    void logout() {
        Session session = Session.getActiveSession();
        if (session != null) {
            session.closeAndClearTokenInformation();
            tellMainActivityLogout();
        }
    }

    private void tellMainActivityLogout() {
        MainActivity mainActivity = (MainActivity) getActivity();
        mainActivity.hasLoggedOut();
    }
}
