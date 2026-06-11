import type { UploadApiResponse } from 'cloudinary';
import cloudinary from './cloudinary';

export type UploadFolder = 'avatars' | 'products' | 'brands' | 'collections' | 'categories';

const MAX_BYTES: Record<UploadFolder, number> = {
  avatars: 1 * 1024 * 1024,
  products: 1 * 1024 * 1024,
  brands: 1 * 1024 * 1024,
  collections: 1 * 1024 * 1024,
  categories: 1 * 1024 * 1024,
};

const ALLOWED = ['image/jpeg', 'image/png', 'image/webp'];

export async function deleteFromCloudinary(publicId: string) {
  if (!publicId.startsWith('sneaker-store/')) return;
  await cloudinary.uploader.destroy(publicId).catch(() => {});
}

export async function uploadToCloudinary(file: File, folder: UploadFolder) {
  if (!ALLOWED.includes(file.type)) throw new Error('Chỉ chấp nhận JPG, PNG, WebP');
  if (file.size > MAX_BYTES[folder]) {
    const mb = MAX_BYTES[folder] / 1024 / 1024;
    throw new Error(`Ảnh tối đa ${mb}MB`);
  }

  const buffer = Buffer.from(await file.arrayBuffer());

  return new Promise<{ publicId: string }>((resolve, reject) => {
    cloudinary.uploader.upload_stream(
      { folder: `sneaker-store/${folder}`, resource_type: 'image' },
      (err, result: UploadApiResponse | undefined) => {
        if (err || !result) return reject(err ?? new Error('Upload thất bại'));
        resolve({ publicId: result.public_id });
      },
    ).end(buffer);
  });
}
