import { ProductCard } from '@/components/molecules/ProductCard/ProductCard';

export const ProductGrid = ({
  products = [],
  onAddToCart,
  columnClassName = 'col-12 col-sm-6 col-lg-4',
  className = '',
  emptyMessage = 'No hay juegos disponibles en este momento.',
}) => (
  <div className={`row g-4 ${className}`.trim()} role="list" aria-label="Catálogo de videojuegos">
    {products.length === 0 ? (
      <div className="col-12">
        <p className="alert alert-info mb-0">{emptyMessage}</p>
      </div>
    ) : products.map((product) => (
      <div key={product.id} className={columnClassName} role="listitem">
        <ProductCard
          id={product.id}
          titulo={product.titulo}
          categoria={product.categoria}
          precio={product.precio}
          imagen={product.imagen}
          descripcionCorta={product.descripcion_corta}
          onAddToCart={onAddToCart}
        />
      </div>
    ))}
  </div>
);
