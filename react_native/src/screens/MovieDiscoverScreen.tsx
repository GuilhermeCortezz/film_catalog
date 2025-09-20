import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  FlatList,
  StyleSheet,
  ActivityIndicator,
  Alert,
  TouchableOpacity,
  Image,
} from 'react-native';
import { StatusBar } from 'expo-status-bar';
import tmdbService from '../services/tmdbService';
import { Movie } from '../types/movie';

export default function MovieDiscoverScreen() {
  const [movies, setMovies] = useState<Movie[]>([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(1);

  const loadMovies = async (pageNumber: number = 1) => {
    try {
      setLoading(true);
      console.log(`📱 Loading movies - Page ${pageNumber}`);
      
      const response = await tmdbService.discoverMovies({
        page: pageNumber,
        sort_by: 'popularity.desc',
        include_adult: false,
        include_video: false,
      });

      if (pageNumber === 1) {
        setMovies(response.results);
      } else {
        setMovies(prev => [...prev, ...response.results]);
      }
      
      console.log(`✅ Loaded ${response.results.length} movies`);
    } catch (error) {
      console.error('Error loading movies:', error);
      Alert.alert(
        'Erro',
        'Não foi possível carregar os filmes. Verifique sua conexão.'
      );
    } finally {
      setLoading(false);
    }
  };

  const loadMoreMovies = () => {
    if (!loading) {
      const nextPage = page + 1;
      setPage(nextPage);
      loadMovies(nextPage);
    }
  };

  useEffect(() => {
    loadMovies(1);
  }, []);

  const renderMovie = ({ item }: { item: Movie }) => (
    <View style={styles.movieCard}>
      <View style={styles.movieContent}>
        {item.poster_path ? (
          <Image
            source={{ uri: tmdbService.getImageUrl(item.poster_path, 'w200') || undefined }}
            style={styles.poster}
            resizeMode="cover"
          />
        ) : (
          <View style={styles.noPoster}>
            <Text style={styles.noPosterText}>🎬</Text>
          </View>
        )}
        
        <View style={styles.movieInfo}>
          <Text style={styles.title} numberOfLines={2}>
            {item.title}
          </Text>
          <Text style={styles.originalTitle} numberOfLines={1}>
            {item.original_title}
          </Text>
          <Text style={styles.overview} numberOfLines={3}>
            {item.overview}
          </Text>
          
          <View style={styles.movieStats}>
            <Text style={styles.stat}>⭐ {item.vote_average.toFixed(1)}</Text>
            <Text style={styles.stat}>📅 {item.release_date}</Text>
            <Text style={styles.stat}>👥 {item.vote_count}</Text>
          </View>
        </View>
      </View>
    </View>
  );

  const renderFooter = () => {
    if (!loading) return null;
    return (
      <View style={styles.footer}>
        <ActivityIndicator size="small" color="#0066cc" />
        <Text style={styles.footerText}>Carregando mais filmes...</Text>
      </View>
    );
  };

  return (
    <View style={styles.container}>
      <StatusBar style="dark" />
      
      <View style={styles.header}>
        <Text style={styles.headerTitle}>🎬 Film Catalog</Text>
        <Text style={styles.headerSubtitle}>Discover Movies - TMDB API Test</Text>
        <TouchableOpacity 
          style={styles.refreshButton}
          onPress={() => {
            setPage(1);
            loadMovies(1);
          }}
        >
          <Text style={styles.refreshButtonText}>🔄 Refresh</Text>
        </TouchableOpacity>
      </View>

      {movies.length === 0 && loading ? (
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" color="#0066cc" />
          <Text style={styles.loadingText}>Carregando filmes...</Text>
        </View>
      ) : (
        <FlatList
          data={movies}
          renderItem={renderMovie}
          keyExtractor={(item) => item.id.toString()}
          onEndReached={loadMoreMovies}
          onEndReachedThreshold={0.5}
          ListFooterComponent={renderFooter}
          showsVerticalScrollIndicator={false}
          contentContainerStyle={styles.listContainer}
        />
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  header: {
    backgroundColor: '#fff',
    paddingTop: 50,
    paddingBottom: 20,
    paddingHorizontal: 20,
    borderBottomWidth: 1,
    borderBottomColor: '#eee',
  },
  headerTitle: {
    fontSize: 24,
    fontWeight: 'bold',
    color: '#333',
    textAlign: 'center',
  },
  headerSubtitle: {
    fontSize: 14,
    color: '#666',
    textAlign: 'center',
    marginTop: 4,
  },
  refreshButton: {
    backgroundColor: '#0066cc',
    paddingVertical: 8,
    paddingHorizontal: 16,
    borderRadius: 20,
    alignSelf: 'center',
    marginTop: 10,
  },
  refreshButtonText: {
    color: '#fff',
    fontSize: 14,
    fontWeight: '600',
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingText: {
    marginTop: 10,
    fontSize: 16,
    color: '#666',
  },
  listContainer: {
    paddingHorizontal: 15,
    paddingVertical: 10,
  },
  movieCard: {
    backgroundColor: '#fff',
    marginBottom: 12,
    borderRadius: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  movieContent: {
    flexDirection: 'row',
    padding: 15,
  },
  poster: {
    width: 80,
    height: 120,
    borderRadius: 8,
    marginRight: 15,
  },
  noPoster: {
    width: 80,
    height: 120,
    borderRadius: 8,
    backgroundColor: '#e0e0e0',
    justifyContent: 'center',
    alignItems: 'center',
    marginRight: 15,
  },
  noPosterText: {
    fontSize: 24,
  },
  movieInfo: {
    flex: 1,
    justifyContent: 'space-between',
  },
  title: {
    fontSize: 18,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 4,
  },
  originalTitle: {
    fontSize: 14,
    color: '#666',
    fontStyle: 'italic',
    marginBottom: 8,
  },
  overview: {
    fontSize: 14,
    color: '#555',
    lineHeight: 20,
    marginBottom: 10,
  },
  movieStats: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  stat: {
    fontSize: 12,
    color: '#777',
    fontWeight: '500',
  },
  footer: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
    paddingVertical: 20,
  },
  footerText: {
    marginLeft: 10,
    fontSize: 14,
    color: '#666',
  },
});