import { useCallback, useEffect, useRef, useState } from 'react';
import { IonPage, useIonAlert } from '@ionic/react';
import { useHistory } from 'react-router-dom';
import { goHome } from '../nav';
import {
  IdentifySession,
  cropFace,
  mapLandmarksToCrop,
  type FaceBox,
} from 'face-recognition-capacitor';
import { loadPeople, loadSettings, type AppSettings } from '../FaceDatabase';
import { ensureCameraPermission } from '../cameraPermission';
import FaceOverlay from '../components/FaceOverlay';
import { displayUri, thumbSrc } from '../pickImage';
import { setIdentifyResult } from '../resultStore';

export default function Identify() {
  const history = useHistory();
  const [presentAlert] = useIonAlert();
  const stageRef = useRef<HTMLDivElement>(null);
  const sessionRef = useRef<IdentifySession | null>(null);
  const recognizedRef = useRef(false);
  const confirmingRef = useRef(false);
  const boxesRef = useRef<FaceBox[]>([]);
  const settingsRef = useRef<AppSettings | null>(null);
  const [settings, setSettings] = useState<AppSettings | null>(null);
  const [size, setSize] = useState({ w: window.innerWidth, h: window.innerHeight });
  const [boxes, setBoxes] = useState<FaceBox[]>([]);
  const [frame, setFrame] = useState({ w: 480, h: 640 });
  const [mirror, setMirror] = useState(false);

  useEffect(() => {
    const el = stageRef.current;
    if (!el) return;
    const update = () => setSize({ w: el.clientWidth, h: el.clientHeight });
    update();
    const ro = new ResizeObserver(update);
    ro.observe(el);
    return () => ro.disconnect();
  }, []);

  const tryConfirm = useCallback(
    async (personIndex: number, score: number, people: Awaited<ReturnType<typeof loadPeople>>) => {
      const s = settingsRef.current;
      const session = sessionRef.current;
      if (!s || !session || recognizedRef.current) return;
      if (confirmingRef.current) return;
      confirmingRef.current = true;
      session.leave();
      const uri = session.lastUri;
      if (!uri) {
        confirmingRef.current = false;
        return;
      }
      try {
        const person = people[personIndex] ?? people[personIndex - 1];
        if (!person) {
          confirmingRef.current = false;
          return;
        }
        const faceBox = boxesRef.current[0] ?? null;
        if (!faceBox) {
          confirmingRef.current = false;
          return;
        }
        recognizedRef.current = true;
        let identifiedUri = uri;
        let cropLandmarks: { x: number; y: number }[] = [];
        try {
          const cropB64 = await cropFace(uri, faceBox);
          identifiedUri = thumbSrc(cropB64) ?? uri;
          const srcW = session.frameSize.w > 0 ? session.frameSize.w : Math.max(faceBox.x2 + 1, 1);
          const srcH = session.frameSize.h > 0 ? session.frameSize.h : Math.max(faceBox.y2 + 1, 1);
          cropLandmarks = mapLandmarksToCrop(faceBox, srcW, srcH, 200, 200);
        } catch {
          identifiedUri = displayUri(uri) ?? uri;
        }
        setIdentifyResult({
          identifiedUri,
          enrolledThumbB64: person.thumbB64,
          personName: person.name,
          similarity: score,
          box: faceBox,
          cropLandmarks,
        });
        history.replace('/result');
      } catch {
        confirmingRef.current = false;
        recognizedRef.current = false;
      }
    },
    [history]
  );

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        await ensureCameraPermission();
        const [people, loaded] = await Promise.all([loadPeople(), loadSettings()]);
        if (cancelled) return;
        setSettings(loaded);
        settingsRef.current = loaded;
        if (!people.length) {
          presentAlert({
            header: 'Identify',
            message: 'Enroll at least one person first.',
            buttons: [{ text: 'OK', handler: () => goHome(history) }],
          });
          return;
        }
        const session = new IdentifySession({
          settings: {
            frontCamera: loaded.camera_lens === 'front',
            matchThreshold: loaded.identify_threshold,
            livenessLevel: loaded.liveness_level,
          },
          featureTemplates: people.map((p) => p.featureB64),
          onTracking: (next, size) => {
            if (recognizedRef.current || cancelled) return;
            boxesRef.current = next;
            setBoxes(next);
            setFrame(size);
          },
          onMatch: (personIndex, score) => {
            void tryConfirm(personIndex, score, people);
          },
        });
        sessionRef.current = session;
        setMirror(session.overlayMirror);
        await session.start();
      } catch (e) {
        presentAlert({
          header: 'Identify',
          message: e instanceof Error ? e.message : String(e),
          buttons: ['OK'],
        });
      }
    })();
    return () => {
      cancelled = true;
      void sessionRef.current?.stop();
    };
  }, [history, presentAlert, tryConfirm]);

  return (
    <IonPage className="live-page">
      <div className="live-stage" ref={stageRef}>
        {settings ? (
          <FaceOverlay
            width={size.w}
            height={size.h}
            frameW={frame.w}
            frameH={frame.h}
            mirror={mirror}
            boxes={boxes}
            settings={settings}
          />
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
