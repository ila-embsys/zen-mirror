package dev.zwin.zen.mirror;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.MediaRouteActionProvider;
import android.app.Presentation;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRouter;
import android.media.MediaRouter.RouteInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


/**
 * <h3>Presentation Activity</h3>
 *
 * <p>
 * This demonstrates how to create an activity that shows some content
 * on a secondary display using a {@link Presentation}.
 * </p><p>
 * The activity uses the {@link MediaRouter} API to automatically detect when
 * a presentation display is available and to allow the user to control the
 * media routes using a menu item.  When a presentation display is available,
 * we stop showing content in the main activity and instead open up a
 * {@link Presentation} on the preferred presentation display.  When a presentation
 * display is removed, we revert to showing content in the main activity.
 * We also write information about displays and display-related events to
 * the Android log which you can read using <code>adb logcat</code>.
 * </p><p>
 * You can try this out using an HDMI or Wifi display or by using the
 * "Simulate secondary displays" feature in Development Settings to create a few
 * simulated secondary displays.  Each display will appear in the list along with a
 * checkbox to show a presentation on that display.
 * </p><p>
 * See also the {@link PresentationActivity} sample which
 * uses the low-level display manager to enumerate displays and to show multiple
 * simultaneous presentations on different displays.
 * </p>
 */
public final class PresentationWithMediaRouterActivity extends Activity {
    private final String TAG = "PresentationWithMediaRouterActivity";

    private MediaRouter mMediaRouter;
    private Intent mPresentation;
    private Display mCurrentDisplay;
    private TextView mInfoTextView;
//    private boolean mPaused;

    /**
     * Initialization of the Activity after it is first created.  Must at least
     * call {@link Activity#setContentView setContentView()} to
     * describe what is to be displayed in the screen.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Be sure to call the super class.
        super.onCreate(savedInstanceState);

        // Get the media router service.
        mMediaRouter = (MediaRouter)getSystemService(Context.MEDIA_ROUTER_SERVICE);

        // See assets/res/any/layout/presentation_with_media_router_activity.xml for this
        // view layout definition, which is being set here as
        // the content of our screen.
        setContentView(R.layout.presentation_with_media_router_activity);

        // Get a text view where we will show information about what's happening.
        mInfoTextView = (TextView)findViewById(R.id.info);
    }

    @Override
    protected void onResume() {
        // Be sure to call the super class.
        super.onResume();

        // Listen for changes to media routes.
        mMediaRouter.addCallback(MediaRouter.ROUTE_TYPE_LIVE_VIDEO, mMediaRouterCallback);

        // Update the presentation based on the currently selected route.
        updatePresentation();
    }

    @Override
    protected void onPause() {
        // Be sure to call the super class.
        super.onPause();

        // Stop listening for changes to media routes.
        mMediaRouter.removeCallback(mMediaRouterCallback);

        updateContents();
    }

    private Display getPresentationDisplay() {
        // Get the current route and its presentation display.
        RouteInfo route = mMediaRouter.getSelectedRoute(
                MediaRouter.ROUTE_TYPE_LIVE_VIDEO);
        Display presentationDisplay = route != null ? route.getPresentationDisplay() : null;
        return presentationDisplay;
    };

    private void updatePresentation() {
        Display presentationDisplay = getPresentationDisplay();

        // Dismiss the current presentation if the display has changed.
        if (mPresentation != null && mCurrentDisplay != presentationDisplay) {
            Log.i(TAG, "Dismissing presentation because the current route no longer "
                    + "has a presentation display.");
            mPresentation = null;
        }

        // Show a new presentation if needed.
        if (mPresentation == null && presentationDisplay != null) {
            Log.i(TAG, "Showing presentation on display: " + presentationDisplay);


            Intent myIntent = new Intent(getBaseContext(), ZenMirror.class);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            Bundle opts = ActivityOptions
                    .makeBasic()
                    .setLaunchDisplayId(presentationDisplay.getDisplayId())
                    .toBundle();

            getBaseContext().startActivity(myIntent, opts);


            mPresentation = myIntent;

            mCurrentDisplay = presentationDisplay;
        }

        // Update the contents playing in this activity.
        updateContents();
    }

    private void updateContents() {
        // Show either the content in the main activity or the content in the presentation
        // along with some descriptive text about what is happening.
        Display presentation_display = getPresentationDisplay();
        if (mPresentation != null && presentation_display != null) {
            mInfoTextView.setText(getResources().getString(
                    R.string.presentation_with_media_router_now_playing_remotely,
                    presentation_display.getName()));
        } else {
            mInfoTextView.setText(getResources().getString(
                    R.string.presentation_with_media_router_now_playing_locally,
                    getWindowManager().getDefaultDisplay().getName()));
        }
    }

    private final MediaRouter.SimpleCallback mMediaRouterCallback =
            new MediaRouter.SimpleCallback() {
                @Override
                public void onRoutePresentationDisplayChanged(MediaRouter router, RouteInfo info) {
                    Log.d(TAG, "onRoutePresentationDisplayChanged: info=" + info);
                    updatePresentation();
                }
            };

}
