import NotificationDetailClient from './_components/NotificationDetailClient';

export default async function NotificationDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  return <NotificationDetailClient notificationId={Number(id)} />;
}
