import { redirect } from 'next/navigation';
import { getSession } from '@/lib/session';
import { createServerAxios } from '@/lib/axios/serverAxios';
import SuppliersClient from './_components/SuppliersClient';

export default async function SuppliersPage() {
  const session = await getSession();
  if (!session) redirect('/login');

  try {
    const api = await createServerAxios();
    const { data } = await api.get('/internal/suppliers?page=0&size=20&sort=name,asc');
    return (
      <SuppliersClient
        initialData={data}
        canWrite={session.roles.includes('ROLE_ADMIN') || session.roles.includes('ROLE_WAREHOUSE')}
        isAdmin={session.roles.includes('ROLE_ADMIN')}
      />
    );
  } catch {
    redirect('/login');
  }
}
