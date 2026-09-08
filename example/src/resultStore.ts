import type { FaceBox } from 'face-recognition-capacitor';

const ATTR_KEY = 'frs_attribute_result';
const IDENT_KEY = 'frs_identify_result';

export type AttributeResult = {
  uri: string;
  box: FaceBox;
  cropB64?: string | null;
  cropLandmarks?: { x: number; y: number }[];
};

export type IdentifyResult = {
  personName: string;
  similarity: number;
  enrolledThumbB64?: string | null;
  identifiedUri?: string | null;
  box?: FaceBox;
  cropLandmarks?: { x: number; y: number }[];
};

function write(key: string, value: unknown): void {
  try {
    sessionStorage.setItem(key, JSON.stringify(value));
  } catch {
    // quota / private mode
  }
}

function read<T>(key: string): T | null {
  try {
    const raw = sessionStorage.getItem(key);
    if (!raw) return null;
    return JSON.parse(raw) as T;
  } catch {
    return null;
  }
}

export function setAttributeResult(data: AttributeResult): void {
  write(ATTR_KEY, data);
}

export function getAttributeResult(): AttributeResult | null {
  return read<AttributeResult>(ATTR_KEY);
}

export function setIdentifyResult(data: IdentifyResult): void {
  write(IDENT_KEY, data);
}

export function getIdentifyResult(): IdentifyResult | null {
  return read<IdentifyResult>(IDENT_KEY);
}
