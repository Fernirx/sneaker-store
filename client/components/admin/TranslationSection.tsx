'use client';

import { routing } from '@/i18n/routing';

export const SUPPORTED_LOCALES = routing.locales.filter(
  l => l !== routing.defaultLocale,
) as string[];

export interface TranslationDraft {
  locale: string;
  name: string;
  description: string;
}

const LOCALE_LABELS: Record<string, string> = {
  en: 'Tiếng Anh (EN)',
};

export function initTranslations(
  existing?: Array<{ locale: string; name: string; description: string | null }>,
): TranslationDraft[] {
  if (!existing?.length) return [];
  return existing
    .filter(t => (SUPPORTED_LOCALES as readonly string[]).includes(t.locale))
    .map(t => ({ locale: t.locale, name: t.name, description: t.description ?? '' }));
}

export function buildTranslationsPayload(drafts: TranslationDraft[]) {
  return drafts
    .filter(t => t.name.trim())
    .map(t => ({ locale: t.locale, name: t.name.trim(), description: t.description.trim() || null }));
}

export default function TranslationSection({
  value,
  onChange,
}: {
  value: TranslationDraft[];
  onChange: (drafts: TranslationDraft[]) => void;
}) {
  const available = SUPPORTED_LOCALES.filter(l => !value.find(t => t.locale === l));

  function addLocale(locale: string) {
    onChange([...value, { locale, name: '', description: '' }]);
  }

  function removeLocale(locale: string) {
    onChange(value.filter(t => t.locale !== locale));
  }

  function update(locale: string, field: 'name' | 'description', val: string) {
    onChange(value.map(t => (t.locale === locale ? { ...t, [field]: val } : t)));
  }

  return (
    <div className="space-y-2">
      {value.length > 0 && (
        <p className="text-[10px] font-bold uppercase tracking-wider text-muted">Bản dịch</p>
      )}

      {value.map(t => (
        <div key={t.locale} className="border border-line rounded-sm p-3 space-y-3">
          <div className="flex items-center justify-between">
            <p className="text-[10px] font-semibold tracking-widest uppercase text-muted">
              {LOCALE_LABELS[t.locale] ?? t.locale.toUpperCase()}
            </p>
            <button
              type="button"
              onClick={() => removeLocale(t.locale)}
              className="text-muted hover:text-danger text-lg leading-none transition-colors"
            >
              &times;
            </button>
          </div>
          <div>
            <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Tên</label>
            <input
              type="text"
              value={t.name}
              onChange={e => update(t.locale, 'name', e.target.value)}
              className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink"
            />
          </div>
          <div>
            <label className="block text-[11px] font-bold uppercase tracking-wider text-muted mb-1">Mô tả</label>
            <textarea
              value={t.description}
              onChange={e => update(t.locale, 'description', e.target.value)}
              rows={2}
              className="w-full border border-line rounded-sm px-3 py-2 text-sm focus:outline-none focus:border-ink resize-none"
            />
          </div>
        </div>
      ))}

      {available.length > 0 && (
        <div className="flex gap-1.5 flex-wrap">
          {available.map(l => (
            <button
              key={l}
              type="button"
              onClick={() => addLocale(l)}
              className="px-2.5 py-1 border border-dashed border-line text-xs text-muted rounded-sm hover:border-ink hover:text-ink transition-colors"
            >
              + {LOCALE_LABELS[l] ?? l.toUpperCase()}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
