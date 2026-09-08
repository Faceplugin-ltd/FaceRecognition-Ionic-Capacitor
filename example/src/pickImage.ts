import { Camera, CameraResultType, CameraSource } from '@capacitor/camera';
import { Capacitor } from '@capacitor/core';

export async function pickGalleryPhoto(): Promise<string> {
  const photo = await Camera.getPhoto({
    quality: 90,
    source: CameraSource.Photos,
    resultType: CameraResultType.Uri,
  });
  const uri = photo.webPath || photo.path;
  if (!uri) {
    throw new Error('No photo selected');
  }
  return photo.path
    ? photo.path.startsWith('file://') || photo.path.startsWith('content:')
      ? photo.path
      : `file://${photo.path}`
    : uri;
}

export function thumbSrc(b64: string | null | undefined): string | undefined {
  if (!b64) return undefined;
  return b64.startsWith('data:') ? b64 : `data:image/jpeg;base64,${b64}`;
}

export function displayUri(uri: string | null | undefined): string | undefined {
  if (!uri) return undefined;
  if (uri.startsWith('data:') || uri.startsWith('blob:') || uri.startsWith('http')) {
    return uri;
  }
  try {
    return Capacitor.convertFileSrc(uri);
  } catch {
    return uri;
  }
}

export function measureImage(uri: string): Promise<{ w: number; h: number }> {
  return new Promise((resolve) => {
    const img = new Image();
    img.onload = () =>
      resolve({
        w: img.naturalWidth || img.width,
        h: img.naturalHeight || img.height,
      });
    img.onerror = () => resolve({ w: 0, h: 0 });
    img.src = displayUri(uri) ?? uri;
  });
}
