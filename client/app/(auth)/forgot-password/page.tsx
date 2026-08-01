import { Suspense } from 'react';
import ForgotPasswordForm from './_components/ForgotPasswordForm';

export default function ForgotPasswordPage() {
  return (
    <Suspense>
      <ForgotPasswordForm />
    </Suspense>
  );
}
