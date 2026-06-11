import axios from 'axios';
import { cookies } from 'next/headers';

export const publicAxios = axios.create({
  baseURL: process.env.SPRING_API_URL,
  headers: { 'Content-Type': 'application/json' },
});

export async function createServerAxios() {
  const store = await cookies();
  const accessToken = store.get('access_token')?.value;
  const locale = store.get('NEXT_LOCALE')?.value ?? 'vi';

  return axios.create({
    baseURL: process.env.SPRING_API_URL,
    headers: {
      ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
      'Accept-Language': locale,
      'Content-Type': 'application/json',
    },
  });
}
