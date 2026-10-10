import { redirect } from 'next/navigation';
import { getSession } from '@/lib/session';
import { createServerAxios } from '@/lib/axios/serverAxios';
import BrandsClient from './_components/BrandsClient';

export default async function BrandsPage() {
  const session = await getSession();
  if (!session) redirect('/login');

  try {
    const api = await createServerAxios();
    const { data } = await api.get('/internal/brands?page=0&size=20&sort=name,asc');
    return (
      <BrandsClient
        initialData={data}
        isAdmin={session.roles.includes('ROLE_ADMIN')}
      />
    );
  } catch {
    redirect('/login');
  }
}
