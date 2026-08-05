'use client';

import { useState } from 'react';
import UsersClient from './UsersClient';
import DeletedUsersClient from './DeletedUsersClient';
import { type PageData } from './types';

export default function UsersTabs({
  initialData,
  isAdmin,
  currentUserId,
}: {
  initialData: PageData;
  isAdmin: boolean;
  currentUserId: number;
}) {
  const [activeTab, setActiveTab] = useState<'active' | 'deleted'>('active');

  return (
    <div>
      <div className="flex gap-4 border-b border-line mb-4">
        <button
          onClick={() => setActiveTab('active')}
          className={`pb-2 text-sm font-bold transition-colors ${
            activeTab === 'active' ? 'border-b-2 border-accent text-accent' : 'text-muted hover:text-ink'
          }`}
        >
          Tài khoản hiện hành
        </button>
        <button
          onClick={() => setActiveTab('deleted')}
          className={`pb-2 text-sm font-bold transition-colors ${
            activeTab === 'deleted' ? 'border-b-2 border-accent text-accent' : 'text-muted hover:text-ink'
          }`}
        >
          Thùng rác
        </button>
      </div>

      {activeTab === 'active' && (
        <UsersClient initialData={initialData} isAdmin={isAdmin} currentUserId={currentUserId} />
      )}
      {activeTab === 'deleted' && (
        <DeletedUsersClient />
      )}
    </div>
  );
}
