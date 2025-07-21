# Testes Unitários no Career Tracker

Este projeto contém testes unitários para a camada de serviço que não dependem de banco de dados. Os testes utilizam mocks para simular as dependências externas.

## Estrutura de Testes

Os testes estão organizados da seguinte forma:

```
src/test/
├── java/api/work/profile/
│   ├── config/
│   │   └── TestConfig.java           # Configuração para testes
│   ├── service/
│   │   ├── ActivityServiceTest.java  # Testes para ActivityService
│   │   ├── DailyReportServiceTest.java # Testes para DailyReportService
│   │   ├── TechDebtServiceTest.java  # Testes para TechDebtService
│   │   └── UserServiceTest.java      # Testes para UserService
│   └── ApiWorkProfileApplicationTests.java # Teste de carregamento do contexto
└── resources/
    └── application-test.yml          # Configuração para ambiente de teste
```

## Executando os Testes

Para executar os testes unitários:

```bash
mvn test
```

Os testes são executados com o perfil `test`, que desabilita o acesso ao banco de dados e outras dependências externas.

## Abordagem de Testes

Os testes unitários seguem estas práticas:

1. **Uso de Mocks**: Todas as dependências externas (repositórios, serviços, etc.) são mockadas usando Mockito.
2. **Isolamento**: Cada teste é isolado e não depende de estado externo.
3. **Perfil de Teste**: Configuração específica para testes que desabilita JPA e Liquibase.
4. **Foco na Lógica de Negócio**: Os testes verificam apenas a lógica de negócio na camada de serviço.

## Serviços Testados

- **TechDebtService**: Gerenciamento de débitos técnicos
- **UserService**: Gerenciamento de usuários e autenticação
- **ActivityService**: Gerenciamento de atividades
- **DailyReportService**: Gerenciamento de relatórios diários

## Adicionando Novos Testes

Para adicionar novos testes:

1. Crie uma classe de teste no pacote correspondente
2. Use a anotação `@ExtendWith(MockitoExtension.class)`
3. Mocke as dependências com `@Mock`
4. Injete os mocks no serviço com `@InjectMocks`
5. Escreva métodos de teste que verifiquem o comportamento esperado

Exemplo:

```java
@ExtendWith(MockitoExtension.class)
class NewServiceTest {

    @Mock
    private SomeRepository someRepository;

    @InjectMocks
    private SomeService someService;

    @Test
    void testSomeMethod() {
        // Arrange
        when(someRepository.findById(1L)).thenReturn(Optional.of(someEntity));

        // Act
        SomeEntity result = someService.findById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(someRepository, times(1)).findById(1L);
    }
}
```