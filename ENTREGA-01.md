# Entrega 01 — Dossiê e diagnóstico inicial

## Equipe e repositório

- Integrantes: Alexandre Destro, Davi Carlos, João Vitor Rodrigues Rocha, Willian Manoel Mesquita.
- Repositório público do trabalho — [https://github.com/Jotas-V/ProBarber-Clean-Code](https://github.com/Jotas-V/ProBarber-Clean-Code)
- Versões publicadas — [original](https://github.com/Jotas-V/ProBarber-Clean-Code/tree/original) e [main](https://github.com/Jotas-V/ProBarber-Clean-Code/tree/main).
- Branch `original`: `28832993c4e11dcad5375b15ca75450998a68042`. O baseline histórico foi medido antes da limpeza da configuração. O commit publicado tem os mesmos fontes Java do legado e omite a configuração do banco.
- Branch `main`: base para o dossiê e para os futuros PRs de refatoração. As duas versões já estão publicadas no GitHub.
- Escopo: ProBarber Backend completo, com 885 linhas Java de produção. O projeto supera em 85 linhas o máximo de 800 previsto no enunciado. A equipe manteve o projeto integral para consultar o professor. A aprovação do escopo está pendente.

## Software

O ProBarber Backend é uma API REST para operação de uma barbearia, desenvolvida anteriormente no projeto integrador do curso. A stack inclui Java 17, Spring Boot 3.2.4, Spring MVC e Maven. As funcionalidades abrangem consulta e cadastro de produtos, serviços, itens de venda, cabeçalhos de venda, geladeira e estoque, além de relatórios de produtos e serviços e um dashboard de vendas.

Na versão legada registrada em `original`, produtos, serviços e itens de venda ficam em listas em memória. Cabeçalhos, geladeira e estoque usam repositórios Spring Data JPA, Hibernate e PostgreSQL. O teste de contexto dessa versão falhou no mapeamento JPA de `Geladeira`.

A preparação já realizada em `main` retirou a configuração de conexão e o driver PostgreSQL, desativou a criação automática de uma fonte de dados e adaptou os cadastros de cabeçalho, geladeira e estoque para memória. Os registros dessas rotas são perdidos ao reiniciar a aplicação. Os POSTs passaram a aproveitar os DTOs recebidos, alterando o comportamento legado que ignorava esses dados. Três testes de integração HTTP passaram após a adaptação, verificando os cadastros e o total de vendas do dashboard. A mudança foi registrada no commit `3a58b7f` e entrou diretamente em `main`, sem PR ou revisão por outro integrante. A equipe deve apresentar essa preparação e suas limitações ao professor.

[Implementação da adaptação e dos testes](https://github.com/Jotas-V/ProBarber-Clean-Code/commit/3a58b7f)

## Contagem de linhas

Na raiz do repositório, com o `cloc` 2.06 e o commit original em checkout, executar `cloc --vcs=git --include-lang=Java src/main/java`. O comando conta os fontes Java de produção rastreados pelo Git. Ficam fora testes, arquivos gerados, dependências, wrappers, lockfiles e arquivos de configuração.

```text
github.com/AlDanial/cloc v 2.06  T=1.34 s (17.9 files/s, 886.1 lines/s)
-------------------------------------------------------------------------------
Language                     files          blank        comment           code
-------------------------------------------------------------------------------
Java                            24            300              4            885
-------------------------------------------------------------------------------
SUM:                            24            300              4            885
-------------------------------------------------------------------------------
```

## Baseline medido

Medições realizadas em 15/11/2026 no checkout legado anterior à higienização do histórico, com Maven Wrapper 3.9.5. Os plugins foram chamados diretamente pelo Maven, sem alterar o POM. Os fontes Java são iguais aos de `original`, cujo hash publicado está registrado acima. A configuração do banco foi removida na publicação, portanto a causa da falha de inicialização ao repetir os testes pode diferir da observada no baseline. Os resultados abaixo descrevem aquela execução histórica.

| Métrica | Resultado | Evidência gerada |
| --- | --- | --- |
| Testes existentes | 1 executado, 1 erro, 0 falhas de asserção, 0 ignorados. `contextLoads` falhou ao criar o `entityManagerFactory`: o Hibernate não conseguiu determinar o tipo JDBC para `Geladeira`. | `target/surefire-reports/com.example.proBarber.ProBarberApplicationTests.txt` |
| Cobertura JaCoCo 0.8.13 | 0/367 linhas e 0/72 branches (0% em ambas) no teste que abortou. O percentual reflete essa execução interrompida. | `target/site/jacoco/jacoco.xml` |
| Lint Checkstyle 9.3 | 698 ocorrências com severidade `error` no perfil padrão `sun_checks.xml`. Entre as mais frequentes, 141 `MissingJavadocMethod`, 123 `DesignForExtension` e 101 `FinalParameters`. Esse total inclui regras de estilo e documentação. | `target/checkstyle-result.xml` |
| Complexidade ciclomática Checkstyle 9.3 | 143 declarações medidas, média 1,10, máximo 3. Exemplos no máximo: `ProdutosCRUD.java:19` e `EstoqueCRUD.java:15`. | `target/cyclomatic-result.xml` |

```powershell
.\mvnw.cmd -B -ntp test
.\mvnw.cmd -B -ntp '-Dmaven.test.failure.ignore=true' org.jacoco:jacoco-maven-plugin:0.8.13:prepare-agent test org.jacoco:jacoco-maven-plugin:0.8.13:report
.\mvnw.cmd -B -ntp org.apache.maven.plugins:maven-checkstyle-plugin:3.4.0:checkstyle
.\mvnw.cmd -B -ntp '-Dcheckstyle.config.location=cyclomatic.xml' '-Dcheckstyle.output.file=target/cyclomatic-result.xml' '-Dcheckstyle.includeTestSourceDirectory=false' org.apache.maven.plugins:maven-checkstyle-plugin:3.4.0:checkstyle
```

Para repetir a medição de complexidade, criar temporariamente `cyclomatic.xml` com o conteúdo abaixo. O limiar zero faz o relatório registrar a complexidade de cada declaração analisada. Essas 143 entradas correspondem à medição e ficam fora do total de ocorrências de lint. Na execução do JaCoCo, a opção de ignorar a falha do teste permite gerar o relatório mesmo com erro.

```xml
<?xml version="1.0"?>
<!DOCTYPE module PUBLIC "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN" "https://checkstyle.org/dtds/configuration_1_3.dtd">
<module name="Checker">
  <module name="TreeWalker">
    <module name="CyclomaticComplexity">
      <property name="max" value="0"/>
    </module>
  </module>
</module>
```

## Catálogo inicial de code smells

Os caminhos são relativos a `src/main/java/com/example/proBarber/` no commit de `original`. As classificações abaixo vêm da leitura do código.

| ID | Smell | Categoria | Arquivo:linha | Impacto |
| --- | --- | --- | --- | --- |
| S01 | Classe grande | Inchaços | `Controller.java:18` | Concentra rotas de vários domínios, estado em memória, acesso a repositórios e relatórios. Mudanças de naturezas diferentes atingem a mesma classe. |
| S02 | Método longo | Inchaços | `Controller.java:88` | O relatório de serviços ordena os dados, formata o texto, soma valores e monta a resposta HTTP no mesmo método. A ordenação também modifica a lista usada pela rota de consulta. |
| S03 | Classes alternativas com interfaces diferentes | Abusos de orientação a objetos | `Produtos.java:11`, `model/Produto.java:12` | As duas representações de produto usam APIs diferentes para nome, ID e preço. A correspondência entre modelos precisa ser explícita para evitar conversões inconsistentes e mudanças no contrato HTTP ou JPA. |
| S04 | Mudança em espingarda | Impedidores de mudança | `Controller.java:55`, `Controller.java:80`, `Controller.java:132` | Uma alteração no envelope comum de sucesso exige mudanças nos três POSTs que montam o mesmo mapa e escolhem o mesmo status HTTP. |
| S05 | Código morto | Dispensáveis | `ProdutosCRUD.java:6`, `EstoqueCRUD.java:6` | As duas classes mantêm operações paralelas sem referências por parte dos fluxos rastreados. Sua presença dificulta identificar quais operações realmente atendem a API. |
| S06 | Inveja de função | Acopladores | `Controller.java:99` a `103`, `Servicos.java:3` | O trecho que representa um serviço no relatório consulta ID, nome e valor de `Servicos` para montar seu texto dentro do controller. Esse trecho concentra conhecimento da representação do serviço fora da classe que possui os dados. A hipótese de diagnóstico será validada ao isolar e mover esse comportamento, preservando o contrato do relatório. |
| S07 | Lista longa de parâmetros | Inchaços | `ItensDeVenda.java:12`, `Controller.java:124` | A construção de um item usa seis argumentos posicionais. A duplicação desse mapeamento no controller aumenta o risco de erro ao evoluir os dados da venda. |

## Plano de refatoração

Plano condicionado à aprovação do escopo pelo professor. Antes de cada refatoração, criar testes de caracterização dos contratos HTTP, cálculos, texto e ordenação afetados. A adaptação para memória descrita acima já foi concluída e fica registrada como preparação do projeto.

São previstos oito PRs de refatoração, distribuídos entre os quatro integrantes com duas autorias por pessoa. Cada integrante também deverá revisar pelo menos dois PRs de colegas. A tabela contempla composição de métodos, movimentação de recursos, organização de dados e simplificação de chamadas, totalizando quatro grupos do catálogo do Tópico 11.

| Ordem | PR previsto | Técnica do catálogo | Grupo | Verificação prevista |
| --- | --- | --- | --- | --- |
| 1 | S04 — Centralizar o envelope de sucesso | Extrair método | Composição de métodos | Status 200 e chaves `dados` e `Retorno` nos três POSTs. |
| 2 | S02 — Separar as etapas do relatório de serviços | Extrair método | Composição de métodos | Texto, total e efeito da ordenação sobre a consulta. |
| 3 | S07 — Simplificar a cópia de itens de venda | Preservar objeto inteiro | Simplificação de chamadas | Os seis campos e o JSON do item de venda permanecem equivalentes. |
| 4 | S01 — Controlar o acesso às listas em memória | Encapsular coleção | Organização de dados | Cadastro, listagem e ordem observada pelos clientes. |
| 5 | S01 — Dividir as responsabilidades do controller | Extrair classe | Movimentação de recursos | Rotas, status, corpos de resposta e armazenamento em memória. |
| 6 | S03 — Alinhar as operações equivalentes dos modelos de produto | Renomear método | Simplificação de chamadas | Identificar primeiro os consumidores e as diferenças de campos e tipos. Renomear somente operações equivalentes, preservando o JSON por mapeamento explícito quando necessário. |
| 7 | S05 — Remover os CRUDs sem consumidores | Remover código morto após verificar referências | Limpeza complementar, fora dos quatro grupos contabilizados | Busca de referências, compilação e testes de regressão dos fluxos ativos. |
| 8 | S06 — Reposicionar a representação textual de um serviço | Mover método, após a extração do PR 2 | Movimentação de recursos | Transferir para `Servicos` a montagem do trecho com ID, nome e valor de um serviço. Preservar exatamente o texto do relatório e manter cabeçalho, totais e resposta HTTP no componente que coordena o relatório. |

Cada refatoração terá seu próprio PR. Todo PR deverá ter ao menos uma aprovação de outro integrante, checks verdes e comentário substantivo de revisão apontando código, classificado como bloqueante ou não bloqueante e resolvido antes do merge. O template identificará smell, técnica, teste, lint e cobertura. A proteção de `original` e `main`, o template, os hooks e o CI ainda precisam ser configurados para a etapa de execução.

[Catálogo de smells do Tópico 10](https://cleancode.venson.dev/slides/topico10-code_smells.html) e [técnicas do Tópico 11](https://cleancode.venson.dev/slides/topico11-refatoracao.html)

## Ferramentas previstas

| Área | Ferramenta e uso |
| --- | --- |
| Linter | Checkstyle via Maven, executado localmente no hook `pre-commit` e no CI. O perfil da entrega final será registrado para manter comparabilidade das medições. |
| Formatador | Spotless com google-java-format, com formatação aplicada em mudança própria para facilitar revisão. |
| Testes | JUnit 5 e Mockito, com Spring MockMvc para contratos HTTP. |
| Cobertura | JaCoCo, com meta de aproximadamente 50% e relatório de linhas e branches. |
| CI | GitHub Actions com build Java 17, testes, lint, cobertura e lint de commits. |
| Hooks | `pre-commit` para lint do Java e `commit-msg` com commitlint e Conventional Commits. |
| IA | OpenAI Codex, identificado nesta sessão como agente baseado em GPT-6, foi usado na análise do código, nas medições, na redação e revisão do dossiê e na implementação da adaptação para memória com seus testes. A identificação exata da versão utilizada na execução inicial não foi registrada. Três testes HTTP passaram, e a revisão humana do código gerado pela equipe ainda precisa ser registrada. Nos próximos PRs, um colega revisará o código e os resultados dos testes antes do merge. |

## Riscos, limitações e exclusões

- **Aprovação de escopo.** O projeto integral tem 885 linhas Java de produção, acima do intervalo de 400 a 800 definido no enunciado. A equipe mantém a contagem real e submeterá esse tamanho ao professor. A aceitação está pendente.
- **Demonstração do legado.** `original` conserva os fontes Java legados e omite a configuração do banco. A execução dessa versão para demonstração continua pendente. A retirada das credenciais alterou os hashes do histórico, e o hash publicado está registrado no início deste dossiê.
- **Compatibilidade e armazenamento.** A preparação de `main` passou a armazenar cabeçalhos, geladeira e estoque em memória e a usar os DTOs recebidos. A equipe deve justificar essa mudança de comportamento. As próximas refatorações devem proteger os endpoints, as chaves JSON, os cálculos e a ordenação observada. Os dados em memória são perdidos ao encerrar o processo.
- **Evidências históricas.** As medições do baseline precedem a retirada da configuração. Os relatórios em `target/` são locais e não acompanham o clone. A tabela registra os resultados observados, e os comandos permitem repetir as medições, observadas as diferenças de configuração descritas. A evidência original disponível deve ser anexada ou publicada para consulta do professor.
- **Trabalho incremental.** A adaptação para memória entrou diretamente em `main`. Os oito PRs planejados devem produzir evidências reais de autoria, revisão e evolução durante a próxima etapa.
- **Exclusões.** Interface de usuário, autenticação nova, migração de framework, novas funcionalidades de negócio, conexão com banco e persistência entre reinicializações ficam fora do escopo proposto.

[Enunciado do Projeto Final de Clean Code](https://cleancode.venson.dev/projetos/projeto_refatoracao/)
