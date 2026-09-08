# Face Recognition — Ionic example

Capacitor 6 + Ionic React demo for `face-recognition-capacitor`.

```bash
# from repo root
npm install && npm run build
cd example
npm install
# place facerecognitionsdk.aar in android/libfacesdk/
# place iOS frameworks in ../ios/Frameworks/
npm run build && npx cap sync
npx cap open android   # or ios
```

Demo license is bound to `com.faceplugin.facerecognitionsdk` (`src/license.ts`).
