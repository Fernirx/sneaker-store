import { redirect } from 'next/navigation';
import { getSession } from '@/lib/session';
import { createServerAxios } from '@/lib/axios/serverAxios';
import ReviewsClient from './_components/ReviewsClient';

export default async function AdminReviewsPage() {
  const session = await getSession();
  if (!session) redirect('/login');

  try {
    const api = await createServerAxios();
    const { data } = await api.get('/internal/reviews?page=0&size=20&sort=createdAt,desc');
    return (
      <ReviewsClient
        initialData={data}
        isAdmin={session.roles.includes('ROLE_ADMIN')}
      />
    );
  } catch {
    redirect('/login');
  }
}
