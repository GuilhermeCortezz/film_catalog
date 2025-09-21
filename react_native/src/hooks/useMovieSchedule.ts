import { useState, useEffect, useCallback } from 'react';
import { movieScheduleService, MovieSchedule } from '../services/movieScheduleService';
import { Movie } from '../types/movie';

export function useMovieSchedule(movieId?: number) {
  const [schedules, setSchedules] = useState<MovieSchedule[]>([]);
  const [currentMovieSchedule, setCurrentMovieSchedule] = useState<MovieSchedule | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Carrega todos os agendamentos
  const loadSchedules = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const allSchedules = await movieScheduleService.getAllSchedules();
      setSchedules(allSchedules);
    } catch (err) {
      setError('Erro ao carregar agendamentos');
      console.error('Error loading schedules:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  // Carrega agendamento específico de um filme
  const loadMovieSchedule = useCallback(async (id: number) => {
    try {
      const schedule = await movieScheduleService.getMovieSchedule(id);
      setCurrentMovieSchedule(schedule);
      return schedule;
    } catch (err) {
      console.error('Error loading movie schedule:', err);
      return null;
    }
  }, []);

  // Agenda um filme
  const scheduleMovie = useCallback(async (
    movie: Movie,
    date: Date,
    notes?: string,
    addToCalendar: boolean = true
  ): Promise<boolean> => {
    try {
      setLoading(true);
      setError(null);
      
      const schedule = await movieScheduleService.scheduleMovie(
        movie,
        date,
        notes,
        addToCalendar
      );
      
      setCurrentMovieSchedule(schedule);
      await loadSchedules(); // Recarrega a lista
      
      return true;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao agendar filme');
      console.error('Error scheduling movie:', err);
      return false;
    } finally {
      setLoading(false);
    }
  }, [loadSchedules]);

  // Remove agendamento
  const removeSchedule = useCallback(async (scheduleId: string): Promise<boolean> => {
    try {
      setLoading(true);
      setError(null);
      
      await movieScheduleService.removeSchedule(scheduleId);
      
      // Se for o agendamento atual, limpa
      if (currentMovieSchedule?.id === scheduleId) {
        setCurrentMovieSchedule(null);
      }
      
      await loadSchedules(); // Recarrega a lista
      
      return true;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao remover agendamento');
      console.error('Error removing schedule:', err);
      return false;
    } finally {
      setLoading(false);
    }
  }, [currentMovieSchedule, loadSchedules]);

  // Remove agendamento específico de um filme
  const removeMovieSchedule = useCallback(async (id: number): Promise<boolean> => {
    try {
      setLoading(true);
      setError(null);
      
      await movieScheduleService.removeMovieSchedule(id);
      
      if (currentMovieSchedule?.movieId === id) {
        setCurrentMovieSchedule(null);
      }
      
      await loadSchedules(); // Recarrega a lista
      
      return true;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao remover agendamento');
      console.error('Error removing movie schedule:', err);
      return false;
    } finally {
      setLoading(false);
    }
  }, [currentMovieSchedule, loadSchedules]);

  // Atualiza agendamento
  const updateSchedule = useCallback(async (
    scheduleId: string,
    newDate: Date,
    notes?: string
  ): Promise<boolean> => {
    try {
      setLoading(true);
      setError(null);
      
      const updatedSchedule = await movieScheduleService.updateSchedule(
        scheduleId,
        newDate,
        notes
      );
      
      if (currentMovieSchedule?.id === scheduleId) {
        setCurrentMovieSchedule(updatedSchedule);
      }
      
      await loadSchedules(); // Recarrega a lista
      
      return true;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Erro ao atualizar agendamento');
      console.error('Error updating schedule:', err);
      return false;
    } finally {
      setLoading(false);
    }
  }, [currentMovieSchedule, loadSchedules]);

  // Carrega agendamentos próximos
  const getUpcomingSchedules = useCallback(async (hoursAhead: number = 24) => {
    try {
      return await movieScheduleService.getUpcomingSchedules(hoursAhead);
    } catch (err) {
      console.error('Error getting upcoming schedules:', err);
      return [];
    }
  }, []);

  // Verifica se um filme está agendado
  const isMovieScheduled = useCallback((id: number): boolean => {
    return schedules.some(schedule => schedule.movieId === id);
  }, [schedules]);

  // Carrega dados iniciais
  useEffect(() => {
    loadSchedules();
  }, [loadSchedules]);

  // Carrega agendamento do filme específico quando movieId muda
  useEffect(() => {
    if (movieId) {
      loadMovieSchedule(movieId);
    }
  }, [movieId, loadMovieSchedule]);

  return {
    // Estados
    schedules,
    currentMovieSchedule,
    loading,
    error,
    
    // Ações
    scheduleMovie,
    removeSchedule,
    removeMovieSchedule,
    updateSchedule,
    loadSchedules,
    loadMovieSchedule,
    getUpcomingSchedules,
    
    // Helpers
    isMovieScheduled,
  };
}