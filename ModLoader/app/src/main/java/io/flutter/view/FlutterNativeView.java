// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.view;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import io.flutter.Log;
import io.flutter.app.FlutterPluginRegistry;
import io.flutter.embedding.engine.FlutterEngine.EngineLifecycleListener;
import io.flutter.embedding.engine.FlutterJNI;
import io.flutter.embedding.engine.dart.DartExecutor;
import io.flutter.embedding.engine.renderer.FlutterUiDisplayListener;
import io.flutter.plugin.common.*;
import java.nio.ByteBuffer;

/**
 * @deprecated {@link io.flutter.embedding.android.FlutterView} is the new API that now replaces
 *     this class. See https://flutter.dev/go/android-project-migration for more migration details.
 */
@Deprecated
public class FlutterNativeView implements BinaryMessenger {
  private static final String TAG = "FlutterNativeView";

  private final FlutterPluginRegistry mPluginRegistry;
  private final DartExecutor dartExecutor;
  private FlutterView mFlutterView;
  private final FlutterJNI mFlutterJNI;
  private final Context mContext;
  private boolean applicationIsRunning;

  private final FlutterUiDisplayListener flutterUiDisplayListener =
      new FlutterUiDisplayListener() {
        @Override
        public void onFlutterUiDisplayed() {
          if (mFlutterView == null) {
            return;
          }
          mFlutterView.onFirstFrame();
        }

        @Override
        public void onFlutterUiNoLongerDisplayed() {
          // no-op
        }
      };

  public FlutterNativeView(@NonNull Context context) {
    this(context, false);
  }

  public FlutterNativeView(@NonNull Context context, boolean isBackgroundView) {
    if (isBackgroundView) {
      Log.w(TAG, "'isBackgroundView' is no longer supported and will be ignored");
    }
    mContext = context;
    mPluginRegistry = new FlutterPluginRegistry(this, context);
    mFlutterJNI = new FlutterJNI();
    mFlutterJNI.addIsDisplayingFlutterUiListener(flutterUiDisplayListener);
    this.dartExecutor = new DartExecutor(mFlutterJNI, context.getAssets());
    mFlutterJNI.addEngineLifecycleListener(new EngineLifecycleListenerImpl());
    attach(this);
    assertAttached();
  }

  public void detachFromFlutterView() {
    mPluginRegistry.detach();
    mFlutterView = null;
  }

  public void destroy() {
    mPluginRegistry.destroy();
    dartExecutor.onDetachedFromJNI();
    mFlutterView = null;
    mFlutterJNI.removeIsDisplayingFlutterUiListener(flutterUiDisplayListener);
    mFlutterJNI.detachFromNativeAndReleaseResources();
    applicationIsRunning = false;
  }

  @NonNull
  public DartExecutor getDartExecutor() {
    return dartExecutor;
  }

  @NonNull
  public FlutterPluginRegistry getPluginRegistry() {
    return mPluginRegistry;
  }

  public void attachViewAndActivity(FlutterView flutterView, Activity activity) {
    mFlutterView = flutterView;
    mPluginRegistry.attach(flutterView, activity);
  }

  public boolean isAttached() {
    return mFlutterJNI.isAttached();
  }

  public void assertAttached() {
    if (!isAttached()) throw new AssertionError("Platform view is not attached");
  }

  public void runFromBundle(FlutterRunArguments args) {
    if (args.entrypoint == null) {
      throw new AssertionError("An entrypoint must be specified");
    }
    assertAttached();
    if (applicationIsRunning)
      throw new AssertionError("This Flutter engine instance is already running an application");
    mFlutterJNI.runBundleAndSnapshotFromLibrary(
        args.bundlePath,
        args.entrypoint,
        args.libraryPath,
        mContext.getResources().getAssets(),
        null);

    applicationIsRunning = true;
  }

  public boolean isApplicationRunning() {
    return applicationIsRunning;
  }

  @Deprecated
  public static String getObservatoryUri() {
    return FlutterJNI.getVMServiceUri();
  }

  public static String getVMServiceUri() {
    return FlutterJNI.getVMServiceUri();
  }

  @Override
  @UiThread
  public TaskQueue makeBackgroundTaskQueue(TaskQueueOptions options) {
    return dartExecutor.getBinaryMessenger().makeBackgroundTaskQueue(options);
  }

  @Override
  @UiThread
  public void send(String channel, ByteBuffer message) {
    dartExecutor.getBinaryMessenger().send(channel, message);
  }

  @Override
  @UiThread
  public void send(String channel, ByteBuffer message, BinaryReply callback) {
    if (!isAttached()) {
      Log.d(TAG, "FlutterView.send called on a detached view, channel=" + channel);
      return;
    }

    dartExecutor.getBinaryMessenger().send(channel, message, callback);
  }

  @Override
  @UiThread
  public void setMessageHandler(String channel, BinaryMessageHandler handler) {
    dartExecutor.getBinaryMessenger().setMessageHandler(channel, handler);
  }

  @Override
  @UiThread
  public void setMessageHandler(String channel, BinaryMessageHandler handler, TaskQueue taskQueue) {
    dartExecutor.getBinaryMessenger().setMessageHandler(channel, handler, taskQueue);
  }

  @Override
  public void enableBufferingIncomingMessages() {}

  @Override
  public void disableBufferingIncomingMessages() {}

  /*package*/ FlutterJNI getFlutterJNI() {
    return mFlutterJNI;
  }

  private void attach(FlutterNativeView view) {
    mFlutterJNI.attachToNative();
    dartExecutor.onAttachedToJNI();
  }

  private final class EngineLifecycleListenerImpl implements EngineLifecycleListener {
    // Called by native to notify right before the engine is restarted (cold reload).
    @SuppressWarnings("unused")
    public void onPreEngineRestart() {
      if (mFlutterView != null) {
        mFlutterView.resetAccessibilityTree();
      }
      if (mPluginRegistry == null) {
        return;
      }
      mPluginRegistry.onPreEngineRestart();
    }

    public void onEngineWillDestroy() {
      // The old embedding doesn't actually have a FlutterEngine. It interacts with the JNI
      // directly.
    }
  }
}
