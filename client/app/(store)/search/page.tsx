import { redirect } from 'next/navigation';

type SearchParams = Promise<{ [key: string]: string | string[] | undefined }>;

export default async function SearchPage({ searchParams }: { searchParams: SearchParams }) {
  const sp = await searchParams;
  const q = sp.q ?? sp.search;
  const keyword = typeof q === 'string' ? q : '';
  redirect(keyword ? `/products?search=${encodeURIComponent(keyword)}` : '/products');
}
