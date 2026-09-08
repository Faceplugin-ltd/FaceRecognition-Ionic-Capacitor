package com.facerecognitionsdk

import android.graphics.Bitmap
import android.graphics.Color
import android.os.SystemClock
import android.util.Base64
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.core.UseCaseGroup
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.getcapacitor.JSArray
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import com.faceplugin.facerecognitionsdk.FaceBox
import com.faceplugin.facerecognitionsdk.FaceDetectionParam
import com.faceplugin.facerecognitionsdk.FaceRecognitionSDK
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.concurrent.Executors

@CapacitorPlugin(name = "FaceRecognitionSdk")
class FaceRecognitionSdkPlugin : Plugin() {

  private val executor = Executors.newSingleThreadExecutor()
  private val analysisExecutor = Executors.newSingleThreadExecutor()
  @Volatile private var lastLiveBitmap: Bitmap? = null
  @Volatile private var nativesLoaded = false
  @Volatile private var analysisBusy = false
  @Volatile private var lastAnalysisMs = 0L

  private var cameraProvider: ProcessCameraProvider? = null
  private var previewView: PreviewView? = null
  private var previewHost: FrameLayout? = null

  override fun load() {
    preloadNatives()
  }

  private fun preloadNatives() {
    if (nativesLoaded) return
    val names = arrayOf(
      "c++_shared",
      "onnxruntime",
      "FaceRecognitionEngine",
      "FaceRecognitionEngine_jni",
      "FaceRecognitionSDK"
    )
    for (name in names) {
      try {
        System.loadLibrary(name)
      } catch (_: UnsatisfiedLinkError) {
      }
    }
    nativesLoaded = true
  }

  @PluginMethod
  fun getMachineCode(call: PluginCall) {
    executor.execute {
      try {
        preloadNatives()
        val mc = FaceRecognitionSDK.getMachineCode(context.applicationContext) ?: ""
        call.resolve(value(mc))
      } catch (t: Throwable) {
        reject(call, "E_MACHINE_CODE", t)
      }
    }
  }

  @PluginMethod
  fun getLicenseStatus(call: PluginCall) {
    executor.execute {
      try {
        preloadNatives()
        call.resolve(value(FaceRecognitionSDK.getLicenseStatus()))
      } catch (t: Throwable) {
        reject(call, "E_LICENSE_STATUS", t)
      }
    }
  }

  @PluginMethod
  fun setActivation(call: PluginCall) {
    val license = call.getString("license")
    if (license.isNullOrBlank()) {
      call.reject("license is required", "E_ACTIVATION")
      return
    }
    executor.execute {
      try {
        preloadNatives()
        val code = FaceRecognitionSDK.setActivation(context.applicationContext, license)
        call.resolve(value(code))
      } catch (t: Throwable) {
        reject(call, "E_ACTIVATION", t)
      }
    }
  }

  @PluginMethod
  fun init(call: PluginCall) {
    executor.execute {
      try {
        preloadNatives()
        val code = FaceRecognitionSDK.init(context.applicationContext)
        call.resolve(value(code))
      } catch (t: Throwable) {
        reject(call, "E_INIT", t)
      }
    }
  }

  @PluginMethod
  fun deinit(call: PluginCall) {
    executor.execute {
      try {
        FaceRecognitionSDK.deinit()
        call.resolve()
      } catch (t: Throwable) {
        reject(call, "E_DEINIT", t)
      }
    }
  }

  @PluginMethod
  fun lastLicenseError(call: PluginCall) {
    try {
      call.resolve(value(FaceRecognitionSDK.lastLicenseError() ?: ""))
    } catch (t: Throwable) {
      reject(call, "E_LICENSE_ERROR", t)
    }
  }

  @PluginMethod
  fun setLandmarkMode(call: PluginCall) {
    val mode = call.getInt("mode") ?: 14
    executor.execute {
      try {
        call.resolve(value(FaceRecognitionSDK.setLandmarkMode(mode)))
      } catch (t: Throwable) {
        reject(call, "E_LANDMARK", t)
      }
    }
  }

  @PluginMethod
  fun getLandmarkMode(call: PluginCall) {
    executor.execute {
      try {
        call.resolve(value(FaceRecognitionSDK.getLandmarkMode()))
      } catch (t: Throwable) {
        reject(call, "E_LANDMARK", t)
      }
    }
  }

