import AsyncStorage from '@react-native-async-storage/async-storage';
import * as Calendar from 'expo-calendar';
import { Platform, Alert } from 'react-native';
import { Movie } from '../types/movie';

export interface MovieSchedule {
  id: string;
  movieId: number;
  movieTitle: string;
  moviePoster: string;
  scheduledDate: string; // ISO string
  notes?: string;
  calendarEventId?: string;
  createdAt: string;
}

const STORAGE_KEY = '@movie_schedules';

class MovieScheduleService {
  private async getCalendarPermission(): Promise<boolean> {
    try {
      const { status } = await Calendar.requestCalendarPermissionsAsync();
      return status === 'granted';
    } catch (error) {
      console.error('Error requesting calendar permission:', error);
      return false;
    }
  }

  private async getDefaultCalendar(): Promise<string | null> {
    try {
      const calendars = await Calendar.getCalendarsAsync(Calendar.EntityTypes.EVENT);
      
      // Procura pelo calendário padrão ou primeiro disponível
      const defaultCalendar = calendars.find(cal => cal.isPrimary) || calendars[0];
      
      return defaultCalendar?.id || null;
    } catch (error) {
      console.error('Error getting calendars:', error);
      return null;
    }
  }

  async getAllSchedules(): Promise<MovieSchedule[]> {
    try {
      const schedulesJson = await AsyncStorage.getItem(STORAGE_KEY);
      if (!schedulesJson) return [];
      
      const schedules = JSON.parse(schedulesJson) as MovieSchedule[];
      
      // Filtra agendamentos passados (opcional - pode manter histórico)
      return schedules.sort((a, b) => 
        new Date(a.scheduledDate).getTime() - new Date(b.scheduledDate).getTime()
      );
    } catch (error) {
      console.error('Error loading schedules:', error);
      return [];
    }
  }

  async getMovieSchedule(movieId: number): Promise<MovieSchedule | null> {
    try {
      const schedules = await this.getAllSchedules();
      return schedules.find(schedule => schedule.movieId === movieId) || null;
    } catch (error) {
      console.error('Error getting movie schedule:', error);
      return null;
    }
  }

  async scheduleMovie(
    movie: Movie,
    scheduledDate: Date,
    notes?: string,
    addToCalendar: boolean = true
  ): Promise<MovieSchedule> {
    try {
      const scheduleId = `schedule_${movie.id}_${Date.now()}`;
      let calendarEventId: string | undefined;

      // Adiciona ao calendário do dispositivo se solicitado
      if (addToCalendar) {
        const hasPermission = await this.getCalendarPermission();
        
        if (hasPermission) {
          const calendarId = await this.getDefaultCalendar();
          
          if (calendarId) {
            const endDate = new Date(scheduledDate.getTime() + 2 * 60 * 60 * 1000); // 2 horas depois
            
            try {
              calendarEventId = await Calendar.createEventAsync(calendarId, {
                title: `Assistir: ${movie.title}`,
                startDate: scheduledDate,
                endDate: endDate,
                notes: notes || `Filme agendado através do app de catálogo.\n\nSinopse: ${movie.overview || 'Não disponível'}`,
                location: 'Casa', // Pode ser customizado
                alarms: [
                  { relativeOffset: -30 }, // 30 minutos antes
                  { relativeOffset: -60 * 24 }, // 1 dia antes
                ],
              });
              
            } catch (calendarError) {
              console.error('Error creating calendar event:', calendarError);
              Alert.alert(
                'Erro no Calendário',
                'Não foi possível adicionar o evento ao calendário, mas o agendamento foi salvo no app.'
              );
            }
          } else {
            Alert.alert(
              'Calendário não encontrado',
              'Não foi possível encontrar um calendário para adicionar o evento.'
            );
          }
        } else {
          Alert.alert(
            'Permissão negada',
            'Para adicionar o lembrete ao calendário, é necessário permitir o acesso ao calendário nas configurações.'
          );
        }
      }

      const schedule: MovieSchedule = {
        id: scheduleId,
        movieId: movie.id,
        movieTitle: movie.title,
        moviePoster: movie.poster_path || '',
        scheduledDate: scheduledDate.toISOString(),
        notes,
        calendarEventId,
        createdAt: new Date().toISOString(),
      };

      // Salva no AsyncStorage
      const schedules = await this.getAllSchedules();
      
      // Remove agendamento anterior do mesmo filme se existir
      const filteredSchedules = schedules.filter(s => s.movieId !== movie.id);
      const updatedSchedules = [...filteredSchedules, schedule];

      await AsyncStorage.setItem(STORAGE_KEY, JSON.stringify(updatedSchedules));
      
      return schedule;
    } catch (error) {
      console.error('Error scheduling movie:', error);
      throw new Error('Erro ao agendar filme. Tente novamente.');
    }
  }

