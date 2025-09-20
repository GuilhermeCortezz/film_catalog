// Design System - Colors, Spacing, Typography

export const Colors = {
  // Primary Colors
  primary: '#1a1a2e',
  primaryDark: '#16213e',
  primaryLight: '#0f3460',
  
  // Secondary Colors
  secondary: '#e94560',
  secondaryLight: '#f27a8a',
  secondaryDark: '#c73650',
  
  // Accent Colors
  accent: '#ff6b6b',
  accentLight: '#ff8e8e',
  accentDark: '#e94560',
  
  // Neutral Colors
  background: '#0a0a0a',
  surface: '#1a1a1a',
  surfaceLight: '#2a2a2a',
  
  // Text Colors
  textPrimary: '#ffffff',
  textSecondary: '#b3b3b3',
  textTertiary: '#666666',
  
  // Status Colors
  success: '#4caf50',
  warning: '#ff9800',
  error: '#f44336',
  info: '#2196f3',
  
  // Rating Colors
  rating: '#ffd700',
  ratingBackground: '#333333',
  
  // Transparent overlays
  overlay: 'rgba(0, 0, 0, 0.5)',
  overlayLight: 'rgba(0, 0, 0, 0.3)',
  overlayDark: 'rgba(0, 0, 0, 0.7)',
  
  // Gradient Colors
  gradientStart: '#1a1a2e',
  gradientEnd: '#16213e',
  
  // Button Colors
  buttonPrimary: '#e94560',
  buttonSecondary: '#2a2a2a',
  buttonDisabled: '#404040',
};

export const Spacing = {
  xs: 4,
  sm: 8,
  md: 16,
  lg: 24,
  xl: 32,
  xxl: 48,
  xxxl: 64,
};

export const BorderRadius = {
  sm: 8,
  md: 12,
  lg: 16,
  xl: 24,
  round: 50,
};

export const Typography = {
  // Font Sizes
  xs: 12,
  sm: 14,
  md: 16,
  lg: 18,
  xl: 20,
  xxl: 24,
  xxxl: 32,
  
  // Font Weights
  light: '300' as const,
  regular: '400' as const,
  medium: '500' as const,
  semibold: '600' as const,
  bold: '700' as const,
  
  // Line Heights
  lineHeight: {
    tight: 1.2,
    normal: 1.4,
    relaxed: 1.6,
  },
};

export const Shadows = {
  small: {
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 2,
  },
  medium: {
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 4 },
    shadowOpacity: 0.15,
    shadowRadius: 8,
    elevation: 4,
  },
  large: {
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 8 },
    shadowOpacity: 0.2,
    shadowRadius: 16,
    elevation: 8,
  },
};

export const Layout = {
  window: {
    width: '100%',
    height: '100%',
  },
  header: {
    height: 90,
  },
  tabBar: {
    height: 60,
  },
  poster: {
    width: 120,
    height: 180,
    aspectRatio: 2/3,
  },
  backdrop: {
    width: '100%',
    height: 200,
    aspectRatio: 16/9,
  },
};

export default {
  Colors,
  Spacing,
  BorderRadius,
  Typography,
  Shadows,
  Layout,
};