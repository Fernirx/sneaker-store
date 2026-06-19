import { redirect } from 'next/navigation';
import { getSession } from '@/lib/session';
import NewStockAdjustmentClient from './_components/NewStockAdjustmentClient';

export default async function NewStockAdjustmentPage() {
  const session = await getSession();
  if (!session) redirect('/login');

  return <NewStockAdjustmentClient />;
}
