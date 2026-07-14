'use client';

import { useEditor, EditorContent } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';
import Image from '@tiptap/extension-image';
import Link from '@tiptap/extension-link';
import { Table } from '@tiptap/extension-table';
import TableRow from '@tiptap/extension-table-row';
import TableCell from '@tiptap/extension-table-cell';
import TableHeader from '@tiptap/extension-table-header';
import { useEffect, useRef } from 'react';
import clientAxios from '@/lib/axios/clientAxios';
import { contentImageUrl } from '@/lib/cloudinaryUrl';

function ToolbarButton({ active, onClick, label, children }: { active?: boolean; onClick: () => void; label: string; children: React.ReactNode }) {
  return (
    <button
      type="button"
      title={label}
      onClick={onClick}
      className={`px-2.5 py-1.5 rounded-sm text-xs font-bold border transition-colors ${
        active ? 'bg-ink text-white border-ink' : 'border-line text-ink-2 hover:bg-paper'
      }`}
    >
      {children}
    </button>
  );
}

export default function TiptapEditor({ value, onChange, editable = true }: { value: string; onChange: (html: string) => void; editable?: boolean }) {
  const fileInputRef = useRef<HTMLInputElement>(null);

  const editor = useEditor({
    immediatelyRender: false,
    editable,
    extensions: [
      StarterKit.configure({ link: false }),
      Link.configure({ openOnClick: false, autolink: true }),
      Image.configure({ inline: false }),
      Table.configure({ resizable: false }),
      TableRow,
      TableHeader,
      TableCell,
    ],
    content: value,
    onUpdate: ({ editor }) => onChange(editor.getHTML()),
    editorProps: {
      attributes: {
        class: 'min-h-[220px] px-3 py-3 text-sm focus:outline-none [&_ul]:list-disc [&_ul]:pl-5 [&_ol]:list-decimal [&_ol]:pl-5 [&_img]:max-w-full [&_img]:rounded-sm [&_a]:text-accent [&_a]:underline [&_table]:border [&_table]:border-line [&_td]:border [&_td]:border-line [&_td]:p-1.5 [&_th]:border [&_th]:border-line [&_th]:p-1.5 [&_th]:bg-paper',
      },
    },
  });

  useEffect(() => {
    editor?.setEditable(editable);
  }, [editable, editor]);

  async function handleImageUpload(e: React.ChangeEvent<HTMLInputElement>) {
    const file = e.target.files?.[0];
    e.target.value = '';
    if (!file || !editor) return;
    try {
      const form = new FormData();
      form.append('file', file);
      form.append('folder', 'content');
      const { data } = await clientAxios.post('/api/upload', form);
      editor.chain().focus().setImage({ src: contentImageUrl(data.publicId) }).run();
    } catch {
      // im lặng bỏ qua - toolbar không có chỗ hiển thị lỗi riêng, admin thấy ảnh không chèn được thì thử lại
    }
  }

  function setLink() {
    if (!editor) return;
    const previousUrl = editor.getAttributes('link').href as string | undefined;
    const url = window.prompt('Nhập URL liên kết', previousUrl ?? 'https://');
    if (url === null) return;
    if (url === '') {
      editor.chain().focus().extendMarkRange('link').unsetLink().run();
      return;
    }
    editor.chain().focus().extendMarkRange('link').setLink({ href: url }).run();
  }

  if (!editor) return null;

  return (
    <div className="border border-line rounded-sm overflow-hidden">
      {editable && (
        <div className="flex flex-wrap gap-1.5 p-2 border-b border-line bg-paper">
          <ToolbarButton label="Đậm" active={editor.isActive('bold')} onClick={() => editor.chain().focus().toggleBold().run()}>B</ToolbarButton>
          <ToolbarButton label="Nghiêng" active={editor.isActive('italic')} onClick={() => editor.chain().focus().toggleItalic().run()}><i>I</i></ToolbarButton>
          <ToolbarButton label="Tiêu đề" active={editor.isActive('heading', { level: 2 })} onClick={() => editor.chain().focus().toggleHeading({ level: 2 }).run()}>H2</ToolbarButton>
          <ToolbarButton label="Danh sách" active={editor.isActive('bulletList')} onClick={() => editor.chain().focus().toggleBulletList().run()}>• List</ToolbarButton>
          <ToolbarButton label="Danh sách số" active={editor.isActive('orderedList')} onClick={() => editor.chain().focus().toggleOrderedList().run()}>1. List</ToolbarButton>
          <ToolbarButton label="Liên kết" active={editor.isActive('link')} onClick={setLink}>Link</ToolbarButton>
          <ToolbarButton label="Chèn ảnh / banner" onClick={() => fileInputRef.current?.click()}>Ảnh</ToolbarButton>
          <ToolbarButton label="Chèn bảng" onClick={() => editor.chain().focus().insertTable({ rows: 3, cols: 3, withHeaderRow: true }).run()}>Bảng</ToolbarButton>
        </div>
      )}
      <input ref={fileInputRef} type="file" accept="image/jpeg,image/png,image/webp" className="hidden" onChange={handleImageUpload} />
      <EditorContent editor={editor} className={!editable ? 'bg-paper' : undefined} />
    </div>
  );
}
