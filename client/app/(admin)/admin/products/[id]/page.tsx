import { redirect } from 'next/navigation';
import { getSession } from '@/lib/session';
import { createServerAxios } from '@/lib/axios/serverAxios';
import ProductDetailClient from './_components/ProductDetailClient';

export default async function ProductDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  const session = await getSession();
  if (!session) redirect('/login');

  try {
    const api = await createServerAxios();
    const { data } = await api.get(`/internal/products/${id}`);
    return (
      <ProductDetailClient
        product={data.data}
        isAdmin={session.roles.includes('ROLE_ADMIN')}
      />
    );
  } catch {
    redirect('/admin/products');
  }
}
