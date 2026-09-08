import { useCallback, useEffect, useRef, useState } from 'react';
import { IonPage, useIonAlert } from '@ionic/react';
import { useHistory } from 'react-router-dom';
import { goHome } from '../nav';
import {
  CaptureSession,
  exportLastLiveFrame,
  qualityText,
  toCaptureSettings,
  type CaptureResult,
  type CaptureSettings,
  type CaptureState,
  type FaceBox,
} from 'face-recognition-capacitor';
import { cropFace, templateExtraction } from 'face-recognition-capacitor';
import { addPerson, autoPersonName, loadSettings } from '../FaceDatabase';
import { ensureCameraPermission } from '../cameraPermission';
import CaptureOverlay, {
  type CaptureViewMode,
} from '../components/CaptureOverlay';
import { displayUri } from '../pickImage';
import enrollGradient from '../assets/tiles/gradient_back.png';

export default function Capture() {
  const history = useHistory();
  const [presentAlert] = useIonAlert();
  const stageRef = useRef<HTMLDivElement>(null);
  const sessionRef = useRef<CaptureSession | null>(null);
  const viewModeRef = useRef<CaptureViewMode>('NO_FACE_PREPARE');
  const lastUriRef = useRef<string | null>(null);
  const capturedFaceRef = useRef<FaceBox | null>(null);
  const [settings, setSettings] = useState<CaptureSettings | null>(null);
  const [size, setSize] = useState({ w: window.innerWidth, h: window.innerHeight });
  const [viewMode, setViewMode] = useState<CaptureViewMode>('NO_FACE_PREPARE');
  const [warning, setWarning] = useState('');
  const [faceBox, setFaceBox] = useState<FaceBox | null>(null);
  const [frameSize, setFrameSize] = useState({ w: 720, h: 1280 });
  const [captureUri, setCaptureUri] = useState<string | null>(null);
  const [showResult, setShowResult] = useState(false);
  const [captureResult, setCaptureResult] = useState<CaptureResult | null>(null);
  const [resultBox, setResultBox] = useState<FaceBox | null>(null);

  const setMode = useCallback((mode: CaptureViewMode) => {
    if (viewModeRef.current === mode) return;
    viewModeRef.current = mode;
    setViewMode(mode);
  }, []);

  useEffect(() => {
    const el = stageRef.current;
    if (!el) return;
    const update = () => setSize({ w: el.clientWidth, h: el.clientHeight });
    update();
    const ro = new ResizeObserver(update);
    ro.observe(el);
    return () => ro.disconnect();
  }, []);

  const finishWithResult = useCallback(async (shown: FaceBox | null, bitmapUri: string | null) => {
    if (!shown || !bitmapUri) {
      setShowResult(true);
      return;
    }
    setResultBox(shown);
    capturedFaceRef.current = shown;
    const result: CaptureResult = {
      uri: bitmapUri,
      faceBox: shown,
      cropB64: null,
    };
    try {
      result.cropB64 = await cropFace(bitmapUri, shown);
    } catch {
      result.cropB64 = null;
    }
    setCaptureResult(result);
    setShowResult(true);
    await sessionRef.current?.stop();
    sessionRef.current = null;
  }, []);

  const onModeFinished = useCallback(
    async (mode: CaptureViewMode) => {
      if (mode === 'NO_FACE_PREPARE') {
        setMode('REPEAT_NO_FACE_PREPARE');
      } else if (mode === 'TO_FACE_CIRCLE') {
        setMode('FACE_CIRCLE');
      } else if (mode === 'FACE_CIRCLE_TO_NO_FACE') {
        setMode('NO_FACE_PREPARE');
      } else if (mode === 'FACE_CAPTURE_PREPARE') {
        setMode('FACE_CAPTURE_DONE');
      } else if (mode === 'FACE_CAPTURE_DONE') {
        const uri = lastUriRef.current;
        const fallback = capturedFaceRef.current;
        if (!uri) {
          setShowResult(true);
          return;
        }
        const session = sessionRef.current;
        if (session) {
          const captured = await session.captureNow(fallback ? [fallback] : []);
          if (captured) {
            setCaptureUri(captured.uri);
            setResultBox(captured.faceBox);
            setCaptureResult(captured);
            setShowResult(true);
            await session.stop();
            sessionRef.current = null;
            return;
          }
        }
        await finishWithResult(fallback, uri);
      }
    },
    [finishWithResult, setMode]
  );

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        await ensureCameraPermission();
        const loaded = toCaptureSettings(await loadSettings());
        if (cancelled) return;
        setSettings(loaded);
        const session = new CaptureSession({
          settings: loaded,
          onState: (state: CaptureState, warn, boxes, frame) => {
            const mode = viewModeRef.current;
            if (mode === 'FACE_CAPTURE_DONE' || mode === 'NO_FACE_PREPARE') return;
            setFaceBox(boxes[0] ?? null);
            setFrameSize(frame);
            if (mode === 'REPEAT_NO_FACE_PREPARE') {
              if (state !== 'NO_FACE') setMode('TO_FACE_CIRCLE');
              return;
            }
            if (mode === 'FACE_CIRCLE') {
              if (state === 'NO_FACE') {
                setWarning('');
                setMode('FACE_CIRCLE_TO_NO_FACE');
                return;
              }
              if (state === 'CAPTURE_OK') {
                capturedFaceRef.current = boxes[0] ?? null;
                setWarning('');
                setMode('FACE_CAPTURE_PREPARE');
                void exportLastLiveFrame()
                  .then((exported) => {
                    if (!exported.uri) return;
                    lastUriRef.current = exported.uri;
                    setCaptureUri(exported.uri);
                  })
                  .catch(() => undefined);
                return;
              }
              setWarning(warn);
            }
          },
          onCaptured: () => undefined,
        });
        sessionRef.current = session;
        await session.start();
      } catch (e) {
        presentAlert({
          header: 'Capture',
          message: e instanceof Error ? e.message : String(e),
          buttons: ['OK'],
        });
      }
    })();
    return () => {
      cancelled = true;
      void sessionRef.current?.stop();
    };
  }, [presentAlert, setMode]);

  const onEnroll = async () => {
    const result = captureResult;
    if (!result?.uri || !result.faceBox) {
      presentAlert({ header: 'Capture', message: 'Enrollment failed', buttons: ['OK'] });
      return;
    }
    try {
      const feature = await templateExtraction(result.uri, result.faceBox);
      await addPerson(autoPersonName(), feature, result.cropB64 ?? null);
      presentAlert({
        header: 'Person enrolled!',
        buttons: [{ text: 'OK', handler: () => goHome(history) }],
      });
    } catch (e) {
      presentAlert({
        header: 'Capture',
        message: e instanceof Error ? e.message : String(e),
        buttons: ['OK'],
      });
    }
  };

  const shown = resultBox;
  const livenessLine = (() => {
    if (!shown) return '';
    const label = (shown.livenessLabel ?? '').toLowerCase();
    const score = shown.liveness ?? 0;
    if (label.includes('spoof') || label.includes('fake')) {
      return `Liveness: Spoof, score = ${score}`;
    }
    if (score >= (settings?.liveness_threshold ?? 0.5)) {
      return `Liveness: Real, score = ${score}`;
    }
    return `Liveness: Spoof, score = ${score}`;
  })();

  return (
    <IonPage className="live-page">
      <div className="live-stage" ref={stageRef}>
        {viewMode !== 'FACE_CAPTURE_DONE' ? null : <div className="live-stage-fill" />}
        <CaptureOverlay
          width={size.w}
          height={size.h}
          frame={frameSize}
          mirror={settings?.camera_lens === 'front'}
          viewMode={viewMode}
          faceBox={faceBox}
          capturedUri={displayUri(captureUri) ?? null}
          onModeFinished={(mode) => void onModeFinished(mode)}
        />
        <div className="live-title">Face Capture</div>
        {warning ? <div className="live-warn">{warning}</div> : null}
        {showResult ? (
          <div className="capture-result-pane">
            <div className="capture-result-line">{livenessLine}</div>
            <div className="capture-result-line">
              {qualityText(shown?.face_quality ?? 0)}
              {shown?.qualityLabel ? `\n${shown.qualityLabel}` : ''}
            </div>
            <div className="capture-result-line">
              Luminance: {shown?.face_luminance ?? 0}
            </div>
            <button
              type="button"
              className="enroll-pill"
              style={{ backgroundImage: `url(${enrollGradient})` }}
              onClick={() => void onEnroll()}
            >
              Enroll
            </button>
          </div>
        ) : null}
        <button
          type="button"
          className="live-back"
          onClick={() => {
            void sessionRef.current?.stop();
            goHome(history);
          }}
        >
          ←
        </button>
      </div>
    </IonPage>
  );
}