  @PluginMethod
  fun detect(call: PluginCall) {
    val imageUri = call.getString("image")
    if (imageUri.isNullOrBlank()) {
      call.reject("image is required", "E_IMAGE")
      return
    }
    val crop = call.getBoolean("crop", false) ?: false
    val flags = call.getInt("flags") ?: -1
    executor.execute {
      try {
        val bitmap = loadBitmap(imageUri) ?: run {
          call.reject("Could not decode image: $imageUri", "E_IMAGE")
          return@execute
        }
        val json = if (flags < 0 || flags == FaceRecognitionSDK.DETECT_ALL) {
          FaceRecognitionSDK.detect(bitmap, crop, FaceRecognitionSDK.DETECT_ALL)
        } else {
          FaceRecognitionSDK.detect(bitmap, crop, flags)
        }
        call.resolve(value(json ?: "{}"))
      } catch (t: Throwable) {
        reject(call, "E_DETECT", t)
      }
    }
  }

  @PluginMethod
  fun faceDetection(call: PluginCall) {
    val imageUri = call.getString("image")
    if (imageUri.isNullOrBlank()) {
      call.reject("image is required", "E_IMAGE")
      return
    }
    val paramJson = call.getString("param")
    executor.execute {
      try {
        val bitmap = loadBitmap(imageUri) ?: run {
          call.reject("Could not decode image: $imageUri", "E_IMAGE")
          return@execute
        }
        val boxes = FaceRecognitionSDK.faceDetection(bitmap, parseParam(paramJson))
        call.resolve(value(boxesToJson(boxes)))
      } catch (t: Throwable) {
        reject(call, "E_FACE_DETECTION", t)
      }
    }
  }

  @PluginMethod
  fun templateExtraction(call: PluginCall) {
    val imageUri = call.getString("image")
    val faceBoxJson = call.getString("faceBox")
    if (imageUri.isNullOrBlank() || faceBoxJson.isNullOrBlank()) {
      call.reject("image and faceBox are required", "E_TEMPLATE")
      return
    }
    executor.execute {
      try {
        val bitmap = loadBitmap(imageUri) ?: run {
          call.reject("Could not decode image: $imageUri", "E_IMAGE")
          return@execute
        }
        val face = jsonToFaceBox(faceBoxJson) ?: run {
          call.reject("Invalid face box JSON", "E_FACE")
          return@execute
        }
        val bytes = FaceRecognitionSDK.templateExtraction(bitmap, face)
        if (bytes == null) {
          call.reject("templateExtraction returned null", "E_TEMPLATE")
          return@execute
        }
        call.resolve(value(Base64.encodeToString(bytes, Base64.NO_WRAP)))
      } catch (t: Throwable) {
        reject(call, "E_TEMPLATE", t)
      }
    }
  }

  @PluginMethod
  fun cropFace(call: PluginCall) {
    val imageUri = call.getString("image")
    val faceBoxJson = call.getString("faceBox")
    if (imageUri.isNullOrBlank() || faceBoxJson.isNullOrBlank()) {
      call.reject("image and faceBox are required", "E_CROP")
      return
    }
    executor.execute {
      try {
        val bitmap = loadBitmap(imageUri) ?: run {
          call.reject("Could not decode image: $imageUri", "E_IMAGE")
          return@execute
        }
        val face = jsonToFaceBox(faceBoxJson) ?: run {
          call.reject("Invalid face box JSON", "E_FACE")
          return@execute
        }
        val cropped = FaceRecognitionSDK.cropFace(bitmap, face) ?: run {
          call.reject("cropFace returned null", "E_CROP")
          return@execute
        }
        call.resolve(value(bitmapToBase64Jpeg(cropped)))
      } catch (t: Throwable) {
        reject(call, "E_CROP", t)
      }
    }
  }

  @PluginMethod
  fun extractFeature(call: PluginCall) {
    val imageUri = call.getString("image")
    if (imageUri.isNullOrBlank()) {
      call.reject("image is required", "E_FEATURE")
      return
    }
    executor.execute {
      try {
        val bitmap = loadBitmap(imageUri) ?: run {
          call.reject("Could not decode image: $imageUri", "E_IMAGE")
          return@execute
        }
        call.resolve(value(FaceRecognitionSDK.extractFeature(bitmap) ?: "{}"))
      } catch (t: Throwable) {
        reject(call, "E_FEATURE", t)
      }
    }
  }

