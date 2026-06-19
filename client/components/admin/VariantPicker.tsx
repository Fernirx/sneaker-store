'use client';

import { useState, useRef, useEffect } from 'react';
import clientAxios from '@/lib/axios/clientAxios';

export interface VariantSearchResult {
  id: number;
  sku: string;
  productId: number;
  productName: string;
  productCode: string;
  colorway: string;
  colorwayCode: string | null;
  colorHex: string | null;
  size: number;
  shoeWidth: string;
  price: number | null;
  basePrice: number;
  stockQuantity: number;
  active: boolean;
}

export default function VariantPicker({
  onSelect,
  placeholder = 'Tìm theo SKU, tên sản phẩm...',
}: {
  onSelect: (variant: VariantSearchResult) => void;
  placeholder?: string;
}) {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState<VariantSearchResult[]>([]);
  const [loading, setLoading] = useState(false);
  const [open, setOpen] = useState(false);

  const debounceTimer = useRef<ReturnType<typeof setTimeout> | null>(null);
  const blurTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    if (debounceTimer.current) clearTimeout(debounceTimer.current);
    const keyword = query.trim();
    if (!keyword) return;
    debounceTimer.current = setTimeout(async () => {
      setLoading(true);
      try {
        const { data } = await clientAxios.get(
          `/api/admin/product-variants/search?keyword=${encodeURIComponent(keyword)}&size=10`,
        );
        setResults(data.data ?? []);
      } finally {
        setLoading(false);
      }
    }, 300);
    return () => { if (debounceTimer.current) clearTimeout(debounceTimer.current); };
  }, [query]);

  function handleSelect(variant: VariantSearchResult) {
    onSelect(variant);
    setQuery('');
    setResults([]);
    setOpen(false);
  }

  return (
    <div className="relative">
      <input
        type="text"
        value={query}
        onChange={e => { setQuery(e.target.value); setOpen(true); }}
        onFocus={() => setOpen(true)}
        onBlur={() => { blurTimer.current = setTimeout(() => setOpen(false), 150); }}
        placeholder={placeholder}
        className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
      />
      {open && query.trim() && (
        <div className="absolute z-20 left-0 right-0 mt-1 bg-white border border-line rounded-sm shadow-lg max-h-72 overflow-y-auto">
          {loading ? (
            <p className="px-3 py-3 text-xs text-muted">Đang tìm...</p>
          ) : results.length === 0 ? (
            <p className="px-3 py-3 text-xs text-muted">Không tìm thấy variant nào.</p>
          ) : (
            results.map(v => (
              <button
                key={v.id}
                type="button"
                onMouseDown={e => e.preventDefault()}
                onClick={() => handleSelect(v)}
                className="w-full text-left px-3 py-2 hover:bg-paper transition-colors border-b border-line-2 last:border-0"
              >
                <div className="flex items-center justify-between gap-2">
                  <span className="font-mono text-xs font-bold">{v.sku}</span>
                  <span className={`text-[10px] font-bold ${v.stockQuantity <= 0 ? 'text-danger' : 'text-muted'}`}>
                    Tồn: {v.stockQuantity}
                  </span>
                </div>
                <div className="text-sm font-medium truncate">{v.productName}</div>
                <div className="text-xs text-muted">
                  {v.colorway} · Size {v.size}
                  {!v.active && ' · Ngừng kinh doanh'}
                </div>
              </button>
            ))
          )}
        </div>
      )}
    </div>
  );
}
