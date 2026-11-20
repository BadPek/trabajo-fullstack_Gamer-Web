package com.gamerzone.pedido.integration;

import com.gamerzone.pedido.dto.integration.ProductoResponse;

public interface CatalogoClient {

    ProductoResponse findProduct(String productId);
}
