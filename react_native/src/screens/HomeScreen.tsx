import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  ScrollView,
  StyleSheet,
  TouchableOpacity,
  FlatList,
  RefreshControl,
  Alert,
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
import { Colors, Typography, Spacing } from '../constants/theme';

// Types
import { Movie } from '../types/movie';
import { RootStackParamList } from '../navigation/AppNavigator';

type HomeScreenNavigationProp = StackNavigationProp<RootStackParamList>;

interface MovieSection {
  title: string;
  data: Movie[];
  endpoint: 'popular' | 'top_rated' | 'now_playing' | 'upcoming';
  loading: boolean;
}

export default function HomeScreen() {
  const navigation = useNavigation<HomeScreenNavigationProp>();
  const [sections, setSections] = useState<MovieSection[]>([]);
  const [refreshing, setRefreshing] = useState(false);

  const loadMovieSection = async (
    title: string,
    endpoint: 'popular' | 'top_rated' | 'now_playing' | 'upcoming',
    apiCall: () => Promise<any>
  ): Promise<MovieSection> => {
    try {
      const response = await apiCall();
      return {
        title,
        endpoint,
        data: response.results.slice(0, 10), // Limit to 10 movies per section
        loading: false,
      };
    } catch (error) {
      console.error(`Error loading ${title}:`, error);
      return {
        title,
        endpoint,
        data: [],
        loading: false,
      };
    }
  };

  const loadAllSections = async () => {
    try {
      console.log('🏠 Loading home sections...');
      
      const sectionPromises = [
        loadMovieSection('Populares', 'popular', () => tmdbService.getPopularMovies(1)),
        loadMovieSection('Mais Bem Avaliados', 'top_rated', () => tmdbService.getTopRatedMovies(1)),
        loadMovieSection('Em Cartaz', 'now_playing', () => tmdbService.getNowPlayingMovies(1)),
        loadMovieSection('Próximos Lançamentos', 'upcoming', () => tmdbService.getUpcomingMovies(1)),
      ];

      const newSections = await Promise.all(sectionPromises);
      setSections(newSections);
      console.log('✅ All sections loaded');
    } catch (error) {
      console.error('❌ Error loading sections:', error);
      Alert.alert('Erro', 'Não foi possível carregar os filmes. Verifique sua conexão.');
    }
  };

  const onRefresh = async () => {
    setRefreshing(true);
    await loadAllSections();
    setRefreshing(false);
  };

  useEffect(() => {
    loadAllSections();
  }, []);

  const handleMoviePress = (movie: Movie) => {
    navigation.navigate('MovieDetails', { movie });
  };

  const handleSeeAllPress = (section: MovieSection) => {
    navigation.navigate('Section', { 
      title: section.title, 
      endpoint: section.endpoint 
    });
  };

  const renderMovieItem = ({ item }: { item: Movie }) => (
    <View style={styles.movieCard}>
      <MovieCard
        movie={item}
        onPress={() => handleMoviePress(item)}
      />
    </View>
  );

  const renderSection = (section: MovieSection) => (
    <View key={section.title} style={styles.section}>
      <View style={styles.sectionHeader}>
        <Text style={styles.sectionTitle}>{section.title}</Text>
        <TouchableOpacity 
          style={styles.seeAllButton}
          onPress={() => handleSeeAllPress(section)}
        >
          <Text style={styles.seeAllText}>Ver todos</Text>
          <Ionicons name="chevron-forward" size={16} color={Colors.secondary} />
        </TouchableOpacity>
      </View>
      
      <FlatList
        data={section.data}
        renderItem={renderMovieItem}
        keyExtractor={(item) => item.id.toString()}
        horizontal
        showsHorizontalScrollIndicator={false}
        contentContainerStyle={styles.moviesList}
      />
    </View>
  );

  return (
    <SafeAreaView style={styles.container}>
      {/* Header */}
      <View style={styles.header}>
        <Text style={styles.headerTitle}>Catálogo de Filmes</Text>
        <Text style={styles.headerSubtitle}>Descubra os melhores filmes</Text>
      </View>

      {/* Content */}
      <ScrollView
        style={styles.content}
        refreshControl={
          <RefreshControl 
            refreshing={refreshing} 
            onRefresh={onRefresh}
            tintColor={Colors.secondary}
            colors={[Colors.secondary]}
          />
        }
        showsVerticalScrollIndicator={false}
      >
        {sections.map(renderSection)}
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.background,
  },
  header: {
    backgroundColor: Colors.surface,
    paddingTop: Spacing.lg,
    paddingBottom: Spacing.md,
    paddingHorizontal: Spacing.md,
    borderBottomWidth: 1,
    borderBottomColor: Colors.surfaceLight,
  },
  headerTitle: {
    fontSize: Typography.xl,
    fontWeight: Typography.bold,
    color: Colors.textPrimary,
    textAlign: 'center',
  },
  headerSubtitle: {
    fontSize: Typography.md,
    color: Colors.textSecondary,
    textAlign: 'center',
    marginTop: Spacing.xs,
  },
  content: {
    flex: 1,
  },
  section: {
    marginVertical: Spacing.md,
  },
  sectionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: Spacing.md,
    marginBottom: Spacing.sm,
  },
  sectionTitle: {
    fontSize: Typography.lg,
    fontWeight: Typography.semibold,
    color: Colors.textPrimary,
  },
  seeAllButton: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: Spacing.xs,
    paddingHorizontal: Spacing.sm,
  },
  seeAllText: {
    color: Colors.secondary,
    fontSize: Typography.sm,
    fontWeight: Typography.medium,
    marginRight: Spacing.xs,
  },
  moviesList: {
    paddingHorizontal: Spacing.sm,
  },
  movieCard: {
    width: 140,
    marginHorizontal: Spacing.xs,
  },
});