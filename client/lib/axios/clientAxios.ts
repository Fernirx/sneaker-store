import axios from 'axios';

const clientAxios = axios.create();

let refreshing: Promise<void> | null = null;

clientAxios.interceptors.response.use(undefined, async err => {
  const original = err.config;
  if (err.response?.status !== 401 || original._retry || /^\/api\/auth\//.test(original.url ?? '')) {
    throw err;
  }
  original._retry = true;

  if (!refreshing) {
    refreshing = axios.post('/api/auth/refresh')
      .then(() => { refreshing = null; })
      .catch(e => { refreshing = null; throw e; });
  }

  try {
    await refreshing;
  } catch {
    window.location.href = '/login';
    throw err;
  }

  return clientAxios(original);
});

export default clientAxios;
