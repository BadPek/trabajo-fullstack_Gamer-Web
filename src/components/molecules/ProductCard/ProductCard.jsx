import { Link } from 'react-router-dom';
import { Button } from '@/components/atoms/Button/Button';
import { Badge } from '@/components/atoms/Badge/Badge';
import { getImageUrl } from '@/utils/imageUtils';

export const ProductCard = ({ id, titulo, categoria, precio, imagen, descripcionCorta, onAddToCart }) => {
  return (
    <div className="card h-100 card-gamer" data-testid={`product-card-${id}`}>
      <img src={getImageUrl(imagen)} className="card-img-top product-card-image" alt={titulo} />
      <div className="card-body d-flex flex-column p-3">
        <Badge variant="purple-light" className="align-self-start mb-2 fw-semibold">
          {categoria}
        </Badge>
        <h5 className="card-title fw-bold text-dark">{titulo}</h5>
        <p className="card-text text-secondary small flex-grow-1">{descripcionCorta}</p>
        <div className="d-flex justify-content-between align-items-center my-3">
          <span className="fs-5 fw-bold text-purple">${precio.toLocaleString('es-CL')}</span>
          <Badge variant="success">Disponible</Badge>
        </div>
        <div className="d-grid gap-2">
          <Link to={`/detalle/${id}`} className="btn btn-outline-purple btn-sm">
            Ver Detalle
          </Link>
          <Button size="sm" onClick={() => onAddToCart({ id, titulo, precio, imagen })}>
            <i className="bi bi-cart-plus me-1"></i> Agregar al Carrito
          </Button>
        </div>
      </div>
    </div>
  );
};