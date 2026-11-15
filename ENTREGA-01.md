# Entrega 01 — Dossiê e diagnóstico inicial

## Equipe e repositório

- Integrante registrado nesta versão: João Vitor Rodrigues Rocha.
- Repositório público previsto: `Jotas-V/ProBarber-Clean-Code` no GitHub, a ser criado pelo integrante. O endereço deve ser confirmado antes do envio.
- Branch `original`: `eabd2a17026f4ef55e3d9874e216c6e358536a51`. As medições usam esse commit, com o código anterior à refatoração.
- Branch `main`: base para o dossiê e para os futuros PRs de refatoração. As duas branches já existem no repositório local.
- Escopo: ProBarber Backend completo, com 885 linhas Java de produção. O projeto supera em 85 linhas o máximo de 800 previsto no enunciado. A equipe manteve o projeto integral para consultar o professor. A aprovação do escopo está pendente.

## Software

O ProBarber Backend é uma API REST em Java 17, Spring Boot 3.2.4 e Maven para operação de uma barbearia. Usa Spring MVC para os endpoints e Spring Data JPA, Hibernate e PostgreSQL para persistência. Há operações de consulta e cadastro para produtos, serviços, itens de venda, cabeçalhos de venda, geladeira e estoque. O controller também expõe relatórios de produtos e serviços e um dashboard de vendas.

Produtos, serviços e itens de venda ficam em listas em memória. Cabeçalhos, geladeira e estoque têm repositórios JPA. As rotas descrevem a implementação encontrada no commit original. A inicialização do contexto Spring falhou no teste existente, por isso o funcionamento ponta a ponta ainda precisa ser validado.

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

Comandos em Windows, executados contra o checkout legado com o Maven Wrapper 3.9.5. Os plugins foram chamados diretamente pelo Maven, sem alterar o POM nem criar commits no legado.

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
| S06 | Intimidade inapropriada | Acopladores | `Controller.java:19`, `Controller.java:43`, `Controller.java:59`, `Controller.java:91` | O controller retorna coleções mutáveis que usa como armazenamento. Cadastro, consulta e ordenação dependem da mesma representação e compartilham seus efeitos. |
| S07 | Lista longa de parâmetros | Inchaços | `ItensDeVenda.java:12`, `Controller.java:124` | A construção de um item usa seis argumentos posicionais. A duplicação desse mapeamento no controller aumenta o risco de erro ao evoluir os dados da venda. |

## Plano de refatoração

Plano condicionado à aprovação do escopo. Antes do primeiro PR, registrar os contratos de rotas e os comportamentos observáveis que os testes devem proteger. A falha JPA deve ser tratada explicitamente em um PR de correção com teste de inicialização e evidência do contrato esperado. Esse PR é adicional aos sete PRs de refatoração abaixo.

| Ordem | PR previsto | Técnica do catálogo | Grupo | Verificação prevista |
| --- | --- | --- | --- | --- |
| 1 | S04 — Centralizar o envelope de sucesso | Extrair método | Composição de métodos | Status 200 e chaves `dados` e `Retorno` nos três POSTs. |
| 2 | S02 — Isolar o cálculo e a formatação do relatório | Extrair método | Composição de métodos | Texto, ordem, total e efeito da ordenação sobre a consulta. |
| 3 | S07 — Simplificar a cópia de itens de venda | Preservar objeto inteiro | Simplificação de chamadas | Os seis campos e o JSON do item de venda permanecem equivalentes. |
| 4 | S06 — Encapsular as coleções em memória | Encapsular coleção | Organização de dados | Cadastro, listagem e ordem observada pelos clientes. |
| 5 | S01 — Dividir as responsabilidades do controller | Extrair classe | Movimentação de recursos | Rotas, status, corpos de resposta e interação com repositórios. |
| 6 | S03 — Explicitar a fronteira entre os modelos de produto | Renomear método | Simplificação de chamadas | Nomes externos no JSON e no banco preservados por mapeamento explícito onde necessário. |
| 7 | S05 — Remover os CRUDs sem consumidores | Incorporar classe quando houver comportamento aproveitável e remover código morto verificado | Movimentação de recursos | Busca de referências, compilação e testes de regressão. |

Cada refatoração terá seu próprio PR. A equipe distribuirá autoria e revisão para atender ao mínimo de dois PRs de autoria e dois de revisão por integrante. Cada PR terá pelo menos uma aprovação de outro integrante, comentário substantivo classificado e resolvido e checks verdes antes do merge. O template identificará smell, técnica, teste, lint e cobertura. A proteção de `original` e `main` será configurada após a criação do remoto.

## Ferramentas previstas

| Área | Ferramenta e uso |
| --- | --- |
| Linter | Checkstyle via Maven, executado localmente no hook `pre-commit` e no CI. O perfil da entrega final será registrado para manter comparabilidade das medições. |
| Formatador | Spotless com google-java-format, com formatação aplicada em mudança própria para facilitar revisão. |
| Testes | JUnit 5 e Mockito, com Spring MockMvc para contratos HTTP. |
| Cobertura | JaCoCo, com meta de aproximadamente 50% e relatório de linhas e branches. |
| CI | GitHub Actions com build Java 17, testes, lint, cobertura e lint de commits. |
| Hooks | `pre-commit` para lint do Java e `commit-msg` com commitlint e Conventional Commits. |
| IA | OpenAI Codex foi usado na análise inicial, na execução das medições e na redação deste dossiê. A equipe revisará o dossiê e registrará o modelo exibido na interface quando identificado. Código eventualmente gerado será revisado por um integrante e testado antes do merge. |

## Riscos, limitações e exclusões

- **Aprovação de escopo.** O backend integral supera o limite de linhas. A entrega registra a medição real para avaliação do professor. A equipe ainda precisa completar a identificação do grupo de 3 a 4 integrantes e confirmar o endereço público.
- **Demonstração do legado.** O commit original está preservado, mas o teste de contexto falha no mapeamento JPA. A demonstração executável está pendente. As rotas e cálculos isoláveis podem ser caracterizados por testes sem contexto Spring enquanto a equipe investiga a inicialização.
- **Compatibilidade.** Instâncias em memória e entidades JPA coexistem. Os PRs devem proteger os endpoints, as chaves JSON, os cálculos dos relatórios e a ordenação observada. Correções que mudem comportamento existente, inclusive POSTs que hoje ignoram o DTO recebido, precisam de decisão explícita e evidência.
- **Fora da refatoração.** Interface de usuário, autenticação nova, migração de banco ou de framework e novas funcionalidades de negócio ficam fora do escopo proposto. A configuração local do PostgreSQL deve servir apenas ao ambiente de desenvolvimento.
- **Repositório público.** Há configuração de conexão local no histórico. Validar que os valores são exclusivos de desenvolvimento e que o grupo tem autorização para republicar o legado antes de enviar o Git completo.

[Enunciado do Projeto Final de Clean Code](https://cleancode.venson.dev/projetos/projeto_refatoracao/)
