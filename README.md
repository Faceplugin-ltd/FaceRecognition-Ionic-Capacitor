<div align="center">
<img alt="FacePlugin" src="https://avatars.githubusercontent.com/u/160751046?s=200&v=4" width="200"/>
</div>

#### 🌐 Company Site - [Here](https://faceplugin.com)
#### 🤗 Hugging Face - [Here](https://huggingface.co/FacePlugin-Ltd)
#### 🛟 Help Center - [Here](https://doc.faceplugin.com)
#### 🐳 Docker Hub - [Here](https://hub.docker.com/u/faceplugin)

# FacePlugin Face Recognition SDK — Ionic Capacitor (Fully On-Premise)

> Drop Android AAR + iOS frameworks → Sync Capacitor → run on a **physical** phone.
> Jump: [Quick Start](#quick-start) · [Get the runtimes](#get-the-runtimes) · [Run the demo](#run-the-demo) · [Integrate](#integrate-into-your-own-app) · [JS API](#about-sdk-js-api)

## Quick Start

- [ ] Install [Node.js 18+](https://nodejs.org/) (includes **npm**)
- [ ] Install [JDK 17](https://adoptium.net/)
- [ ] Install [Android Studio](https://developer.android.com/studio) (Android) or [Xcode 15+](https://developer.apple.com/xcode/) (iOS, macOS only)
- [ ] `git clone https://github.com/Faceplugin-ltd/FaceRecognition-Ionic-Capacitor.git`
- [ ] `cd FaceRecognition-Ionic-Capacitor`
- [ ] `npm install`
- [ ] `npm run build`
- [ ] `cd example && npm install`
- [ ] Download runtimes → [Get the runtimes](#get-the-runtimes)
- [ ] Copy `facerecognitionsdk.aar` → `example/android/libfacesdk/`
- [ ] Unzip iOS frameworks → `ios/Frameworks/` (`facerecognitionsdk`, `FaceRecognitionEngine`, `onnxruntime`)
- [ ] `npm run build`
- [ ] `npx cap sync`
- [ ] `npx cap open android` **or** `npx cap open ios`
- [ ] Run on a **physical** phone from Android Studio / Xcode
- [ ] Home status bar → **Ready** → Enroll / Identify / Capture / Attribute

> Own app? → [Integrate into your own app](#integrate-into-your-own-app). Docs: [https://doc.faceplugin.com](https://doc.faceplugin.com)

## Introduction

FacePlugin **Face Recognition SDK for Ionic Capacitor** is a fully on-device biometric plugin for Android and iOS. Enroll faces, identify in 1:N with VideoWorker, capture with an oval coach, and read attributes with 2D liveness — all on the phone. The npm package `face-recognition-capacitor` wraps the same native engines as our FaceRecognitionSDK Android and iOS apps.

All processing stays on the device. **No** biometric data is sent to FacePlugin cloud — built for KYC, eKYC, and hybrid mobile onboarding.

This repository contains:

| Folder | Purpose |
| ------ | ------- |
| Repository root | `face-recognition-capacitor` — the Capacitor plugin you depend on in your app |
| `example/` | Full demo (Enroll, Identify, Capture, Attribute, Settings, About) |

Native binaries are **not** on GitHub (too large). Download them from Google Drive (links below) and copy into the paths shown.

`ionic serve` / browser alone **cannot** load the engine.

### Main Functionalities

| Feature | Supported |
| ------- | --------- |
| Gallery enroll (single-face template) | ✓ |
| Live 1:N identify with VideoWorker | ✓ |
| 2D liveness / anti-spoofing on identify | ✓ |
| Oval capture coach with optional enroll | ✓ |
| Gallery attributes, landmarks, age, gender, emotion | ✓ |
| Local person database (demo) | ✓ |
| Settings (thresholds / clear DB) | ✓ |

### Product List

| Platform | Repository |
|----------|------------|
| Android (Recognition) | [FaceRecognition-Android](https://github.com/Faceplugin-ltd/FaceRecognition-Android) |
| iOS (Recognition) | [FaceRecognition-iOS](https://github.com/Faceplugin-ltd/FaceRecognition-iOS) |
| React Native (Recognition) | [FaceRecognition-React-Native](https://github.com/Faceplugin-ltd/FaceRecognition-React-Native) |
| Flutter (Recognition) | [FaceRecognition-Flutter](https://github.com/Faceplugin-ltd/FaceRecognition-Flutter) |
| **Ionic Capacitor (Recognition)** | **[FaceRecognition-Ionic-Capacitor](https://github.com/Faceplugin-ltd/FaceRecognition-Ionic-Capacitor)** (**this repo**) |
| Ionic Cordova (Recognition) | [FaceRecognition-Ionic-Cordova](https://github.com/Faceplugin-ltd/FaceRecognition-Ionic-Cordova) |
| Windows (Recognition) | [FaceRecognition-Windows](https://github.com/Faceplugin-ltd/FaceRecognition-Windows) |
| Linux / Docker (Recognition) | [FaceRecognition-Docker](https://github.com/Faceplugin-ltd/FaceRecognition-Docker) |
| Android (Liveness) | [FaceLivenessDetection-Android](https://github.com/Faceplugin-ltd/FaceLivenessDetection-Android) |
| iOS (Liveness) | [FaceLivenessDetection-iOS](https://github.com/Faceplugin-ltd/FaceLivenessDetection-iOS) |
| Windows (Liveness) | [FaceLivenessDetection-Windows](https://github.com/Faceplugin-ltd/FaceLivenessDetection-Windows) |
| Linux / Docker (Liveness) | [FaceLivenessDetection-Docker](https://github.com/Faceplugin-ltd/FaceLivenessDetection-Docker) |


---

## Before you start

| Step | What you need |
| ---- | ------------- |
| 1 | **Node.js 18+**, npm |
| 2 | **Capacitor 6** + Ionic React 8 (see `example/package.json`) |
| 3 | **Physical device** recommended (camera / liveness; emulator is limited) |
| 4 | Android `facerecognitionsdk.aar` and iOS frameworks — see [Get the runtimes](#get-the-runtimes) |
| 5 | Demo licenses are in `example/src/license.ts` (bound to `com.faceplugin.facerecognitionsdk`). Request a new key only if you change `applicationId` / bundle id — see [SDK License](#sdk-license) |

You can run the **example** app as-is after placing the runtimes. Home tiles unlock when the status bar shows **Ready**.

### System requirements

| Platform | Requirement |
| -------- | ----------- |
| Node | 18+ |
| Capacitor | 6.2.x |
| Android | minSdk 24, physical device recommended |
| iOS | iOS 13+, A12+ recommended, physical device |

---

## Get the runtimes

Same Google Drive packs as FaceRecognitionSDK Android / iOS (includes `frc.fpk` models inside the runtime).

### Android

[Google Drive — Android](https://drive.google.com/drive/folders/1kpzYVv9Gbm_pEpDe9-x7FGB4NWZzvez0)

| File | Example app path | Your own app path |
| ---- | ---------------- | ----------------- |
| `facerecognitionsdk.aar` | `example/android/libfacesdk/facerecognitionsdk.aar` | `node_modules/face-recognition-capacitor/android/libs/facerecognitionsdk.aar` |

### iOS

[Google Drive — iOS](https://drive.google.com/drive/folders/1PKmV-o7gq7s7dDtiNgXPfCi2ZlWaRy5H)

Unzip into `ios/Frameworks/`:

```text
ios/Frameworks/
├── facerecognitionsdk.framework
├── FaceRecognitionEngine.framework
└── onnxruntime.framework
```

Then `npx cap sync ios` / `pod install`. Frameworks are **device arm64**.

---

## Run the demo

Follow these steps in order to run the demo.

### 1. Install tools on your computer

1. Install **Node.js 18 or newer** from [https://nodejs.org/](https://nodejs.org/) (choose the LTS version). This also installs **npm**.
2. Open a terminal (PowerShell, Terminal, or Command Prompt) and check:

```bash
node -v
npm -v
```

3. Install **JDK 17** from [https://adoptium.net/](https://adoptium.net/) (Temurin 17).
4. **Android:** install [Android Studio](https://developer.android.com/studio), open it once, and install the Android SDK + a device USB driver if needed.
5. **iOS (Mac only):** install **Xcode 15+** from the App Store, then open Xcode once and accept the license. Install CocoaPods if prompted (`sudo gem install cocoapods`).
6. Plug in a **physical phone** and enable **Developer / USB debugging** (Android) or trust the computer (iPhone). Emulators are limited for camera and liveness.

### 2. Download this project

```bash
git clone https://github.com/Faceplugin-ltd/FaceRecognition-Ionic-Capacitor.git
cd FaceRecognition-Ionic-Capacitor
```

If you do not use Git, download the ZIP from GitHub → **Code → Download ZIP**, unzip it, and `cd` into the folder.

### 3. Install npm packages and build the plugin

From the **repository root**:

```bash
npm install
npm run build
```

Then install the **example** app:

```bash
cd example
npm install
```

### 4. Place the native runtimes

Download the binaries from [Get the runtimes](#get-the-runtimes), then copy them exactly here:

| Platform | What to copy | Put it here |
| -------- | ------------ | ----------- |
| Android | `facerecognitionsdk.aar` | `example/android/libfacesdk/facerecognitionsdk.aar` |
| iOS | Unzip the three frameworks | `ios/Frameworks/` (`facerecognitionsdk.framework`, `FaceRecognitionEngine.framework`, `onnxruntime.framework`) |

Create the folders if they do not exist. Do **not** rename the files.

### 5. Build the web app and sync Capacitor

Still inside `example/`:

```bash
npm run build
npx cap sync
```

This copies the web build into the native Android / iOS projects and links the Capacitor plugin.

### 6. Open and run on your phone

**Android**

```bash
npx cap open android
```

1. Android Studio opens the `example/android` project.
2. Wait until Gradle finishes syncing.
3. Choose your USB phone in the device list.
4. Press the green **Run** button.

**iOS (macOS only)**

```bash
npx cap open ios
```

1. Xcode opens the workspace.
2. Select your **Team** under Signing & Capabilities.
3. Choose your **physical iPhone** (not a simulator).
4. Press **Run**.

Keep these demo app ids so the included license works:

| Platform | Id |
| -------- | -- |
| Android `applicationId` | `com.faceplugin.facerecognitionsdk` |
| iOS bundle id | `com.faceplugin.facerecognitionsdk` |

### 7. Use the demo

1. Wait for the home **status bar** → **Ready**.
2. Use **Enroll**, **Identify**, **Capture**, and **Attribute** for on-device 1:N matching, oval capture, attributes, and 2D liveness.
3. Open **Settings** to adjust thresholds if needed.

### Screenshots

| Home | Identify | Capture |
| ---- | -------- | ------- |
| <p align="center"><img src="https://raw.githubusercontent.com/Faceplugin-ltd/faceplugin-assets/main/screenshots/face-recognition/android/home.png" alt="FacePlugin Face Recognition — Home with Enroll, Identify, Capture, Attribute, Settings, About" width="240"/></p> | <p align="center"><img src="https://raw.githubusercontent.com/Faceplugin-ltd/faceplugin-assets/main/screenshots/face-recognition/android/identify.png" alt="FacePlugin Face Recognition — live 1:N identify with face box, landmarks, and liveness" width="240"/></p> | <p align="center"><img src="https://raw.githubusercontent.com/Faceplugin-ltd/faceplugin-assets/main/screenshots/face-recognition/android/capture.png" alt="FacePlugin Face Recognition — oval capture coach with Move closer" width="240"/></p> |

| Capture result | Attribute | Attribute (emotion) |
| -------------- | --------- | ------------------- |
| <p align="center"><img src="https://raw.githubusercontent.com/Faceplugin-ltd/faceplugin-assets/main/screenshots/face-recognition/android/capture-result.png" alt="FacePlugin Face Recognition — capture result with liveness, quality, and Enroll" width="240"/></p> | <p align="center"><img src="https://raw.githubusercontent.com/Faceplugin-ltd/faceplugin-assets/main/screenshots/face-recognition/android/attribute.png" alt="FacePlugin Face Recognition — attributes: 14 landmarks, liveness, age, gender" width="240"/></p> | <p align="center"><img src="https://raw.githubusercontent.com/Faceplugin-ltd/faceplugin-assets/main/screenshots/face-recognition/android/attribute-emotion.png" alt="FacePlugin Face Recognition — attributes: landmarks, age, gender, emotion" width="240"/></p> |

| Attribute (quality) | Settings | About |
| ------------------- | -------- | ----- |
| <p align="center"><img src="https://raw.githubusercontent.com/Faceplugin-ltd/faceplugin-assets/main/screenshots/face-recognition/android/attribute-quality.png" alt="FacePlugin Face Recognition — quality: blur, noise, pose, bounding box" width="240"/></p> | <p align="center"><img src="https://raw.githubusercontent.com/Faceplugin-ltd/faceplugin-assets/main/screenshots/face-recognition/android/settings.png" alt="FacePlugin Face Recognition — Settings for camera lens and thresholds" width="240"/></p> | <p align="center"><img src="https://raw.githubusercontent.com/Faceplugin-ltd/faceplugin-assets/main/screenshots/face-recognition/android/about.png" alt="FacePlugin Face Recognition SDK — About, on-device identity" width="240"/></p> |

| Home (tiles) | Attribute (liveness) |
| ------------ | -------------------- |
| <p align="center"><img src="https://raw.githubusercontent.com/Faceplugin-ltd/faceplugin-assets/main/screenshots/face-recognition/android/home-tiles.png" alt="FacePlugin Face Recognition — six home action tiles" width="240"/></p> | <p align="center"><img src="https://raw.githubusercontent.com/Faceplugin-ltd/faceplugin-assets/main/screenshots/face-recognition/android/attribute-liveness.png" alt="FacePlugin Face Recognition — liveness spoof score, age, gender" width="240"/></p> |

---

## SDK License

Licenses are **offline** and bound to your `applicationId` / bundle identifier.

The sample app already includes a valid key for `com.faceplugin.facerecognitionsdk`. You only need a new key if you use a different id.

### How to get a license

The code below shows how to use the license:

[https://github.com/Faceplugin-ltd/FaceRecognition-Ionic-Capacitor/blob/54204547fdf955489e4213e82c2f8a809533433e/example/src/license.ts#L7-L15](https://github.com/Faceplugin-ltd/FaceRecognition-Ionic-Capacitor/blob/54204547fdf955489e4213e82c2f8a809533433e/example/src/license.ts#L7-L15)

[https://github.com/Faceplugin-ltd/FaceRecognition-Ionic-Capacitor/blob/54204547fdf955489e4213e82c2f8a809533433e/example/src/SdkContext.tsx#L60-L70](https://github.com/Faceplugin-ltd/FaceRecognition-Ionic-Capacitor/blob/54204547fdf955489e4213e82c2f8a809533433e/example/src/SdkContext.tsx#L60-L70)

Please [contact us](#contact) to get a license for **your own app**.

---

## Integrate into your own app

You need the Capacitor plugin + native runtimes. You do **not** need the example UI or person database (those are demo-only).

```bash
npm install git+https://github.com/Faceplugin-ltd/FaceRecognition-Ionic-Capacitor.git
npx cap sync
```

1. Copy runtimes:

| Platform | Copy to |
| -------- | ------- |
| Android | `node_modules/face-recognition-capacitor/android/libs/facerecognitionsdk.aar` |
| iOS | `node_modules/face-recognition-capacitor/ios/Frameworks/` (three frameworks), then `npx cap sync` |

2. Set **your** `applicationId` / iOS bundle id (`capacitor.config.ts` `appId`) and request a license for **that** id (not the demo id).

3. Permissions: camera + photo library (see example `AndroidManifest.xml` / `Info.plist`).

4. Minimal usage:

```ts
import {
  getMachineCode,
  setActivation,
  init,
  lastLicenseError,
  faceDetection,
  templateExtraction,
  cropFace,
  SDK_SUCCESS,
} from 'face-recognition-capacitor';

async function activate() {
  const mc = await getMachineCode(); // FPMC1.… — send when requesting a key
  const act = await setActivation('FP1.…'); // bound to YOUR applicationId / bundle id
  if (act !== SDK_SUCCESS) {
    throw new Error(await lastLicenseError());
  }
  const initCode = await init();
  if (initCode !== SDK_SUCCESS) {
    throw new Error(`init failed: ${initCode}`);
  }
}

async function enrollStill(imageUri: string) {
  const faces = await faceDetection(imageUri);
  if (faces.length !== 1) return;
  const template = await templateExtraction(imageUri, faces[0]);
  const cropB64 = await cropFace(imageUri, faces[0]);
  // Store template + crop in YOUR database
}
```

Thresholds (identify, liveness, pose, eye-close, Capture settings) are **parameters** you pass from JS — change them freely for your product. Defaults in the example Settings match FaceRecognitionSDK Apps (`identify` 0.67, `liveness` 0.5, level `0`, pose `40°`, eyeclose `0.5`).

---

## About SDK (JS API)

Call order: `getMachineCode` → `setActivation` → `init` → detect / VideoWorker → `deinit` when done.

| API | Purpose |
| --- | ------- |
| `getMachineCode()` | `FPMC1.…` for license requests |
| `setActivation(license)` | Activate with `FP1.…` |
| `init()` / `deinit()` | Load / unload engine |
| `faceDetection(image, param?)` | Canonical `FaceBox` list |
| `detect(image, crop?, flags?)` | Raw engine JSON |
| `templateExtraction` / `cropFace` / `similarity` | Enroll + 1:1 match |
| `startVideoWorker` / `syncVideoWorkerDatabase` / `startLivePreview` | Live 1:N from camera frames |
| `subscribeVideoWorker` | Tracking / match events |
| `IdentifySession` / `CaptureSession` | Optional live session helpers |
| `lastLicenseError()` | Human-readable license failure |

### FaceBox (cheat sheet)

Boxes include geometry (`x1,y1,x2,y2`), pose, liveness / quality / eyes / occlusion scores and labels, age / gender / emotion / mask / glasses, optional `attributes` map, and `landmarks` + `landmarkCount`.

### Status codes

| Code | Meaning |
| ---- | ------- |
| 0 | Success |
| 1 | License invalid |
| 2 | License expired |
| 3 | Not activated |
| 4 | Init failed |

---

## Example app layout

| Path | Role |
| ---- | ---- |
| `example/src/license.ts` | Demo FP1 keys only |
| `example/src/SdkContext.tsx` | Activate → init → Ready |
| `example/src/FaceDatabase.ts` | Demo person + settings store |
| `example/src/pages/` | Home, Identify, Capture, Attribute, Result, Settings, About |

---

## Contact

<div align="left">
<a target="_blank" href="mailto:info@faceplugin.com"><img src="https://img.shields.io/badge/email-info@faceplugin.com-blue.svg?logo=gmail" alt="faceplugin.com"></a>&emsp;
<a target="_blank" href="https://wa.me/+14692784822"><img src="https://img.shields.io/badge/whatsapp-faceplugin-blue.svg?logo=whatsapp" alt="faceplugin.com"></a>
</div>