  async removeSchedule(scheduleId: string): Promise<void> {
    try {
      const schedules = await this.getAllSchedules();
      const schedule = schedules.find(s => s.id === scheduleId);
      
      if (schedule?.calendarEventId) {
        try {
          await Calendar.deleteEventAsync(schedule.calendarEventId);
        } catch (error) {
          console.error('Error deleting calendar event:', error);
        }
      }

      const updatedSchedules = schedules.filter(s => s.id !== scheduleId);
      await AsyncStorage.setItem(STORAGE_KEY, JSON.stringify(updatedSchedules));
    } catch (error) {
      console.error('Error removing schedule:', error);
      throw new Error('Erro ao remover agendamento.');
    }
  }

  async removeMovieSchedule(movieId: number): Promise<void> {
    try {
      const schedule = await this.getMovieSchedule(movieId);
      if (schedule) {
        await this.removeSchedule(schedule.id);
      }
    } catch (error) {
      console.error('Error removing movie schedule:', error);
      throw new Error('Erro ao remover agendamento do filme.');
    }
  }

  async updateSchedule(
    scheduleId: string,
    newDate: Date,
    notes?: string
  ): Promise<MovieSchedule> {
    try {
      const schedules = await this.getAllSchedules();
      const scheduleIndex = schedules.findIndex(s => s.id === scheduleId);
      
      if (scheduleIndex === -1) {
        throw new Error('Agendamento não encontrado');
      }

      const schedule = schedules[scheduleIndex];
      
      // Atualiza evento no calendário se existir
      if (schedule.calendarEventId) {
        try {
          const endDate = new Date(newDate.getTime() + 2 * 60 * 60 * 1000);
          
          await Calendar.updateEventAsync(schedule.calendarEventId, {
            startDate: newDate,
            endDate: endDate,
            notes: notes || schedule.notes,
          });
        } catch (error) {
          console.error('Error updating calendar event:', error);
        }
      }

      // Atualiza no AsyncStorage
      const updatedSchedule: MovieSchedule = {
        ...schedule,
        scheduledDate: newDate.toISOString(),
        notes: notes || schedule.notes,
      };

      schedules[scheduleIndex] = updatedSchedule;
      await AsyncStorage.setItem(STORAGE_KEY, JSON.stringify(schedules));
      return updatedSchedule;
    } catch (error) {
      console.error('Error updating schedule:', error);
      throw new Error('Erro ao atualizar agendamento.');
    }
  }

  // Método para verificar agendamentos próximos (pode ser usado para notificações locais)
  async getUpcomingSchedules(hoursAhead: number = 24): Promise<MovieSchedule[]> {
    try {
      const schedules = await this.getAllSchedules();
      const now = new Date();
      const futureTime = new Date(now.getTime() + hoursAhead * 60 * 60 * 1000);

      return schedules.filter(schedule => {
        const scheduledTime = new Date(schedule.scheduledDate);
        return scheduledTime >= now && scheduledTime <= futureTime;
      });
    } catch (error) {
      console.error('Error getting upcoming schedules:', error);
      return [];
    }
  }
}

export const movieScheduleService = new MovieScheduleService();