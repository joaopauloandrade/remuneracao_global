package br.gov.mgi.remuneracaoglobal.application;

import br.gov.mgi.remuneracaoglobal.infrastructure.persistence.AuditoriaEntity;
import br.gov.mgi.remuneracaoglobal.infrastructure.persistence.AuditoriaRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoria;

    public AuditoriaService(AuditoriaRepository auditoria) {
        this.auditoria = auditoria;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void registrar(String usuario, String acao, String entidade, String entidadeId, String detalhe) {
        auditoria.save(new AuditoriaEntity(usuario, acao, entidade, entidadeId, detalhe, LocalDateTime.now()));
    }
}
