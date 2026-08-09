export default function OrderDetailSkeleton() {
  return (
    <div className="max-w-3xl mx-auto px-4 py-10 space-y-6">
      <div className="h-8 w-64 bg-line rounded-sm animate-pulse" />
      <div className="h-48 bg-line rounded-sm animate-pulse" />
      <div className="h-32 bg-line rounded-sm animate-pulse" />
    </div>
  );
}
