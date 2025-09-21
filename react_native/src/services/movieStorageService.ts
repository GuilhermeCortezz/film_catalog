import AsyncStorage from '@react-native-async-storage/async-storage';
import { Movie } from '../types/movie';

export type MovieStatus = 'favorite' | 'watched' | 'watchlist';

interface MovieStorage {
  id: number;
  movie: Movie;
  status: MovieStatus[];
  dateAdded: string;
}

class MovieStorageService {
  private readonly STORAGE_KEY = '@movie_catalog_storage';

  async getMovieStorage(): Promise<MovieStorage[]> {
    try {
      const data = await AsyncStorage.getItem(this.STORAGE_KEY);
      return data ? JSON.parse(data) : [];
    } catch (error) {
      console.error('Error getting movie storage:', error);
      return [];
    }
  }

  async saveMovieStorage(storage: MovieStorage[]): Promise<void> {
    try {
      await AsyncStorage.setItem(this.STORAGE_KEY, JSON.stringify(storage));
    } catch (error) {
      console.error('Error saving movie storage:', error);
    }
  }

  async addMovieStatus(movie: Movie, status: MovieStatus): Promise<void> {
    const storage = await this.getMovieStorage();
    const existingIndex = storage.findIndex(item => item.id === movie.id);

    if (existingIndex >= 0) {
      // Movie exists, add status if not already present
      if (!storage[existingIndex].status.includes(status)) {
        storage[existingIndex].status.push(status);
      }
    } else {
      // New movie
      storage.push({
        id: movie.id,
        movie,
        status: [status],
        dateAdded: new Date().toISOString(),
      });
    }

    await this.saveMovieStorage(storage);
  }

  async removeMovieStatus(movieId: number, status: MovieStatus): Promise<void> {
    const storage = await this.getMovieStorage();
    const existingIndex = storage.findIndex(item => item.id === movieId);

    if (existingIndex >= 0) {
      storage[existingIndex].status = storage[existingIndex].status.filter(
        s => s !== status
      );

      // Remove movie completely if no status left
      if (storage[existingIndex].status.length === 0) {
        storage.splice(existingIndex, 1);
      }

      await this.saveMovieStorage(storage);
    }
  }

  async getMovieStatus(movieId: number): Promise<MovieStatus[]> {
    const storage = await this.getMovieStorage();
    const movie = storage.find(item => item.id === movieId);
    return movie ? movie.status : [];
  }

  async hasMovieStatus(movieId: number, status: MovieStatus): Promise<boolean> {
    const statuses = await this.getMovieStatus(movieId);
    return statuses.includes(status);
  }

  async toggleMovieStatus(movie: Movie, status: MovieStatus): Promise<boolean> {
    const hasStatus = await this.hasMovieStatus(movie.id, status);
    
    if (hasStatus) {
      await this.removeMovieStatus(movie.id, status);
      return false;
    } else {
      await this.addMovieStatus(movie, status);
      return true;
    }
  }

  async getFavoriteMovies(): Promise<Movie[]> {
    const storage = await this.getMovieStorage();
    return storage
      .filter(item => item.status.includes('favorite'))
      .map(item => item.movie);
  }

  async getWatchedMovies(): Promise<Movie[]> {
    const storage = await this.getMovieStorage();
    return storage
      .filter(item => item.status.includes('watched'))
      .map(item => item.movie);
  }

  async getWatchlistMovies(): Promise<Movie[]> {
    const storage = await this.getMovieStorage();
    return storage
      .filter(item => item.status.includes('watchlist'))
      .map(item => item.movie);
  }

  async getMoviesByStatus(status: MovieStatus): Promise<Movie[]> {
    switch (status) {
      case 'favorite':
        return this.getFavoriteMovies();
      case 'watched':
        return this.getWatchedMovies();
      case 'watchlist':
        return this.getWatchlistMovies();
      default:
        return [];
    }
  }

  async clearAllData(): Promise<void> {
    try {
      await AsyncStorage.removeItem(this.STORAGE_KEY);
    } catch (error) {
      console.error('Error clearing movie storage:', error);
    }
  }
}

export const movieStorageService = new MovieStorageService();