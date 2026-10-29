import { render, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import { CartPage } from './CartPage';


//npm test --run src\pages\CartPage.test.jsx


const cartItems = [
  { id: 1, titulo: 'Elden Ring', categoria: 'RPG', precio: 59990, imagen: '/elden.jpg', cantidad: 2 },
];

const renderCart = (props = {}) => render(
  <MemoryRouter>
    <CartPage
      cartItems={cartItems}
      coupon=""
      currentUser={null}
      onApplyCoupon={vi.fn()}
      onClearCart={vi.fn()}
      onClearCoupon={vi.fn()}
      onRemoveItem={vi.fn()}
      onUpdateQuantity={vi.fn()}
      onCheckout={vi.fn()}
      {...props}
    />
  </MemoryRouter>
);

describe('CartPage', () => {
  it('muestra el subtotal y el total calculados', () => {
    renderCart();

    expect(within(screen.getByText('Subtotal').parentElement).getByText('$119.980')).toBeInTheDocument();
    //cambiar valor por cualquiera
    expect(within(screen.getByText('TOTAL').parentElement).getByText('$119.980')).toBeInTheDocument();
    expect(screen.getByText('Elden Ring')).toBeInTheDocument();
  });

  it('envía acciones de cambiar cantidad y quitar producto', async () => {
    const user = userEvent.setup();
    const onUpdateQuantity = vi.fn();
    const onRemoveItem = vi.fn();
    renderCart({ onUpdateQuantity, onRemoveItem });

    await user.click(screen.getByRole('button', { name: 'Añadir una unidad de Elden Ring' }));
    await user.click(screen.getByRole('button', { name: 'Quitar Elden Ring del carrito' }));
// cambiar cantidad del callback esperada, 2do param, por cualquier numero
    expect(onUpdateQuantity).toHaveBeenCalledWith(1, 1);
    expect(onRemoveItem).toHaveBeenCalledWith(1);
  });

  it('solicita iniciar sesión si un visitante intenta pagar', async () => {
    const user = userEvent.setup();
    renderCart();

    await user.click(screen.getByRole('button', { name: /PAGAR AHORA/i }));

    //cambiar contexto por cualquiera
    expect(screen.getByRole('status')).toHaveTextContent(
      'Debes iniciar sesión para completar tu compra.'
    );
  });

  it('completa la compra demostrativa cuando hay un usuario autenticado', async () => {
    const user = userEvent.setup();
    const onCheckout = vi.fn();
    renderCart({ currentUser: { nombre: 'Ana' }, onCheckout });

    await user.click(screen.getByRole('button', { name: /PAGAR AHORA/i }));

    expect(onCheckout).toHaveBeenCalledOnce();
    // cambiar contexto por cualquiera
    expect(screen.getByRole('status')).toHaveTextContent('Compra de demostración completada');
  });
});
