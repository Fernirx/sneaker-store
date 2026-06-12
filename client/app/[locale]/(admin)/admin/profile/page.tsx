import { redirect } from 'next/navigation';
import { getSession } from '@/lib/session';
import { createServerAxios } from '@/lib/axios/serverAxios';
import AdminProfileClient from './_components/AdminProfileClient';
import type { Profile } from '@/app/[locale]/(store)/profile/_components/ProfileClient';

export default async function AdminProfilePage() {
  const session = await getSession();
  if (!session) redirect('/login');

  try {
    const api = await createServerAxios();
    const { data } = await api.get('/me');
    return <AdminProfileClient initialProfile={data.data as Profile} />;
  } catch {
    redirect('/login');
  }
}
