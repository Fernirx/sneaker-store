import { NextRequest, NextResponse } from 'next/server';
import { uploadToCloudinary, deleteFromCloudinary, type UploadFolder } from '@/lib/uploadToCloudinary';

const ALLOWED_FOLDERS: UploadFolder[] = ['avatars', 'products', 'brands', 'collections', 'categories'];

export async function POST(req: NextRequest) {
  try {
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
