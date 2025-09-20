import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  ActivityIndicator,
  RefreshControl,
  SafeAreaView,
} from 'react-native';
import { useNavigation, useRoute, RouteProp } from '@react-navigation/native';
import { StackNavigationProp } from '@react-navigation/stack';

// Services
import { tmdbService } from '../services/tmdbService';

// Components
import MovieCard from '../components/MovieCard';

// Constants
import { Colors, Typography, Spacing } from '../constants/theme';

// Types
import { Movie } from '../types/movie';
import { RootStackParamList } from '../navigation/AppNavigator';

type SectionScreenRouteProp = RouteProp<RootStackParamList, 'Section'>;
type SectionScreenNavigationProp = StackNavigationProp<RootStackParamList>;

export default function SectionScreen() {
  const navigation = useNavigation<SectionScreenNavigationProp>();
  const route = useRoute<SectionScreenRouteProp>();
  const { title, endpoint } = route.params;

  const [movies, setMovies] = useState<Movie[]>([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);

  useEffect(() => {
    fetchMovies(1, false);
  }, []);

  const fetchMovies = async (page: number, append: boolean = false) => {
    try {
      if (page === 1) {
        setLoading(true);
      } else {
        setLoadingMore(true);
      }
      setError(null);

      let response;
      switch (endpoint) {
        case 'popular':
          response = await tmdbService.getPopularMovies(page);
          break;
        case 'top_rated':
          response = await tmdbService.getTopRatedMovies(page);
          break;
        case 'now_playing':
          response = await tmdbService.getNowPlayingMovies(page);
          break;
        case 'upcoming':
          response = await tmdbService.getUpcomingMovies(page);
          break;
        default:
          throw new Error('Endpoint inválido');
      }

      if (append) {
        setMovies(prev => [...prev, ...response.results]);
      } else {
        setMovies(response.results);
      }
      
      setCurrentPage(response.page);
      setTotalPages(response.total_pages);
    } catch (err) {
      setError('Erro ao carregar filmes. Tente novamente.');
      console.error('Section movies error:', err);
    } finally {
      setLoading(false);
      setLoadingMore(false);
    }
  };

  const handleRefresh = async () => {
    setRefreshing(true);
    await fetchMovies(1, false);
    setRefreshing(false);
  };

  const handleLoadMore = () => {
    if (!loadingMore && currentPage < totalPages) {
      fetchMovies(currentPage + 1, true);
    }
  };

  const handleMoviePress = (movie: Movie) => {
    navigation.navigate('MovieDetails', { movie });
  };

  const renderMovie = ({ item }: { item: Movie }) => (
    <View style={styles.movieCard}>
      <MovieCard
        movie={item}
        onPress={() => handleMoviePress(item)}
      />
    </View>
  );

  const renderFooter = () => {
    if (!loadingMore) return null;
    
    return (
      <View style={styles.footerLoading}>
        <ActivityIndicator size="small" color={Colors.secondary} />
        <Text style={styles.footerLoadingText}>Carregando mais filmes...</Text>
      </View>
    );
  };

  const renderEmptyState = () => {
    if (loading) return null;

    return (
      <View style={styles.emptyState}>
        <Text style={styles.emptyStateTitle}>
          Nenhum filme encontrado
        </Text>
        <Text style={styles.emptyStateSubtitle}>
          Não foi possível carregar os filmes desta seção.
        </Text>
      </View>
    );
  };

  if (loading && movies.length === 0) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color={Colors.secondary} />
        <Text style={styles.loadingText}>Carregando filmes...</Text>
      </View>
    );
  }

  return (
    <SafeAreaView style={styles.container}>
      {error && (
        <View style={styles.errorContainer}>
          <Text style={styles.errorText}>{error}</Text>
        </View>
      )}

      <FlatList
        data={movies}
        renderItem={renderMovie}
        keyExtractor={(item) => item.id.toString()}
        numColumns={2}
        columnWrapperStyle={styles.row}
        contentContainerStyle={[
          styles.moviesList,
          movies.length === 0 ? styles.moviesListEmpty : null,
        ]}
        showsVerticalScrollIndicator={false}
        refreshControl={
          <RefreshControl
            refreshing={refreshing}
            onRefresh={handleRefresh}
            tintColor={Colors.secondary}
            colors={[Colors.secondary]}
          />
        }
        onEndReached={handleLoadMore}
        onEndReachedThreshold={0.5}
        ListFooterComponent={renderFooter}
        ListEmptyComponent={renderEmptyState}
      />
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.background,
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
    padding: Spacing.md,
    backgroundColor: Colors.error + '20',
    borderRadius: 8,
    margin: Spacing.md,
  },
  errorText: {
    color: Colors.error,
    fontSize: Typography.sm,
    textAlign: 'center',
  },
  moviesList: {
    padding: Spacing.md,
  },
  moviesListEmpty: {
    flex: 1,
  },
  row: {
    justifyContent: 'space-between',
  },
  movieCard: {
    flex: 0.48,
    marginBottom: Spacing.md,
  },
  footerLoading: {
    paddingVertical: Spacing.lg,
    alignItems: 'center',
  },
  footerLoadingText: {
    marginTop: Spacing.xs,
    fontSize: Typography.sm,
    color: Colors.textSecondary,
  },
  emptyState: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingHorizontal: Spacing.xl,
  },
  emptyStateTitle: {
    fontSize: Typography.lg,
    fontWeight: Typography.semibold,
    color: Colors.textPrimary,
    textAlign: 'center',
    marginBottom: Spacing.xs,
  },
  emptyStateSubtitle: {
    fontSize: Typography.sm,
    color: Colors.textSecondary,
    textAlign: 'center',
    lineHeight: 20,
  },
});