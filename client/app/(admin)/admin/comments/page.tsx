import { redirect } from 'next/navigation';
import { getSession } from '@/lib/session';
import { createServerAxios } from '@/lib/axios/serverAxios';
import CommentsClient from './_components/CommentsClient';

export default async function AdminCommentsPage() {
  const session = await getSession();
  if (!session) redirect('/login');

  try {
    const api = await createServerAxios();
    const { data } = await api.get('/internal/comments?page=0&size=20&sort=createdAt,desc');
    return (
      <CommentsClient
        initialData={data}
        isAdmin={session.roles.includes('ROLE_ADMIN')}
      />
    );
  } catch {
    redirect('/login');
  }
}
