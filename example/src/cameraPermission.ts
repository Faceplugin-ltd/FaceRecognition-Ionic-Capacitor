import { Camera } from '@capacitor/camera';

export async function ensureCameraPermission(): Promise<void> {
  const current = await Camera.checkPermissions();
  if (current.camera === 'granted') return;
  const next = await Camera.requestPermissions({ permissions: ['camera'] });
  if (next.camera !== 'granted') {
    throw new Error('Camera permission denied');
  }
}
