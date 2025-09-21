import React, { useState, useEffect, useCallback } from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TouchableOpacity,
  ActivityIndicator,
  RefreshControl,
  StatusBar,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useNavigation, useRoute, RouteProp } from '@react-navigation/native';
import { StackNavigationProp } from '@react-navigation/stack';

// Components
import MovieCard from '../components/MovieCard';

// Services
import { tmdbService } from '../services/tmdbService';

// Constants
import { Colors, Typography, Spacing } from '../constants/theme';

// Types
import { Movie, Genre } from '../types/movie';
import { RootStackParamList } from '../navigation/AppNavigator';

type CategoriesScreenRouteProp = RouteProp<RootStackParamList, 'Categories'>;
type CategoriesScreenNavigationProp = StackNavigationProp<RootStackParamList>;

export default function CategoriesScreen() {
  const navigation = useNavigation<CategoriesScreenNavigationProp>();
  const route = useRoute<CategoriesScreenRouteProp>();
  const { genreId, genreName } = route.params;

  const [movies, setMovies] = useState<Movie[]>([]);
  const [genres, setGenres] = useState<Genre[]>([]);
  const [selectedGenre, setSelectedGenre] = useState<number | null>(genreId || null);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [hasMore, setHasMore] = useState(true);

  useEffect(() => {
    loadGenres();
    loadMovies(1, true);
  }, [selectedGenre]);

  const loadGenres = async () => {
    try {
      const genresResponse = await tmdbService.getGenres();
      setGenres([{ id: 0, name: 'Todos' }, ...genresResponse.genres]);
    } catch (err) {
      console.error('Error loading genres:', err);
    }
  };

  const loadMovies = async (page: number = 1, reset: boolean = false) => {
    try {
      if (page === 1) {
        setLoading(true);
        setError(null);
      } else {
        setLoadingMore(true);
      }

      const params = {
        page,
        with_genres: selectedGenre && selectedGenre > 0 ? selectedGenre.toString() : undefined,
        sort_by: 'popularity.desc' as const,
      };

      const response = await tmdbService.discoverMovies(params);
      
      if (reset || page === 1) {
        setMovies(response.results);
      } else {
        setMovies(prev => [...prev, ...response.results]);
      }

      setCurrentPage(response.page);
      setTotalPages(response.total_pages);
      setHasMore(response.page < response.total_pages);
    } catch (err) {
      setError('Erro ao carregar filmes. Tente novamente.');
      console.error('Movies loading error:', err);
    } finally {
      setLoading(false);
      setLoadingMore(false);
      setRefreshing(false);
    }
  };

  const onRefresh = useCallback(() => {
    setRefreshing(true);
    setCurrentPage(1);
    loadMovies(1, true);
  }, [selectedGenre]);

  const loadMoreMovies = () => {
    if (!loadingMore && hasMore && currentPage < totalPages) {
      loadMovies(currentPage + 1, false);
    }
  };

  const handleGenreSelect = (genreId: number) => {
    setSelectedGenre(genreId);
    setCurrentPage(1);
    setMovies([]);
  };

  const handleMoviePress = (movie: Movie) => {
    navigation.navigate('MovieDetails', { movie });
  };

  const renderGenreFilter = () => (
    <View style={styles.genreFilterContainer}>
      <FlatList
        horizontal
        showsHorizontalScrollIndicator={false}
        data={genres}
        keyExtractor={(item) => item.id.toString()}
        contentContainerStyle={styles.genreFilterContent}
        renderItem={({ item }) => (
          <TouchableOpacity
            style={[
              styles.genreFilterItem,
              selectedGenre === item.id && styles.genreFilterItemActive
            ]}
            onPress={() => handleGenreSelect(item.id)}
          >
            <Text style={[
              styles.genreFilterText,
              selectedGenre === item.id && styles.genreFilterTextActive
            ]}>
              {item.name}
            </Text>
          </TouchableOpacity>
        )}
      />
    </View>
  );

  const renderMovieItem = ({ item }: { item: Movie }) => (
    <View style={styles.movieItemContainer}>
      <MovieCard 
        movie={item} 
        onPress={() => handleMoviePress(item)}
      />
    </View>
  );

  const renderFooter = () => {
    if (!loadingMore) return null;
    
    return (
      <View style={styles.footerLoader}>
        <ActivityIndicator size="small" color={Colors.secondary} />
        <Text style={styles.footerLoaderText}>Carregando mais filmes...</Text>
      </View>
    );
  };

  const renderEmpty = () => {
    if (loading) return null;
    
    return (
      <View style={styles.emptyContainer}>
        <Ionicons name="film-outline" size={60} color={Colors.textSecondary} />
        <Text style={styles.emptyTitle}>Nenhum filme encontrado</Text>
        <Text style={styles.emptySubtitle}>
          Tente selecionar uma categoria diferente
        </Text>
      </View>
    );
  };

  if (loading) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color={Colors.secondary} />
        <Text style={styles.loadingText}>Carregando filmes...</Text>
      </View>
    );
  }

  if (error && movies.length === 0) {
    return (
      <View style={styles.errorContainer}>
        <Ionicons name="alert-circle-outline" size={60} color={Colors.error} />
        <Text style={styles.errorText}>{error}</Text>
        <TouchableOpacity
          style={styles.retryButton}
          onPress={() => loadMovies(1, true)}
        >
          <Text style={styles.retryButtonText}>Tentar novamente</Text>
        </TouchableOpacity>
      </View>
    );
  }

  const selectedGenreName = selectedGenre === 0 || !selectedGenre 
    ? 'Todos os Filmes' 
    : genres.find(g => g.id === selectedGenre)?.name || genreName || 'Categoria';

  return (
    <View style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor={Colors.background} />
      
      {/* Header */}
      <View style={styles.header}>
        <TouchableOpacity
          style={styles.backButton}
          onPress={() => navigation.goBack()}
        >
          <Ionicons name="arrow-back" size={24} color={Colors.textPrimary} />
        </TouchableOpacity>
        
        <View style={styles.headerContent}>
          <Text style={styles.headerTitle}>{selectedGenreName}</Text>
          <Text style={styles.headerSubtitle}>
            {movies.length} {movies.length === 1 ? 'filme' : 'filmes'}
            {totalPages > 1 && ` • Página ${currentPage} de ${totalPages}`}
          </Text>
        </View>
      </View>

      {/* Genre Filter */}
      {renderGenreFilter()}

      {/* Movies List */}
      <FlatList
        data={movies}
        renderItem={renderMovieItem}
        keyExtractor={(item) => item.id.toString()}
        numColumns={2}
        contentContainerStyle={styles.moviesContainer}
        columnWrapperStyle={styles.movieRow}
        showsVerticalScrollIndicator={false}
        refreshControl={
          <RefreshControl
            refreshing={refreshing}
            onRefresh={onRefresh}
            colors={[Colors.secondary]}
            tintColor={Colors.secondary}
          />
        }
        onEndReached={loadMoreMovies}
        onEndReachedThreshold={0.1}
        ListFooterComponent={renderFooter}
        ListEmptyComponent={renderEmpty}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.background,
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: Spacing.md,
    paddingTop: 50,
    paddingBottom: Spacing.md,
    backgroundColor: Colors.background,
  },
  backButton: {
    marginRight: Spacing.md,
  },
  headerContent: {
    flex: 1,
  },
  headerTitle: {
    fontSize: Typography.xl,
    fontWeight: Typography.bold,
    color: Colors.textPrimary,
  },
  headerSubtitle: {
    fontSize: Typography.sm,
    color: Colors.textSecondary,
    marginTop: Spacing.xs,
  },
  genreFilterContainer: {
    paddingVertical: Spacing.sm,
    borderBottomWidth: 1,
    borderBottomColor: Colors.surfaceLight,
  },
  genreFilterContent: {
    paddingHorizontal: Spacing.md,
  },
  genreFilterItem: {
    backgroundColor: Colors.surface,
    paddingHorizontal: Spacing.md,
    paddingVertical: Spacing.sm,
    borderRadius: 20,
    marginRight: Spacing.sm,
    borderWidth: 1,
    borderColor: Colors.surfaceLight,
  },
  genreFilterItemActive: {
    backgroundColor: Colors.secondary,
    borderColor: Colors.secondary,
  },
  genreFilterText: {
    fontSize: Typography.sm,
    color: Colors.textSecondary,
    fontWeight: Typography.medium,
  },
  genreFilterTextActive: {
    color: Colors.textPrimary,
  },
  moviesContainer: {
    padding: Spacing.md,
  },
  movieRow: {
    justifyContent: 'space-between',
  },
  movieItemContainer: {
    flex: 1,
    maxWidth: '48%',
    marginBottom: Spacing.md,
  },
  footerLoader: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    paddingVertical: Spacing.lg,
  },
  footerLoaderText: {
    marginLeft: Spacing.sm,
    fontSize: Typography.sm,
    color: Colors.textSecondary,
  },
  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    paddingVertical: Spacing.xl * 2,
  },
  emptyTitle: {
    fontSize: Typography.lg,
    fontWeight: Typography.semibold,
    color: Colors.textPrimary,
    marginTop: Spacing.md,
    marginBottom: Spacing.xs,
  },
  emptySubtitle: {
    fontSize: Typography.sm,
    color: Colors.textSecondary,
    textAlign: 'center',
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
});