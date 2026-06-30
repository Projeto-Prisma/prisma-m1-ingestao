package br.ufrpe.prisma.m1.controller;

import br.ufrpe.prisma.m1.domain.dto.*;
import br.ufrpe.prisma.m1.domain.enums.Emissor;
import br.ufrpe.prisma.m1.domain.enums.Status;
import br.ufrpe.prisma.m1.service.DenunciaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DenunciaController.class)
class DenunciaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DenunciaService denunciaService;

    private DenunciaDTO criarDTO() {
        ManifestanteDTO manifestante = new ManifestanteDTO(
                Emissor.CIDADAO,
                false,
                "João",
                "Professor",
                "CPF",
                "12345678900",
                "Masculino",
                "Masculino",
                "81999999999",
                "joao@email.com"
        );

        return new DenunciaDTO(
                "Buraco",
                "Existe um buraco na rua.",
                "Rua A",
                null,
                manifestante,
                List.of()
        );
    }

    private DenunciaResponseDTO criarResponse() {
        return new DenunciaResponseDTO(
                UUID.randomUUID(),
                "PRISMA-12345678",
                "Buraco",
                "Existe um buraco na rua.",
                "Rua A",
                null,
                Status.RECEBIDA,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                List.of(),
                List.of()
        );
    }

    @Test
    void deveCriarDenuncia() throws Exception {

        Mockito.when(denunciaService.criarDenuncia(any()))
                .thenReturn(criarResponse());

        mockMvc.perform(post("/denuncias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(criarDTO())))
                .andExpect(status().isCreated());

        Mockito.verify(denunciaService).criarDenuncia(any());
    }

    @Test
    void deveListarTodas() throws Exception {

        Mockito.when(denunciaService.listarTodas())
                .thenReturn(List.of(criarResponse()));

        mockMvc.perform(get("/denuncias"))
                .andExpect(status().isOk());

        Mockito.verify(denunciaService).listarTodas();
    }

    @Test
    void deveBuscarPorId() throws Exception {

        UUID id = UUID.randomUUID();

        Mockito.when(denunciaService.buscarPorId(id))
                .thenReturn(criarResponse());

        mockMvc.perform(get("/denuncias/{id}", id))
                .andExpect(status().isOk());

        Mockito.verify(denunciaService).buscarPorId(id);
    }

    @Test
    void deveBuscarPorProtocolo() throws Exception {

        Mockito.when(denunciaService.buscarPorProtocolo("PRISMA-123"))
                .thenReturn(criarResponse());

        mockMvc.perform(get("/denuncias/protocolo/PRISMA-123"))
                .andExpect(status().isOk());

        Mockito.verify(denunciaService).buscarPorProtocolo("PRISMA-123");
    }

    @Test
    void deveListarPorStatus() throws Exception {

        Mockito.when(denunciaService.listarPorStatus(Status.RECEBIDA))
                .thenReturn(List.of(criarResponse()));

        mockMvc.perform(get("/denuncias/status/RECEBIDA"))
                .andExpect(status().isOk());

        Mockito.verify(denunciaService).listarPorStatus(Status.RECEBIDA);
    }

    @Test
    void deveAtualizarStatus() throws Exception {

        UUID id = UUID.randomUUID();

        mockMvc.perform(
                        patch("/denuncias/{id}/status", id)
                                .param("novoStatus", "CLASSIFICADA"))
                .andExpect(status().isNoContent());

        Mockito.verify(denunciaService)
                .atualizarStatus(eq(id), eq(Status.CLASSIFICADA));
    }
}