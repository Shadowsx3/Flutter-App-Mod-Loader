// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.app;

import android.content.ComponentCallbacks2;
import android.content.Intent;
import android.os.Bundle;
import io.flutter.plugin.common.PluginRegistry.ActivityResultListener;
import io.flutter.plugin.common.PluginRegistry.RequestPermissionsResultListener;

/**
 * A collection of Android {@code Activity} methods that are relevant to the core operation of
 * Flutter applications.
 *
 * <p>Application authors that use an activity other than {@link FlutterActivity} should forward all
 * events herein from their activity to an instance of {@link FlutterActivityDelegate} in order to
 * wire the activity up to the Flutter framework. This forwarding is already provided in {@code
 * FlutterActivity}.
 */
public interface FlutterActivityEvents
    extends ComponentCallbacks2, ActivityResultListener, RequestPermissionsResultListener {
  /**
   * @param savedInstanceState If the activity is being re-initialized after previously being shut
   *     down then this Bundle contains the data it most recently supplied in {@code
   *     onSaveInstanceState(Bundle)}.
   * @see android.app.Activity#onCreate(Bundle)
   */
  void onCreate(Bundle savedInstanceState);

  /**
   * @param intent The new intent that was started for the activity.
   * @see android.app.Activity#onNewIntent(Intent)
   */
  void onNewIntent(Intent intent);

  /** @see android.app.Activity#onPause() */
  void onPause();

  /** @see android.app.Activity#onStart() */
  void onStart();

  /** @see android.app.Activity#onResume() */
  void onResume();

  /** @see android.app.Activity#onPostResume() */
  void onPostResume();

  /** @see android.app.Activity#onDestroy() */
  void onDestroy();

  /** @see android.app.Activity#onStop() */
  void onStop();

  /**
   * Invoked when the activity has detected the user's press of the back key.
   *
   * @return {@code true} if the listener handled the event; {@code false} to let the activity
   *     continue with its default back button handling.
   * @see android.app.Activity#onBackPressed()
   */
  boolean onBackPressed();

  /** @see android.app.Activity#onUserLeaveHint() */
  void onUserLeaveHint();

  /**
   * @param hasFocus True if the current activity window has focus.
   * @see android.app.Activity#onWindowFocusChanged(boolean)
   */
  void onWindowFocusChanged(boolean hasFocus);
}
