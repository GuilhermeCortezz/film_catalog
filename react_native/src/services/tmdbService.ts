import { 
  MoviesResponse, 
  DiscoverMovieParams, 
  GenresResponse, 
  MovieDetails, 
  SearchMovieParams 
} from '../types/movie';

const API_BASE_URL = 'https://api.themoviedb.org/3';
const API_TOKEN = 'eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJiZmUwMWE1Y2ZmYzBmNTY3MDZhYmM1NmU4YzNlNzA4ZCIsIm5iZiI6MTc1ODMzNDY5Mi4xNjQsInN1YiI6IjY4Y2UwZWU0MWVjNDQzYjEwMGNkNmU1ZSIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.3Q1ybsIUmSvSikNED11dtMIQ-beSVXfJOMZE-jO8HKg';

class TMDBService {
  private readonly headers = {
    accept: 'application/json',
    Authorization: `Bearer ${API_TOKEN}`,
  };

  private readonly defaultLanguage = 'pt-BR';
  private readonly defaultRegion = 'BR';

  private buildQueryString(params: Record<string, any>): string {
    const searchParams = new URLSearchParams();
    
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        searchParams.append(key, value.toString());
      }
    });
    
    return searchParams.toString();
  }

  private async makeRequest<T>(endpoint: string, params?: Record<string, any>): Promise<T> {
    const defaultParams = {
      language: this.defaultLanguage,
      ...params,
    };

    const queryString = this.buildQueryString(defaultParams);
    const url = `${API_BASE_URL}${endpoint}${queryString ? `?${queryString}` : ''}`;
    
    console.log('🔗 API Request:', url);
    
    try {
      const response = await fetch(url, {
        method: 'GET',
        headers: this.headers,
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const data = await response.json();
      console.log('✅ API Response received');
      return data;
    } catch (error) {
      console.error('❌ API Error:', error);
      throw error;
    }
  }

  /**
   * Get the list of official genres for movies
   */
  async getGenres(): Promise<GenresResponse> {
    return this.makeRequest<GenresResponse>('/genre/movie/list');
  }

  /**
   * Discover movies using over 30 filters and sort options
   */
  async discoverMovies(params: DiscoverMovieParams = {}): Promise<MoviesResponse> {
    const defaultParams = {
      include_adult: false,
      include_video: false,
      page: 1,
      sort_by: 'popularity.desc' as const,
      region: this.defaultRegion,
    };

    const finalParams = { ...defaultParams, ...params };
    return this.makeRequest<MoviesResponse>('/discover/movie', finalParams);
  }

  /**
   * Get a list of movies that are currently in theatres
   */
  async getNowPlayingMovies(page: number = 1): Promise<MoviesResponse> {
    return this.makeRequest<MoviesResponse>('/movie/now_playing', {
      page,
      region: this.defaultRegion,
    });
  }

  /**
   * Get a list of movies ordered by popularity
   */
  async getPopularMovies(page: number = 1): Promise<MoviesResponse> {
    return this.makeRequest<MoviesResponse>('/movie/popular', {
      page,
      region: this.defaultRegion,
    });
  }

  /**
   * Get a list of movies ordered by rating
   */
  async getTopRatedMovies(page: number = 1): Promise<MoviesResponse> {
    return this.makeRequest<MoviesResponse>('/movie/top_rated', {
      page,
      region: this.defaultRegion,
    });
  }

  /**
   * Get a list of movies that are being released soon
   */
  async getUpcomingMovies(page: number = 1): Promise<MoviesResponse> {
    return this.makeRequest<MoviesResponse>('/movie/upcoming', {
      page,
      region: this.defaultRegion,
    });
  }

  /**
   * Get the top level details of a movie by ID
   */
  async getMovieDetails(movieId: number, appendToResponse?: string): Promise<MovieDetails> {
    const params: Record<string, any> = {};
    if (appendToResponse) {
      params.append_to_response = appendToResponse;
    }
    
    return this.makeRequest<MovieDetails>(`/movie/${movieId}`, params);
  }

  /**
   * Search for movies by their original, translated and alternative titles
   */
  async searchMovies(params: SearchMovieParams): Promise<MoviesResponse> {
    const defaultParams = {
      include_adult: false,
      page: 1,
    };

    const finalParams = { ...defaultParams, ...params };
    return this.makeRequest<MoviesResponse>('/search/movie', finalParams);
  }

  /**
   * Get movies by genre (convenience method)
   */
  async getMoviesByGenre(genreId: number, page: number = 1): Promise<MoviesResponse> {
    return this.discoverMovies({ 
      with_genres: genreId.toString(),
      page 
    });
  }

  /**
   * Get movies by year (convenience method)
   */
  async getMoviesByYear(year: number, page: number = 1): Promise<MoviesResponse> {
    return this.discoverMovies({
      year,
      page
    });
  }

  /**
   * Get highly rated movies with minimum vote count (convenience method)
   */
  async getHighlyRatedMovies(page: number = 1, minVoteCount: number = 1000): Promise<MoviesResponse> {
    return this.discoverMovies({ 
      sort_by: 'vote_average.desc',
      'vote_count.gte': minVoteCount,
      page 
    });
  }

  /**
   * Get image URL for posters and backdrops
   */
  getImageUrl(path: string | null, size: 'w200' | 'w500' | 'w780' | 'original' = 'w500'): string | null {
    if (!path) return null;
    return `https://image.tmdb.org/t/p/${size}${path}`;
  }

  /**
   * Format release date to Brazilian format
   */
  formatBrazilianDate(dateString: string): string {
    if (!dateString) return 'Data não disponível';
    
    try {
      const date = new Date(dateString);
      return date.toLocaleDateString('pt-BR');
    } catch (error) {
      return dateString;
    }
  }

  /**
   * Format runtime to hours and minutes in Portuguese
   */
  formatRuntime(minutes: number): string {
    if (!minutes) return 'Duração não disponível';
    
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    
    if (hours > 0) {
      return `${hours}h ${mins}min`;
    }
    return `${mins}min`;
  }

  /**
   * Format currency to Brazilian Real
   */
  formatCurrency(amount: number): string {
    if (!amount) return 'Não informado';
    
    return new Intl.NumberFormat('pt-BR', {
      style: 'currency',
      currency: 'USD'
    }).format(amount);
  }
}

// Export singleton instance
export const tmdbService = new TMDBService();
export default tmdbService;