  @PluginMethod
  fun similarity(call: PluginCall) {
    val f1b64 = call.getString("feature1")
    val f2b64 = call.getString("feature2")
    if (f1b64.isNullOrBlank() || f2b64.isNullOrBlank()) {
      call.reject("feature1 and feature2 are required", "E_SIMILARITY")
      return
    }
    executor.execute {
      try {
        val f1 = Base64.decode(f1b64, Base64.DEFAULT)
        val f2 = Base64.decode(f2b64, Base64.DEFAULT)
        call.resolve(value(FaceRecognitionSDK.similarity(f1, f2).toDouble()))
      } catch (t: Throwable) {
        reject(call, "E_SIMILARITY", t)
      }
    }
  }

  @PluginMethod
  fun quality(call: PluginCall) {
    val imageUri = call.getString("image")
    if (imageUri.isNullOrBlank()) {
      call.reject("image is required", "E_QUALITY")
      return
    }
    val crop = call.getBoolean("crop", false) ?: false
    executor.execute {
      try {
        val bitmap = loadBitmap(imageUri) ?: run {
          call.reject("Could not decode image: $imageUri", "E_IMAGE")
          return@execute
        }
        call.resolve(value(FaceRecognitionSDK.quality(bitmap, crop) ?: "{}"))
      } catch (t: Throwable) {
        reject(call, "E_QUALITY", t)
      }
    }
  }

  @PluginMethod
  fun startVideoWorker(call: PluginCall) {
    val configJson = call.getString("config")
    executor.execute {
      try {
        FaceRecognitionSDK.setVideoWorkerEventHandler { json ->
          val data = JSObject()
          data.put("json", json ?: "{}")
          notifyListeners(EVENT_VIDEO_WORKER, data)
        }
        val threshold = parseMatchThreshold(configJson)
        val code = FaceRecognitionSDK.startVideoWorker(threshold)
        call.resolve(value(code))
      } catch (t: Throwable) {
        reject(call, "E_VIDEO_WORKER", t)
      }
    }
  }

  @PluginMethod
  fun stopVideoWorker(call: PluginCall) {
    executor.execute {
      try {
        FaceRecognitionSDK.stopVideoWorker()
        FaceRecognitionSDK.setVideoWorkerEventHandler(null)
        call.resolve()
      } catch (t: Throwable) {
        reject(call, "E_VIDEO_WORKER", t)
      }
    }
  }

  @PluginMethod
  fun syncVideoWorkerDatabase(call: PluginCall) {
    val threshold = call.getDouble("matchThreshold") ?: 0.67
    val features = call.getArray("features") ?: JSArray()
    executor.execute {
      try {
        val list = ArrayList<ByteArray>()
        for (i in 0 until features.length()) {
          val s = features.getString(i) ?: continue
          list.add(Base64.decode(s, Base64.DEFAULT))
        }
        val code = FaceRecognitionSDK.syncVideoWorkerDatabase(list, threshold.toFloat())
        call.resolve(value(code))
      } catch (t: Throwable) {
        reject(call, "E_SYNC_DB", t)
      }
    }
  }

  @PluginMethod
  fun probeLiveImage(call: PluginCall) {
    val imageUri = call.getString("image")
    if (imageUri.isNullOrBlank()) {
      call.reject("image is required", "E_IMAGE")
      return
    }
    executor.execute {
      try {
        val bitmap = loadBitmap(imageUri) ?: run {
          call.reject("Could not decode image: $imageUri", "E_IMAGE")
          return@execute
        }
        val ret = JSObject()
        ret.put("width", bitmap.width)
        ret.put("height", bitmap.height)
        call.resolve(ret)
      } catch (t: Throwable) {
        reject(call, "E_IMAGE", t)
      }
    }
  }

