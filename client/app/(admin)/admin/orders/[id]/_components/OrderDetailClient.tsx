'use client';

import { useState } from 'react';
import Link from 'next/link';
import { type OrderInternalResponse } from '../../_components/types';
import InfoTab from './InfoTab';
import ItemsTab from './ItemsTab';
import HistoryTab from './HistoryTab';
import PaymentsTab from './PaymentsTab';

const TABS = [
  { key: 'info',     label: 'Thông tin' },
  { key: 'items',    label: 'Sản phẩm' },
  { key: 'history',  label: 'Lịch sử' },
  { key: 'payments', label: 'Thanh toán' },
] as const;

type TabKey = typeof TABS[number]['key'];

export default function OrderDetailClient({
  order: initialOrder,
  roles,
}: {
  order: OrderInternalResponse;
  roles: string[];
}) {
  const [order, setOrder] = useState(initialOrder);
  const [activeTab, setActiveTab] = useState<TabKey>('info');

  return (
    <div className="space-y-5">
      {/* Breadcrumb */}
      <div className="flex items-center gap-2 text-sm">
        <Link href="/admin/orders" className="text-muted hover:text-ink transition-colors">
          Đơn hàng
        </Link>
        <span className="text-muted">/</span>
        <span className="font-body font-bold">{order.code}</span>
        <span className="text-xs text-muted ml-1">#{order.id}</span>
      </div>

      {/* Tab bar */}
      <div className="border-b border-line flex gap-0">
        {TABS.map(t => (
          <button
            key={t.key}
            onClick={() => setActiveTab(t.key)}
            className={`px-4 py-2.5 text-sm font-bold border-b-2 transition-colors ${
              activeTab === t.key
                ? 'border-accent text-accent'
                : 'border-transparent text-muted hover:text-ink'
            }`}
          >
            {t.label}
          </button>
        ))}
      </div>

      {/* Tab content */}
      {activeTab === 'info'     && <InfoTab order={order} onUpdated={setOrder} roles={roles} />}
      {activeTab === 'items'    && <ItemsTab order={order} />}
      {activeTab === 'history'  && <HistoryTab orderId={order.id} />}
      {activeTab === 'payments' && <PaymentsTab orderId={order.id} />}
    </div>
  );
}
