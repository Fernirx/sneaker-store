import axios from 'axios';

const clientAxios = axios.create();

let refreshing: Promise<void> | null = null;

clientAxios.interceptors.response.use(
  res => res,
  async err => {
    const original = err.config;
    if (err.response?.status !== 401 || original._retry) {
      return Promise.reject(err);
    }
    original._retry = true;

    if (!refreshing) {
      refreshing = axios.post('/api/auth/refresh')
        .then(() => { refreshing = null; })
        .catch(() => { refreshing = null; });
    }

    try {
      await refreshing;
      return clientAxios(original);
    } catch {
      window.location.href = '/login';
      return Promise.reject(err);
    }
  },
);

export default clientAxios;