  @PluginMethod
  fun applyLiveFrame(call: PluginCall) {
    val imageUri = call.getString("image")
    if (imageUri.isNullOrBlank()) {
      call.reject("image is required", "E_FRAME")
      return
    }
    val rotateDegrees = (call.getDouble("rotateDegrees") ?: 0.0).toFloat()
    val maxEdge = (call.getInt("maxEdge") ?: 640).coerceAtLeast(1)
    val feedWorker = call.getBoolean("feedWorker", true) ?: true
    executor.execute {
      try {
        val bitmap = loadBitmap(imageUri) ?: run {
          call.reject("Could not decode image: $imageUri", "E_IMAGE")
          return@execute
        }
        val prepared = applyLiveTransform(bitmap, rotateDegrees, maxEdge)
        lastLiveBitmap = prepared
        if (feedWorker) {
          FaceRecognitionSDK.addVideoWorkerFrame(prepared)
          call.resolve(liveFrameMap(prepared, ingested = true, uri = null))
        } else {
          call.resolve(liveFrameMap(prepared, ingested = true, uri = writeLiveJpeg(prepared)))
        }
      } catch (t: Throwable) {
        reject(call, "E_FRAME", t)
      }
    }
  }

  @PluginMethod
  fun exportLastLiveFrame(call: PluginCall) {
    executor.execute {
      try {
        val prepared = lastLiveBitmap ?: run {
          call.reject("No live frame", "E_IMAGE")
          return@execute
        }
        call.resolve(liveFrameMap(prepared, ingested = true, uri = writeLiveJpeg(prepared)))
      } catch (t: Throwable) {
        reject(call, "E_FRAME", t)
      }
    }
  }

  @PluginMethod
  fun writeStatus(call: PluginCall) {
    val json = call.getString("payload") ?: "{}"
    try {
      File(context.filesDir, "facerecognition_status.json").writeText(json)
      call.resolve()
    } catch (t: Throwable) {
      reject(call, "E_STATUS", t)
    }
  }

  @PluginMethod
  fun estimatorStatus(call: PluginCall) {
    executor.execute {
      try {
        call.resolve(value(FaceRecognitionSDK.estimatorStatusJSON() ?: "{}"))
      } catch (t: Throwable) {
        reject(call, "E_ESTIMATOR", t)
      }
    }
  }

  @PluginMethod
  fun startLivePreview(call: PluginCall) {
    val front = call.getBoolean("frontCamera", true) ?: true
    val activity = activity
    if (activity == null) {
      call.reject("No activity", "E_CAMERA")
      return
    }
    activity.runOnUiThread {
      try {
        attachPreviewHost()
        val previewView = previewView ?: run {
          call.reject("Preview view missing", "E_CAMERA")
          return@runOnUiThread
        }
        // Wait for layout so ViewPort matches the WebView / overlay size.
        previewView.post {
          bindCameraUseCases(front, call)
        }
      } catch (t: Throwable) {
        reject(call, "E_CAMERA", t)
      }
    }
  }

  private fun bindCameraUseCases(front: Boolean, call: PluginCall) {
    val activity = activity ?: run {
      call.reject("No activity", "E_CAMERA")
      return
    }
    val previewView = previewView ?: run {
      call.reject("Preview view missing", "E_CAMERA")
      return
    }
    val future = ProcessCameraProvider.getInstance(context)
    future.addListener({
      try {
        val provider = future.get()
        cameraProvider?.unbindAll()
        cameraProvider = provider
        syncPreviewHostToWebView()

        val preview = Preview.Builder()
          .setTargetAspectRatio(AspectRatio.RATIO_4_3)
          .build()
          .also { it.setSurfaceProvider(previewView.surfaceProvider) }

        val analysis = ImageAnalysis.Builder()
          .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
          .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
          .setTargetAspectRatio(AspectRatio.RATIO_4_3)
          .build()
        analysis.setAnalyzer(analysisExecutor) { image ->
          ingestAnalysisFrame(image)
        }

        val selector = if (front) {
          CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
          CameraSelector.DEFAULT_BACK_CAMERA
        }

        // Shared ViewPort = Preview and Analysis crop identically (FILL_CENTER).
        val viewPort = previewView.viewPort
        if (viewPort != null) {
          val group = UseCaseGroup.Builder()
            .addUseCase(preview)
            .addUseCase(analysis)
            .setViewPort(viewPort)
            .build()
          provider.bindToLifecycle(activity as LifecycleOwner, selector, group)
        } else {
          provider.bindToLifecycle(
            activity as LifecycleOwner,
            selector,
            preview,
            analysis
          )
        }
        setWebViewTransparent(true)
        call.resolve()
      } catch (t: Throwable) {
        reject(call, "E_CAMERA", t)
      }
    }, ContextCompat.getMainExecutor(context))
  }

