package br.ufrpe.prisma.m1.service;
import br.ufrpe.prisma.m1.domain.dto.AnexoDTO;
import org.mockito.ArgumentMatchers;
import br.ufrpe.prisma.m1.domain.dto.DenunciaDTO;
import br.ufrpe.prisma.m1.domain.dto.DenunciaResponseDTO;
import br.ufrpe.prisma.m1.domain.dto.ManifestanteDTO;
import br.ufrpe.prisma.m1.domain.enums.Emissor;
import br.ufrpe.prisma.m1.domain.enums.Status;
import br.ufrpe.prisma.m1.domain.model.Denuncia;
import br.ufrpe.prisma.m1.domain.model.HistoricoStatus;
import br.ufrpe.prisma.m1.exception.DenunciaException;
import br.ufrpe.prisma.m1.domain.repository.DenunciaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DenunciaServiceTest {

    @Mock
    private DenunciaRepository repository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private DenunciaService service;

    private DenunciaDTO denunciaDTO;

    @BeforeEach
    void setup() {
        denunciaDTO = criarDTOValido();
    }

    @Test
    void deveCriarDenunciaComSucesso() {

        Denuncia denunciaSalva = criarDenunciaValida();

        when(repository.findByProtocolo(anyString()))
                .thenReturn(Optional.empty());

        when(repository.save(any(Denuncia.class)))
                .thenReturn(denunciaSalva);

        DenunciaResponseDTO response = service.criarDenuncia(denunciaDTO);

        assertNotNull(response);
        assertEquals("Buraco na rua", response.assunto());

        verify(repository).save(any(Denuncia.class));
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), ArgumentMatchers.<Object>any());
    }

    @Test
    void devePublicarEventoRabbitAoCriarDenuncia() {

        Denuncia denunciaSalva = criarDenunciaValida();

        when(repository.findByProtocolo(anyString()))
                .thenReturn(Optional.empty());

        when(repository.save(any(Denuncia.class)))
                .thenReturn(denunciaSalva);

        service.criarDenuncia(denunciaDTO);

        verify(rabbitTemplate, times(1))
                .convertAndSend(anyString(), anyString(), ArgumentMatchers.<Object>any());
    }

    @Test
    void deveLancarExcecaoQuandoAssuntoForVazio() {

        DenunciaDTO dto = new DenunciaDTO(
                "",
                "descricao",
                null,
                null,
                denunciaDTO.manifestante(),
                List.of()
        );

        assertThrows(
                DenunciaException.class,
                () -> service.criarDenuncia(dto)
        );

        verify(repository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoDescricaoForVazia() {

        DenunciaDTO dto = new DenunciaDTO(
                "Assunto",
                "",
                null,
                null,
                denunciaDTO.manifestante(),
                List.of()
        );

        assertThrows(
                DenunciaException.class,
                () -> service.criarDenuncia(dto)
        );

        verify(repository, never()).save(any());
    }


    @Test
    void deveAceitarProtocoloAnteriorExistente() {

        Denuncia denunciaAnterior = criarDenunciaValida();

        Denuncia denunciaSalva = criarDenunciaValida();

        when(repository.findByProtocolo("PROTOCOLO-ANTERIOR"))
                .thenReturn(Optional.of(denunciaAnterior));

        when(repository.findByProtocolo(startsWith("PRISMA-")))
                .thenReturn(Optional.empty());

        when(repository.save(any(Denuncia.class)))
                .thenReturn(denunciaSalva);

        DenunciaDTO dto = new DenunciaDTO(
                "Assunto",
                "Descricao",
                null,
                "PROTOCOLO-ANTERIOR",
                denunciaDTO.manifestante(),
                List.of()
        );

        assertDoesNotThrow(() -> service.criarDenuncia(dto));
    }

    @Test
    void deveLancarExcecaoParaMimeTypeInvalido() {

        AnexoDTO anexo = new AnexoDTO(
                "arquivo.pdf",
                "application/pdf",
                1000L
        );

        DenunciaDTO dto = new DenunciaDTO(
                "Assunto",
                "Descricao",
                null,
                null,
                denunciaDTO.manifestante(),
                List.of(anexo)
        );

        assertThrows(
                DenunciaException.class,
                () -> service.criarDenuncia(dto)
        );
    }

    @Test
    void deveLancarExcecaoQuandoArquivoExcede10MB() {

        AnexoDTO anexo = new AnexoDTO(
                "foto.jpg",
                "image/jpeg",
                11 * 1024 * 1024
        );

        DenunciaDTO dto = new DenunciaDTO(
                "Assunto",
                "Descricao",
                null,
                null,
                denunciaDTO.manifestante(),
                List.of(anexo)
        );

        assertThrows(
                DenunciaException.class,
                () -> service.criarDenuncia(dto)
        );
    }

    @Test
    void deveBuscarPorIdComSucesso() {

        UUID id = UUID.randomUUID();

        Denuncia denuncia = criarDenunciaValida();
        denuncia.setId(id);

        when(repository.findById(id))
                .thenReturn(Optional.of(denuncia));

        DenunciaResponseDTO response = service.buscarPorId(id);

        assertEquals(id, response.id());
    }

    @Test
    void deveLancarExcecaoQuandoBuscarPorIdInexistente() {

        UUID id = UUID.randomUUID();

        when(repository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                DenunciaException.class,
                () -> service.buscarPorId(id)
        );
    }

    @Test
    void deveBuscarPorProtocoloComSucesso() {

        Denuncia denuncia = criarDenunciaValida();

        when(repository.findByProtocolo("PROTOCOLO"))
                .thenReturn(Optional.of(denuncia));

        DenunciaResponseDTO response =
                service.buscarPorProtocolo("PROTOCOLO");

        assertEquals("PROTOCOLO", response.protocolo());
    }

    @Test
    void deveLancarExcecaoQuandoProtocoloNaoExiste() {

        when(repository.findByProtocolo("INVALIDO"))
                .thenReturn(Optional.empty());

        assertThrows(
                DenunciaException.class,
                () -> service.buscarPorProtocolo("INVALIDO")
        );
    }

    @Test
    void deveListarTodas() {

        when(repository.findAll())
                .thenReturn(List.of(
                        criarDenunciaValida(),
                        criarDenunciaValida()
                ));

        List<DenunciaResponseDTO> resultado =
                service.listarTodas();

        assertEquals(2, resultado.size());
    }

    @Test
    void deveListarPorStatus() {

        Denuncia denuncia = criarDenunciaValida();
        denuncia.setStatus(Status.RECEBIDA);

        when(repository.findByStatus(Status.RECEBIDA))
                .thenReturn(List.of(denuncia));

        List<DenunciaResponseDTO> resultado =
                service.listarPorStatus(Status.RECEBIDA);

        assertEquals(1, resultado.size());
    }

    @Test
    void deveAtualizarStatusComSucesso() {

        UUID id = UUID.randomUUID();

        Denuncia denuncia = criarDenunciaValida();
        denuncia.setId(id);
        denuncia.setHistorico(new ArrayList<>());

        when(repository.findById(id))
                .thenReturn(Optional.of(denuncia));

        service.atualizarStatus(id, Status.ENCAMINHADA);

        assertEquals(Status.ENCAMINHADA, denuncia.getStatus());
        assertEquals(1, denuncia.getHistorico().size());

        verify(repository).save(denuncia);
    }

    @Test
    void deveLancarExcecaoAoAtualizarStatusDeDenunciaInexistente() {

        UUID id = UUID.randomUUID();

        when(repository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                DenunciaException.class,
                () -> service.atualizarStatus(id, Status.ENCAMINHADA)
        );
    }

    @Test
    void deveAdicionarHistoricoAoAtualizarStatus() {

        UUID id = UUID.randomUUID();

        Denuncia denuncia = criarDenunciaValida();
        denuncia.setId(id);
        denuncia.setHistorico(new ArrayList<>());

        when(repository.findById(id))
                .thenReturn(Optional.of(denuncia));

        service.atualizarStatus(id, Status.CLASSIFICADA);

        HistoricoStatus historico =
                denuncia.getHistorico().get(0);

        assertEquals(Status.CLASSIFICADA, historico.getStatus());
        assertNotNull(historico.getDataAlteracao());
    }

    private DenunciaDTO criarDTOValido() {

        ManifestanteDTO manifestante = new ManifestanteDTO(
                Emissor.CIDADAO,
                false,
                "João",
                "Professor",
                "CPF",
                "12345678900",
                "M",
                "Masculino",
                "81999999999",
                "joao@email.com"
        );

        AnexoDTO anexo = new AnexoDTO(
                "foto.jpg",
                "image/jpeg",
                1000L
        );

        return new DenunciaDTO(
                "Buraco na rua",
                "Existe um buraco enorme.",
                "Rua Central",
                null,
                manifestante,
                List.of(anexo)
        );
    }

    private Denuncia criarDenunciaValida() {

        return Denuncia.builder()
                .id(UUID.randomUUID())
                .protocolo("PROTOCOLO")
                .assunto("Buraco na rua")
                .descricao("Descricao")
                .onde("Rua Central")
                .status(Status.RECEBIDA)
                .recebidaEm(LocalDateTime.now())
                .historico(new ArrayList<>())
                .build();
    }
}
