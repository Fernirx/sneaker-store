import { redirect } from 'next/navigation';
import { getSession } from '@/lib/session';
import { createServerAxios } from '@/lib/axios/serverAxios';
import UsersClient from './_components/UsersClient';

export default async function UsersPage() {
  const session = await getSession();
  if (!session) redirect('/login');

  try {
    const api = await createServerAxios();
    const { data } = await api.get('/internal/users?page=0&size=20&sort=createdAt,desc');
    return (
      <UsersClient
        initialData={data}
        isAdmin={session.roles.includes('ROLE_ADMIN')}
        currentUserId={Number(session.userId)}
      />
    );
  } catch {
    redirect('/login');
  }
}
