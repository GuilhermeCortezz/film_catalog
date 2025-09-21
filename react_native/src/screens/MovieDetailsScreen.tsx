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

// Hooks
import { useMovieStorage } from '../hooks/useMovieStorage';
import { useMovieSchedule } from '../hooks/useMovieSchedule';

// Components
import ScheduleMovieModal from '../components/ScheduleMovieModal';

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
  const { hasStatus, toggleStatus } = useMovieStorage(movie.id);
  const { 
    currentMovieSchedule, 
    scheduleMovie, 
    removeMovieSchedule, 
    updateSchedule,
    loading: scheduleLoading 
  } = useMovieSchedule(movie.id);

  const [movieDetails, setMovieDetails] = useState<MovieDetails | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showScheduleModal, setShowScheduleModal] = useState(false);

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

  const formatRuntime = (minutes: number): string => {
    if (!minutes || minutes === 0 || typeof minutes !== 'number') return 'Duração não informada';
    try {
      const hours = Math.floor(minutes / 60);
      const remainingMinutes = minutes % 60;
      return `${hours}h ${remainingMinutes}m`;
    } catch (error) {
      console.error('Error formatting runtime:', error);
      return 'Duração inválida';
    }
  };

  const formatDate = (dateString: string): string => {
    if (!dateString || typeof dateString !== 'string') return 'Data não informada';
    try {
      const date = new Date(dateString);
      if (isNaN(date.getTime())) return 'Data inválida';
      return date.toLocaleDateString('pt-BR');
    } catch (error) {
      console.error('Error formatting date:', error);
      return 'Data inválida';
    }
  };

  const formatCurrency = (amount: number): string => {
    if (!amount || amount === 0 || typeof amount !== 'number') return 'Não informado';
    try {
      const formatted = new Intl.NumberFormat('pt-BR', {
        style: 'currency',
        currency: 'USD',
      }).format(amount);
      return formatted || 'Não informado';
    } catch (error) {
      console.error('Error formatting currency:', error);
      return 'Valor inválido';
    }
  };

  const renderGenres = (genres: Genre[]) => (
    <View style={styles.genresContainer}>
      {genres
        .filter(genre => genre.name) // Filtra apenas gêneros com nome
        .map((genre) => (
          <View key={genre.id} style={styles.genreTag}>
            <Text style={styles.genreText}>{genre.name}</Text>
          </View>
        ))}
    </View>
  );

  const handleScheduleMovie = async (date: Date, notes?: string, addToCalendar?: boolean): Promise<boolean> => {
    return await scheduleMovie(movie, date, notes, addToCalendar);
  };

  const handleUpdateSchedule = async (scheduleId: string, date: Date, notes?: string): Promise<boolean> => {
    return await updateSchedule(scheduleId, date, notes);
  };

  const handleRemoveSchedule = async (scheduleId: string): Promise<boolean> => {
    return await removeMovieSchedule(movie.id);
  };

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
      
      {/* Fixed Action Buttons */}
      <View style={styles.fixedActionButtons}>
        <View style={styles.actionButtonsContainer}>
          <TouchableOpacity
            style={[styles.actionButton, hasStatus('favorite') && styles.actionButtonActive]}
            onPress={() => toggleStatus(movie, 'favorite')}
          >
            <Ionicons
              name={hasStatus('favorite') ? 'heart' : 'heart-outline'}
              size={20}
              color={hasStatus('favorite') ? Colors.error : Colors.textPrimary}
            />
          </TouchableOpacity>

          <TouchableOpacity
            style={[styles.actionButton, hasStatus('watched') && styles.actionButtonActive]}
            onPress={() => toggleStatus(movie, 'watched')}
          >
            <Ionicons
              name={hasStatus('watched') ? 'checkmark-circle' : 'checkmark-circle-outline'}
              size={20}
              color={hasStatus('watched') ? Colors.success : Colors.textPrimary}
            />
          </TouchableOpacity>

          <TouchableOpacity
            style={[styles.actionButton, hasStatus('watchlist') && styles.actionButtonActive]}
            onPress={() => toggleStatus(movie, 'watchlist')}
          >
            <Ionicons
              name={hasStatus('watchlist') ? 'bookmark' : 'bookmark-outline'}
              size={20}
              color={hasStatus('watchlist') ? Colors.secondary : Colors.textPrimary}
            />
          </TouchableOpacity>

          <TouchableOpacity
            style={[styles.actionButton, currentMovieSchedule && styles.actionButtonActive]}
            onPress={() => setShowScheduleModal(true)}
          >
            <Ionicons
              name={currentMovieSchedule ? 'calendar' : 'calendar-outline'}
              size={20}
              color={currentMovieSchedule ? Colors.warning : Colors.textPrimary}
            />
          </TouchableOpacity>
        </View>
      </View>
      
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

          {/* Movie Info */}
          <View style={styles.movieInfoContainer}>
            <Image
              source={{
                uri: tmdbService.getImageUrl(movieDetails.poster_path || '', 'w500') || '',
              }}
              style={styles.posterImage}
            />
            <View style={styles.movieInfo}>
              <Text style={styles.movieTitle}>{movieDetails.title || 'Título não disponível'}</Text>
              {movieDetails.tagline ? (
                <Text style={styles.tagline}>{movieDetails.tagline}</Text>
              ) : null}
              
              {/* Rating and Year */}
              <View style={styles.ratingContainer}>
                <Ionicons name="star" size={16} color={Colors.warning} />
                <Text style={styles.rating}>
                  {(movieDetails.vote_average && typeof movieDetails.vote_average === 'number') 
                    ? movieDetails.vote_average.toFixed(1) 
                    : 'N/A'}
                </Text>
                {movieDetails.release_date ? (
                  <Text style={styles.year}>
                    • {(() => {
                      try {
                        const year = new Date(movieDetails.release_date).getFullYear();
                        return isNaN(year) ? 'Ano inválido' : year.toString();
                      } catch {
                        return 'Ano inválido';
                      }
                    })()}
                  </Text>
                ) : null}
                {movieDetails.runtime ? (
                  <Text style={styles.runtime}>
                    • {formatRuntime(movieDetails.runtime)}
                  </Text>
                ) : null}
              </View>
            </View>
          </View>
        </View>

        {/* Content */}
        <View style={styles.contentContainer}>
          {/* Genres */}
          {movieDetails.genres && movieDetails.genres.length > 0 ? (
            <View style={styles.section}>
              <Text style={styles.sectionTitle}>Gêneros</Text>
              {renderGenres(movieDetails.genres)}
            </View>
          ) : null}

          {/* Overview */}
          {movieDetails.overview && movieDetails.overview.trim() ? (
            <View style={styles.section}>
              <Text style={styles.sectionTitle}>Sinopse</Text>
              <Text style={styles.overview}>{movieDetails.overview.trim()}</Text>
            </View>
          ) : null}

          {/* Additional Info */}
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>Informações</Text>
            <View style={styles.infoGrid}>
              {movieDetails.release_date ? (
                <View style={styles.infoItem}>
                  <Text style={styles.infoLabel}>Lançamento</Text>
                  <Text style={styles.infoValue}>
                    {formatDate(movieDetails.release_date)}
                  </Text>
                </View>
              ) : null}
              
              {movieDetails.original_language ? (
                <View style={styles.infoItem}>
                  <Text style={styles.infoLabel}>Idioma Original</Text>
                  <Text style={styles.infoValue}>
                    {(movieDetails.original_language || '').toUpperCase() || 'Não informado'}
                  </Text>
                </View>
              ) : null}

              {movieDetails.budget && movieDetails.budget > 0 ? (
                <View style={styles.infoItem}>
                  <Text style={styles.infoLabel}>Orçamento</Text>
                  <Text style={styles.infoValue}>
                    {formatCurrency(movieDetails.budget) || 'Não informado'}
                  </Text>
                </View>
              ) : null}

              {movieDetails.revenue && movieDetails.revenue > 0 ? (
                <View style={styles.infoItem}>
                  <Text style={styles.infoLabel}>Bilheteria</Text>
                  <Text style={styles.infoValue}>
                    {formatCurrency(movieDetails.revenue) || 'Não informado'}
                  </Text>
                </View>
              ) : null}
            </View>
          </View>

          {/* Production Companies */}
          {movieDetails.production_companies && movieDetails.production_companies.length > 0 ? (
            <View style={styles.section}>
              <Text style={styles.sectionTitle}>Produção</Text>
              <Text style={styles.productionText}>
                {movieDetails.production_companies
                  .filter(company => company.name) // Filtra apenas empresas com nome
                  .map(company => company.name)
                  .join(', ') || 'Informação não disponível'}
              </Text>
            </View>
          ) : null}
        </View>
      </ScrollView>

      {/* Schedule Modal */}
      <ScheduleMovieModal
        visible={showScheduleModal}
        movie={movie}
        existingSchedule={currentMovieSchedule}
        onClose={() => setShowScheduleModal(false)}
        onSchedule={handleScheduleMovie}
        onUpdate={handleUpdateSchedule}
        onRemove={handleRemoveSchedule}
        loading={scheduleLoading}
      />
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
  actionButtonsContainer: {
    position: 'absolute',
    top: 50,
    right: Spacing.md,
    flexDirection: 'row',
    gap: Spacing.xs,
  },
  actionButton: {
    backgroundColor: 'rgba(0,0,0,0.5)',
    borderRadius: 20,
    padding: Spacing.sm,
    alignItems: 'center',
    justifyContent: 'center',
    width: 44,
    height: 44,
    ...Shadows.medium,
  },
  actionButtonActive: {
    backgroundColor: Colors.secondary + '40',
  },
  fixedActionButtons: {
    position: 'absolute',
    top: 50,
    left: 0,
    right: 0,
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingHorizontal: Spacing.md,
    zIndex: 10,
  },
});