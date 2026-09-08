import { useEffect, useState } from 'react';
import {
  IonButton,
  IonButtons,
  IonContent,
  IonHeader,
  IonPage,
  IonTitle,
  IonToolbar,
} from '@ionic/react';
import { useHistory } from 'react-router-dom';
import { goHome } from '../nav';
import {
  cropFace,
  mapLandmarksToCrop,
  resultDetailRows,
} from 'face-recognition-capacitor';
import { loadSettings } from '../FaceDatabase';
import LandmarkImage from '../components/LandmarkImage';
import ResultDetailsList from '../components/ResultDetailsList';
import { displayUri, measureImage, thumbSrc } from '../pickImage';
import { getAttributeResult, type AttributeResult } from '../resultStore';

export default function Attribute() {
  const history = useHistory();
  const [data, setData] = useState<AttributeResult | null>(getAttributeResult());
  const [cropUri, setCropUri] = useState<string | null>(null);
  const [marks, setMarks] = useState<{ x: number; y: number }[]>(
    getAttributeResult()?.cropLandmarks ?? []
  );
  const [rows, setRows] = useState<
    { kind: 'section' | 'field'; title: string; value: string }[]
  >([]);

  useEffect(() => {
    const next = getAttributeResult() ?? data;
    if (!next?.box) return;
    setData(next);
    (async () => {
      try {
        const b64 = next.cropB64 ?? (await cropFace(next.uri, next.box));
        setCropUri(thumbSrc(b64) ?? displayUri(next.uri) ?? null);
        if (!next.cropLandmarks?.length) {
          const size = await measureImage(next.uri);
          const srcW = size.w > 0 ? size.w : Math.max(next.box.x2 + 1, 1);
          const srcH = size.h > 0 ? size.h : Math.max(next.box.y2 + 1, 1);
          setMarks(mapLandmarksToCrop(next.box, srcW, srcH, 200, 200));
        } else {
          setMarks(next.cropLandmarks);
        }
      } catch {
        setCropUri(displayUri(next.uri) ?? null);
      }
      const settings = await loadSettings();
      setRows(resultDetailRows(next.box, settings, { includeMatch: false }));
    })();
  }, []);

  if (!data?.box) {
    return (
      <IonPage>
        <IonHeader>
          <IonToolbar>
            <IonButtons slot="start">
              <IonButton onClick={() => goHome(history)}>Back</IonButton>
            </IonButtons>
            <IonTitle>Attribute Result</IonTitle>
          </IonToolbar>
        </IonHeader>
        <IonContent className="ion-padding">
          <p className="muted">No result. Go back and pick a photo.</p>
        </IonContent>
      </IonPage>
    );
  }

  return (
    <IonPage>
      <IonHeader>
        <IonToolbar>
          <IonButtons slot="start">
            <IonButton onClick={() => goHome(history)}>Back</IonButton>
          </IonButtons>
          <IonTitle>Attribute Result</IonTitle>
        </IonToolbar>
      </IonHeader>
      <IonContent>
        <div className="result-header">Attribute Result</div>
        <div className="attribute-card">
          <LandmarkImage
            uri={cropUri}
            landmarks={marks}
            imageSize={{ w: 200, h: 200 }}
            width={240}
            height={240}
          />
        </div>
        <ResultDetailsList rows={rows} />
      </IonContent>
    </IonPage>
  );
}