  @PluginMethod
  fun stopLivePreview(call: PluginCall) {
    val activity = activity
    if (activity == null) {
      cameraProvider = null
      call.resolve()
      return
    }
    activity.runOnUiThread {
      try {
        cameraProvider?.unbindAll()
        setWebViewTransparent(false)
        detachPreviewHost()
        call.resolve()
      } catch (t: Throwable) {
        reject(call, "E_CAMERA", t)
      }
    }
  }

  @PluginMethod
  fun takeLiveSnapshot(call: PluginCall) {
    executor.execute {
      try {
        val prepared = lastLiveBitmap ?: run {
          call.reject("Live preview is not running", "E_CAMERA")
          return@execute
        }
        val uri = writeLiveJpeg(prepared)
        val ret = JSObject()
        ret.put("uri", uri)
        ret.put("path", uri.removePrefix("file://"))
        call.resolve(ret)
      } catch (t: Throwable) {
        reject(call, "E_CAMERA", t)
      }
    }
  }

  private fun ingestAnalysisFrame(image: androidx.camera.core.ImageProxy) {
    val now = SystemClock.elapsedRealtime()
    if (now - lastAnalysisMs < 120L || analysisBusy) {
      image.close()
      return
    }
    analysisBusy = true
    lastAnalysisMs = now
    try {
      val bitmap = ImageUtils.bitmapFromImageProxy(image) ?: return
      val prepared = applyLiveTransform(bitmap, 0f, 640)
      if (prepared !== bitmap && !bitmap.isRecycled) {
        bitmap.recycle()
      }
      lastLiveBitmap = prepared
      try {
        FaceRecognitionSDK.addVideoWorkerFrame(prepared)
      } catch (_: Throwable) {
        // Worker may not be running yet.
      }
    } catch (_: Throwable) {
      // Drop a bad preview frame.
    } finally {
      image.close()
      analysisBusy = false
    }
  }

