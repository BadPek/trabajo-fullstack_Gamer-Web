export const cleanRut = (rut) => {
  if (typeof rut !== 'string') return '';
  return rut.replace(/[^0-9kK]/g, '').toUpperCase();
};

export const validateRut = (rut) => {
  const cleaned = cleanRut(rut);
  if (cleaned.length < 8 || cleaned.length > 9) return false;

  const body = cleaned.slice(0, -1);
  const dv = cleaned.slice(-1);
  if (!/^\d+$/.test(body) || /^(\d)\1+$/.test(body)) return false;

  let sum = 0;
  let multiplier = 2;

  for (let i = body.length - 1; i >= 0; i--) {
    sum += parseInt(body[i], 10) * multiplier;
    multiplier = multiplier === 7 ? 2 : multiplier + 1;
  }

  const expectedDvNum = 11 - (sum % 11);
  let expectedDv = expectedDvNum.toString();
  if (expectedDvNum === 11) expectedDv = '0';
  if (expectedDvNum === 10) expectedDv = 'K';

  return dv === expectedDv;
};