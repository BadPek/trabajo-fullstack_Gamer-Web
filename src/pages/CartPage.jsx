import { useState } from 'react';
import { Link } from 'react-router-dom';
import { calculateSubtotal, applyCouponDiscount } from '@/utils/cartUtils';
import { getImageUrl } from '@/utils/imageUtils';
import { DiscountCoupon } from '@/components/molecules/DiscountCoupon/DiscountCoupon';

export const CartPage = ({
  cartItems,
  coupon,
  currentUser,
  onApplyCoupon,
  onClearCart,
  onClearCoupon,
  onRemoveItem,
  onUpdateQuantity,
  onCheckout,
}) => {
  const [checkoutMessage, setCheckoutMessage] = useState('');

  const subtotal = calculateSubtotal(cartItems);
  const { discountAmount, finalTotal } = applyCouponDiscount(subtotal, coupon);

  const handleClearCart = () => {
    if (window.confirm('¿Estás seguro de que deseas vaciar tu carrito de compras?')) {
      onClearCart();
      setCheckoutMessage('');
    }
  };

  const handleCheckout = () => {
    if (!currentUser) {
      setCheckoutMessage('Debes iniciar sesión para completar tu compra.');
      return;
    }

    const formattedTotal = `$${finalTotal.toLocaleString('es-CL')}`;
    onCheckout();
    setCheckoutMessage(`Compra de demostración completada por ${formattedTotal}. No hay un proveedor de pago conectado.`);
  };

  return (
    <div>
      <div className="d-flex justify-content-between align-items-center mb-4 flex-wrap gap-2">
        <h1 className="text-purple fw-bold mb-0">
          <i className="bi bi-cart3 me-2"></i>Mi Carrito de Compras
        </h1>
        {cartItems.length > 0 && (
          <button type="button" className="btn btn-outline-danger btn-sm" onClick={handleClearCart}>
            <i className="bi bi-trash3 me-1"></i>Vaciar carrito
          </button>
        )}
      </div>

      {checkoutMessage && (
        <div className={`alert ${currentUser ? 'alert-success' : 'alert-warning'}`} role="status">
          {checkoutMessage}
          {!currentUser && <> <Link to="/login" className="alert-link">Iniciar sesión</Link>.</>}
        </div>
      )}

      <div className="row g-4">
        <div className="col-12 col-lg-8">
          {cartItems.length === 0 ? (
            <div className="alert alert-info rounded-4 p-4 text-center">
              Tu carrito está vacío. <Link to="/productos" className="fw-bold">Explora nuestro catálogo aquí</Link>.
            </div>
          ) : (
            <>
              {cartItems.map((item) => (
                <article key={item.id} className="card border-0 shadow-sm p-3 mb-3 rounded-4">
                  <div className="row align-items-center g-3">
                    <div className="col-3 col-sm-2">
                      <img src={getImageUrl(item.imagen)} alt={item.titulo} className="cart-item-image rounded-3" />
                    </div>
                    <div className="col-9 col-sm-4">
                      <h2 className="h6 fw-bold mb-1">{item.titulo}</h2>
                      <small className="text-muted d-block">{item.categoria || 'Videojuego'}</small>
                      <span className="text-purple fw-bold">${item.precio.toLocaleString('es-CL')} c/u</span>
                    </div>
                    <div className="col-7 col-sm-4">
                      <div className="input-group input-group-sm quantity-control">
                        <button type="button" className="btn btn-outline-purple" aria-label={`Restar una unidad de ${item.titulo}`} onClick={() => onUpdateQuantity(item.id, -1)}>-</button>
                        <span className="input-group-text fw-bold" aria-label={`Cantidad: ${item.cantidad}`}>{item.cantidad}</span>
                        <button type="button" className="btn btn-outline-purple" aria-label={`Añadir una unidad de ${item.titulo}`} onClick={() => onUpdateQuantity(item.id, 1)}>+</button>
                      </div>
                    </div>
                    <div className="col-5 col-sm-2 text-end">
                      <span className="fw-bold text-purple">${(item.precio * item.cantidad).toLocaleString('es-CL')}</span>
                      <button type="button" className="btn btn-link text-danger p-0 d-block ms-auto" onClick={() => onRemoveItem(item.id)} aria-label={`Quitar ${item.titulo} del carrito`}>
                        <i className="bi bi-trash3"></i> Quitar
                      </button>
                    </div>
                  </div>
                </article>
              ))}
              <Link to="/productos" className="btn btn-outline-purple">
                <i className="bi bi-arrow-left me-1"></i>Seguir comprando juegos
              </Link>
            </>
          )}
        </div>

        <div className="col-12 col-lg-4">
          <div className="card border-0 shadow-sm p-4 bg-purple-light rounded-4">
            <h2 className="h5 fw-bold text-purple mb-3">Resumen de Compra</h2>
            <div className="d-flex justify-content-between mb-2">
              <span className="text-secondary">Subtotal</span>
              <span className="fw-semibold">${subtotal.toLocaleString('es-CL')}</span>
            </div>
            {discountAmount > 0 && (
              <div className="d-flex justify-content-between mb-2 text-success fw-bold">
                <span>Descuento ({coupon}, 10%)</span>
                <span>-${discountAmount.toLocaleString('es-CL')}</span>
              </div>
            )}
            <div className="d-flex justify-content-between mb-2">
              <span className="text-secondary">Envío digital</span>
              <span className="text-success fw-semibold">Gratis</span>
            </div>
            <hr />
            <div className="d-flex justify-content-between mb-4 fs-5 fw-bold text-purple">
              <span>TOTAL</span>
              <span>${finalTotal.toLocaleString('es-CL')}</span>
            </div>

            <DiscountCoupon coupon={coupon} onApply={onApplyCoupon} onClear={onClearCoupon} />

            <button type="button" className="btn btn-purple btn-lg w-100 fw-bold" disabled={cartItems.length === 0} onClick={handleCheckout}>
              <i className="bi bi-credit-card me-2"></i>PAGAR AHORA
            </button>
            {!currentUser && cartItems.length > 0 && <p className="small text-muted mt-2 mb-0">Necesitas iniciar sesión para completar la compra.</p>}
          </div>
        </div>
      </div>
    </div>
  );
};
