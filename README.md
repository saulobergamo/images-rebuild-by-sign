# Universidade Tecnológica Federal do Paraná - UTFPR
___
## Desenvolvimento Integrado de Sistemas - CSM30

Microsserviço responsável por consumir mensagens recebidas de uma fila (MessageBroker - ActiveMQ),
processar informações de sinal de entrada e matriz modelo realizando operações matémáticas em matrizes
para reconstruir uma imagem de ultrassom.

## Documentação
  Este microsserviço faz o processamento de vetor sinal de entrada e matriz modelo esparsa para reconstrução
  de imagem de ultrassom. Conecta-se com uma fila de mensagens (activeMQ), banco de dados não relacional (MongoDB)
  e utiliza um script python chamado como um processo para realizar as operações necessárias e reconstruir a imagem.

  Outro microsserviço é utilizado como api para expor endpoints que servem para envio de vetor sinal através de um
  método HTTP POST e recebimento de imagem através de um método HTTP GET. O microsseerviço
  ['entry-sign-publisher'](https://github.com/saulobergamo/entry-sign-publisher) tem a única responsabilidade de
  receber o sinal, salvar no banco de dados e enviar uma mensagem na fila indicando que um novo sinal foi recebido e
  uma nova imagem deve ser processada.

  Diagrama de sequência pode ser visualizado na pasta src/main/resources/images/SequenceDiagram.png ou
  pelo [Link](https://mermaid.live/edit#pako:eNqNVV1v2jAU_StWpKqaRgqkhUK0ZSqjqqqpLS3tQ6dIyCSGWkrszB8Mivg1e9jTnqb9gv6xXTtJExib9lA12Nfn3nvOufbaiXhMHN-R5IsmLCJDiucCpyELGY4UF2iJsEQPkoiQZVgoGtEMM4XOxyOzQZgSK1fSOXMzPU2ofNoNPLu6NYFnkaILcnW7vTkcmL0hVniAJdneGz2avdFKPXGGMsEjIuV2xOXdYGxiaIrnxBVkqmkSu9O8HtuBVpzpdGpqskeRhkaQ4rWUS9cNAujGh14WFCNJGU4QxGIkSMSZVEK_fH_5xlFM8kxADuOKIL4ALHtydDO-91HT7srm2iS5xinZNHWWcByb5BAHiWz0nAC2TnOwyxi9br8ZDnwkcbIoqmigEgmRMvrdVAT_X50BjHiSEGCfMz-Xa2LomSg-KRib2HNVFaBYSUZKmDSYecYZTXAd3QaCbTTxa0aoug2WPlQpMyiToPH92f3DGHmtFrr5VEdZNnISoR8yhbVXXmzOjEtFBOXCNm461onCVbc1Gn7EUB1hQPfBAcIs3vHOtuy5HfY6ZzsQmgROjNV8m56DGjkVpoiCICYNogkqVMQmYyHjriAzyuLB6jLvsjoXBKNHvyyWAw-5EyEHVoI-oxQGNeFAQ_MvBsB7DGAgtaIJfcaIS4STORdUpfD58eL6HMDh390eT1GBKriQjR633FluISOXIAlWLz9BI0OIVSjl_zIhCCiw5JoVzpNlhiDIaRZEacGQ1FE-83VNH2oDPC2UrEZY8gQuB4Vr5tAZjFuM_5jZi_P6yMY80qClura3xaYZ86_MjO6HwozvD4uPw5q935rOyhSi4qRiZJsGtEf74cAgueUEWKg6v-Z6YzMuUgwq_8q52jdfliBLVMichpMSOEFjuNjXIUModNQTSUno-PAZkxkGDUInZBsINZfkeMUix5_hRJKGo7MYq_Ih2Fk9jyk8Cq-LcA1_5jyt_3b8tbN0fPek1T_qece93nGr0-2etLoNZ2WW295Rr3_i9U7brf7xaWfTcJ4tgnfkddudPvy1W16n57XhALHZrvIXyj5Um9-f5Fus)

![SequenceDiagram.png](..%2F..%2F..%2FDownloads%2FSequenceDiagram.png)

### Pré-requisitos

O que será necessário instalar para que o microsserviço funcione corretamente

- Intellij IDEA
- Git
- Java 11
- Gradle
- ActiveMq
- MongoDB
- Python3
  - numpy
  - pandas
  - csv


### Instalação

Passo a passo de execução para que o microsserviço fique em execução e disponível para uso.

- Realizar o clone do projeto:

  Comando: ```git clone git@ssh.dev.azure.com:v3/alelo/Portador/ma-card-tracking-orch```

- Acessar a pasta do projeto:

  - Executar script para o docker compose:

    Comando: ```docker-compose up```


- Fazer o build do projeto:

  Comando: ```./gradlew build```


- Subir a aplicação local:

  Comando: ```./gradlew bootRun --args='--spring.profiles.active=dev'```

### Qualidade de código

- Executar inspeção com SonarQube:

  Comando: ```./cmds/build-sonar.sh```


- Executar inspeção com Spotless e Detekt:

  Comando: ```./gradlew spotlessCheck```


- Executar correção com Spotless e Detekt:

  Comando: ```./gradlew spotlessApply```
