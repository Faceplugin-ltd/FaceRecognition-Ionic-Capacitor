import { WebPlugin } from '@capacitor/core';
import type { FaceRecognitionSdkPlugin } from './definitions';

const MSG = 'Face Recognition SDK requires a native Android or iOS build (npx cap sync).';

export class FaceRecognitionSdkWeb
  extends WebPlugin
  implements FaceRecognitionSdkPlugin
{
  async getMachineCode(): Promise<{ value: string }> {
    throw this.unimplemented(MSG);
  }
  async setActivation(): Promise<{ value: number }> {
    throw this.unimplemented(MSG);
  }
  async init(): Promise<{ value: number }> {
    throw this.unimplemented(MSG);
  }
  async deinit(): Promise<void> {
    throw this.unimplemented(MSG);
  }
  async lastLicenseError(): Promise<{ value: string }> {
    throw this.unimplemented(MSG);
  }
  async getLicenseStatus(): Promise<{ value: string }> {
    throw this.unimplemented(MSG);
  }
  async setLandmarkMode(): Promise<{ value: number }> {
    throw this.unimplemented(MSG);
  }
  async getLandmarkMode(): Promise<{ value: number }> {
    throw this.unimplemented(MSG);
  }
  async detect(): Promise<{ value: string }> {
    throw this.unimplemented(MSG);
  }
  async faceDetection(): Promise<{ value: string }> {
    throw this.unimplemented(MSG);
  }
  async templateExtraction(): Promise<{ value: string }> {
    throw this.unimplemented(MSG);
  }
  async cropFace(): Promise<{ value: string }> {
    throw this.unimplemented(MSG);
  }
  async extractFeature(): Promise<{ value: string }> {
    throw this.unimplemented(MSG);
  }
  async similarity(): Promise<{ value: number }> {
    throw this.unimplemented(MSG);
  }
  async quality(): Promise<{ value: string }> {
    throw this.unimplemented(MSG);
  }
  async startVideoWorker(): Promise<{ value: number }> {
    throw this.unimplemented(MSG);
  }
  async stopVideoWorker(): Promise<void> {
    throw this.unimplemented(MSG);
  }
  async syncVideoWorkerDatabase(): Promise<{ value: number }> {
    throw this.unimplemented(MSG);
  }
  async probeLiveImage(): Promise<{ width: number; height: number }> {
    throw this.unimplemented(MSG);
  }
  async applyLiveFrame(): Promise<{
    ingested: boolean;
    width: number;
    height: number;
    uri?: string | null;
  }> {
    throw this.unimplemented(MSG);
  }
  async exportLastLiveFrame(): Promise<{
    ingested: boolean;
    width: number;
    height: number;
    uri?: string | null;
  }> {
    throw this.unimplemented(MSG);
  }
  async writeStatus(): Promise<void> {
    throw this.unimplemented(MSG);
  }
  async estimatorStatus(): Promise<{ value: string }> {
    throw this.unimplemented(MSG);
  }
  async startLivePreview(): Promise<void> {
    throw this.unimplemented(MSG);
  }
  async stopLivePreview(): Promise<void> {
    throw this.unimplemented(MSG);
  }
  async takeLiveSnapshot(): Promise<{ uri: string; path: string }> {
    throw this.unimplemented(MSG);
  }
}
