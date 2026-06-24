import { notFound } from 'next/navigation';
import { publicAxios } from '@/lib/axios/serverAxios';
import { getSession } from '@/lib/session';
import { type ProductDetailResponse } from '../_components/types';
import ProductDetailClient from './_components/ProductDetailClient';

type Props = { params: Promise<{ slug: string }> };

export default async function ProductDetailPage({ params }: Props) {
  const { slug } = await params;

  try {
    const [{ data }, session] = await Promise.all([
      publicAxios.get<{ data: ProductDetailResponse }>(`/products/${slug}`),
      getSession(),
    ]);
    return (
      <ProductDetailClient
        product={data.data}
        isLoggedIn={!!session}
        currentUserId={session ? Number(session.userId) : null}
      />
    );
  } catch {
    notFound();
  }
}
