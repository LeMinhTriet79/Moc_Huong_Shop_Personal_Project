import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';

// Base axios instance targeting the proxy route /api
const api = axios.create({
  baseURL: '',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 20000,
});

let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value?: any) => void;
  reject: (reason?: any) => void;
}> = [];

const processQueue = (error: any, token: string | null = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

// Request Interceptor: Attach Access Token
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    if (typeof window !== 'undefined') {
      try {
        const authStorage = localStorage.getItem('moc-huong-auth');
        if (authStorage) {
          const parsed = JSON.parse(authStorage);
          const token = parsed?.state?.accessToken;
          if (token && config.headers) {
            config.headers.Authorization = `Bearer ${token}`;
          }
        }
      } catch (e) {
        console.error('Error reading auth state from localStorage:', e);
      }
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor: Handle 401 & Token Refresh
api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

    // Do not attempt refresh for login, register, or refresh endpoints
    const requestUrl = originalRequest?.url || '';
    const isAuthEndpoint =
      requestUrl.includes('/api/identity/auth/login') ||
      requestUrl.includes('/api/identity/auth/refresh') ||
      requestUrl.includes('/api/identity/auth/register');

    if (error.response?.status === 401 && !originalRequest._retry && !isAuthEndpoint) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            if (originalRequest.headers && token) {
              originalRequest.headers.Authorization = `Bearer ${token}`;
            }
            return api(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const authStorage = typeof window !== 'undefined' ? localStorage.getItem('moc-huong-auth') : null;
        const refreshToken = authStorage ? JSON.parse(authStorage)?.state?.refreshToken : null;

        if (!refreshToken) {
          throw new Error('No refresh token available');
        }

        // Call refresh endpoint directly using raw axios to avoid interceptor loop
        const refreshResponse = await axios.post('/api/identity/auth/refresh', {
          refreshToken,
        });

        const authData = refreshResponse.data?.data;
        if (authData?.accessToken) {
          // Update zustand persisted store in localStorage
          if (authStorage) {
            const parsed = JSON.parse(authStorage);
            parsed.state.accessToken = authData.accessToken;
            parsed.state.refreshToken = authData.refreshToken || refreshToken;
            localStorage.setItem('moc-huong-auth', JSON.stringify(parsed));
          }

          processQueue(null, authData.accessToken);

          if (originalRequest.headers) {
            originalRequest.headers.Authorization = `Bearer ${authData.accessToken}`;
          }

          return api(originalRequest);
        } else {
          throw new Error('Refresh response missing token');
        }
      } catch (refreshError) {
        processQueue(refreshError, null);
        if (typeof window !== 'undefined') {
          localStorage.removeItem('moc-huong-auth');
          // Optional redirect to login
          if (window.location.pathname.startsWith('/account') || window.location.pathname.startsWith('/admin')) {
            window.location.href = '/login?expired=true';
          }
        }
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

export default api;
