import { useEffect, useState } from 'react';
import { Badge } from '@/components/atoms/Badge/Badge';
import { Button } from '@/components/atoms/Button/Button';
import { Input } from '@/components/atoms/Input/Input';
import { FormField } from '@/components/molecules/FormField/FormField';

export const DiscountCoupon = ({ coupon = '', onApply, onClear }) => {
  const [code, setCode] = useState(coupon);
  const [error, setError] = useState('');

  useEffect(() => {
    setCode(coupon);
  }, [coupon]);

  const handleSubmit = (event) => {
    event.preventDefault();
    if (!code.trim()) {
      setError('Ingresa un código de cupón.');
      return;
    }

    if (!onApply(code)) {
      setError('Cupón no válido o expirado. Prueba con GAMER10 o DUOC2026.');
      return;
    }

    setError('');
  };

  const handleClear = () => {
    onClear();
    setCode('');
    setError('');
  };

  return (
    <form onSubmit={handleSubmit} className="mb-3">
      <FormField
        id="cart-coupon"
        label="Código de cupón"
        className=""
        labelClassName="visually-hidden"
        error={error}
        control={<Input
          type="text"
          placeholder="Cupón (Ej: GAMER10)"
          value={code}
          onChange={(event) => {
            setCode(event.target.value);
            setError('');
          }}
        />}
        append={<Button type="submit">Aplicar</Button>}
      />
      {coupon && (
        <>
          <div className="mt-2">
            <Button type="button" variant="link" className="text-danger px-0" onClick={handleClear}>
              Quitar cupón
            </Button>
          </div>
          <Badge variant="success" className="mt-2">
            Cupón {coupon} aplicado: 10% de descuento
          </Badge>
        </>
      )}
    </form>
  );
};
