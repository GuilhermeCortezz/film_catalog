import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  Image,
  TouchableOpacity,
  ActivityIndicator,
  StatusBar,
  Dimensions,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { Ionicons } from '@expo/vector-icons';
import { useNavigation, useRoute, RouteProp } from '@react-navigation/native';
import { StackNavigationProp } from '@react-navigation/stack';

// Services
import { tmdbService } from '../services/tmdbService';

// Constants
import { Colors, Typography, Spacing, Shadows } from '../constants/theme';

// Types
import { MovieDetails, Genre } from '../types/movie';
import { RootStackParamList } from '../navigation/AppNavigator';

type MovieDetailsScreenRouteProp = RouteProp<RootStackParamList, 'MovieDetails'>;
type MovieDetailsScreenNavigationProp = StackNavigationProp<RootStackParamList>;

const { width: screenWidth, height: screenHeight } = Dimensions.get('window');

export default function MovieDetailsScreen() {
  const navigation = useNavigation<MovieDetailsScreenNavigationProp>();
  const route = useRoute<MovieDetailsScreenRouteProp>();
  const { movie } = route.params;

  const [movieDetails, setMovieDetails] = useState<MovieDetails | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchMovieDetails();
  }, []);

  const fetchMovieDetails = async () => {
    try {
      setLoading(true);
      setError(null);
      const details = await tmdbService.getMovieDetails(movie.id);
      setMovieDetails(details);
    } catch (err) {
      setError('Erro ao carregar detalhes do filme.');
      console.error('Movie details error:', err);
    } finally {
      setLoading(false);
    }
  };

  const formatRuntime = (minutes: number) => {
    const hours = Math.floor(minutes / 60);
    const remainingMinutes = minutes % 60;
    return `${hours}h ${remainingMinutes}m`;
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('pt-BR');
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'USD',
    }).format(amount);
  };

  const renderGenres = (genres: Genre[]) => (
    <View style={styles.genresContainer}>
      {genres.map((genre) => (
        <View key={genre.id} style={styles.genreTag}>
          <Text style={styles.genreText}>{genre.name}</Text>
        </View>
      ))}
    </View>
  );

  if (loading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color={Colors.secondary} />
        <Text style={styles.loadingText}>Carregando detalhes...</Text>
      </View>
    );
  }

  if (error || !movieDetails) {
    return (
      <View style={styles.errorContainer}>
        <Ionicons name="alert-circle-outline" size={60} color={Colors.error} />
        <Text style={styles.errorText}>{error}</Text>
        <TouchableOpacity
          style={styles.retryButton}
          onPress={fetchMovieDetails}
        >
          <Text style={styles.retryButtonText}>Tentar novamente</Text>
        </TouchableOpacity>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" translucent backgroundColor="transparent" />
      
      <ScrollView style={styles.scrollView} showsVerticalScrollIndicator={false}>
        {/* Hero Section */}
        <View style={styles.heroContainer}>
          <Image
            source={{
              uri: tmdbService.getImageUrl(movieDetails.backdrop_path || '', 'w500') || '',
            }}
            style={styles.backdropImage}
            resizeMode="cover"
          />
          <LinearGradient
            colors={['transparent', 'rgba(0,0,0,0.7)', Colors.background]}
            style={styles.heroGradient}
          />
          
          {/* Back Button */}
          <TouchableOpacity
            style={styles.backButton}
            onPress={() => navigation.goBack()}
          >
            <Ionicons name="arrow-back" size={24} color={Colors.textPrimary} />
          </TouchableOpacity>

          {/* Movie Info */}
          <View style={styles.movieInfoContainer}>
            <Image
              source={{
                uri: tmdbService.getImageUrl(movieDetails.poster_path || '', 'w500') || '',
              }}
              style={styles.posterImage}
            />
            <View style={styles.movieInfo}>
              <Text style={styles.movieTitle}>{movieDetails.title}</Text>
              {movieDetails.tagline && (
                <Text style={styles.tagline}>{movieDetails.tagline}</Text>
              )}
              
              {/* Rating and Year */}
              <View style={styles.ratingContainer}>
                <Ionicons name="star" size={16} color={Colors.warning} />
                <Text style={styles.rating}>
                  {movieDetails.vote_average.toFixed(1)}
                </Text>
                <Text style={styles.year}>
                  • {new Date(movieDetails.release_date).getFullYear()}
                </Text>
                {movieDetails.runtime && (
                  <Text style={styles.runtime}>
                    • {formatRuntime(movieDetails.runtime)}
                  </Text>
                )}
              </View>
            </View>
          </View>
        </View>

        {/* Content */}
        <View style={styles.contentContainer}>
          {/* Genres */}
          {movieDetails.genres && movieDetails.genres.length > 0 && (
            <View style={styles.section}>
              <Text style={styles.sectionTitle}>Gêneros</Text>
              {renderGenres(movieDetails.genres)}
            </View>
          )}

          {/* Overview */}
          {movieDetails.overview && (
            <View style={styles.section}>
              <Text style={styles.sectionTitle}>Sinopse</Text>
              <Text style={styles.overview}>{movieDetails.overview}</Text>
            </View>
          )}

          {/* Additional Info */}
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>Informações</Text>
            <View style={styles.infoGrid}>
              {movieDetails.release_date && (
                <View style={styles.infoItem}>
                  <Text style={styles.infoLabel}>Lançamento</Text>
                  <Text style={styles.infoValue}>
                    {formatDate(movieDetails.release_date)}
                  </Text>
                </View>
              )}
              
              {movieDetails.original_language && (
                <View style={styles.infoItem}>
                  <Text style={styles.infoLabel}>Idioma Original</Text>
                  <Text style={styles.infoValue}>
                    {movieDetails.original_language.toUpperCase()}
                  </Text>
                </View>
              )}

              {movieDetails.budget && movieDetails.budget > 0 && (
                <View style={styles.infoItem}>
                  <Text style={styles.infoLabel}>Orçamento</Text>
                  <Text style={styles.infoValue}>
                    {formatCurrency(movieDetails.budget)}
                  </Text>
                </View>
              )}

              {movieDetails.revenue && movieDetails.revenue > 0 && (
                <View style={styles.infoItem}>
                  <Text style={styles.infoLabel}>Bilheteria</Text>
                  <Text style={styles.infoValue}>
                    {formatCurrency(movieDetails.revenue)}
                  </Text>
                </View>
              )}
            </View>
          </View>

          {/* Production Companies */}
          {movieDetails.production_companies && movieDetails.production_companies.length > 0 && (
            <View style={styles.section}>
              <Text style={styles.sectionTitle}>Produção</Text>
              <Text style={styles.productionText}>
                {movieDetails.production_companies.map(company => company.name).join(', ')}
              </Text>
            </View>
          )}
        </View>
      </ScrollView>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.background,
  },
  scrollView: {
    flex: 1,
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: Colors.background,
  },
  loadingText: {
    marginTop: Spacing.sm,
    fontSize: Typography.sm,
    color: Colors.textSecondary,
  },
  errorContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: Colors.background,
    paddingHorizontal: Spacing.xl,
  },
  errorText: {
    fontSize: Typography.md,
    color: Colors.error,
    textAlign: 'center',
    marginVertical: Spacing.md,
  },
  retryButton: {
    backgroundColor: Colors.secondary,
    paddingHorizontal: Spacing.lg,
    paddingVertical: Spacing.sm,
    borderRadius: 25,
  },
  retryButtonText: {
    color: Colors.textPrimary,
    fontSize: Typography.sm,
    fontWeight: Typography.semibold,
  },
  heroContainer: {
    height: screenHeight * 0.6,
    position: 'relative',
  },
  backdropImage: {
    width: screenWidth,
    height: '100%',
  },
  heroGradient: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    height: '60%',
  },
  backButton: {
    position: 'absolute',
    top: 50,
    left: Spacing.md,
    backgroundColor: 'rgba(0,0,0,0.5)',
    borderRadius: 20,
    padding: Spacing.sm,
    ...Shadows.medium,
  },
  movieInfoContainer: {
    position: 'absolute',
    bottom: Spacing.lg,
    left: Spacing.md,
    right: Spacing.md,
    flexDirection: 'row',
  },
  posterImage: {
    width: 120,
    height: 180,
    borderRadius: 12,
    ...Shadows.large,
  },
  movieInfo: {
    flex: 1,
    marginLeft: Spacing.md,
    justifyContent: 'flex-end',
  },
  movieTitle: {
    fontSize: Typography.xl,
    fontWeight: Typography.bold,
    color: Colors.textPrimary,
    marginBottom: Spacing.xs,
  },
  tagline: {
    fontSize: Typography.sm,
    color: Colors.textSecondary,
    fontStyle: 'italic',
    marginBottom: Spacing.sm,
  },
  ratingContainer: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  rating: {
    fontSize: Typography.sm,
    color: Colors.textPrimary,
    marginLeft: Spacing.xs,
    fontWeight: Typography.medium,
  },
  year: {
    fontSize: Typography.sm,
    color: Colors.textSecondary,
    marginLeft: Spacing.xs,
  },
  runtime: {
    fontSize: Typography.sm,
    color: Colors.textSecondary,
  },
  contentContainer: {
    padding: Spacing.md,
  },
  section: {
    marginBottom: Spacing.lg,
  },
  sectionTitle: {
    fontSize: Typography.lg,
    fontWeight: Typography.semibold,
    color: Colors.textPrimary,
    marginBottom: Spacing.sm,
  },
  genresContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: Spacing.xs,
  },
  genreTag: {
    backgroundColor: Colors.secondary,
    paddingHorizontal: Spacing.sm,
    paddingVertical: Spacing.xs,
    borderRadius: 15,
  },
  genreText: {
    fontSize: Typography.xs,
    color: Colors.textPrimary,
    fontWeight: Typography.medium,
  },
  overview: {
    fontSize: Typography.md,
    color: Colors.textSecondary,
    lineHeight: 22,
  },
  infoGrid: {
    gap: Spacing.sm,
  },
  infoItem: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingVertical: Spacing.xs,
    borderBottomWidth: 1,
    borderBottomColor: Colors.surfaceLight,
  },
  infoLabel: {
    fontSize: Typography.sm,
    color: Colors.textSecondary,
    flex: 1,
  },
  infoValue: {
    fontSize: Typography.sm,
    color: Colors.textPrimary,
    fontWeight: Typography.medium,
    flex: 1,
    textAlign: 'right',
  },
  productionText: {
    fontSize: Typography.sm,
    color: Colors.textSecondary,
    lineHeight: 20,
  },
});