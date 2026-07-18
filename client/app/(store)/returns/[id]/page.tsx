import ReturnDetailClient from './_components/ReturnDetailClient';

export default async function ReturnDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  return <ReturnDetailClient returnId={Number(id)} />;
}
