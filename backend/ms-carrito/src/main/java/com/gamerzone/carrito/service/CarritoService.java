package com.gamerzone.carrito.service;

import com.gamerzone.carrito.dto.request.AgregarProductoRequest;
import com.gamerzone.carrito.dto.request.ActualizarCantidadRequest;
import com.gamerzone.carrito.dto.response.CarritoResponse;
import com.gamerzone.carrito.dto.response.ItemCarritoResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CarritoService {

    public CarritoResponse findCart() {
        throw persistenceNotConfigured();
    }

    public ItemCarritoResponse addItem(AgregarProductoRequest request) {
        throw persistenceNotConfigured();
    }

    public ItemCarritoResponse updateItem(String itemId, ActualizarCantidadRequest request) {
        throw persistenceNotConfigured();
    }

    public void removeItem(String itemId) {
        throw persistenceNotConfigured();
    }

    public void clearCart() {
        throw persistenceNotConfigured();
    }

    private ResponseStatusException persistenceNotConfigured() {
        return new ResponseStatusException(
                HttpStatus.NOT_IMPLEMENTED,
                "La persistencia del carrito depende de la aprobación del modelo relacional.");
    }
}
