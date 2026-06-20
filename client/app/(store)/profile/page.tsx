import { redirect } from 'next/navigation';
import { getSession } from '@/lib/session';
import { createServerAxios } from '@/lib/axios/serverAxios';
import ProfileClient from './_components/ProfileClient';

export default async function ProfilePage() {
  const session = await getSession();
  if (!session) redirect('/login');

  const api = await createServerAxios();
  const [profileRes, customerRes] = await Promise.allSettled([
    api.get('/me'),
    api.get('/me/customer'),
  ]);

  const profile = profileRes.status === 'fulfilled' ? profileRes.value.data.data : null;
  const customer = customerRes.status === 'fulfilled' ? customerRes.value.data.data : null;

  return <ProfileClient initialProfile={profile} initialCustomer={customer} />;
}
