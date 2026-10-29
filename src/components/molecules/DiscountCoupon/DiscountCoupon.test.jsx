import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { DiscountCoupon } from './DiscountCoupon';

describe('DiscountCoupon', () => {
  it('informa si se intenta aplicar un cupón vacío', async () => {
    const user = userEvent.setup();
    render(<DiscountCoupon onApply={vi.fn()} onClear={vi.fn()} />);

    await user.click(screen.getByRole('button', { name: 'Aplicar' }));

    expect(screen.getByText('Ingresa un código de cupón.')).toBeInTheDocument();
  });

  it('muestra un error cuando el cupón no es aceptado', async () => {
    const user = userEvent.setup();
    render(<DiscountCoupon onApply={() => false} onClear={vi.fn()} />);

    await user.type(screen.getByLabelText('Código de cupón'), 'MALO');
    await user.click(screen.getByRole('button', { name: 'Aplicar' }));

    expect(screen.getByText(/Cupón no válido o expirado/)).toBeInTheDocument();
  });

  it('aplica un cupón válido y muestra su confirmación', async () => {
    const user = userEvent.setup();
    const onApply = vi.fn(() => true);
    render(<DiscountCoupon onApply={onApply} onClear={vi.fn()} />);

    await user.type(screen.getByLabelText('Código de cupón'), 'GAMER10');
    await user.click(screen.getByRole('button', { name: 'Aplicar' }));

    expect(onApply).toHaveBeenCalledWith('GAMER10');
  });

  it('permite quitar el cupón aplicado', async () => {
    const user = userEvent.setup();
    const onClear = vi.fn();
    const props = { coupon: 'GAMER10', onApply: vi.fn(), onClear };
    const { rerender } = render(<DiscountCoupon {...props} />);

    await user.click(screen.getByRole('button', { name: 'Quitar cupón' }));

    expect(onClear).toHaveBeenCalledOnce();
    rerender(<DiscountCoupon {...props} coupon="" />);
    expect(screen.queryByText(/Cupón GAMER10 aplicado/)).not.toBeInTheDocument();
  });
});
