const SHIPPING_0_TO_30 = 5;
const SHIPPING_30_TO_60 = 3;
const SHIPPING_60_TO_90 = 1.5;
const FREE_SHIPPING_THRESHOLD = 90;

export function calculateSubtotal(items = []) {
  return roundToTwoDecimals(
    items.reduce((total, item) => total + (Number(item?.price ?? 0) * Number(item?.quantity ?? 1)), 0)
  );
}

export function calculateShippingCost(subtotal = 0) {
  const normalizedSubtotal = Number(subtotal ?? 0);

  if (normalizedSubtotal <= 0) {
    return 0;
  }

  if (normalizedSubtotal < 30) {
    return SHIPPING_0_TO_30;
  }

  if (normalizedSubtotal < 60) {
    return SHIPPING_30_TO_60;
  }

  if (normalizedSubtotal < FREE_SHIPPING_THRESHOLD) {
    return SHIPPING_60_TO_90;
  }

  return 0;
}

export function calculateTotal(items = []) {
  const subtotal = calculateSubtotal(items);
  const shippingCost = calculateShippingCost(subtotal);

  return roundToTwoDecimals(subtotal + shippingCost);
}

function roundToTwoDecimals(value) {
  return Math.round((Number(value) + Number.EPSILON) * 100) / 100;
}
