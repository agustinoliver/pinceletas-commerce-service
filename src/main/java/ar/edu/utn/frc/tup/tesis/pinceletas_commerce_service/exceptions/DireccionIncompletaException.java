package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.exceptions;

/**
 * Excepción personalizada para cuando el usuario no tiene dirección completa
 */
public class DireccionIncompletaException extends RuntimeException{
    public DireccionIncompletaException() {
        super("El usuario no tiene una dirección completa registrada. Complete su perfil antes de realizar un pedido.");
    }

    public DireccionIncompletaException(String mensaje) {
        super(mensaje);
    }
}
