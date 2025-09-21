import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TouchableOpacity,
  SafeAreaView,
  RefreshControl,
  Alert,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import { useNavigation, useFocusEffect } from '@react-navigation/native';
import { StackNavigationProp } from '@react-navigation/stack';

// Services
import { movieStorageService, MovieStatus } from '../services/movieStorageService';

// Hooks
import { useMovieSchedule } from '../hooks/useMovieSchedule';

// Components
import MovieCard from '../components/MovieCard';

// Constants
import { Colors, Typography, Spacing } from '../constants/theme';

// Types
import { Movie } from '../types/movie';
import { RootStackParamList } from '../navigation/AppNavigator';

type ProfileScreenNavigationProp = StackNavigationProp<RootStackParamList>;

export default function ProfileScreen() {
  const navigation = useNavigation<ProfileScreenNavigationProp>();
  const [selectedTab, setSelectedTab] = useState<MovieStatus>('favorite');
  const [movies, setMovies] = useState<Movie[]>([]);
  const [loading, setLoading] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const [showSchedules, setShowSchedules] = useState(false);
  
  const { schedules, loading: schedulesLoading, loadSchedules } = useMovieSchedule();

  const loadMovies = async (status: MovieStatus) => {
    try {
      setLoading(true);
      const movieList = await movieStorageService.getMoviesByStatus(status);
      setMovies(movieList);
    } catch (error) {
      console.error(`Error loading ${status} movies:`, error);
    } finally {
      setLoading(false);
    }
  };

  const handleRefresh = async () => {
    setRefreshing(true);
    if (showSchedules) {
      await loadSchedules();
    } else {
      await loadMovies(selectedTab);
    }
    setRefreshing(false);
  };

  const handleTabChange = (tab: MovieStatus) => {
    setSelectedTab(tab);
    loadMovies(tab);
  };

  const handleMoviePress = (movie: Movie) => {
    navigation.navigate('MovieDetails', { movie });
  };

  const handleClearAll = () => {
    Alert.alert(
      'Limpar Dados',
      'Tem certeza que deseja limpar todos os dados salvos?',
      [
        { text: 'Cancelar', style: 'cancel' },
        {
          text: 'Limpar',
          style: 'destructive',
          onPress: async () => {
            await movieStorageService.clearAllData();
            setMovies([]);
          },
        },
      ]
    );
  };

  // Reload when screen comes into focus
  useFocusEffect(
    React.useCallback(() => {
      loadMovies(selectedTab);
    }, [selectedTab])
  );

  useEffect(() => {
    loadMovies(selectedTab);
  }, [selectedTab]);

  const renderMovie = ({ item }: { item: Movie }) => (
    <View style={styles.movieCard}>
      <MovieCard
        movie={item}
        onPress={() => handleMoviePress(item)}
        size="medium"
        showActions={true}
      />
    </View>
  );

  const renderSchedule = ({ item }: { item: any }) => {
    const scheduledDate = new Date(item.scheduledDate);
    const isUpcoming = scheduledDate > new Date();
    const isPast = scheduledDate < new Date();
    
    return (
      <TouchableOpacity
        style={[styles.scheduleCard, isPast && styles.scheduleCardPast]}
        onPress={() => {
          // Cria um objeto Movie básico para navegação
          const movieObj: Movie = {
            id: item.movieId,
            title: item.movieTitle,
            poster_path: item.moviePoster,
            backdrop_path: '',
            overview: '',
            release_date: '',
            vote_average: 0,
            genre_ids: [],
            adult: false,
            original_language: '',
            original_title: item.movieTitle,
            popularity: 0,
            video: false,
            vote_count: 0,
          };
          handleMoviePress(movieObj);
        }}
      >
        <View style={styles.scheduleInfo}>
          <Text style={[styles.scheduleTitle, isPast && styles.scheduleTextPast]}>
            {item.movieTitle}
          </Text>
          <View style={styles.scheduleDateContainer}>
            <Ionicons 
              name={isUpcoming ? "calendar-outline" : "time-outline"} 
              size={16} 
              color={isPast ? Colors.textTertiary : Colors.secondary} 
            />
            <Text style={[styles.scheduleDate, isPast && styles.scheduleTextPast]}>
              {scheduledDate.toLocaleDateString('pt-BR', {
                day: '2-digit',
                month: '2-digit',
                year: 'numeric',
              })} às {scheduledDate.toLocaleTimeString('pt-BR', {
                hour: '2-digit',
                minute: '2-digit',
              })}
            </Text>
          </View>
          {item.notes && (
            <Text style={[styles.scheduleNotes, isPast && styles.scheduleTextPast]} numberOfLines={2}>
              {item.notes}
            </Text>
          )}
        </View>
        <View style={styles.scheduleStatus}>
          <Ionicons
            name={isUpcoming ? "alert-circle-outline" : "checkmark-circle"}
            size={24}
            color={isPast ? Colors.textTertiary : isUpcoming ? Colors.warning : Colors.success}
          />
        </View>
      </TouchableOpacity>
    );
  };

  const renderEmptyState = () => (
    <View style={styles.emptyState}>
      <Ionicons 
        name={getTabIcon(selectedTab)} 
        size={80} 
        color={Colors.textTertiary} 
      />
      <Text style={styles.emptyStateTitle}>
        {getEmptyStateTitle(selectedTab)}
      </Text>
      <Text style={styles.emptyStateSubtitle}>
        {getEmptyStateSubtitle(selectedTab)}
      </Text>
    </View>
  );

  const getTabIcon = (tab: MovieStatus) => {
    switch (tab) {
      case 'favorite':
        return 'heart-outline';
      case 'watched':
        return 'checkmark-circle-outline';
      case 'watchlist':
        return 'bookmark-outline';
      default:
        return 'film-outline';
    }
  };

  const getEmptyStateTitle = (tab: MovieStatus) => {
    switch (tab) {
      case 'favorite':
        return 'Nenhum favorito';
      case 'watched':
        return 'Nenhum filme assistido';
      case 'watchlist':
        return 'Lista vazia';
      default:
        return 'Nenhum filme';
    }
  };

  const getEmptyStateSubtitle = (tab: MovieStatus) => {
    switch (tab) {
      case 'favorite':
        return 'Marque filmes como favoritos para vê-los aqui';
      case 'watched':
        return 'Marque filmes como assistidos para acompanhar seu progresso';
      case 'watchlist':
        return 'Adicione filmes à sua lista para assistir depois';
      default:
        return 'Explore e adicione filmes às suas listas';
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      {/* Header */}
      <View style={styles.header}>
        <View style={styles.headerContent}>
          <Text style={styles.headerTitle}>Meu Perfil</Text>
          <View style={styles.headerActions}>
            <TouchableOpacity
              style={styles.toggleButton}
              onPress={() => setShowSchedules(!showSchedules)}
            >
              <Ionicons 
                name={showSchedules ? "list-outline" : "calendar-outline"} 
                size={20} 
                color={Colors.secondary} 
              />
              <Text style={styles.toggleButtonText}>
                {showSchedules ? 'Filmes' : 'Agendamentos'}
              </Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={styles.clearButton}
              onPress={handleClearAll}
            >
              <Ionicons name="trash-outline" size={24} color={Colors.error} />
            </TouchableOpacity>
          </View>
        </View>

        {/* Tabs - Only show when not showing schedules */}
        {!showSchedules && (
          <View style={styles.tabsContainer}>
          <TouchableOpacity
            style={[styles.tab, selectedTab === 'favorite' && styles.tabActive]}
            onPress={() => handleTabChange('favorite')}
          >
            <Ionicons
              name={selectedTab === 'favorite' ? 'heart' : 'heart-outline'}
              size={20}
              color={selectedTab === 'favorite' ? Colors.secondary : Colors.textSecondary}
            />
            <Text style={[
              styles.tabText,
              selectedTab === 'favorite' && styles.tabTextActive
            ]}>
              Favoritos
            </Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={[styles.tab, selectedTab === 'watched' && styles.tabActive]}
            onPress={() => handleTabChange('watched')}
          >
            <Ionicons
              name={selectedTab === 'watched' ? 'checkmark-circle' : 'checkmark-circle-outline'}
              size={20}
              color={selectedTab === 'watched' ? Colors.success : Colors.textSecondary}
            />
            <Text style={[
              styles.tabText,
              selectedTab === 'watched' && styles.tabTextActive
            ]}>
              Assistidos
            </Text>
          </TouchableOpacity>

          <TouchableOpacity
            style={[styles.tab, selectedTab === 'watchlist' && styles.tabActive]}
            onPress={() => handleTabChange('watchlist')}
          >
            <Ionicons
              name={selectedTab === 'watchlist' ? 'bookmark' : 'bookmark-outline'}
              size={20}
              color={selectedTab === 'watchlist' ? Colors.secondary : Colors.textSecondary}
            />
            <Text style={[
              styles.tabText,
              selectedTab === 'watchlist' && styles.tabTextActive
            ]}>
              Quero Ver
            </Text>
          </TouchableOpacity>
          </View>
        )}
      </View>

      {/* Content */}
      {showSchedules ? (
        <FlatList
          key="schedules-list"
          data={schedules}
          renderItem={renderSchedule}
          keyExtractor={(item) => item.id}
          contentContainerStyle={[
            styles.schedulesList,
            schedules.length === 0 ? styles.moviesListEmpty : null,
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
          ListEmptyComponent={() => (
            <View style={styles.emptyState}>
              <Ionicons name="calendar-outline" size={80} color={Colors.textTertiary} />
              <Text style={styles.emptyStateTitle}>Nenhum agendamento</Text>
              <Text style={styles.emptyStateSubtitle}>
                Agende filmes para assistir e veja os lembretes aqui
              </Text>
            </View>
          )}
        />
      ) : (
        <FlatList
          key="movies-list"
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
          ListEmptyComponent={renderEmptyState}
        />
      )}
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: Colors.background,
  },
  header: {
    paddingTop: Spacing.lg,
    backgroundColor: Colors.surface,
    borderBottomWidth: 1,
    borderBottomColor: Colors.surfaceLight,
  },
  headerContent: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingHorizontal: Spacing.md,
    paddingBottom: Spacing.md,
  },
  headerTitle: {
    fontSize: Typography.xl,
    fontWeight: Typography.bold,
    color: Colors.textPrimary,
  },
  clearButton: {
    padding: Spacing.sm,
  },
  tabsContainer: {
    flexDirection: 'row',
    paddingHorizontal: Spacing.md,
  },
  tab: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    paddingVertical: Spacing.sm,
    paddingHorizontal: Spacing.xs,
    borderBottomWidth: 2,
    borderBottomColor: 'transparent',
    gap: Spacing.xs,
  },
  tabActive: {
    borderBottomColor: Colors.secondary,
  },
  tabText: {
    fontSize: Typography.sm,
    color: Colors.textSecondary,
    fontWeight: Typography.medium,
  },
  tabTextActive: {
    color: Colors.secondary,
    fontWeight: Typography.semibold,
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
  headerActions: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  toggleButton: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: Colors.surface,
    paddingHorizontal: Spacing.sm,
    paddingVertical: Spacing.xs,
    borderRadius: 8,
    marginRight: Spacing.sm,
  },
  toggleButtonText: {
    fontSize: Typography.sm,
    color: Colors.secondary,
    marginLeft: Spacing.xs,
    fontWeight: Typography.medium,
  },
  schedulesList: {
    padding: Spacing.md,
  },
  scheduleCard: {
    backgroundColor: Colors.surface,
    borderRadius: 12,
    padding: Spacing.md,
    marginBottom: Spacing.md,
    flexDirection: 'row',
    alignItems: 'center',
    borderWidth: 1,
    borderColor: Colors.surfaceLight,
  },
  scheduleCardPast: {
    opacity: 0.6,
  },
  scheduleInfo: {
    flex: 1,
  },
  scheduleTitle: {
    fontSize: Typography.md,
    fontWeight: Typography.semibold,
    color: Colors.textPrimary,
    marginBottom: Spacing.xs,
  },
  scheduleDateContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: Spacing.xs,
  },
  scheduleDate: {
    fontSize: Typography.sm,
    color: Colors.textSecondary,
    marginLeft: Spacing.xs,
  },
  scheduleNotes: {
    fontSize: Typography.sm,
    color: Colors.textSecondary,
    fontStyle: 'italic',
  },
  scheduleStatus: {
    marginLeft: Spacing.md,
  },
  scheduleTextPast: {
    color: Colors.textTertiary,
  },
});