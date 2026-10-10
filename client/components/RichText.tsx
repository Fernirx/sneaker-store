/**
 * Render HTML đã được sanitize phía server (owasp-java-html-sanitizer) - an toàn để dangerouslySetInnerHTML.
 * Dùng cho mọi field rich-text trong hệ thống: mô tả sản phẩm/thương hiệu/danh mục/bộ sưu tập, nội dung thông báo marketing.
 */
export default function RichText({ html, className = '' }: { html: string; className?: string }) {
  return (
    <div
      className={`[&_p]:mb-3 [&_h1]:font-display [&_h1]:font-black [&_h1]:text-xl [&_h1]:mb-3 [&_h1]:mt-5
        [&_h2]:font-display [&_h2]:font-bold [&_h2]:text-lg [&_h2]:mb-2 [&_h2]:mt-4
        [&_h3]:font-bold [&_h3]:text-base [&_h3]:mb-2 [&_h3]:mt-3
        [&_ul]:list-disc [&_ul]:pl-5 [&_ul]:mb-3 [&_ol]:list-decimal [&_ol]:pl-5 [&_ol]:mb-3
        [&_img]:rounded-sm [&_img]:my-4 [&_img]:max-w-full
        [&_a]:text-accent [&_a]:underline
        [&_table]:border [&_table]:border-line [&_table]:mb-3 [&_td]:border [&_td]:border-line [&_td]:p-2 [&_th]:border [&_th]:border-line [&_th]:p-2 [&_th]:bg-paper
        ${className}`}
      dangerouslySetInnerHTML={{ __html: html }}
    />
  );
}
