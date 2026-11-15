package com.example.proBarber;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProBarberApplicationTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void registraVendasEmMemoriaESomaOsValoresNoDashboard() throws Exception {
        mvc.perform(post("/cabecalho").contentType(MediaType.APPLICATION_JSON)
                .content("{\"pedidoNumero\":10,\"usuario\":\"Ana\",\"valorTotal\":40}"))
                .andExpect(status().isOk());
        mvc.perform(post("/cabecalho").contentType(MediaType.APPLICATION_JSON)
                .content("{\"pedidoNumero\":11,\"usuario\":\"Bia\",\"valorTotal\":25}"))
                .andExpect(status().isOk());

        mvc.perform(get("/cabecalho"))
                .andExpect(jsonPath("$[0].pedidoNumero").value(10))
                .andExpect(jsonPath("$[0].valorTotal").value(40))
                .andExpect(jsonPath("$[1].valorTotal").value(25));
        mvc.perform(get("/dashboard"))
                .andExpect(jsonPath("$.totalCabecalhos").value(2))
                .andExpect(jsonPath("$.valorTotalVendas").value(65));
    }

    @Test
    void registraAlimentosEmMemoria() throws Exception {
        mvc.perform(post("/geladeira").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nomeAlimento\":\"Agua\",\"valorAlimento\":5}"))
                .andExpect(status().isOk());
        mvc.perform(get("/geladeira"))
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].nomeAlimento").value("Agua"));
    }

    @Test
    void registraEstoqueEmMemoria() throws Exception {
        mvc.perform(post("/estoque").contentType(MediaType.APPLICATION_JSON)
                .content("{\"produtosList\":[{\"id\":1,\"nome\":\"Shampoo\",\"preco\":10}],\"geladeiraList\":[]}"))
                .andExpect(status().isOk());
        mvc.perform(get("/estoque"))
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].produtosList[0].nome").value("Shampoo"))
                .andExpect(jsonPath("$[0].geladeiraList").isEmpty());
    }
}
