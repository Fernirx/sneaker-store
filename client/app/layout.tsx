import type { Metadata } from 'next';
import { Archivo, Roboto } from 'next/font/google';
import './globals.css';

const archivo = Archivo({
  subsets: ['latin', 'vietnamese'],
  weight: ['400', '600', '700', '800', '900'],
  variable: '--font-archivo',
  display: 'swap',
});

const roboto = Roboto({
  subsets: ['latin', 'vietnamese'],
  weight: ['400', '500', '700'],
  variable: '--font-roboto',
  display: 'swap',
});

export const metadata: Metadata = {
  title: 'STRIDE — Sneaker Store',
  description: 'Cửa hàng sneaker hiệu năng & lifestyle chính hãng',
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html className={`${archivo.variable} ${roboto.variable}`}>
      <body className="min-h-screen bg-white font-body text-ink antialiased">
        {children}
      </body>
    </html>
  );
}
