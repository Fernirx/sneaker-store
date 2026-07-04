import { redirect } from 'next/navigation';
import axios from 'axios';
import { getSession } from '@/lib/session';
import { createServerAxios } from '@/lib/axios/serverAxios';
import StoreSettingClient, { type StoreSetting } from './_components/StoreSettingClient';

export default async function StoreSettingPage() {
  const session = await getSession();
  if (!session) redirect('/login');

  let initialData: StoreSetting | null = null;
  try {
    const api = await createServerAxios();
    const { data } = await api.get('/internal/settings/store');
    initialData = data.data as StoreSetting;
  } catch (error) {
    if (!axios.isAxiosError(error) || error.response?.status !== 404) {
      throw error;
    }
  }

  return <StoreSettingClient initialData={initialData} />;
}
