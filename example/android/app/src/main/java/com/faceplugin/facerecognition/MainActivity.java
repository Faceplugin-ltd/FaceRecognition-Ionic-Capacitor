package com.faceplugin.facerecognition;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    preloadNativeLibs();
    super.onCreate(savedInstanceState);
  }

  /**
   * FaceRecognitionSDK.init() dlopens engine .so by filesystem path.
   * Load in this order before the first JS init() (same as RN MainApplication).
   */
  private static void preloadNativeLibs() {
    String[] names = {
      "c++_shared",
      "onnxruntime",
      "FaceRecognitionEngine",
      "FaceRecognitionEngine_jni",
      "FaceRecognitionSDK"
    };
    for (String name : names) {
      try {
        System.loadLibrary(name);
      } catch (UnsatisfiedLinkError ignored) {
        // Optional on some AAR layouts; init() still attempts its own load.
      }
    }
  }
}