  private fun attachPreviewHost() {
    val activity = activity ?: return
    val webView = bridge.webView ?: return
    val parent = webView.parent as? ViewGroup ?: return
    if (previewHost != null) {
      syncPreviewHostToWebView()
      return
    }
    val host = FrameLayout(activity)
    host.setBackgroundColor(Color.BLACK)
    val view = PreviewView(activity)
    view.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
    view.scaleType = PreviewView.ScaleType.FILL_CENTER
    host.addView(
      view,
      FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
      )
    )
    // Match WebView frame so FILL_CENTER crop matches the HTML overlay.
    val lp = ViewGroup.MarginLayoutParams(
      if (webView.width > 0) webView.width else ViewGroup.LayoutParams.MATCH_PARENT,
      if (webView.height > 0) webView.height else ViewGroup.LayoutParams.MATCH_PARENT
    )
    lp.leftMargin = webView.left
    lp.topMargin = webView.top
    parent.addView(host, 0, lp)
    host.isClickable = false
    host.isFocusable = false
    view.isClickable = false
    webView.bringToFront()
    previewHost = host
    previewView = view
    webView.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
      syncPreviewHostToWebView()
    }
  }

  private fun syncPreviewHostToWebView() {
    val webView = bridge.webView ?: return
    val host = previewHost ?: return
    val lp = host.layoutParams as? ViewGroup.MarginLayoutParams ?: return
    if (webView.width <= 0 || webView.height <= 0) return
    if (
      lp.width == webView.width &&
      lp.height == webView.height &&
      lp.leftMargin == webView.left &&
      lp.topMargin == webView.top
    ) {
      return
    }
    lp.width = webView.width
    lp.height = webView.height
    lp.leftMargin = webView.left
    lp.topMargin = webView.top
    host.layoutParams = lp
  }

  private fun detachPreviewHost() {
    val host = previewHost ?: return
    (host.parent as? ViewGroup)?.removeView(host)
    previewHost = null
    previewView = null
  }

  private fun setWebViewTransparent(transparent: Boolean) {
    val webView = bridge.webView ?: return
    webView.setBackgroundColor(if (transparent) Color.TRANSPARENT else Color.BLACK)
    webView.setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
    webView.bringToFront()
  }

  private fun value(v: Any): JSObject {
    val ret = JSObject()
    ret.put("value", v)
    return ret
  }

  private fun reject(call: PluginCall, code: String, t: Throwable) {
    val msg = t.message ?: code
    val ex = if (t is Exception) t else Exception(t)
    call.reject(msg, code, ex)
  }

  private fun loadBitmap(uriOrBase64: String): Bitmap? {
    return if (uriOrBase64.startsWith("data:") || looksLikeBase64(uriOrBase64)) {
      ImageUtils.bitmapFromBase64(uriOrBase64)
    } else {
      ImageUtils.bitmapFromUri(context.applicationContext, uriOrBase64)
    }
  }

  private fun looksLikeBase64(s: String): Boolean {
    if (s.startsWith("content:") || s.startsWith("file:") || s.startsWith("/")) return false
    return s.length > 256 && !s.contains("://")
  }

  private fun parseParam(paramJson: String?): FaceDetectionParam {
    if (paramJson.isNullOrBlank()) return FaceDetectionParam()
    return try {
      val o = JSONObject(paramJson)
      if (o.optBoolean("allAttributes", false)) {
        return FaceDetectionParam.allAttributes().also {
          if (o.has("check_liveness_level")) {
            it.check_liveness_level = o.optInt("check_liveness_level", 0)
          }
        }
      }
      FaceDetectionParam().apply {
        check_liveness = o.optBoolean("check_liveness", check_liveness)
        check_liveness_level = o.optInt("check_liveness_level", check_liveness_level)
        check_eye_closeness = o.optBoolean("check_eye_closeness", check_eye_closeness)
        check_face_occlusion = o.optBoolean("check_face_occlusion", check_face_occlusion)
        estimate_age_gender = o.optBoolean("estimate_age_gender", estimate_age_gender)
        check_pose = o.optBoolean("check_pose", check_pose)
        check_landmarks = o.optBoolean("check_landmarks", check_landmarks)
        check_quality = o.optBoolean("check_quality", check_quality)
        check_emotion = o.optBoolean("check_emotion", check_emotion)
        check_mask = o.optBoolean("check_mask", check_mask)
        check_glasses = o.optBoolean("check_glasses", check_glasses)
      }
    } catch (_: Exception) {
      FaceDetectionParam()
    }
  }

  private fun boxesToJson(boxes: List<FaceBox>): String {
    val arr = JSONArray()
    for (b in boxes) {
      arr.put(faceBoxToJson(b))
    }
    return arr.toString()
  }

  private fun faceBoxToJson(b: FaceBox): JSONObject {
    val o = JSONObject()
    o.put("x1", b.x1)
    o.put("y1", b.y1)
    o.put("x2", b.x2)
    o.put("y2", b.y2)
    o.put("yaw", b.yaw.toDouble())
    o.put("roll", b.roll.toDouble())
    o.put("pitch", b.pitch.toDouble())
    o.put("liveness", b.liveness.toDouble())
    o.put("face_quality", b.face_quality.toDouble())
    o.put("face_luminance", b.face_luminance.toDouble())
    o.put("left_eye_closed", b.left_eye_closed.toDouble())
    o.put("right_eye_closed", b.right_eye_closed.toDouble())
    o.put("face_occlusion", b.face_occlusion.toDouble())
    o.put("mouth_opened", b.mouth_opened.toDouble())
    o.put("age", b.age)
    o.put("gender", b.gender)
    o.put("livenessLabel", b.livenessLabel ?: "")
    o.put("genderLabel", b.genderLabel ?: "")
    o.put("emotionLabel", b.emotionLabel ?: "")
    o.put("maskLabel", b.maskLabel ?: "")
    o.put("qualityLabel", b.qualityLabel ?: "")
    o.put("eyesLeftLabel", b.eyesLeftLabel ?: "")
    o.put("eyesRightLabel", b.eyesRightLabel ?: "")
    val attrs = JSONObject()
    for ((key, value) in b.extraAttributes) {
      if (key.isNotBlank() && !value.isNullOrBlank()) {
        attrs.put(key, value)
      }
    }
    o.put("attributes", attrs)
    o.put("glassesLabel", b.extraAttributes["Glasses"] ?: b.extraAttributes["glasses"] ?: "")
    o.put("sunglassesLabel", b.extraAttributes["Sunglasses"] ?: b.extraAttributes["sunglasses"] ?: "")
    o.put(
      "occlusionLabel",
      b.extraAttributes["Occlusion"]
        ?: b.extraAttributes["FaceOcclusion"]
        ?: b.extraAttributes["occlusion"]
        ?: ""
    )
    o.put("landmarkCount", b.landmarkCount)
    val lm = JSONArray()
    val n = (b.landmarkCount * 2).coerceAtMost(b.landmarks_68.size)
    for (i in 0 until n) {
      lm.put(b.landmarks_68[i].toDouble())
    }
    o.put("landmarks", lm)
    return o
  }

  private fun jsonToFaceBox(json: String): FaceBox? {
    return try {
      val o = JSONObject(json)
      FaceBox().apply {
        x1 = o.optInt("x1")
        y1 = o.optInt("y1")
        x2 = o.optInt("x2")
        y2 = o.optInt("y2")
        yaw = o.optDouble("yaw").toFloat()
        roll = o.optDouble("roll").toFloat()
        pitch = o.optDouble("pitch").toFloat()
        liveness = o.optDouble("liveness").toFloat()
        face_quality = o.optDouble("face_quality").toFloat()
        left_eye_closed = o.optDouble("left_eye_closed").toFloat()
        right_eye_closed = o.optDouble("right_eye_closed").toFloat()
        face_occlusion = o.optDouble("face_occlusion").toFloat()
        age = o.optInt("age")
        gender = o.optInt("gender")
        landmarkCount = o.optInt("landmarkCount")
        val lm = o.optJSONArray("landmarks")
        if (lm != null) {
          val n = lm.length().coerceAtMost(landmarks_68.size)
          for (i in 0 until n) {
            landmarks_68[i] = lm.optDouble(i).toFloat()
          }
        }
      }
    } catch (_: Exception) {
      null
    }
  }

  private fun parseMatchThreshold(configJson: String?): Float {
    if (configJson.isNullOrBlank()) return 0.67f
    return try {
      JSONObject(configJson).optDouble("matchThreshold", 0.67).toFloat()
    } catch (_: Exception) {
      0.67f
    }
  }

  private fun bitmapToBase64Jpeg(bitmap: Bitmap): String {
    val out = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
    return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
  }

  private fun liveFrameMap(prepared: Bitmap, ingested: Boolean, uri: String?): JSObject {
    val map = JSObject()
    map.put("ingested", ingested)
    map.put("width", prepared.width)
    map.put("height", prepared.height)
    if (uri != null) map.put("uri", uri) else map.put("uri", JSONObject.NULL)
    return map
  }

  private fun writeLiveJpeg(prepared: Bitmap): String {
    val file = File(context.cacheDir, "frs_live_${System.currentTimeMillis()}.jpg")
    file.outputStream().use { out ->
      prepared.compress(Bitmap.CompressFormat.JPEG, 85, out)
    }
    return "file://${file.absolutePath}"
  }

  private fun applyLiveTransform(src: Bitmap, rotateDegrees: Float, maxEdge: Int): Bitmap {
    var frame = src
    val deg = rotateDegrees % 360f
    if (kotlin.math.abs(deg) > 0.01f) {
      val matrix = android.graphics.Matrix().apply { postRotate(deg) }
      val rotated = Bitmap.createBitmap(frame, 0, 0, frame.width, frame.height, matrix, true)
      if (rotated !== frame && frame !== src) frame.recycle()
      frame = rotated
    }
    return scaleMax(frame, maxEdge)
  }

  private fun scaleMax(src: Bitmap, maxEdge: Int): Bitmap {
    val w = src.width
    val h = src.height
    val edge = maxOf(w, h)
    if (edge <= maxEdge) return src
    val scale = maxEdge.toFloat() / edge
    return Bitmap.createScaledBitmap(src, (w * scale).toInt(), (h * scale).toInt(), true)
  }

  companion object {
    const val EVENT_VIDEO_WORKER = "FaceRecognitionVideoWorkerEvent"
  }
}
