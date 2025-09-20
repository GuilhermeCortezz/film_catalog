import React from 'react';
import {
  TouchableOpacity,
  Image,
  Text,
  View,
  StyleSheet,
  Dimensions,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { Ionicons } from '@expo/vector-icons';
import { Movie } from '../types/movie';
import { tmdbService } from '../services/tmdbService';
import { Colors, Spacing, BorderRadius, Typography, Shadows } from '../constants/theme';

interface MovieCardProps {
  movie: Movie;
  onPress?: (movie: Movie) => void;
  size?: 'small' | 'medium' | 'large';
  showDetails?: boolean;
}

const { width } = Dimensions.get('window');

export default function MovieCard({ 
  movie, 
  onPress, 
  size = 'medium',
  showDetails = true 
}: MovieCardProps) {
  const cardSizes = {
    small: { width: width * 0.3, height: width * 0.45 },
    medium: { width: width * 0.4, height: width * 0.6 },
    large: { width: width * 0.9, height: width * 0.6 },
  };

  const cardSize = cardSizes[size];

  const handlePress = () => {
    onPress?.(movie);
  };

  return (
    <TouchableOpacity
      style={[styles.container, { width: cardSize.width }]}
      onPress={handlePress}
      activeOpacity={0.8}
    >
      {/* Poster Image */}
      <View style={[styles.imageContainer, { height: cardSize.height }]}>
        {movie.poster_path ? (
          <Image
            source={{ 
              uri: tmdbService.getImageUrl(movie.poster_path, 'w500') || undefined 
            }}
            style={styles.poster}
            resizeMode="cover"
          />
        ) : (
          <View style={styles.noPoster}>
            <Ionicons name="film-outline" size={40} color={Colors.textTertiary} />
          </View>
        )}
        
        {/* Rating Badge */}
        <View style={styles.ratingBadge}>
          <Ionicons name="star" size={12} color={Colors.rating} />
          <Text style={styles.ratingText}>
            {movie.vote_average.toFixed(1)}
          </Text>
        </View>
        
        {/* Gradient Overlay */}
        <LinearGradient
          colors={['transparent', Colors.overlayDark]}
          style={styles.gradient}
        />
      </View>

      {/* Movie Details */}
      {showDetails && (
        <View style={styles.details}>
          <Text style={styles.title} numberOfLines={2}>
            {movie.title}
          </Text>
          
          <Text style={styles.releaseDate}>
            {tmdbService.formatBrazilianDate(movie.release_date)}
          </Text>
          
          {size === 'large' && (
            <Text style={styles.overview} numberOfLines={3}>
              {movie.overview}
            </Text>
          )}
        </View>
      )}
    </TouchableOpacity>
  );
}

const styles = StyleSheet.create({
  container: {
    marginHorizontal: Spacing.xs,
    marginVertical: Spacing.sm,
  },
  imageContainer: {
    position: 'relative',
    borderRadius: BorderRadius.md,
    overflow: 'hidden',
    ...Shadows.medium,
  },
  poster: {
    width: '100%',
    height: '100%',
  },
  noPoster: {
    width: '100%',
    height: '100%',
    backgroundColor: Colors.surfaceLight,
    justifyContent: 'center',
    alignItems: 'center',
  },
  ratingBadge: {
    position: 'absolute',
    top: Spacing.sm,
    right: Spacing.sm,
    backgroundColor: Colors.overlay,
    borderRadius: BorderRadius.sm,
    paddingHorizontal: Spacing.xs,
    paddingVertical: 4,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 4,
  },
  ratingText: {
    color: Colors.textPrimary,
    fontSize: Typography.xs,
    fontWeight: Typography.semibold,
  },
  gradient: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    height: '40%',
  },
  details: {
    paddingTop: Spacing.sm,
  },
  title: {
    color: Colors.textPrimary,
    fontSize: Typography.md,
    fontWeight: Typography.semibold,
    lineHeight: Typography.lineHeight.tight * Typography.md,
    marginBottom: 4,
  },
  releaseDate: {
    color: Colors.textSecondary,
    fontSize: Typography.sm,
    fontWeight: Typography.regular,
  },
  overview: {
    color: Colors.textSecondary,
    fontSize: Typography.sm,
    lineHeight: Typography.lineHeight.normal * Typography.sm,
    marginTop: Spacing.xs,
  },
});