package br.ufrpe.prisma.m1.service;

import br.ufrpe.prisma.m1.domain.dto.DenunciaDTO;
import br.ufrpe.prisma.m1.domain.exception.DenunciaException;
import br.ufrpe.prisma.m1.domain.model.Anexo;
import br.ufrpe.prisma.m1.domain.model.Denuncia;
import br.ufrpe.prisma.m1.domain.model.Manifestante;
import br.ufrpe.prisma.m1.domain.repository.DenunciaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DenunciaService {

    //TO DO: Validar anexos
    
    private final DenunciaRepository repository;

    @Transactional
    public Denuncia criarDenuncia(DenunciaDTO dto) {
        // 1. Instancia a Denuncia
        Denuncia denuncia = Denuncia.builder()
                .protocolo(gerarProtocoloUnico())
                .assunto(dto.assunto())
                .descricao(dto.descricao())
                .onde(dto.onde())
                .protocoloAnterior(dto.protocoloAnterior())
                .build();

        // 2. Mapeia e vincula o Manifestante
        if (dto.manifestante() != null) {
            Manifestante manifestante = Manifestante.builder()
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
                    .denuncia(denuncia) // Vincula a denuncia ao manifestante
                    .build();
            denuncia.setManifestante(manifestante);
        }

        // 3. Mapeia e vincula Anexos
        if (dto.anexos() != null && !dto.anexos().isEmpty()) {
            var listaAnexos = dto.anexos().stream().map(a -> {
                Anexo anexo = new Anexo();
                anexo.setArquivoUrl(a.arquivoUrl());
                anexo.setMimeType(a.mimeType());
                anexo.setDenuncia(denuncia); // Vincula a denuncia ao anexo
                return anexo;
            }).collect(Collectors.toList());
            denuncia.setAnexos(listaAnexos);
        }

        // 4. Salva no banco
        try {
            return repository.save(denuncia);
        } catch (Exception e) {
            throw new DenunciaException("Erro ao persistir a denúncia no banco de dados.");
        }
    }

    private String gerarProtocoloUnico() {
        int maxTentativas = 5;
        for (int i = 0; i < maxTentativas; i++) {
            String protocolo = "PRISMA-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            if (repository.findByProtocolo(protocolo).isEmpty()) {
                return protocolo;
            }
        }
        throw new DenunciaException("Não foi possível gerar um protocolo único após várias tentativas.");
    }
}