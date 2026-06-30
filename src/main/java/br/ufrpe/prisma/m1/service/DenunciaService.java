package br.ufrpe.prisma.m1.service;

import br.ufrpe.prisma.m1.domain.dto.*;
import br.ufrpe.prisma.m1.exception.DenunciaException;
import br.ufrpe.prisma.m1.domain.model.*;
import br.ufrpe.prisma.m1.domain.repository.DenunciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import br.ufrpe.prisma.m1.config.RabbitConfig;
import java.time.LocalDateTime;
import br.ufrpe.prisma.m1.domain.enums.Status;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DenunciaService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private final DenunciaRepository repository;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public DenunciaResponseDTO criarDenuncia(DenunciaDTO dto) {
        if (dto.assunto().isBlank() || dto.descricao().isBlank()) {
            throw new DenunciaException("Assunto e Descrição são obrigatórios.");
        }

        if (dto.protocoloAnterior() != null && repository.findByProtocolo(dto.protocoloAnterior()).isEmpty()) {
            throw new DenunciaException("O protocolo anterior informado não existe.");
        
        }

        Denuncia denuncia = Denuncia.builder()
                .protocolo(gerarProtocoloUnico())
                .assunto(dto.assunto())
                .descricao(dto.descricao())
                .onde(dto.onde())
                .protocoloAnterior(dto.protocoloAnterior())
                .build();

        if (dto.manifestante() != null) {
            denuncia.setManifestante(buildManifestante(dto, denuncia));
        }

        if (dto.anexos() != null && !dto.anexos().isEmpty()) {
            denuncia.setAnexos(dto.anexos().stream().map(a -> {
                validarAnexo(a);
                return Anexo.builder()
                        .arquivoUrl(a.arquivoUrl())
                        .mimeType(a.mimeType())
                        .tamanhoEmBytes(a.tamanhoEmBytes())
                        .denuncia(denuncia)
                        .build();
            }).collect(Collectors.toList()));
        }

        Denuncia saved = repository.save(denuncia);

        Map<String, String> localizacao = saved.getOnde() != null
            ? Map.of("endereco", saved.getOnde())
            : null;

        var evento = new DenunciaRecebidaEvent(
            saved.getId().toString(),
            saved.getDescricao(),
            saved.getAssunto(),
            localizacao,
            (saved.getAnexos() == null || saved.getAnexos().isEmpty()) ? null : saved.getAnexos().get(0).getArquivoUrl(),
            saved.getRecebidaEm()
        );

        rabbitTemplate.convertAndSend(
            RabbitConfig.EXCHANGE_DENUNCIAS, 
            RabbitConfig.ROUTING_KEY_RECEBIDA, 
            evento
        );

        return toResponseDTO(saved);
    }

    @Transactional
    public void atualizarStatus(UUID denunciaId, Status novoStatus) {
        Denuncia den = repository.findById(denunciaId).orElseThrow(() -> new DenunciaException("Denúncia não encontrada com o ID: " + denunciaId));
    
        // Adiciona ao histórico
        HistoricoStatus log = HistoricoStatus.builder()
            .status(novoStatus)
            .dataAlteracao(LocalDateTime.now())
            .denuncia(den)
            .build();
    
        den.setStatus(novoStatus);
        den.getHistorico().add(log);
        repository.save(den);
    }

    @Transactional(readOnly = true)
    public DenunciaResponseDTO buscarPorProtocolo(String protocolo) {
        Denuncia den = repository.findByProtocolo(protocolo)
                .orElseThrow(() -> new DenunciaException("Denúncia não encontrada: " + protocolo));
        return toResponseDTO(den);
    }

    @Transactional(readOnly = true)
    public List<DenunciaResponseDTO> listarTodas() {
        return repository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DenunciaResponseDTO buscarPorId(UUID id) {
        return repository.findById(id)
                .map(this::toResponseDTO)
                .orElseThrow(() -> new DenunciaException("Denúncia não encontrada com o ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<DenunciaResponseDTO> listarPorStatus(Status status) {
        return repository.findByStatus(status).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }


    // --- Mapeador (Service converte tudo para ResponseDTO) ---

    public DenunciaResponseDTO toResponseDTO(Denuncia den) {
        var manifestanteDto = den.getManifestante() != null ? new ManifestanteDTO(
                den.getManifestante().getTipo(), den.getManifestante().isSigilo(),
                den.getManifestante().getNome(), den.getManifestante().getProfissao(),
                den.getManifestante().getTipoDoc(), den.getManifestante().getNumeroDoc(),
                den.getManifestante().getSexo(), den.getManifestante().getGenero(),
                den.getManifestante().getCelular(), den.getManifestante().getEmail()
        ) : null;

        List<AnexoDTO> anexosDto = (den.getAnexos() != null) 
            ? den.getAnexos().stream()
                .map(a -> new AnexoDTO(a.getArquivoUrl(), a.getMimeType(), a.getTamanhoEmBytes()))
                .collect(Collectors.toList()) 
            : List.of();
        
        List<DenunciaResponseDTO.HistoricoStatusResponse> historicoDto = den.getHistorico() != null 
            ? den.getHistorico().stream()
                .map(h -> new DenunciaResponseDTO.HistoricoStatusResponse(h.getStatus(), h.getDataAlteracao()))
                .collect(Collectors.toList()) 
            : List.of();

        return new DenunciaResponseDTO(
                den.getId(), den.getProtocolo(), den.getAssunto(), den.getDescricao(), den.getOnde(),
                den.getProtocoloAnterior(), den.getStatus(), den.getRecebidaEm(),
                den.getUpdatedAt(), manifestanteDto, anexosDto, historicoDto
        );
    }

    // --- Métodos Auxiliares ---
    
    private Manifestante buildManifestante(DenunciaDTO dto, Denuncia denuncia) {
        return Manifestante.builder()
            .tipo(dto.manifestante().tipo())
            .sigilo(dto.manifestante().sigilo())
            .nome(dto.manifestante().nome())
            .profissao(dto.manifestante().profissao())
            .tipoDoc(dto.manifestante().tipoDoc())
            .numeroDoc(dto.manifestante().numeroDoc())
            .sexo(dto.manifestante().sexo())
            .genero(dto.manifestante().genero())
            .celular(dto.manifestante().celular())
            .email(dto.manifestante().email())
            .denuncia(denuncia) 
            .build();
    }

    private void validarAnexo(AnexoDTO anexo) {
        String mime = anexo.mimeType().toLowerCase();
        if (!mime.equals("image/png") && !mime.equals("image/jpeg") && !mime.equals("image/jpg")) {
            throw new DenunciaException("Formato de arquivo inválido.");
        }
        if (anexo.tamanhoEmBytes() > MAX_FILE_SIZE) {
            throw new DenunciaException("Arquivo excede 10MB.");
        }
    }

    private String gerarProtocoloUnico() {
        for (int i = 0; i < 5; i++) {
            String p = "PRISMA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            if (repository.findByProtocolo(p).isEmpty()) return p;
        }
        throw new DenunciaException("Erro ao gerar protocolo.");
    }

    record DenunciaRecebidaEvent(
        String id,
        String texto,
        String assunto_usuario,
        Map<String, String> localizacao,
        String foto,
        LocalDateTime timestamp
    ) {}
}