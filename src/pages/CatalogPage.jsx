import { Badge } from '@/components/atoms/Badge/Badge';
import { ProductGrid } from '@/components/organisms/ProductGrid/ProductGrid';
import { juegosData } from '@/data/juegosData';

export const CatalogPage = ({ onAddToCart }) => {
  return (
    <>
      <section className="bg-purple-light p-4 rounded-4 mb-4 border border-purple">
        <div className="d-flex justify-content-between align-items-center flex-wrap gap-2">
          <div>
            <h1 className="text-purple fw-bold mb-1">Nuestros Juegos</h1>
            <p className="text-muted mb-0">Catálogo completo de títulos físicos y digitales disponibles para despacho.</p>
          </div>
          <Badge variant="purple" className="px-3 py-2 fs-6">
            {juegosData.length} Juegos Disponibles
          </Badge>
        </div>
      </section>

      <ProductGrid products={juegosData} onAddToCart={onAddToCart} />
    </>
  );
};