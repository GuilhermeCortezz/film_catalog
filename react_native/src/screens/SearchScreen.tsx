import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TextInput,
  FlatList,
  TouchableOpacity,
  ActivityIndicator,
  SafeAreaView,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';
import { StackNavigationProp } from '@react-navigation/stack';

// Services
import { tmdbService } from '../services/tmdbService';

// Components
import MovieCard from '../components/MovieCard';

// Constants
import { Colors, Typography, Spacing, Shadows } from '../constants/theme';

// Types
import { Movie } from '../types/movie';
import { RootStackParamList } from '../navigation/AppNavigator';

type SearchScreenNavigationProp = StackNavigationProp<RootStackParamList>;

export default function SearchScreen() {
  const navigation = useNavigation<SearchScreenNavigationProp>();
  const [searchQuery, setSearchQuery] = useState('');
  const [movies, setMovies] = useState<Movie[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const delayedSearch = setTimeout(() => {
      if (searchQuery.trim().length >= 2) {
        searchMovies(searchQuery);
      } else {
        setMovies([]);
      }
    }, 500);

    return () => clearTimeout(delayedSearch);
  }, [searchQuery]);

  const searchMovies = async (query: string) => {
    try {
      setLoading(true);
      setError(null);
      const response = await tmdbService.searchMovies({ query });
      setMovies(response.results);
    } catch (err) {
      setError('Erro ao buscar filmes. Tente novamente.');
      console.error('Search error:', err);
    } finally {
      setLoading(false);
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

  const renderEmptyState = () => {
    if (searchQuery.trim().length === 0) {
      return (
        <View style={styles.emptyState}>
          <Ionicons 
            name="search-outline" 
            size={80} 
            color={Colors.textTertiary} 
          />
          <Text style={styles.emptyStateTitle}>
            Buscar Filmes
          </Text>
          <Text style={styles.emptyStateSubtitle}>
            Digite o nome de um filme para começar a busca
          </Text>
        </View>
      );
    }

    if (movies.length === 0 && !loading) {
      return (
        <View style={styles.emptyState}>
          <Ionicons 
            name="film-outline" 
            size={80} 
            color={Colors.textTertiary} 
          />
          <Text style={styles.emptyStateTitle}>
            Nenhum filme encontrado
          </Text>
          <Text style={styles.emptyStateSubtitle}>
            Tente buscar com palavras diferentes
          </Text>
        </View>
      );
    }

    return null;
  };

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.searchContainer}>
        <View style={styles.searchInputContainer}>
          <Ionicons 
            name="search-outline" 
            size={20} 
            color={Colors.textTertiary} 
          />
          <TextInput
            style={styles.searchInput}
            placeholder="Buscar filmes..."
            placeholderTextColor={Colors.textTertiary}
            value={searchQuery}
            onChangeText={setSearchQuery}
            autoCapitalize="none"
            autoCorrect={false}
            returnKeyType="search"
          />
          {searchQuery.length > 0 && (
            <TouchableOpacity
              onPress={() => setSearchQuery('')}
              style={styles.clearButton}
            >
              <Ionicons 
                name="close-circle" 
                size={20} 
                color={Colors.textTertiary} 
              />
            </TouchableOpacity>
          )}
        </View>
      </View>

      {error && (
        <View style={styles.errorContainer}>
          <Text style={styles.errorText}>{error}</Text>
        </View>
      )}

      {loading && (
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" color={Colors.secondary} />
          <Text style={styles.loadingText}>Buscando filmes...</Text>
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
  searchContainer: {
    paddingHorizontal: Spacing.md,
    paddingVertical: Spacing.sm,
    backgroundColor: Colors.surface,
    borderBottomWidth: 1,
    borderBottomColor: Colors.surfaceLight,
  },
  searchInputContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: Colors.surfaceLight,
    borderRadius: 25,
    paddingHorizontal: Spacing.md,
    paddingVertical: Spacing.sm,
    ...Shadows.small,
  },
  searchInput: {
    flex: 1,
    marginLeft: Spacing.sm,
    fontSize: Typography.md,
    color: Colors.textPrimary,
  },
  clearButton: {
    marginLeft: Spacing.sm,
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
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingText: {
    marginTop: Spacing.sm,
    fontSize: Typography.sm,
    color: Colors.textSecondary,
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
    marginTop: Spacing.md,
    textAlign: 'center',
  },
  emptyStateSubtitle: {
    fontSize: Typography.sm,
    color: Colors.textSecondary,
    marginTop: Spacing.xs,
    textAlign: 'center',
    lineHeight: 20,
  },
});