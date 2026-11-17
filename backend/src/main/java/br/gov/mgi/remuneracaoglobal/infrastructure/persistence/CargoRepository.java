package br.gov.mgi.remuneracaoglobal.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CargoRepository extends JpaRepository<CargoEntity, Long> {

    List<CargoEntity> findAllByOrderByNomeAsc();
}
