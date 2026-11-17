package br.gov.mgi.remuneracaoglobal.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaRepository extends JpaRepository<EmpresaEntity, String> {

    List<EmpresaEntity> findAllByOrderByCodigoAsc();
}
