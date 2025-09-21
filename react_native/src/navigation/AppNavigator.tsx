import React from 'react';
import { NavigationContainer } from '@react-navigation/native';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { createStackNavigator } from '@react-navigation/stack';
import { Ionicons } from '@expo/vector-icons';
import { SafeAreaView } from 'react-native-safe-area-context';

// Screens
import HomeScreen from '../screens/HomeScreen';
import SearchScreen from '../screens/SearchScreen';
import MovieDetailsScreen from '../screens/MovieDetailsScreen';
import SectionScreen from '../screens/SectionScreen';
import ProfileScreen from '../screens/ProfileScreen';
import CategoriesScreen from '../screens/CategoriesScreen';

// Theme
import { Colors, Typography } from '../constants/theme';

// Types
import { Movie } from '../types/movie';

export type RootStackParamList = {
  MainTabs: undefined;
  MovieDetails: { movie: Movie };
  Section: { 
    title: string; 
    endpoint: 'popular' | 'top_rated' | 'now_playing' | 'upcoming';
  };
  Categories: {
    genreId?: number;
    genreName?: string;
  };
};

export type TabParamList = {
  Home: undefined;
  Search: undefined;
  Profile: undefined;
};

const Stack = createStackNavigator<RootStackParamList>();
const Tab = createBottomTabNavigator<TabParamList>();

function TabNavigator() {
  return (
    <SafeAreaView style={{ flex: 1, backgroundColor: Colors.background }}>
      <Tab.Navigator
        screenOptions={({ route }) => ({
        tabBarIcon: ({ focused, color, size }) => {
          let iconName: keyof typeof Ionicons.glyphMap;

          switch (route.name) {
            case 'Home':
              iconName = focused ? 'home' : 'home-outline';
              break;
            case 'Search':
              iconName = focused ? 'search' : 'search-outline';
              break;
            case 'Profile':
              iconName = focused ? 'person' : 'person-outline';
              break;
            default:
              iconName = 'home-outline';
          }

          return <Ionicons name={iconName} size={size} color={color} />;
        },
        tabBarActiveTintColor: Colors.secondary,
        tabBarInactiveTintColor: Colors.textTertiary,
        tabBarStyle: {
          backgroundColor: Colors.surface,
          borderTopColor: Colors.surfaceLight,
          borderTopWidth: 1,
          height: 60,
          paddingBottom: 8,
          paddingTop: 8,
        },
        tabBarLabelStyle: {
          fontSize: Typography.xs,
          fontWeight: Typography.medium,
        },
        headerShown: false,
      })}
    >
      <Tab.Screen 
        name="Home" 
        component={HomeScreen}
        options={{ tabBarLabel: 'Início' }}
      />
      <Tab.Screen 
        name="Search" 
        component={SearchScreen}
        options={{ tabBarLabel: 'Buscar' }}
      />
      <Tab.Screen 
        name="Profile" 
        component={ProfileScreen}
        options={{ tabBarLabel: 'Perfil' }}
      />
    </Tab.Navigator>
    </SafeAreaView>
  );
}

export default function AppNavigator() {
  return (
    <NavigationContainer>
      <Stack.Navigator
        screenOptions={{
          headerStyle: {
            backgroundColor: Colors.primary,
            elevation: 0,
            shadowOpacity: 0,
          },
          headerTintColor: Colors.textPrimary,
          headerTitleStyle: {
            fontSize: Typography.lg,
            fontWeight: Typography.semibold,
          },
          cardStyle: {
            backgroundColor: Colors.background,
          },
        }}
      >
        <Stack.Screen 
          name="MainTabs" 
          component={TabNavigator}
          options={{ headerShown: false }}
        />
        <Stack.Screen 
          name="MovieDetails" 
          component={MovieDetailsScreen}
          options={{ 
            title: 'Detalhes do Filme',
            headerTransparent: true,
            headerTitle: '',
          }}
        />
        <Stack.Screen 
          name="Section" 
          component={SectionScreen}
          options={({ route }) => ({ 
            title: route.params.title,
          })}
        />
        <Stack.Screen 
          name="Categories" 
          component={CategoriesScreen}
          options={{ 
            title: 'Categorias',
            headerShown: false,
          }}
        />
      </Stack.Navigator>
    </NavigationContainer>
  );
}