import { notFound } from 'next/navigation';
import { publicAxios } from '@/lib/axios/serverAxios';
import { type ProductDetailResponse } from '../_components/types';
import ProductDetailClient from './_components/ProductDetailClient';

type Props = { params: Promise<{ slug: string }> };

export default async function ProductDetailPage({ params }: Props) {
  const { slug } = await params;

  try {
    const { data } = await publicAxios.get<{ data: ProductDetailResponse }>(`/products/${slug}`);
    return <ProductDetailClient product={data.data} />;
  } catch {
    notFound();
  }
}
