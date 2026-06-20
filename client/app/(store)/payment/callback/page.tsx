import PaymentCallbackClient from './_components/PaymentCallbackClient';

type Props = {
  searchParams: Promise<Record<string, string>>;
};

export default async function PaymentCallbackPage({ searchParams }: Props) {
  const params = await searchParams;

  return (
    <PaymentCallbackClient
      responseCode={params.vnp_ResponseCode ?? ''}
      txnRef={params.vnp_TxnRef ?? ''}
      amount={Number(params.vnp_Amount ?? 0) / 100}
    />
  );
}
