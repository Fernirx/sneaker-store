import { createServerAxios } from '@/lib/axios/serverAxios';
import { getSession } from '@/lib/session';
import Sidebar from '@/components/admin/Sidebar';
import AdminHeader from '@/components/admin/AdminHeader';

async function getProfile() {
  try {
    const api = await createServerAxios();
    const { data } = await api.get('/me');
    return data.data as { firstName: string };
  } catch {
    return null;
  }
}

export default async function AdminLayout({ children }: { children: React.ReactNode }) {
  const session = await getSession();
  const profile = session ? await getProfile() : null;

  return (
    <div className="flex min-h-screen bg-paper">
      <Sidebar />
      <div className="flex-1 flex flex-col min-w-0">
        <AdminHeader firstName={profile?.firstName} />
        <main className="flex-1 p-6">
          {children}
        </main>
      </div>
    </div>
  );
}
