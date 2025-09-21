import { useState, useEffect, useCallback } from 'react';
import { movieStorageService, MovieStatus } from '../services/movieStorageService';
import { Movie } from '../types/movie';

export const useMovieStorage = (movieId?: number) => {
  const [statuses, setStatuses] = useState<MovieStatus[]>([]);
  const [loading, setLoading] = useState(false);

  const loadMovieStatus = useCallback(async () => {
    if (!movieId) return;
    
    try {
      setLoading(true);
      const movieStatuses = await movieStorageService.getMovieStatus(movieId);
      setStatuses(movieStatuses);
    } catch (error) {
      console.error('Error loading movie status:', error);
    } finally {
      setLoading(false);
    }
  }, [movieId]);

  const toggleStatus = useCallback(async (movie: Movie, status: MovieStatus) => {
    try {
      setLoading(true);
      const newValue = await movieStorageService.toggleMovieStatus(movie, status);
      await loadMovieStatus();
      return newValue;
    } catch (error) {
      console.error('Error toggling movie status:', error);
      return false;
    } finally {
      setLoading(false);
    }
  }, [loadMovieStatus]);

  const hasStatus = useCallback((status: MovieStatus) => {
    return statuses.includes(status);
  }, [statuses]);

  useEffect(() => {
    loadMovieStatus();
  }, [loadMovieStatus]);

  return {
    statuses,
    loading,
    hasStatus,
    toggleStatus,
    refresh: loadMovieStatus,
  };
};

export const useMoviesByStatus = (status: MovieStatus) => {
  const [movies, setMovies] = useState<Movie[]>([]);
  const [loading, setLoading] = useState(false);

  const loadMovies = useCallback(async () => {
    try {
      setLoading(true);
      const movieList = await movieStorageService.getMoviesByStatus(status);
      setMovies(movieList);
    } catch (error) {
      console.error(`Error loading ${status} movies:`, error);
    } finally {
      setLoading(false);
    }
  }, [status]);

  useEffect(() => {
    loadMovies();
  }, [loadMovies]);

  return {
    movies,
    loading,
    refresh: loadMovies,
  };
};