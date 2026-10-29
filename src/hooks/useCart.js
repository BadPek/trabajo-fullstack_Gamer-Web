import { useEffect, useState } from 'react';
import { isValidCoupon } from '@/utils/cartUtils';
import { juegosData } from '@/data/juegosData';

const CART_KEY = 'gz_carrito';
const COUPON_KEY = 'gz_cupon';

const readStoredCart = () => {
  const storedCart = sessionStorage.getItem(CART_KEY);
  if (storedCart === null) return [];

  const cart = JSON.parse(storedCart);
  if (!Array.isArray(cart)) {
    throw new Error('El carrito guardado en esta sesión tiene un formato inválido.');
  }

  return cart.map((item) => ({
    ...item,
    categoria: item.categoria || juegosData.find((game) => game.id === item.id)?.categoria || 'Videojuego',
  }));
};

export const useCart = () => {
  const [cartItems, setCartItems] = useState(readStoredCart);
  const [coupon, setCoupon] = useState(() => sessionStorage.getItem(COUPON_KEY) || '');

  useEffect(() => {
    sessionStorage.setItem(CART_KEY, JSON.stringify(cartItems));
  }, [cartItems]);

  useEffect(() => {
    if (coupon) sessionStorage.setItem(COUPON_KEY, coupon);
    else sessionStorage.removeItem(COUPON_KEY);
  }, [coupon]);

  const addToCart = (product, quantity = 1) => {
    const category = product.categoria || juegosData.find((game) => game.id === product.id)?.categoria;
    const cartProduct = { ...product, categoria: category || 'Videojuego' };

    setCartItems((currentItems) => {
      const existingItem = currentItems.find((item) => item.id === cartProduct.id);
      if (existingItem) {
        return currentItems.map((item) =>
          item.id === cartProduct.id ? { ...item, cantidad: item.cantidad + quantity } : item
        );
      }

      return [...currentItems, { ...cartProduct, cantidad: quantity }];
    });
  };

  const updateQuantity = (id, change) => {
    setCartItems((currentItems) =>
      currentItems
        .map((item) => (item.id === id ? { ...item, cantidad: item.cantidad + change } : item))
        .filter((item) => item.cantidad > 0)
    );
  };

  const removeFromCart = (id) => {
    setCartItems((currentItems) => currentItems.filter((item) => item.id !== id));
  };

  const clearCart = () => {
    setCartItems([]);
    setCoupon('');
  };

  const applyCoupon = (code) => {
    const normalizedCode = code.trim().toUpperCase();
    if (!isValidCoupon(normalizedCode)) return false;
    setCoupon(normalizedCode);
    return true;
  };

  const totalCount = cartItems.reduce((total, item) => total + item.cantidad, 0);

  return {
    cartItems,
    coupon,
    totalCount,
    addToCart,
    updateQuantity,
    removeFromCart,
    clearCart,
    applyCoupon,
    clearCoupon: () => setCoupon(''),
  };
};
