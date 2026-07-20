import { NextRequest, NextResponse } from 'next/server';
import { uploadToCloudinary, deleteFromCloudinary, type UploadFolder } from '@/lib/uploadToCloudinary';
import { getSession } from '@/lib/session';

const ALLOWED_FOLDERS: UploadFolder[] = ['avatars', 'products', 'brands', 'collections', 'categories', 'reviews', 'content', 'returns', 'banners'];

export async function POST(req: NextRequest) {
  try {
    // Route này upload thẳng lên Cloudinary từ server Next.js, không đi qua Spring Boot nên @PreAuthorize
    // không giúp được gì ở đây - phải tự check session, khác với đa số route BFF khác chỉ forward request.
    const session = await getSession();
    if (!session) {
      return NextResponse.json({ message: 'Vui lòng đăng nhập để tải ảnh lên' }, { status: 401 });
    }

    const form = await req.formData();
    const file = form.get('file') as File | null;
    const folder = form.get('folder') as string | null;
    const oldPublicId = form.get('oldPublicId') as string | null;

    if (!file) return NextResponse.json({ message: 'Không tìm thấy file' }, { status: 400 });
    if (!folder || !ALLOWED_FOLDERS.includes(folder as UploadFolder)) {
      return NextResponse.json({ message: 'Loại tài nguyên không hợp lệ' }, { status: 400 });
    }

    const result = await uploadToCloudinary(file, folder as UploadFolder);
    if (oldPublicId) await deleteFromCloudinary(oldPublicId);
    return NextResponse.json(result);
  } catch (err) {
    const message = err instanceof Error ? err.message : 'Lỗi upload ảnh';
    return NextResponse.json({ message }, { status: 400 });
  }
}
