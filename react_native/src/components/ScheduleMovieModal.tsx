import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  Modal,
  TouchableOpacity,
  TextInput,
  Platform,
  Alert,
  ScrollView,
} from 'react-native';
import { Ionicons } from '@expo/vector-icons';
import DateTimePicker from '@react-native-community/datetimepicker';

// Constants
import { Colors, Typography, Spacing, Shadows } from '../constants/theme';

// Types
import { Movie } from '../types/movie';
import { MovieSchedule } from '../services/movieScheduleService';

interface ScheduleMovieModalProps {
  visible: boolean;
  movie: Movie | null;
  existingSchedule?: MovieSchedule | null;
  onClose: () => void;
  onSchedule: (date: Date, notes?: string, addToCalendar?: boolean) => Promise<boolean>;
  onUpdate?: (scheduleId: string, date: Date, notes?: string) => Promise<boolean>;
  onRemove?: (scheduleId: string) => Promise<boolean>;
  loading?: boolean;
}

export default function ScheduleMovieModal({
  visible,
  movie,
  existingSchedule,
  onClose,
  onSchedule,
  onUpdate,
  onRemove,
  loading = false,
}: ScheduleMovieModalProps) {
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [notes, setNotes] = useState('');
  const [addToCalendar, setAddToCalendar] = useState(true);
  const [showDatePicker, setShowDatePicker] = useState(false);
  const [showTimePicker, setShowTimePicker] = useState(false);
  const [isEditing, setIsEditing] = useState(false);

  // Reseta o modal quando abrir/fechar
  useEffect(() => {
    if (visible) {
      if (existingSchedule) {
        setSelectedDate(new Date(existingSchedule.scheduledDate));
        setNotes(existingSchedule.notes || '');
        setIsEditing(true);
      } else {
        // Define data padrão para amanhã às 20:00
        const tomorrow = new Date();
        tomorrow.setDate(tomorrow.getDate() + 1);
        tomorrow.setHours(20, 0, 0, 0);
        setSelectedDate(tomorrow);
        setNotes('');
        setIsEditing(false);
      }
      setAddToCalendar(true);
    }
  }, [visible, existingSchedule]);

  const handleDateChange = (event: any, date?: Date) => {
    setShowDatePicker(Platform.OS === 'ios');
    if (date) {
      setSelectedDate(date);
    }
  };

  const handleTimeChange = (event: any, time?: Date) => {
    setShowTimePicker(Platform.OS === 'ios');
    if (time) {
      const newDate = new Date(selectedDate);
      newDate.setHours(time.getHours(), time.getMinutes());
      setSelectedDate(newDate);
    }
  };

  const handleSchedule = async () => {
    if (!movie) return;

    // Valida se a data não é no passado
    if (selectedDate < new Date()) {
      Alert.alert('Data inválida', 'Por favor, selecione uma data e hora futuras.');
      return;
    }

    try {
      let success = false;

      if (isEditing && existingSchedule && onUpdate) {
        success = await onUpdate(existingSchedule.id, selectedDate, notes);
      } else {
        success = await onSchedule(selectedDate, notes, addToCalendar);
      }

      if (success) {
        onClose();
      }
    } catch (error) {
      console.error('Error scheduling movie:', error);
    }
  };

  const handleRemove = async () => {
    if (!existingSchedule || !onRemove) return;

    Alert.alert(
      'Remover Agendamento',
      'Tem certeza que deseja remover este agendamento? Isso também removerá o evento do calendário.',
      [
        { text: 'Cancelar', style: 'cancel' },
        {
          text: 'Remover',
          style: 'destructive',
          onPress: async () => {
            const success = await onRemove(existingSchedule.id);
            if (success) {
              onClose();
            }
          },
        },
      ]
    );
  };

  const formatDate = (date: Date): string => {
    return date.toLocaleDateString('pt-BR', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    });
  };

  const formatTime = (date: Date): string => {
    return date.toLocaleTimeString('pt-BR', {
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  if (!movie) return null;

  return (
    <Modal
      visible={visible}
      animationType="slide"
      presentationStyle="pageSheet"
      onRequestClose={onClose}
    >
      <View style={styles.container}>
        {/* Header */}
        <View style={styles.header}>
          <TouchableOpacity onPress={onClose} style={styles.closeButton}>
            <Ionicons name="close" size={24} color={Colors.textPrimary} />
          </TouchableOpacity>
          
          <Text style={styles.headerTitle}>
            {isEditing ? 'Editar Agendamento' : 'Agendar Filme'}
          </Text>
          
          {isEditing && (
            <TouchableOpacity onPress={handleRemove} style={styles.deleteButton}>
              <Ionicons name="trash-outline" size={24} color={Colors.error} />
            </TouchableOpacity>
          )}
        </View>

        <ScrollView style={styles.content} showsVerticalScrollIndicator={false}>
          {/* Movie Info */}
          <View style={styles.movieInfo}>
            <Text style={styles.movieTitle}>{movie.title}</Text>
            <Text style={styles.movieSubtitle}>
              {movie.release_date && new Date(movie.release_date).getFullYear()}
            </Text>
          </View>

          {/* Date Selection */}
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>Data</Text>
            <TouchableOpacity
              style={styles.dateButton}
              onPress={() => setShowDatePicker(true)}
            >
              <Ionicons name="calendar-outline" size={20} color={Colors.secondary} />
              <Text style={styles.dateButtonText}>{formatDate(selectedDate)}</Text>
              <Ionicons name="chevron-forward" size={16} color={Colors.textSecondary} />
            </TouchableOpacity>
          </View>

          {/* Time Selection */}
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>Horário</Text>
            <TouchableOpacity
              style={styles.dateButton}
              onPress={() => setShowTimePicker(true)}
            >
              <Ionicons name="time-outline" size={20} color={Colors.secondary} />
              <Text style={styles.dateButtonText}>{formatTime(selectedDate)}</Text>
              <Ionicons name="chevron-forward" size={16} color={Colors.textSecondary} />
            </TouchableOpacity>
          </View>

          {/* Notes */}
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>Notas (opcional)</Text>
            <TextInput
              style={styles.notesInput}
              value={notes}
              onChangeText={setNotes}
              placeholder="Ex: Assistir com amigos, preparar pipoca..."
              placeholderTextColor={Colors.textSecondary}
              multiline
              numberOfLines={3}
              maxLength={200}
            />
            <Text style={styles.charCount}>{notes.length}/200</Text>
          </View>

          {/* Calendar Option */}
          {!isEditing && (
            <View style={styles.section}>
              <TouchableOpacity
                style={styles.toggleOption}
                onPress={() => setAddToCalendar(!addToCalendar)}
              >
                <View style={styles.toggleLeft}>
                  <Ionicons 
                    name="calendar" 
                    size={20} 
                    color={addToCalendar ? Colors.secondary : Colors.textSecondary} 
                  />
                  <View style={styles.toggleText}>
                    <Text style={styles.toggleTitle}>Adicionar ao Calendário</Text>
                    <Text style={styles.toggleSubtitle}>
                      Cria um lembrete no calendário do seu celular
                    </Text>
                  </View>
                </View>
                <Ionicons
                  name={addToCalendar ? 'checkbox' : 'square-outline'}
                  size={24}
                  color={addToCalendar ? Colors.secondary : Colors.textSecondary}
                />
              </TouchableOpacity>
            </View>
          )}
        </ScrollView>

        {/* Actions */}
        <View style={styles.actions}>
          <TouchableOpacity
            style={[styles.scheduleButton, loading && styles.scheduleButtonDisabled]}
            onPress={handleSchedule}
            disabled={loading}
          >
            <Text style={styles.scheduleButtonText}>
              {loading ? 'Salvando...' : isEditing ? 'Atualizar' : 'Agendar'}
            </Text>
          </TouchableOpacity>
        </View>

        {/* Date/Time Pickers */}
        {showDatePicker && (
          <DateTimePicker
            value={selectedDate}
            mode="date"
            display="default"
            onChange={handleDateChange}
            minimumDate={new Date()}
          />
        )}

        {showTimePicker && (
          <DateTimePicker
            value={selectedDate}
            mode="time"
            display="default"
            onChange={handleTimeChange}
          />
        )}
      </View>
    </Modal>
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
    justifyContent: 'space-between',
    paddingHorizontal: Spacing.md,
    paddingTop: 50,
    paddingBottom: Spacing.md,
    borderBottomWidth: 1,
    borderBottomColor: Colors.surfaceLight,
  },
  closeButton: {
    padding: Spacing.xs,
  },
  headerTitle: {
    fontSize: Typography.lg,
    fontWeight: Typography.semibold,
    color: Colors.textPrimary,
  },
  deleteButton: {
    padding: Spacing.xs,
  },
  content: {
    flex: 1,
    paddingHorizontal: Spacing.md,
  },
  movieInfo: {
    paddingVertical: Spacing.lg,
    alignItems: 'center',
    borderBottomWidth: 1,
    borderBottomColor: Colors.surfaceLight,
    marginBottom: Spacing.lg,
  },
  movieTitle: {
    fontSize: Typography.lg,
    fontWeight: Typography.semibold,
    color: Colors.textPrimary,
    textAlign: 'center',
    marginBottom: Spacing.xs,
  },
  movieSubtitle: {
    fontSize: Typography.sm,
    color: Colors.textSecondary,
  },
  section: {
    marginBottom: Spacing.lg,
  },
  sectionTitle: {
    fontSize: Typography.md,
    fontWeight: Typography.semibold,
    color: Colors.textPrimary,
    marginBottom: Spacing.sm,
  },
  dateButton: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: Colors.surface,
    paddingVertical: Spacing.md,
    paddingHorizontal: Spacing.md,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: Colors.surfaceLight,
  },
  dateButtonText: {
    flex: 1,
    fontSize: Typography.md,
    color: Colors.textPrimary,
    marginLeft: Spacing.sm,
  },
  notesInput: {
    backgroundColor: Colors.surface,
    borderWidth: 1,
    borderColor: Colors.surfaceLight,
    borderRadius: 12,
    paddingVertical: Spacing.md,
    paddingHorizontal: Spacing.md,
    fontSize: Typography.md,
    color: Colors.textPrimary,
    textAlignVertical: 'top',
    minHeight: 80,
  },
  charCount: {
    fontSize: Typography.xs,
    color: Colors.textSecondary,
    textAlign: 'right',
    marginTop: Spacing.xs,
  },
  toggleOption: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: Colors.surface,
    paddingVertical: Spacing.md,
    paddingHorizontal: Spacing.md,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: Colors.surfaceLight,
  },
  toggleLeft: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
  },
  toggleText: {
    marginLeft: Spacing.sm,
  },
  toggleTitle: {
    fontSize: Typography.md,
    fontWeight: Typography.medium,
    color: Colors.textPrimary,
  },
  toggleSubtitle: {
    fontSize: Typography.sm,
    color: Colors.textSecondary,
    marginTop: Spacing.xs,
  },
  actions: {
    padding: Spacing.md,
    borderTopWidth: 1,
    borderTopColor: Colors.surfaceLight,
  },
  scheduleButton: {
    backgroundColor: Colors.secondary,
    paddingVertical: Spacing.md,
    borderRadius: 12,
    alignItems: 'center',
    ...Shadows.medium,
  },
  scheduleButtonDisabled: {
    opacity: 0.6,
  },
  scheduleButtonText: {
    fontSize: Typography.md,
    fontWeight: Typography.semibold,
    color: Colors.textPrimary,
  },
});