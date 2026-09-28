import { CapacitorConfig } from '@capacitor/cli';

const config: CapacitorConfig = {
  appId: 'com.faceplugin.facerecognitionsdk',
  appName: 'Faceplugin-Face-SDK',
  webDir: 'dist',
  backgroundColor: '#00000000',
  server: {
    androidScheme: 'https',
  },
  plugins: {
    Camera: {
      permissions: ['camera', 'photos'],
    },
  },
};

export default config;
