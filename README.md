# Framework de Persistência POOA

## Descrição

Este projeto implementa um framework de persistência customizado desenvolvido como parte da disciplina de Programação Orientada a Objetos Avançada (POOA). 
O framework oferece funcionalidades básicas de persistência para objetos Java, utilizando PostgreSQL como banco de dados.

## Funcionalidades Principais

O framework de persistência oferece as seguintes operações:

- **Salvar objetos**: Persiste objetos Java no banco de dados
- **Carregar objetos**: Recupera objetos do banco de dados para a memória
- **Verificar existência**: Verifica se um objeto existe no banco de dados
- **Buscar objetos**: Realiza consultas e buscas no banco de dados

## Tecnologias Utilizadas

- **Java 21**: Linguagem de programação principal
- **PostgreSQL**: Sistema de gerenciamento de banco de dados
- **Maven**: Gerenciamento de dependências e build
- **Docker**: Containerização do banco de dados

## Pré-requisitos

- Java 21 ou superior
- Maven 3.6+
- Docker e Docker Compose
- PostgreSQL (via Docker)

## Subindo o Banco de dados
Usar o comando 'docker compose up --build -d'

## Futuras Atualizações

- Atualizar as tabelas no banco de dados quando houver alterações nas classes
- Mapeamento de relacionamentos entre classes