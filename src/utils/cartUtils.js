const IVA_RATE = 0.19;

export const calculateSubtotal = (cartItems = []) => {
  return cartItems.reduce((acc, item) => acc + item.precio * item.cantidad, 0);
};

export const calculateIncludedVat = (grossAmount) =>
  Math.round((grossAmount * IVA_RATE) / (1 + IVA_RATE));

export const isValidCoupon = (couponCode) =>
  typeof couponCode === 'string' && ['GAMER10', 'DUOC2026'].includes(couponCode.toUpperCase());

export const applyCouponDiscount = (subtotal, couponCode) => {
  const discountPercentage = isValidCoupon(couponCode) ? 0.1 : 0;
  const discountAmount = Math.round(subtotal * discountPercentage);
  const finalTotal = subtotal - discountAmount;

  return { discountPercentage: discountPercentage * 100, discountAmount, finalTotal };
};