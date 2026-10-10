import { redirect } from 'next/navigation';
import { getSession } from '@/lib/session';
import { createServerAxios } from '@/lib/axios/serverAxios';
import BannersClient from './_components/BannersClient';

export default async function BannersPage() {
  const session = await getSession();
  if (!session) redirect('/login');

  try {
    const api = await createServerAxios();
    const { data } = await api.get('/internal/banners?page=0&size=20&sort=displayOrder,asc');
    return (
      <BannersClient
        initialData={data}
        canCreateDelete={session.roles.includes('ROLE_ADMIN')}
        canUpdate={session.roles.includes('ROLE_ADMIN')}
      />
    );
  } catch {
    redirect('/login');
  }
}
