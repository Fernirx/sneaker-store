import { NextRequest, NextResponse } from 'next/server';

export async function GET(req: NextRequest) {
  const provider = req.nextUrl.searchParams.get('provider');
  if (!provider) return NextResponse.json({ message: 'Missing provider' }, { status: 400 });

  return NextResponse.redirect(
    `${process.env.SPRING_API_URL}/oauth2/authorization/${provider}`,
  );
}
