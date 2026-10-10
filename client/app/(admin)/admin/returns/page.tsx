import { redirect } from 'next/navigation';
import { getSession } from '@/lib/session';
import { createServerAxios } from '@/lib/axios/serverAxios';
import ReturnsClient from './_components/ReturnsClient';
import type { PageData } from './_components/types';

export default async function ReturnsPage() {
  const session = await getSession();
  if (!session) redirect('/login');

  try {
    const api = await createServerAxios();
    const { data } = await api.get('/internal/returns?page=0&size=20&sort=createdAt,desc');
    return <ReturnsClient initialData={data as PageData} />;
  } catch {
    redirect('/login');
  }
}
