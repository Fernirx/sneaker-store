'use client';

import { useState } from 'react';
import Link from 'next/link';
import { type ProductRow } from '../../_components/types';
import BasicInfoTab from './BasicInfoTab';
import VariantsTab from './VariantsTab';
import ImagesTab from './ImagesTab';
import CategoriesTab from './CategoriesTab';

const TABS = [
  { key: 'basic',      label: 'Thông tin cơ bản' },
  { key: 'variants',   label: 'Size & Màu' },
  { key: 'images',     label: 'Ảnh sản phẩm' },
  { key: 'categories', label: 'Danh mục' },
] as const;

type TabKey = typeof TABS[number]['key'];

export default function ProductDetailClient({
  product,
  isAdmin,
}: {
  product: ProductRow;
  isAdmin: boolean;
}) {
  const [activeTab, setActiveTab] = useState<TabKey>('basic');

  return (
    <div className="space-y-5">
      {/* Breadcrumb */}
      <div className="flex items-center gap-2 text-sm">
        <Link href="/admin/products" className="text-muted hover:text-ink transition-colors">
          Sản phẩm
        </Link>
        <span className="text-muted">/</span>
        <span className="font-bold truncate max-w-xs">{product.name}</span>
        <span className="font-mono text-xs text-muted ml-1">#{product.id}</span>
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
      {activeTab === 'basic'      && <BasicInfoTab product={product} isAdmin={isAdmin} />}
      {activeTab === 'variants'   && <VariantsTab productId={product.id} isAdmin={isAdmin} />}
      {activeTab === 'images'     && <ImagesTab productId={product.id} isAdmin={isAdmin} />}
      {activeTab === 'categories' && <CategoriesTab productId={product.id} isAdmin={isAdmin} />}
    </div>
  );
}
