'use client';

import { useRef, useState } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import { brandUrl, categoryUrl, collectionUrl, productUrl } from '@/lib/cloudinaryUrl';

type Folder = 'brands' | 'categories' | 'collections' | 'products';

function previewUrl(folder: Folder, publicId: string) {
  if (folder === 'brands') return brandUrl(publicId, 120, 60);
  if (folder === 'categories') return categoryUrl(publicId, 80, 80);
  if (folder === 'products') return productUrl(publicId, 120, 120);
  return collectionUrl(publicId, 160, 100);
}

export default function ImageUpload({
  value,
  folder,
  onChange,
}: {
  value: string;
  folder: Folder;
  onChange: (publicId: string) => void;
}) {
  const fileRef = useRef<HTMLInputElement>(null);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState('');

  async function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    if (!file) return;
    setError('');
    setUploading(true);
    try {
      const form = new FormData();
      form.append('file', file);
      form.append('folder', folder);
      if (value) form.append('oldPublicId', value);
      const { data } = await clientAxios.post('/api/upload', form);
      onChange(data.publicId);
    } catch {
      setError('Upload thất bại, thử lại.');
    } finally {
      setUploading(false);
      e.target.value = '';
    }
  }

  const src = value ? previewUrl(folder, value) : null;

  return (
    <div className="space-y-2">
      {src && (
        <div className="border border-line rounded-sm overflow-hidden inline-flex bg-paper">
          <img src={src} alt="preview" className="max-h-16 object-contain" />
        </div>
      )}
      <div className="flex items-center gap-2">
        <button
          type="button"
          onClick={() => fileRef.current?.click()}
          disabled={uploading}
          className="px-3 py-1.5 border border-line text-xs font-bold rounded-sm hover:bg-paper transition-colors disabled:opacity-40"
        >
          {uploading ? 'Đang tải...' : value ? 'Đổi ảnh' : 'Chọn ảnh'}
        </button>
        {value && (
          <span className="font-mono text-[10px] text-muted truncate max-w-[180px]">{value}</span>
        )}
      </div>
      {error && <p className="text-danger text-xs">{error}</p>}
      <input
        ref={fileRef}
        type="file"
        accept="image/jpeg,image/png,image/webp"
        className="hidden"
        onChange={handleChange}
      />
    </div>
  );
}
