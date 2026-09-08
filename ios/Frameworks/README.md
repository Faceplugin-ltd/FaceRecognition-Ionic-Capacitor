# ios/Frameworks — Face Recognition native runtimes

Drop from the FacePlugin Drive **iOS** pack (same as FaceRecognitionSDK-iOS-App / Flutter / React Native):

```text
ios/Frameworks/
├── facerecognitionsdk.framework
├── FaceRecognitionEngine.framework
└── onnxruntime.framework
```

[Google Drive — iOS](https://drive.google.com/drive/folders/1PKmV-o7gq7s7dDtiNgXPfCi2ZlWaRy5H)

Then from `example/ios/App` run `pod install` (or `npx cap sync ios`).

Header stubs alone are not enough to run on device. Frameworks are **device arm64**.

For **customer apps**, copy the same folder to `node_modules/face-recognition-capacitor/ios/Frameworks/` after install.
