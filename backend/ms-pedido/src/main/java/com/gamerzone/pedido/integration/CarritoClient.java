package com.gamerzone.pedido.integration;

import com.gamerzone.pedido.dto.integration.CarritoResponse;

public interface CarritoClient {

    CarritoResponse getCurrentCart(String bearerToken);
}
