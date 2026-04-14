export const CATEGORIES = {
  CAMISETA: 'Camiseta',
  CAMISA: 'Camisa',
  PANTALON: 'Pantalón',
  FALDA: 'Falda',
  VESTIDO: 'Vestido',
  ZAPATOS: 'Zapatos',
  ABRIGO: 'Abrigo',
  JERSEY: 'Jersey',
  OTRO: 'Otro'
};

export const CATEGORY_SIZES = {
  'Camiseta': ['XS', 'S', 'M', 'L', 'XL', 'XXL'],
  'Camisa': ['XS', 'S', 'M', 'L', 'XL', 'XXL'],
  'Pantalón': ['32', '34', '36', '38', '40', '42', '44'],
  'Falda': ['XS', 'S', 'M', 'L', 'XL', 'XXL'],
  'Vestido': ['XS', 'S', 'M', 'L', 'XL', 'XXL'],
  'Zapatos': ['35', '36', '37', '38', '39', '40', '41', '42', '43', '44', '45'],
  'Abrigo': ['XS', 'S', 'M', 'L', 'XL', 'XXL'],
  'Jersey': ['XS', 'S', 'M', 'L', 'XL', 'XXL'],
  'Otro': ['Única']
};

export const getCategoriesList = () => Object.values(CATEGORIES);

export const getSizesForCategory = (category) => {
  return CATEGORY_SIZES[category] || ['Única'];
};
