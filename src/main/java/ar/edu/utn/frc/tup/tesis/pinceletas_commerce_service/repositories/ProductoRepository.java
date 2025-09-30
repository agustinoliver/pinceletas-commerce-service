package ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.repositories;

import ar.edu.utn.frc.tup.tesis.pinceletas_commerce_service.entities.ProductoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<ProductoEntity, Long> {

}
