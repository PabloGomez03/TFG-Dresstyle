const catalogDb = db.getSiblingDB('dresstyle_catalog');
const fs = require('fs');
const path = require('path');

const TARGET_PRODUCTS = 5000;
const PRODUCT_PREFIX = 'DS-SEED-';

const categories = [
  'Camiseta',
  'Camisa',
  'Falda',
  'Vestido',
  'Abrigo',
  'Jersey',
  'Pantalón',
  'Zapatos',
  'Otro'
];

const styles = [
  'Urban',
  'Classic',
  'Essential',
  'Modern',
  'Comfort',
  'Street',
  'Premium',
  'Minimal',
  'Active',
  'Soft'
];

const colors = [
  'Negro',
  'Blanco',
  'Beige',
  'Azul',
  'Gris',
  'Verde',
  'Rojo',
  'Marrón',
  'Burdeos',
  'Arena'
];


const LOGO_URL = 'https://res.cloudinary.com/dapegkgwi/image/upload/v1779390582/logo_rkcr0x.png';


function generateImageUrl(index) {
  
  
  return LOGO_URL;
}

function buildStock(index, category) {
  const baseStock = {
    'Zapatos': 24,
    'Abrigo': 12,
    'Vestido': 18,
    'Jersey': 20
  }[category] ?? 36;

  return baseStock + (index % 25);
}

function buildPrice(category, index) {
  const basePrice = {
    'Zapatos': 55.00,
    'Abrigo': 82.50,
    'Vestido': 46.00,
    'Camisa': 27.50,
    'Jersey': 32.00,
    'Pantalón': 36.50,
    'Falda': 23.00,
    'Otro': 18.50
  }[category] ?? 16.50;

  return Math.round((basePrice + ((index % 15) * 1.61)) * 100) / 100;
}

function buildDescription(category, style, color) {
  return `${category} de estilo ${style.toLowerCase()} en color ${color.toLowerCase()}, pensado para completar el catalogo con una imagen homogenea.`;
}

function buildName(index) {
  const category = categories[index % categories.length];
  const style = styles[index % styles.length];
  const color = colors[index % colors.length];

  return {
    category,
    style,
    color,
    name: `${PRODUCT_PREFIX}${String(index).padStart(4, '0')} ${category} ${style} ${color}`
  };
}

const currentCount = catalogDb.products.countDocuments({ name: { $regex: `^${PRODUCT_PREFIX}` } });
print(`Productos semilla actuales: ${currentCount}`);

if (currentCount >= TARGET_PRODUCTS) {
  print(`No se inserta nada porque ya hay ${currentCount} productos semilla o mas.`);
} else {
  const productsToCreate = TARGET_PRODUCTS - currentCount;
  const documents = [];

  for (let offset = 1; offset <= productsToCreate; offset++) {
    const absoluteIndex = currentCount + offset;
    const { category, style, color, name } = buildName(absoluteIndex);

    documents.push({
      name,
      imageUrl: generateImageUrl(absoluteIndex),
      stock: buildStock(absoluteIndex, category),
      price: buildPrice(category, absoluteIndex),
      description: buildDescription(category, style, color),
      category
    });
  }

  const result = catalogDb.products.insertMany(documents, { ordered: false });
  print(`Insertados ${Object.keys(result.insertedIds).length} productos nuevos.`);
}
