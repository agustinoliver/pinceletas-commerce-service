package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.services;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.CarritoEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.OpcionProductoEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.ProductoEntity;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories.CarritoRepository;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories.OpcionProductoRepository;
import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories.ProductoRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CarritoService {

    private final CarritoRepository carritoRepository;
    private final ProductoRepository productoRepository;
    private final OpcionProductoRepository opcionProductoRepository;

    public CarritoEntity agregarProducto(Long usuarioId, Long productoId, int cantidad, Long opcionSeleccionadaId) {
        ProductoEntity producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

        // Verificar si ya existe el mismo producto con la misma opción seleccionada
        List<CarritoEntity> carritoExistente = carritoRepository.findByUsuarioId(usuarioId);

        for (CarritoEntity item : carritoExistente) {
            if (item.getProducto().getId().equals(productoId)) {
                // Comparar opciones seleccionadas
                Long itemOpcionId = (item.getOpcionSeleccionada() != null) ? item.getOpcionSeleccionada().getId() : null;

                if ((itemOpcionId == null && opcionSeleccionadaId == null) ||
                        (itemOpcionId != null && itemOpcionId.equals(opcionSeleccionadaId))) {
                    throw new RuntimeException("El producto ya está en el carrito con la misma opción");
                }
            }
        }

        CarritoEntity item = new CarritoEntity();
        item.setUsuarioId(usuarioId);
        item.setProducto(producto);
        item.setCantidad(cantidad);

        // Asignar opción seleccionada si existe
        if (opcionSeleccionadaId != null) {
            OpcionProductoEntity opcion = opcionProductoRepository.findById(opcionSeleccionadaId)
                    .orElseThrow(() -> new RuntimeException("Opción no encontrada"));
            item.setOpcionSeleccionada(opcion);
        }

        return carritoRepository.save(item);
    }

    public CarritoEntity modificarItem(Long itemId, int nuevaCantidad) {
        CarritoEntity item = carritoRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item no encontrado"));

        item.setCantidad(nuevaCantidad);
        return carritoRepository.save(item);
    }

    public void eliminarItem(Long itemId) {
        carritoRepository.deleteById(itemId);
    }

    public List<CarritoEntity> obtenerCarrito(Long usuarioId) {
        return carritoRepository.findByUsuarioId(usuarioId);
    }

    public BigDecimal calcularTotalCarrito(Long usuarioId) {
        List<CarritoEntity> items = carritoRepository.findByUsuarioId(usuarioId);
        return items.stream()
                .map(item -> {
                    BigDecimal precio = item.getProducto().getPrecio();
                    BigDecimal descuento = item.getProducto().getDescuentoPorcentaje();
                    BigDecimal precioConDescuento = precio.subtract(precio.multiply(descuento.divide(BigDecimal.valueOf(100))));
                    return precioConDescuento.multiply(BigDecimal.valueOf(item.getCantidad()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    // Agregar este método al CarritoService existente
    public void limpiarCarrito(Long usuarioId) {
        List<CarritoEntity> items = carritoRepository.findByUsuarioId(usuarioId);
        carritoRepository.deleteAll(items);
    }

}
